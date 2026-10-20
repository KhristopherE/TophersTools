package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.world.item.ItemType;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolModifierChecker;
import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.inventory.ItemStack;

public class brushToolModify {

    /**
     * Processes the BrushToolModify Listener. In the Event the Main BrushToolListener is called for and the command is already known to be modifying this function is called.
     * These functions keep in mind that the main BrushToolListener is Cancelling any FAWE Command Events that we're filtering for, binding, unbinding, & modify.
     * Then we call FAWE to run the command on the current thread to prevent having to calculate changes over a few varying tick delay due to the async command calculations
     * when the command is handled naturally.
     * @param player The player in the event being called.
     * @param command The command the player is calling.
     */
    public static void processCommand(org.bukkit.entity.Player player, String command) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemType worldEditItemInHand = BukkitAdapter.asItemType(itemInHand.getType());
        if (worldEditItemInHand == null) return;

        Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        BaseItem worldEditBaseItemInHand = worldEditItemInHand.getDefaultState();

        Tool previousTool = worldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        /*
         * Can't modify a non-existing brush/tool, do not listen.
         * It does technically create a brush/tool in world edit,
         * BUT it's useless. Even if later modified, it does not retain any detail.
         */
        if (previousTool == null) return;

        BrushToolModifier brushToolModifier = BrushToolCommandLogic.getStandardizedModifier(command);
        BrushToolModifierChecker modifierChecker = new BrushToolModifierChecker(previousTool, brushToolModifier);

        //Above is collecting information before the command runs, Below is Collecting & Comparing information from after it runs.
        WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager()
                .handleCommandOnCurrentThread(
                        new CommandEvent(worldEditPlayer, BrushToolCommandLogic.cleanCommand(command)));

        LocalSession refreshWorldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        Tool newTool = refreshWorldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        if (newTool == null) return;

        if (modifierChecker.checkIfModified(newTool))
            TophersTools.getInstance().runSequentialAsync( () -> {
                BrushToolManager.brushToolSQL.brushToolSQLRequests.saveBrushToolModifier(player.getUniqueId(), itemInHand.getType().name(), brushToolModifier, command);
            });
    }


}
