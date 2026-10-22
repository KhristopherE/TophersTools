package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout;

import io.github.khristophere.topherstools.data.management.yml.YMLHandler;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.dkmacros.DKFirstMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BrushToolLoadoutMacro {

    public final YMLHandler macroYML;
    private final HashMap<Integer, BTLoadout> brushToolLoadoutMap = new HashMap<>();
    private final String macroName;

    public BrushToolLoadoutMacro(String fileName){
        this.macroYML = new YMLHandler("FAWE-BT-Utils/Macros", fileName, false);
        this.macroName = fileName;
    }

    /**
     * Checks if the BrushToolLoadoutMacro already exists.
     * @return boolean.
     */
    public boolean alreadyExists(){
        return macroYML.doesFileAlreadyExist();
    }

    /**
     * Saves all the BrushToolLoadouts from the players inventory from slots 0-35.
     * @param onlinePlayer The player we're copying BrushToolLoadouts from.
     */
    public void saveToFile(Player onlinePlayer){
        Scheduler.runSequentialAsync( () -> {
            macroYML.setup();
            for (String key : macroYML.getConfig().getKeys(false)) macroYML.getConfig().set(key, null);
            for (int i = 0; i <= 35; i++){
                ItemStack item = onlinePlayer.getInventory().getItem(i);
                if (item != null){
                    BTLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(onlinePlayer.getUniqueId(), item.getType().name());
                    if (loadout != null && !loadout.isEmpty()) {
                        saveLoadout(i, loadout);
                    }
                }
            }
            macroYML.saveConfig();
        });
    }

    /**
     * Loads the BrushToolLoadouts from the File into the Macro to later apply to a player.
     */
    public void loadFromFile(){
        macroYML.setup();
        brushToolLoadoutMap.clear();
        for (int i = 0; i <= 35; i++){
            String material = this.macroYML.getConfig().getString("InventorySlot." + i + ".Material");
            if (material != null){
                brushToolLoadoutMap.put(i, getLoadout(i));
            }
        }
    }

    /**
     * Checks if the Loadout Map is empty.
     * @return boolean.
     */
    public boolean isBrushToolLoadoutsEmpty(){
        return brushToolLoadoutMap.isEmpty();
    }

    /**
     * Attempts to delete the BrushToolLoadoutMacro File.
     * @return boolean.
     */
    public boolean deleteBrushToolLoadoutMacro(){
        return macroYML.attemptFileDeletion();
    }

    /**
     * Applies this BrushToolMacroLoadout to a given player by UUID.
     * @param playerUUID The player UUID to apply to.
     */
    public void applyTo(UUID playerUUID){
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            if (!brushToolLoadoutMap.isEmpty()) {
                Utils.clearInventorySlotsFromTo(player, 0, 35);
                for (int i : brushToolLoadoutMap.keySet()){
                    brushToolLoadoutMap.get(i).applyToSpecifiedSlot(player, i);
                }
            }
            player.sendMessage(PlayerMessages.BrushTool_Macro_Applied.getMessageMacroName(macroName));
            if (DKFirstMacro.hasGlobalMask(player)) player.sendMessage(PlayerMessages.Warning_GMask_Is_Enabled.getMessage());
        }
    }

    /**
     * Saves a given BTLoadout to the YML File at a given InventorySlot.
     * @param inventorySlot The inventory slot #.
     * @param BTLoadout The BTLoadout to save.
     */
    private void saveLoadout(int inventorySlot, BTLoadout BTLoadout){
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".Material", BTLoadout.getMaterial());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".Item", BTLoadout.getCustomItem());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".PrimaryCommand", BTLoadout.getPrimaryCommand());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".SecondaryCommand", BTLoadout.getSecondaryCommand());
        saveBrushToolModifiers(inventorySlot, BTLoadout.getModifiers());
    }

    /**
     * Util Request to save the BrushToolModifiers in a system & human-readable format.
     * @param inventorySlot The inventory slot to save as in the YML File.
     * @param brushToolModifiers The list of BrushToolModifiers as commands.
     */
    private void saveBrushToolModifiers(int inventorySlot, List<String> brushToolModifiers){
        if (brushToolModifiers.isEmpty()) return;
        List<String> brushToolModifiersProper = new ArrayList<>();
        for (String modiferRaw : brushToolModifiers){
            BrushToolModifier modifierType = BrushToolCommandLogic.getStandardizedModifier(modiferRaw);
            if (modifierType != null) brushToolModifiersProper.add(modifierType.name() + BrushToolModifier.modifierSplitValue() + modiferRaw);
        }
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".Modifiers", brushToolModifiersProper);
    }

    /**
     * Gathers the loadout's details from the YML File & Returns a BTLoadout object.
     * @param inventorySlot the inventory slot as stored in the YML File.
     * @return BTLoadout
     */
    private BTLoadout getLoadout(int inventorySlot){
        return new BTLoadout(
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".Material"),
                macroYML.getConfig().getItemStack("InventorySlot." + inventorySlot + ".Item"),
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".PrimaryCommand"),
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".SecondaryCommand"),
                BrushToolModifier.removeSplitValuesInBrushToolModifiers(macroYML.getConfig().getStringList("InventorySlot." + inventorySlot + ".Modifiers"))
        );
    }

}
