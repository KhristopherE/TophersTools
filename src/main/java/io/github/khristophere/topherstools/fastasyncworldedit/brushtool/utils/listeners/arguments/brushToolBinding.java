package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.world.item.ItemType;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class brushToolBinding {

    /**
     * Processes the BrushToolBinding Listener. In the Event the Main BrushToolListener is called for and the command is already known to be binding this function is called.
     * These functions keep in mind that the main BrushToolListener is Cancelling any FAWE Command Events that we're filtering for, binding, unbinding, & modify.
     * Then we call FAWE to run the command on the current thread to prevent having to calculate changes over a few varying tick delay due to the async command calculations
     * when the command is handled naturally.
     * @param player The player in the event being called.
     * @param command The command the player is calling.
     */
    public static void processCommand(Player player, String command){
        ItemStack minecraftItemInHand = player.getInventory().getItemInMainHand();
        ItemType worldEditItemInHand = BukkitAdapter.asItemType(minecraftItemInHand.getType());
        if (worldEditItemInHand == null) return;

        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        BaseItem worldEditBaseItemInHand = worldEditItemInHand.getDefaultState();

        Tool previousTool = worldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        boolean hadPreviousTool = previousTool != null;
        String previousToolStringReference = hadPreviousTool ? previousTool.toString() : "";

        //Above is collecting information before the command runs, Below is Collecting & Comparing information from after it runs.
        WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager()
                .handleCommandOnCurrentThread(
                        new CommandEvent(worldEditPlayer, BrushToolCommandLogic.cleanCommand(command)));

        LocalSession refreshWorldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        Tool newTool = refreshWorldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        String newToolStringReference = newTool != null ? newTool.toString() : "";

        boolean wasBindingSuccessful = isBindingSuccessful(hadPreviousTool, newToolStringReference, previousToolStringReference);
        boolean isPrimaryBinding = BrushToolCommandLogic.isCommandPrimaryBinding(command);
        boolean isSecondaryBinding = BrushToolCommandLogic.isCommandSecondaryBinding(command);

        /*
         * TODO: FAWE Bugged State, if this bug is ever resolved, change this. Certainly possible that it's not a bug and I'm just an idiot...
         *
         *  The below checks for a bugged state in FAWE, where if you have a valid brush/tool, and utilized the "//(brush/tool) secondary"
         *  or "//secondary" command with invalid arguments, it causes the brush/tool to become entirely invalid.
         *
         *  To account for this, we will follow how FAWE behaves, unfortunately unbinding the brush.
         *  Although, before that we will provide the player with a clickable chat message to reapply all the prior brush commands.
         */
        if (hadPreviousTool && isSecondaryBinding && newToolStringReference.isEmpty()) {
            Scheduler.runSequentialAsync( () -> {
                BTLoadout oldLoadout = BTCacheManager.getBrushToolSQL().getBrushToolLoadoutSQL(player.getUniqueId(), minecraftItemInHand.getType().name());
                if (oldLoadout != null && !oldLoadout.isEmpty()) {
                    player.sendMessage(oldLoadout.getLoadoutPromptMessage());
                    player.sendMessage(PlayerMessages.getBuggedStateSecondaryBrushInvalidMessage());
                }
                BTCacheManager.getBrushToolSQL().deleteBrushToolLoadoutSQL(player.getUniqueId(), minecraftItemInHand.getType().name());
            });
            BTCacheManager.getBrushToolSQL().brushToolSQLRequests.deleteBrushTool(player.getUniqueId(), minecraftItemInHand.getType().name());
            BTCacheManager.removeUUIDMaterialBindFromCache(player.getUniqueId(), minecraftItemInHand.getType().name());
            return;
        }

        /*
         * This checks for a specific condition that results in your secondary brush/tool being overridden by a brush/tool command that did not
         * specify primary OR secondary. To comply with the functionality of FAWE, we will also delete the secondary functionality from the save file.
         */
        if (!isPrimaryBinding && !isSecondaryBinding && wasBindingSuccessful){
            BTCacheManager.getBrushToolSQL().brushToolSQLRequests.removeSecondaryBrushToolCommand(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
        }

        if (wasBindingSuccessful) {
            BTCacheManager.getBrushToolSQL().brushToolSQLRequests.deleteAllBrushToolModifiers(player.getUniqueId(), minecraftItemInHand.getType().name());
            BTCacheManager.getBrushToolSQL().brushToolSQLRequests.saveBrushToolCommandSelfIdentifyOrdinal(player.getUniqueId(), minecraftItemInHand, command);
            BTCacheManager.addUUIDMaterialBindToCache(player.getUniqueId(), minecraftItemInHand.getType().name());
        }
    }

    /**
     * Logic to compare if the binding command was successful.
     * @param hadPreviousTool If the player had a previous tool.
     * @param newToolStringReference A toString version of the newTool.
     * @param previousToolStringReference A toString version of the previousTool.
     * @return True if successful, False if not.
     */
    private static boolean isBindingSuccessful(boolean hadPreviousTool, String newToolStringReference, String previousToolStringReference) {
        /*
         * We don't need to run these comparisons if the below states return true when determined and if all else return false. But they're helpful to understand the logic.
         * if (!hadPreviousTool && newToolStringReference.isEmpty()) bindingSuccessful = false; // Did not have tool, still no tool. bindingSuccessful is False.
         * if (hadPreviousTool && previousToolStringReference.equals(newToolStringReference)) bindingSuccessful = false; // Had tool, new tool is same tool. bindingSuccessful is False.
         */
        // Did not have tool, has a new tool. bindingSuccessful is True.
        if (!hadPreviousTool && !newToolStringReference.isEmpty()) return true;
        // Had a tool, and new tool is different. bindingSuccessful is True.
        return hadPreviousTool && !previousToolStringReference.equals(newToolStringReference) && !newToolStringReference.isEmpty();
    }

}
