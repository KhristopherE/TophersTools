package khristopher.FAWE_BT_Utils.commands.brushToolCommands.shoutBrushTool.commandHandler;

import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.b.utils.Utils;
import khristopher.FAWE_BT_Utils.cooldown.CoolDownManager;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolLoadout;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import khristopher.a.main.TophersTools;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.ArrayList;
import java.util.List;

public class shoutBrushToolCommand {

    /**
     * Instantiates the ShoutBrushTool Command.
     */
    public shoutBrushToolCommand(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new CoolDownManager(5000);
    }

    private final CoolDownManager coolDowns;

    /**
     * Sends all online players a message containing the command issuing players currently held brush.
     * @param sender The command sender.
     */
    @Command("shoutbrush")
    @Command("shouttool")
    @Command("sharebrush")
    @Command("sharetool")
    @Command("sb")
    @Command("st")
    @Permission({"topherstools.bt.shout", "topherstools.bt.share"})
    private void shoutBrushTool(Source sender) {
        if (sender.source() instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                if (coolDowns.needsToWait(player)) return;
                BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
                if (loadout != null && !loadout.isBrushToolLoadoutEmpty()) {
                    player.sendMessage(PlayerMessages.BrushTool_Info_Below_Self.getMessage());
                    player.sendMessage(loadout.getLoadoutPromptMessage());
                    player.sendMessage(PlayerMessages.BrushTool_Shared_With_Everyone.getMessage());

                    List<Player> copyListOfOnlinePlayers = new ArrayList<>(List.copyOf(Bukkit.getServer().getOnlinePlayers()));
                    copyListOfOnlinePlayers.remove(player);
                    Component messageForOnlinePlayers = PlayerMessages.BrushTool_Shared_From_Specified_Player.getMessage(player.getName());
                    for (Player playersOnline : copyListOfOnlinePlayers) {
                        playersOnline.sendMessage(loadout.getLoadoutPromptMessage());
                        playersOnline.sendMessage(messageForOnlinePlayers);
                    }
                } else {
                    player.sendMessage(PlayerMessages.BrushTool_Not_Bound_Self.getMessage());
                }
                coolDowns.placeCooldown(player);
            });
        }
    }

}
