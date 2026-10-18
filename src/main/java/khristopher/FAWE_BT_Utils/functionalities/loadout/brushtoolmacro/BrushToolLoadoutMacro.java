package khristopher.FAWE_BT_Utils.functionalities.loadout.brushtoolmacro;

import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.b.utils.Utils;
import khristopher.FAWE_BT_Utils.c.yml.YMLHandler;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.utils.DKInstantMacro.DKInstantMacro;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolLoadout;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolCommandLogic;
import khristopher.FAWE_BT_Utils.listeners.fawetool.util.BrushToolModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BrushToolLoadoutMacro {

    public final YMLHandler macroYML;
    private final HashMap<Integer, BrushToolLoadout> brushToolLoadoutMap = new HashMap<>();
    private final String macroName;

    public BrushToolLoadoutMacro(String fileName){
        this.macroYML = new YMLHandler("FAWE-BrushTools/BrushToolMacros", fileName, false);
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
        Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
            macroYML.setup();
            for (String key : macroYML.getConfig().getKeys(false)) macroYML.getConfig().set(key, null);
            for (int i = 0; i <= 35; i++){
                ItemStack item = onlinePlayer.getInventory().getItem(i);
                if (item != null){
                    BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(onlinePlayer.getUniqueId(), item.getType().name());
                    if (loadout != null && !loadout.isBrushToolLoadoutEmpty()) {
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
            if (DKInstantMacro.hasGlobalMask(player)) player.sendMessage(PlayerMessages.Warning_GMask_Is_Enabled.getMessage());
        }
    }

    /**
     * Saves a given BrushToolLoadout to the YML File at a given InventorySlot.
     * @param inventorySlot The inventory slot #.
     * @param brushToolLoadout The BrushToolLoadout to save.
     */
    private void saveLoadout(int inventorySlot, BrushToolLoadout brushToolLoadout){
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".Material", brushToolLoadout.getMaterial());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".Item", brushToolLoadout.getCustomItem());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".PrimaryCommand", brushToolLoadout.getPrimaryCommand());
        macroYML.getConfig().set("InventorySlot." + inventorySlot + ".SecondaryCommand", brushToolLoadout.getSecondaryCommand());
        saveBrushToolModifiers(inventorySlot, brushToolLoadout.getModifiers());
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
     * Gathers the loadout's details from the YML File & Returns a BrushToolLoadout object.
     * @param inventorySlot the inventory slot as stored in the YML File.
     * @return BrushToolLoadout
     */
    private BrushToolLoadout getLoadout(int inventorySlot){
        return new BrushToolLoadout(
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".Material"),
                macroYML.getConfig().getItemStack("InventorySlot." + inventorySlot + ".Item"),
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".PrimaryCommand"),
                macroYML.getConfig().getString("InventorySlot." + inventorySlot + ".SecondaryCommand"),
                BrushToolModifier.removeSplitValuesInBrushToolModifiers(macroYML.getConfig().getStringList("InventorySlot." + inventorySlot + ".Modifiers"))
        );
    }

}
