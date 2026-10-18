package khristopher.FAWE_BT_Utils.listeners.fawetool.arguments;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.world.item.ItemType;
import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolCommandLogic;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolModifier;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolModifierChecker;
import org.bukkit.Bukkit;
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
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                BrushToolManager.brushToolSQL.brushToolSQLRequests.saveBrushToolModifier(player.getUniqueId(), itemInHand.getType().name(), brushToolModifier, command);
            });
    }


}
