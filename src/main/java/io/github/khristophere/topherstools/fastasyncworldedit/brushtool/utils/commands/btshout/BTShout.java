/*
 * Copyright (C) 2026 KhristopherE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Command handler for sharing a player's currently held brush / tool loadout and prompt all online players.
 */
public class BTShout {

    private static Cooldown coolDowns;

    /**
     * Constructs a new BTShout instance and registers it with the command manager.
     */
    public BTShout() {
        Utils.registerClassWithCommandManager(this);
        refreshCooldownConfig();
    }

    /**
     * Refreshes the command's cooldown duration from the latest configuration settings.
     */
    public static void refreshCooldownConfig() {
        coolDowns = new Cooldown(Config.CooldownBTShout);
    }


    /**
     * Shares the command issuer's currently held brush / tool loadout and prompts all other online players.
     *
     * @param sender the command sender executing the command.
     */
    @Command("shoutbrush|shouttool|sharebrush|sharetool|sb|st")
    @Permission({"topherstools.bt.shout", "topherstools.bt.share"})
    private void shoutBT(@NotNull Source sender) {
        //noinspection DuplicatedCode - Duplicated to BTGet
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        if (coolDowns.needsToWait(player)) return;
        coolDowns.placeCooldown(player);

        UUID uuid = player.getUniqueId();
        String material = player.getInventory().getItemInMainHand().getType().name();

        Scheduler.runSequentialAsync(() -> {
            BTLoadout loadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(uuid, material);
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null || !onlinePlayer.isOnline()) return;

            if (loadout == null || loadout.isEmpty()) {
                onlinePlayer.sendMessage(Messages.BrushTool_Not_Bound_Self.getMessage());
                return;
            }

            onlinePlayer.sendMessage(Messages.BrushTool_Info_Below_Self.getMessage());
            onlinePlayer.sendMessage(loadout.getLoadoutPromptMessage());
            onlinePlayer.sendMessage(Messages.BrushTool_Shared_With_Everyone.getMessage());

            Component broadcastMessage = Messages.BrushTool_Shared_From_Specified_Player.getMessage(onlinePlayer.getName());
            for (Player onlineTarget : Bukkit.getServer().getOnlinePlayers()) {
                if (!onlineTarget.equals(onlinePlayer)) {
                    onlineTarget.sendMessage(loadout.getLoadoutPromptMessage());
                    onlineTarget.sendMessage(broadcastMessage);
                }
            }
        });
    }

}
