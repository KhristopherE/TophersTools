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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadoutMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.dkmacros.DKFirstMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command handler for saving, loading, and deleting brush / tool macros.
 */
public class BTMacro {

    private static Cooldown coolDownsSaves;
    private static Cooldown coolDownsLoads;
    private static Cooldown coolDownsDeletes;

    /**
     * Constructs a new BTMacro instance and registers it with the command manager.
     */
    public BTMacro() {
        Utils.registerClassWithCommandManager(this);
        refreshCooldownConfig();
    }

    /**
     * Refreshes all command cooldown durations from the latest configuration settings.
     */
    public static void refreshCooldownConfig() {
        coolDownsSaves = new Cooldown(Config.CooldownBTMacroSave);
        coolDownsLoads = new Cooldown(Config.CooldownBTMacroLoad);
        coolDownsDeletes = new Cooldown(Config.CooldownBTMacroDelete);
    }

    /**
     * Sanitizes input strings to ensure safe file naming conventions.
     *
     * @param input the raw input string.
     * @return the sanitized string.
     */
    @NotNull
    public static String sanitizeInput(@NotNull String input) {
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    /**
     * Allows a player to save their current inventory of brush / tool loadouts into a shareable macro.
     *
     * @param sender    the command sender executing the command.
     * @param macroName the name of the macro to save.
     * @param override  whether to overwrite an existing macro file.
     */
    @Command("brushmacro save <macroname>")
    @Command("bm save <macroname>")
    @Command("toolmacro save <macroname>")
    @Command("tm save <macroname>")
    @Permission("topherstools.bt.macro.save")
    private void saveBTMacro(@NotNull Source sender, @NotNull @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName, @Flag(value = "override", aliases = "o") boolean override) {
        //noinspection DuplicatedCode - Duplicated 3x in this Class.
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        String finalMacroName = sanitizeInput(macroName);
        if (finalMacroName.length() > 48) {
            player.sendMessage(Messages.BrushTool_Max_Length.getMessage());
            return;
        }

        if (coolDownsSaves.needsToWait(player)) return;

        UUID uuid = player.getUniqueId();

        Scheduler.runSequentialAsync(() -> {
            Player asyncPlayer = Bukkit.getPlayer(uuid);
            if (asyncPlayer == null || !asyncPlayer.isOnline()) return;

            BTLoadoutMacro loadoutMacro = BTCacheManager.getBTLoadoutMacroFromCacheOrFile(finalMacroName);
            if (loadoutMacro.alreadyExists() && !override) {
                asyncPlayer.sendMessage(Messages.BrushTool_Macro_Already_Exists_Requesting_Override.getMessageMacroName(finalMacroName));
                return;
            }

            Scheduler.runOnPrimaryThread(() -> {
                Player syncPlayer = Bukkit.getPlayer(uuid);
                if (syncPlayer == null || !syncPlayer.isOnline()) return;

                loadoutMacro.saveToFile(syncPlayer); // Inventory calls from main thread, then schedules as Async for File Writing.
                syncPlayer.sendMessage(Messages.BrushTool_Macro_Saved.getMessageMacroName(finalMacroName));
                coolDownsSaves.placeCooldown(syncPlayer);

                Scheduler.runSequentialAsync(() -> BTCacheManager.addBTLoadoutMacroNameToCache(finalMacroName));
            });
        });
    }

    /**
     * Allows a player to load a saved macro of brush / tool loadouts into their inventory.
     *
     * @param sender    the command sender executing the command.
     * @param macroName the name of the macro to load.
     */
    @Command("brushmacro load <macroname>")
    @Command("bm load <macroname>")
    @Command("toolmacro load <macroname>")
    @Command("tm load <macroname>")
    @Permission("topherstools.bt.macro.load")
    private void loadBTMacro(@NotNull Source sender, @NotNull @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName) {
        //noinspection DuplicatedCode - Duplicated 3x in this Class.
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        String finalMacroName = sanitizeInput(macroName);
        if (finalMacroName.length() > 48) {
            player.sendMessage(Messages.BrushTool_Max_Length.getMessage());
            return;
        }

        if (coolDownsLoads.needsToWait(player)) return;

        UUID uuid = player.getUniqueId();

        Scheduler.runSequentialAsync(() -> {
            //noinspection DuplicatedCode - Duplicated 2x in this Class.
            Player asyncPlayer = Bukkit.getPlayer(uuid);
            if (asyncPlayer == null || !asyncPlayer.isOnline()) return;

            BTLoadoutMacro loadoutMacro = BTCacheManager.getBTLoadoutMacroFromCacheOrFile(finalMacroName);

            if (!loadoutMacro.alreadyExists()) {
                asyncPlayer.sendMessage(Messages.BrushTool_Macro_Not_Exist.getMessageMacroName(finalMacroName));
                return;
            }

            if (!loadoutMacro.alreadyLoadedFromFile()) loadoutMacro.loadFromFile();

            if (loadoutMacro.isBrushToolLoadoutEmpty()) {
                asyncPlayer.sendMessage(Messages.Warning_BrushTool_Macro_Is_Empty.getMessageMacroName(finalMacroName));
                return;
            }

            Scheduler.runOnPrimaryThread(() -> {
                Player syncPlayer = Bukkit.getPlayer(uuid);
                if (syncPlayer == null || !syncPlayer.isOnline()) return;

                loadoutMacro.applyTo(uuid);
                coolDownsLoads.placeCooldown(syncPlayer);
            });
        });
    }

    /**
     * Allows a player to delete a saved brush / tool loadout macro with a 15-second confirmation window.
     *
     * @param sender       the command sender executing the command.
     * @param macroName    the name of the macro to delete.
     * @param confirmation 'semi-optional' confirmation argument.
     */
    @Command("brushmacro delete <macroname> [confirm]")
    @Command("bm delete <macroname> [confirm]")
    @Command("toolmacro delete <macroname> [confirm]")
    @Command("tm delete <macroname> [confirm]")
    @Permission("topherstools.bt.macro.delete")
    private void deleteBTMacro(@NotNull Source sender, @NotNull @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName, @Nullable @Argument(value = "confirm") String confirmation) {
        //noinspection DuplicatedCode - Duplicated 3x in this Class.
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        String finalMacroName = sanitizeInput(macroName);
        if (finalMacroName.length() > 48) {
            player.sendMessage(Messages.BrushTool_Max_Length.getMessage());
            return;
        }

        if (coolDownsDeletes.needsToWait(player)) return;

        UUID uuid = player.getUniqueId();

        Scheduler.runSequentialAsync(() -> {
            //noinspection DuplicatedCode - Duplicated 2x in this Class.
            Player asyncPlayer = Bukkit.getPlayer(uuid);
            if (asyncPlayer == null || !asyncPlayer.isOnline()) return;

            BTLoadoutMacro loadoutMacro = BTCacheManager.getBTLoadoutMacroFromCacheOrFile(finalMacroName);

            if (!loadoutMacro.alreadyExists()) {
                asyncPlayer.sendMessage(Messages.BrushTool_Macro_Not_Exist.getMessageMacroName(finalMacroName));
                return;
            }

            if (confirmation != null && confirmation.equalsIgnoreCase("Confirm")) {
                PendingDeletion pending = PendingDeletion.pendingRequests.get(uuid);

                if (pending != null && pending.macroName.equals(finalMacroName)) {
                    if (!pending.isExpired()) {
                        if (loadoutMacro.deleteBrushToolLoadoutMacro()) {
                            player.sendMessage(Messages.BrushTool_Macro_Deleted.getMessageMacroName(finalMacroName));
                            BTCacheManager.removeBTLoadoutMacroNameFromCache(finalMacroName);
                            BTCacheManager.removeBTLoadoutMacroFromCache(finalMacroName);
                            PendingDeletion.pendingRequests.remove(uuid);
                            coolDownsDeletes.placeCooldown(player);
                        } else {
                            player.sendMessage(Messages.BrushTool_Macro_Deletion_Failed.getMessageMacroName(finalMacroName));
                        }
                    } else {
                        player.sendMessage(Messages.BrushTool_Macro_Delete_Confirmation_Window_Expired.getMessageMacroName(finalMacroName));
                        PendingDeletion.pendingRequests.remove(uuid);
                    }
                } else {
                    player.sendMessage(Messages.BrushTool_Macro_Delete_Requesting_Initial_Request_Before_Confirmation.getMessageMacroName(finalMacroName));
                }
            } else {
                player.sendMessage(Messages.BrushTool_Macro_Delete_Requesting_Confirmation.getMessageMacroName(finalMacroName));
                PendingDeletion.pendingRequests.put(uuid, new PendingDeletion(finalMacroName));
            }
        });
    }

    /**
     * Provides the sender with a help menu for macro commands, or triggers developer shortcuts for specified players.
     * Shortcuts for KhristopherE or TheWither_MAN, provides a set of premade brush/tools. (This was the original purpose / functionality of the plugin but has since expanded.)
     *
     * @param sender the command sender executing the command.
     */
    @Command("brushmacro")
    @Command("bm")
    @Command("toolmacro")
    @Command("tm")
    @Permission("topherstools.bt.macro")
    private void help(@NotNull Source sender) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }
        var uuid = player.getUniqueId();
        if (uuid.equals(UUID.fromString("9e278e4b-5296-4313-8e05-feedaeaaa050")) ||
                uuid.equals(UUID.fromString("fbbfa97b-d2e2-47e1-8987-15e83d8e005c"))) {
            DKFirstMacro.DKInstantMacro(player);
        } else {
            player.sendMessage(Messages.BrushTool_Macro_Help.getMessage());
        }
    }

    /**
     * Provides a list of suggested brush / tool macro names.
     *
     * @param context the command context.
     * @param input   the input string being matched.
     * @return the list of suggested macro names.
     */
    @NotNull
    @Suggestions("suggestedMacros")
    public List<String> getSuggestedBrushToolMacros(@NotNull CommandContext<Source> context, @NotNull String input) {
        return BTCacheManager.getBTLoadoutMacroNamesInCache();
    }

    /**
     * Represents a pending deletion confirmation request with an expiration timestamp.
     */
    public static class PendingDeletion {
        public static final Map<UUID, PendingDeletion> pendingRequests = new ConcurrentHashMap<>();
        private final String macroName;
        private final long timestamp;

        /**
         * Constructs a new PendingDeletion instance.
         *
         * @param macroName the name of the macro pending deletion.
         */
        public PendingDeletion(@NotNull String macroName) {
            this.macroName = macroName;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Checks whether the pending deletion request has expired.
         *
         * @return true if expired, false otherwise.
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 15000;
        }
    }

}
