package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {

    public static final List<Cooldown> allCoolDowns = new ArrayList<>();
    private final Map<UUID, Long> uuidToLongMap = new ConcurrentHashMap<>();
    private final long CoolDownTimeMS;

    public Cooldown(long coolDownTimeMS){
        this.CoolDownTimeMS = coolDownTimeMS;
        allCoolDowns.add(this);
    }

    public boolean needsToWait(Player player){
        if (uuidToLongMap.get(player.getUniqueId()) == null) return false;
        long timeLeftMs = uuidToLongMap.get(player.getUniqueId()) - System.currentTimeMillis();
        if (timeLeftMs > 0) {
            long secondsLeft = (timeLeftMs / 1000) + 1;
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    "&cPlease wait &e" + secondsLeft + " second(s) &cbefore using this command again!"));
            return true;
        }
        return false;
    }

    public void placeCooldown(Player player){
        uuidToLongMap.put(player.getUniqueId(), System.currentTimeMillis() + CoolDownTimeMS);
    }

    public static void removePlayerFromMaps(UUID uuid){
        allCoolDowns.forEach(CoolDownManager -> CoolDownManager.uuidToLongMap.remove(uuid));
    }

}
