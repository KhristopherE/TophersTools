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
    /**
     * Refreshes the command's cooldown duration from the latest configuration settings.
     */
    public static void refreshCooldownConfig(){
        coolDowns = new Cooldown(Config.CooldownBTClearAll);
    }

    /**
     * Clears all of the requester's brush and tool bindings from the SQL database
     * and unbinds all associated tools from FastAsyncWorldEdit.
     *
     * @param sender the command source executing the command
     */
    @Command("clearallbrushes|cab|clearalltools|cat")
    @Permission("topherstools.bt.clearall")
    private void clearAllBrushTools(Source sender) {
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
            BTCacheManager.getBrushToolSQL().deleteBrushToolLoadoutSQL(uuid, materialName);
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

