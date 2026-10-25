package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall.BTClearAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget.BTGet;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.BTMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall.BTReturnAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout.BTShout;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.*;
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
     * @param cooldownTimeMs the cooldown duration in milliseconds
     */
    public Cooldown(long cooldownTimeMs) {
        this.cooldownTimeMs = cooldownTimeMs;
        ALL_COOLDOWNS.add(this);
    }

    /**
     * Clears all registered cooldown instances and re-loads cooldown configurations
     * across command handlers.
     */
    public static void refreshAllCooldowns(){
        ALL_COOLDOWNS.clear();
        BTClearAll.refreshCooldownConfig();
        BTGet.refreshCooldownConfig();
        BTMacro.refreshCooldownConfig();
        BTReturnAll.refreshCooldownConfig();
        BTShout.refreshCooldownConfig();
    }

    /**
     * Checks if a player is currently on cooldown. If so, sends a formatted warning message
     * displaying the remaining time.
     *
     * @param player the player to check
     * @return true if the player must wait, false if the cooldown has expired or does not exist
     */
    public boolean needsToWait(Player player) {
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
     * @param player the player to place on cooldown
     */
    public void placeCooldown(Player player) {
        uuidToExpirationMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTimeMs);
    }

    /**
     * Removes a player from all active cooldown maps.
     *
     * @param uuid the UUID of the player to remove
     */
    public static void removePlayerFromAllCooldowns(UUID uuid) {
        for (Cooldown cooldown : ALL_COOLDOWNS) {
            cooldown.uuidToExpirationMap.remove(uuid);
        }
    }

}
