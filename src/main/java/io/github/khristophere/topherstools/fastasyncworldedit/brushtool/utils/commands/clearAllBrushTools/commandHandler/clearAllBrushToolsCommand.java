package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.clearAllBrushTools.commandHandler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.utilities.Utils;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class clearAllBrushToolsCommand {

    /**
     * Instantiates the ClearAllBrushTools Command.
     */
    public clearAllBrushToolsCommand(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new Cooldown(10000);
    }

    private final Cooldown coolDowns;

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
            TophersTools.getInstance().runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                List<String> allMaterialsInFileForUUID = BrushToolManager.allUUIDsToListMaterialsBound.get(player.getUniqueId());
                TophersTools.getInstance().runOnPrimaryThread(( () -> {
                    Player playerMainThread = Bukkit.getPlayer(uuid);
                    if (playerMainThread == null || !playerMainThread.isOnline()) return;
                    if (allMaterialsInFileForUUID == null || allMaterialsInFileForUUID.isEmpty()) {
                        player.sendMessage(PlayerMessages.BrushTools_Cleared.getMessage());
                        return;
                    }
                    com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(playerMainThread);
                    LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
                    for (String materialInFile : allMaterialsInFileForUUID){
                        BrushToolManager.brushToolSQL.deleteBrushToolLoadoutSQL(uuid, materialInFile);
                        Material mat = Material.matchMaterial(materialInFile);
                        if (mat != null) {
                            BaseItem baseItem = BukkitAdapter.adapt(new ItemStack(mat));
                            worldEditSession.setTool(baseItem, null);
                        }
                    }
                    BrushToolManager.allUUIDsToListMaterialsBound.put(player.getUniqueId(), new ArrayList<>());
                    player.sendMessage(PlayerMessages.BrushTools_Cleared.getMessage());
                }));
                coolDowns.placeCooldown(player);
            });

        }

    }
}

