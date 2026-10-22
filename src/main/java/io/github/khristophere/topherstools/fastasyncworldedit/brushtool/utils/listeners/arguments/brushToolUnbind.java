package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.platform.CommandEvent;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BrushToolManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolCommandLogic;
import org.bukkit.entity.Player;

public class brushToolUnbind {

    /**
     * Processes the BrushToolUnbinding Listener. In the Event the Main BrushToolListener is called for and the command is already known to be unbinding this function is called.
     * These functions keep in mind that the main BrushToolListener is Cancelling any FAWE Command Events that we're filtering for, binding, unbinding, & modify.
     * Then we call FAWE to run the command on the current thread to prevent having to calculate changes over a few varying tick delay due to the async command calculations
     * when the command is handled naturally.
     * @param player The player in the event being called.
     * @param command The command the player is calling.
     */
    public static void processCommand(Player player, String command) {
        WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager()
                .handleCommandOnCurrentThread(
                        new CommandEvent(BukkitAdapter.adapt(player), BrushToolCommandLogic.cleanCommand(command)));

        // Deletes the Brush/Tool from the YML File.
        BrushToolManager.brushToolSQL.brushToolSQLRequests.deleteBrushTool(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
        BrushToolManager.removeFromUUIDsToListMaterialsBoundInMemory(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
    }

}
