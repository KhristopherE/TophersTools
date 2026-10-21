package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.utilities.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
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

public class BTReturnAll {

    public BTReturnAll(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new Cooldown(10000);
    }
    private final Cooldown coolDowns;

    /**
     * Provides the requester all of their brush/tool bindings from the database.
     * @param sender The command sender.
     */
    @Command("returnallbrushes")
    @Command("rab")
    @Command("returnalltools")
    @Command("rat")
    @Permission("topherstools.bt.returnall")
    private void returnAllBrushesTools(Source sender) {
        if (sender.source() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            Scheduler.runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                List<String> allMaterialsInFileForUUID = BrushToolManager.allUUIDsToListMaterialsBound.get(player.getUniqueId());
                if (allMaterialsInFileForUUID == null || allMaterialsInFileForUUID.isEmpty()) {
                    player.sendMessage(PlayerMessages.BrushTools_Not_On_File.getMessage());
                    return;
                }
                boolean didAnyLoadoutApply = false;
                List<BrushToolLoadout> loadoutList = new ArrayList<>();
                for (String materialInFile : allMaterialsInFileForUUID){
                    BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(uuid, materialInFile);
                    if (loadout != null && !loadout.isBrushToolLoadoutEmpty()){
                        Scheduler.runOnPrimaryThread(( () -> {
                            loadoutList.add(loadout);
                            Player playerMainThread = Bukkit.getPlayer(uuid);
                            if (playerMainThread != null && playerMainThread.isOnline()){
                                Material mat = Material.matchMaterial(materialInFile);
                                if (mat != null) {
                                    if (loadout.hasCustomItem()){
                                        player.getInventory().setItemInMainHand(loadout.getCustomItem());
                                        loadout.applyTo(uuid);
                                    } else {
                                        player.getInventory().setItemInMainHand(new ItemStack(mat));
                                        loadout.applyTo(uuid);
                                    }
                                }
                            }
                        }));
                        didAnyLoadoutApply = true;
                    }
                }
                boolean finalDidAnyLoadoutApply = didAnyLoadoutApply;
                Scheduler.runOnPrimaryThread(( () -> {
                    Player playerMainThread = Bukkit.getPlayer(uuid);
                    if (playerMainThread != null && playerMainThread.isOnline() && finalDidAnyLoadoutApply) {
                        Utils.clearInventorySlotsFromTo(playerMainThread, 0, 35);
                        for (BrushToolLoadout loadoutInList : loadoutList) {
                            if (loadoutInList.hasCustomItem()){
                                assert loadoutInList.getCustomItem() != null;
                                player.getInventory().addItem(loadoutInList.getCustomItem());
                            } else {
                                Material mat = Material.matchMaterial(loadoutInList.getMaterial());
                                if (mat != null) {
                                    player.getInventory().addItem(new ItemStack(mat));
                                }
                            }
                        }
                    }
                    if (finalDidAnyLoadoutApply){
                        player.sendMessage(PlayerMessages.BrushTools_ReApplied.getMessage());
                    } else {
                        player.sendMessage(PlayerMessages.Warning_Unable_To_Apply_BrushTools_From_File.getMessage());
                    }
                }));
                coolDowns.placeCooldown(player);
            });

        }
    }
}
