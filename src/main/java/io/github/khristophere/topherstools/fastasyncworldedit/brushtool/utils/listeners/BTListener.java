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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners;

import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.BTMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTBinding;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTModifying;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTUnbinding;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for player events related to brush / tool commands, joins, and quits.
 */
public class BTListener implements Listener {

    public static BTCacheManager btCacheManager;

    /**
     * Constructs a new BTListener and registers events.
     */
    public BTListener() {
        btCacheManager = new BTCacheManager();
        TophersTools.getInstance().getServer().getPluginManager().registerEvents(this, TophersTools.getInstance());
    }

    /**
     * Listens for player command preprocessing to intercept brush / tool binding, modification, and unbinding commands.
     *
     * @param event the player command preprocess event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        if (BTCommandLogic.isCommandUnbinding(command)) {
            BTUnbinding.processCommand(event.getPlayer(), command, false);
            event.setCancelled(true);
            return;
        }
        if (BTCommandLogic.isCommandModifying(command)) {
            event.setCancelled(true);
            BTModifying.processCommand(event.getPlayer(), command, false);
            return;
        }
        if (BTCommandLogic.isCommandBinding(command)) {
            event.setCancelled(true);
            BTBinding.processCommand(event.getPlayer(), command, false);
        }
    }

    /**
     * Updates the player's username in the database & active cache upon joining the server.
     *
     * @param event the player join event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        var playerName = player.getName();
        var playerUUID = player.getUniqueId();
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().updatePlayerName(playerUUID, playerName);
        BTCacheManager.addPlayerToCache(playerUUID, playerName);
    }

    /**
     * Removes the player from cooldowns and pending deletions upon leaving the server.
     *
     * @param event the player quit event.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        Cooldown.removePlayerFromAllCooldowns(uuid);
        BTMacro.PendingDeletion.pendingRequests.remove(uuid);
    }

}
