package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.utilities.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.ArrayList;
import java.util.List;

public class BTShout {

    public BTShout(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new Cooldown(5000);
    }
    private final Cooldown coolDowns;

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
            Scheduler.runSequentialAsync( () -> {
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
