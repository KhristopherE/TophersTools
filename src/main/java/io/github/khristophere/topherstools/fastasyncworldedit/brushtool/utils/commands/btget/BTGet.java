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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for retrieving active or previously bound brush / tool bindings and loadout prompts
 * for self, specified players, & or specific materials.
 */
public class BTGet {

    private static Cooldown coolDowns;

    /**
     * Constructs a new BTGet instance and registers it with the command manager.
     */
    public BTGet() {
        Utils.registerClassWithCommandManager(this);
        refreshCooldownConfig();
    }

    /**
     * Refreshes the command's cooldown duration from the latest configuration settings.
     */
    public static void refreshCooldownConfig() {
        coolDowns = new Cooldown(Config.CooldownBTGet);
    }

    /**
     * Provides the sender with the currently held item's bound brush / tool command and modifiers in a click-to-apply format.
     *
     * @param sender the command sender executing the command.
     */
    @Command("getbrush")
    @Command("gb")
    @Command("gettool")
    @Command("gt")
    @Permission("topherstools.bt.get.self")
    private void getBT(@NotNull Source sender) {
        //noinspection DuplicatedCode - Duplicated to BTShout
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

            if (loadout != null && !loadout.isEmpty()) {
                onlinePlayer.sendMessage(Messages.BrushTool_Info_Below_Self.getMessage());
                onlinePlayer.sendMessage(loadout.getLoadoutPromptMessage());
            } else {
                onlinePlayer.sendMessage(Messages.BrushTool_Not_Bound_Self.getMessage());
            }
        });
    }

    /**
     * Provides the specified player's currently held item's bound brush / tool command and modifiers in a click-to-apply format.
     *
     * @param sender          the command sender executing the command.
     * @param requestedPlayer the name of the target player.
     */
    @Command("getbrush <player>")
    @Command("gb <player>")
    @Command("gettool <player>")
    @Command("gt <player>")
    @Permission("topherstools.bt.get.specified.player")
    private void getBTByPlayer(@NotNull Source sender, @NotNull @Argument(value = "player", suggestions = "suggestedPlayers") String requestedPlayer) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        if (coolDowns.needsToWait(player)) return;
        coolDowns.placeCooldown(player);

        Player targetPlayer = Bukkit.getPlayer(requestedPlayer);
        if (targetPlayer == null) {
            player.sendMessage(Messages.BrushTool_Info_Inquiry_Player_Offline_Requesting_Specify_Material.getMessage());
            return;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        String material = targetPlayer.getInventory().getItemInMainHand().getType().name();
        UUID senderUuid = player.getUniqueId();

        Scheduler.runSequentialAsync(() -> {
            BTLoadout loadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(targetUuid, material);
            Player senderPlayer = Bukkit.getPlayer(senderUuid);
            if (senderPlayer == null || !senderPlayer.isOnline()) return;

            if (loadout != null && !loadout.isEmpty()) {
                senderPlayer.sendMessage(Messages.BrushTool_Info_Below_Specified_Player.getMessage(targetPlayer.getName()));
                senderPlayer.sendMessage(loadout.getLoadoutPromptMessage());
            } else {
                senderPlayer.sendMessage(Messages.BrushTool_Not_Bound_Specified_Player.getMessage(targetPlayer.getName()));
            }
        });
    }

    /**
     * Provides the specified player's bound brush / tool command and modifiers for a specific material in a click-to-apply format.
     *
     * @param sender            the command sender executing the command.
     * @param requestedPlayer   the name of the target player.
     * @param requestedMaterial the requested material.
     */
    @Command("getbrush <player> <material>")
    @Command("gb <player> <material>")
    @Command("gettool <player> <material>")
    @Command("gt <player> <material>")
    @Permission("topherstools.bt.get.specified.player.specified.material")
    private void getBTByPlayerByMaterial(@NotNull Source sender, @NotNull @Argument(value = "player", suggestions = "suggestedPlayers") String requestedPlayer, @NotNull @Argument(value = "material", suggestions = "suggestedMaterials") String requestedMaterial) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        if (coolDowns.needsToWait(player)) return;
        coolDowns.placeCooldown(player);

        UUID senderUuid = player.getUniqueId();
        UUID targetUuid = BTCacheManager.getUsernameUUIDMapInCache().get(requestedPlayer);
        String material = requestedMaterial.toUpperCase();

        Scheduler.runSequentialAsync(() -> {
            BTLoadout loadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(targetUuid, material);
            Player senderPlayer = Bukkit.getPlayer(senderUuid);
            if (senderPlayer == null || !senderPlayer.isOnline()) return;

            if (loadout != null && !loadout.isEmpty()) {
                senderPlayer.sendMessage(Messages.BrushTool_Info_Below_Specified_Player_Specified_Material.getMessage(requestedPlayer, requestedMaterial));
                senderPlayer.sendMessage(loadout.getLoadoutPromptMessage());
            } else {
                senderPlayer.sendMessage(Messages.BrushTool_Not_Bound_Specified_Player_Specified_Material.getMessage(requestedPlayer, requestedMaterial));
            }
        });
    }

    /**
     * Provides a filtered and limited list of suggested player names.
     *
     * @param context the command context.
     * @param input   the current string the user has typed so far.
     * @return a filtered, capped list of matching usernames.
     */
    @Suggestions("suggestedPlayers")
    @NotNull
    public List<String> getSuggestedPlayers(@NotNull CommandContext<Source> context, @NotNull String input) {
        return BTCacheManager.getUsernameUUIDMapInCache().keySet().stream()
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .limit(25)
                .toList();
    }

    /**
     * Provides a filtered and limited list of suggested materials for a target player.
     *
     * @param context the command context.
     * @param input   the current string the user has typed so far.
     * @return a filtered, capped list of matching materials.
     */
    @Suggestions("suggestedMaterials")
    @NotNull
    public List<String> getSuggestedMaterials(@NotNull CommandContext<CommandSender> context, @NotNull String input) {
        String requestedPlayer = context.getOrDefault("player", null);
        if (requestedPlayer == null || requestedPlayer.isEmpty())
            return Collections.emptyList();
        UUID targetUUID = BTCacheManager.getUsernameUUIDMapInCache().get(requestedPlayer);
        if (targetUUID == null)
            return Collections.emptyList();
        List<String> targetMaterials = BTCacheManager.getUUIDMaterialBindsInCache().get(targetUUID);
        if (targetMaterials == null || targetMaterials.isEmpty())
            return Collections.emptyList();
        return targetMaterials.stream()
                .filter(material -> material.toLowerCase().contains(input.toLowerCase()))
                .limit(25)
                .toList();
    }

}
