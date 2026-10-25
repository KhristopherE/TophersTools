package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.List;
import java.util.UUID;

/**
 * Command handler for clearing all active brush and tool bindings for a player,
 * removing them from persistent database storage and unbinding them from FastAsyncWorldEdit.
 */
public class BTClearAll {

    public BTClearAll(){
        Utils.registerClassWithCommandManager(this);
        coolDowns = new Cooldown(Config.CooldownBTClearAll);
    }

    private static Cooldown coolDowns;
    public static void refreshCooldownConfig(){
        coolDowns = new Cooldown(Config.CooldownBTClearAll);
    }

    /**
     * Clears all the requesters brush/tool bindings from the SQL File, & unbinds all brushes/tools from FAWE.
     * @param sender The command sender.
     */
    @Command("clearallbrushes")
    @Command("cab")
    @Command("clearalltools")
    @Command("cat")
    @Permission("topherstools.bt.clearall")
    private void clearALlBrushTools(Source sender) {
        if (sender.source() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            Scheduler.runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                List<String> allMaterialsInFileForUUID = BTCacheManager.getUUIDMaterialBindsInCache().get(player.getUniqueId());
                Scheduler.runOnPrimaryThread(( () -> {
                    Player playerMainThread = Bukkit.getPlayer(uuid);
                    if (playerMainThread == null || !playerMainThread.isOnline()) return;
                    if (allMaterialsInFileForUUID == null || allMaterialsInFileForUUID.isEmpty()) {
                        player.sendMessage(PlayerMessages.BrushTools_Cleared.getMessage());
                        return;
                    }
                    com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(playerMainThread);
                    LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
                    for (String materialInFile : allMaterialsInFileForUUID){
                        BTCacheManager.getBrushToolSQL().deleteBrushToolLoadoutSQL(uuid, materialInFile);
                        Material mat = Material.matchMaterial(materialInFile);
                        if (mat != null) {
                            BaseItem baseItem = BukkitAdapter.adapt(new ItemStack(mat));
                            worldEditSession.setTool(baseItem, null);
                        }
                    }
                    BTCacheManager.emptyUUIDMaterialBindsInCache(player.getUniqueId());
                    player.sendMessage(PlayerMessages.BrushTools_Cleared.getMessage());
                }));
                coolDowns.placeCooldown(player);
            });

        }

    }
}

