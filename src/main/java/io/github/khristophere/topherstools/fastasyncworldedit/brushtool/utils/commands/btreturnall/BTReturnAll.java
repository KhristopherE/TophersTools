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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command handler for returning and rebinding all saved brush / tool loadouts (limit of 36) for a player.
 */
public class BTReturnAll {

    private static Cooldown coolDowns;

    /**
     * Constructs a new BTReturnAll instance and registers it with the command manager.
     */
    public BTReturnAll() {
        Utils.registerClassWithCommandManager(this);
        refreshCooldownConfig();
    }

    /**
     * Refreshes the command's cooldown duration from the latest configuration settings.
     */
    public static void refreshCooldownConfig() {
        coolDowns = new Cooldown(Config.CooldownBTReturnAll);
    }

    /**
     * Returns and rebinds all saved brush / tool loadouts (limit of 36) for the player.
     *
     * @param sender the command sender executing the command.
     */
    @Command("returnallbrushes|rab|returnalltools|rat")
    @Permission("topherstools.bt.returnall")
    private void returnAllBT(@NotNull Source sender) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        if (coolDowns.needsToWait(player)) return;
        coolDowns.placeCooldown(player);

        UUID uuid = player.getUniqueId();

        Scheduler.runSequentialAsync(() -> {
            List<String> cachedMaterials = BTCacheManager.getUUIDMaterialBindsInCache().get(uuid);
            Player asyncPlayer = Bukkit.getPlayer(uuid);
            if (asyncPlayer == null || !asyncPlayer.isOnline()) return;
            if (cachedMaterials == null || cachedMaterials.isEmpty()) {
                asyncPlayer.sendMessage(Messages.BrushTools_Not_On_File.getMessage());
                return;
            }
            cachedMaterials = cachedMaterials.stream().limit(36).toList();
            List<BTLoadout> successfullyFetchedLoadouts = new ArrayList<>();
            for (String materialName : cachedMaterials) {
                BTLoadout loadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(uuid, materialName);
                if (loadout != null && !loadout.isEmpty()) {
                    successfullyFetchedLoadouts.add(loadout);
                }
            }
            if (successfullyFetchedLoadouts.isEmpty()) {
                asyncPlayer.sendMessage(Messages.Warning_Unable_To_Apply_BrushTools_From_File.getMessage());
                return;
            }

            Scheduler.runOnPrimaryThread(() -> {
                Player syncPlayer = Bukkit.getPlayer(uuid);
                if (syncPlayer == null || !syncPlayer.isOnline()) return;
                Utils.clearInventorySlots(syncPlayer, 0, 35);
                int limit = Math.min(successfullyFetchedLoadouts.size(), 36);
                for (int i = 0; i < limit; i++) {
                    BTLoadout loadout = successfullyFetchedLoadouts.get(i);
                    loadout.applyToSpecifiedSlot(syncPlayer, i);
                }
                syncPlayer.sendMessage(Messages.BrushTools_ReApplied.getMessage());
            });
        });
    }

}
