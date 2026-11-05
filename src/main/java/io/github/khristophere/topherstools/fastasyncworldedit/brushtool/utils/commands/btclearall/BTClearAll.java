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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Command handler for clearing all active brush / tool bindings for a player,
 * removing them from the database and unbinding them from FastAsyncWorldEdit.
 */
public class BTClearAll {

    private static Cooldown coolDowns;

    /**
     * Constructs a new BTClearAll instance and registers it with the command manager.
     */
    public BTClearAll() {
        Utils.registerClassWithCommandManager(this);
        refreshCooldownConfig();
    }

    /**
     * Refreshes the command's cooldown duration from the latest configuration settings.
     */
    public static void refreshCooldownConfig() {
        coolDowns = new Cooldown(Config.CooldownBTClearAll);
    }

    /**
     * Clears all of the requester's brush / tool bindings from the database
     * and unbinds all associated brush / tools from FastAsyncWorldEdit.
     *
     * @param sender the command sender executing the command.
     */
    @Command("clearallbrushes|cab|clearalltools|cat")
    @Permission("topherstools.bt.clearall")
    private void clearAllBT(@NotNull Source sender) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(Messages.Command_Reserved_For_Players.getMessage());
            return;
        }

        if (coolDowns.needsToWait(player)) return;
        coolDowns.placeCooldown(player);

        UUID uuid = player.getUniqueId();
        List<String> cachedBindings = BTCacheManager.getUUIDMaterialBindsInCache().get(uuid);

        if (cachedBindings == null || cachedBindings.isEmpty()) {
            player.sendMessage(Messages.BrushTools_Cleared.getMessage());
            return;
        }

        for (String materialName : cachedBindings) {
            BTCacheManager.getBTSQLExecutor().deleteBrushToolLoadout(uuid, materialName);
        }
        BTCacheManager.emptyUUIDMaterialBindsInCache(uuid);

        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        if (worldEditSession != null) {
            for (String materialName : cachedBindings) {
                Material mat = Material.matchMaterial(materialName);
                if (mat != null) {
                    BaseItem baseItem = BukkitAdapter.adapt(new ItemStack(mat));
                    worldEditSession.setTool(baseItem, null);
                }
            }
        }

        player.sendMessage(Messages.BrushTools_Cleared.getMessage());
    }

}

