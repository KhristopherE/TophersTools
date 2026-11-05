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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall.BTClearAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget.BTGet;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.BTMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall.BTReturnAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout.BTShout;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages command and action cooldowns for players, expiration checks, and global registry management.
 */
public class Cooldown {

    public static List<Cooldown> ALL_COOLDOWNS = new ArrayList<>();
    private final Map<UUID, Long> uuidToExpirationMap = new ConcurrentHashMap<>();
    private final long cooldownTimeMs;

    /**
     * Constructs a new Cooldown timer with a specified duration.
     *
     * @param cooldownTimeMs the cooldown duration in milliseconds.
     */
    public Cooldown(long cooldownTimeMs) {
        this.cooldownTimeMs = cooldownTimeMs;
        ALL_COOLDOWNS.add(this);
    }

    /**
     * Clears all registered cooldown instances and re-loads cooldown configurations across command handlers.
     */
    public static void refreshAllCooldowns() {
        ALL_COOLDOWNS.clear();
        BTClearAll.refreshCooldownConfig();
        BTGet.refreshCooldownConfig();
        BTMacro.refreshCooldownConfig();
        BTReturnAll.refreshCooldownConfig();
        BTShout.refreshCooldownConfig();
    }

    /**
     * Removes a player from all active cooldown maps.
     *
     * @param uuid the unique identifier of the player to remove.
     */
    public static void removePlayerFromAllCooldowns(@NotNull UUID uuid) {
        for (Cooldown cooldown : ALL_COOLDOWNS) cooldown.uuidToExpirationMap.remove(uuid);
    }

    /**
     * Checks if a player is currently on cooldown.
     * If on cooldown, sends a formatted warning message with the remaining time to the player.
     *
     * @param player the player to check.
     * @return true if the player must wait, false if the cooldown has expired or does not exist.
     */
    public boolean needsToWait(@NotNull Player player) {
        Long expirationTime = uuidToExpirationMap.get(player.getUniqueId());
        if (expirationTime == null) return false;

        long timeLeftMs = expirationTime - System.currentTimeMillis();
        if (timeLeftMs > 0) {
            long secondsLeft = (timeLeftMs / 1000) + 1;
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    "&cPlease wait &e" + secondsLeft + " second(s) &cbefore using this command again!"
            ));
            return true;
        }
        uuidToExpirationMap.remove(player.getUniqueId());
        return false;
    }

    /**
     * Places a player on cooldown for the configured duration.
     *
     * @param player the player to place on cooldown.
     */
    public void placeCooldown(@NotNull Player player) {
        uuidToExpirationMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTimeMs);
    }

}
