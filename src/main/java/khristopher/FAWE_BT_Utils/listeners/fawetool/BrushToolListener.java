package khristopher.FAWE_BT_Utils.listeners.fawetool;

import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import khristopher.FAWE_BT_Utils.listeners.fawetool.arguments.brushToolBinding;
import khristopher.FAWE_BT_Utils.listeners.fawetool.arguments.brushToolModify;
import khristopher.FAWE_BT_Utils.listeners.fawetool.arguments.brushToolUnbind;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolCommandLogic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class BrushToolListener implements Listener {

    public static BrushToolManager brushToolManager;

    /**
     * Instantiates the entirety of the BrushToolListener functionalities.
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    public BrushToolListener(){
        //Instantiates the BrushToolManager, and makes it public static accessible.
        brushToolManager = new BrushToolManager();
        //Registers this class with the server as a listener. See above, "implements Listener".
        TophersTools.getInstance().getServer().getPluginManager().registerEvents(this, TophersTools.getInstance());


    }

    /**
     * This listens for a player to initiate a command, we listen in at the HIGHEST priority or the last opportunity in line to cancel the event, ignoring canceled events.
     * We then check the command to see if it is either FAWE / World Edit unbinding, a modifier or binding. If it is, we cancel the event to prevent FAWE from processing
     * it as asyncronous as this make's the checking of binding vary in effectivness. We then pass the details player and command to the dedicated class for processing those
     * requests. In each (except unbinding) it will process the initial state of the tool, then process the FAWE / World Edit command on the current thread, then immediatly
     * check the binding details to confirm if it was succesful. If succesful it saves the binding command / modifier. If an unbinding command it still runs the command on
     * the current thread, but just deletes the command details from that material in the save file.
     * @param event The event being called.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        if (BrushToolCommandLogic.isCommandUnbinding(command)){
            brushToolUnbind.processCommand(event.getPlayer(), command);
            event.setCancelled(true);
            return;
        }
        if (BrushToolCommandLogic.isCommandModifier(command)) {
            event.setCancelled(true);
            brushToolModify.processCommand(event.getPlayer(), command);
            return;
        }
        if(BrushToolCommandLogic.isCommandBinding(command)){
            event.setCancelled(true);
            brushToolBinding.processCommand(event.getPlayer(), command);
        }
    }

    /**
     * This is called only when a player joins the server to update their name in the "FAWE-BrushTools/SQLData", "PlayerData.DB" file.
     * @param event The event being called.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BrushToolManager.brushToolSQL.brushToolSQLRequests.updatePlayerName(player.getUniqueId(), player.getName());
    }

}
