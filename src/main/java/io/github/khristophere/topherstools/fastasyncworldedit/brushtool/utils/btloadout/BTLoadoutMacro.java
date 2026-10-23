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

import java.util.*;

/**
 * Manages a collection of brush and tool loadouts saved to a specific macro configuration file,
 * allowing players to save, load, and apply entire inventory layouts.
 */
public class BTLoadoutMacro {

    private final YMLHandler yml;
    private final Map<Integer, BTLoadout> brushToolLoadoutMap = new HashMap<>();
    private final String macroName;
    private boolean loadedFromFile = false;

    /**
     * Constructs a new {@code BTLoadoutMacro} (brush/tool loadout macro) for the specified macro file name.
     *
     * @param fileName the name of the macro file
     */
    public BTLoadoutMacro(String fileName) {
        this.yml = new YMLHandler("FAWE-BT-Utils/Macros", fileName, false);
        this.macroName = fileName;
    }

    /**
     * Checks whether the macro configuration file already exists on disk.
     *
     * @return true if the file exists, false otherwise
     */
    public boolean alreadyExists() {
        return yml.doesFileAlreadyExist();
    }

    /**
     * Saves all valid brush/tool loadouts from the player's inventory (slots 0-35) to file asynchronously.
     * <p>
     * Note: Inventory snapshots are executed prior to dispatching file I/O to the async scheduler.
     *
     * @param player the player whose inventory is being saved.
     */
    public void saveToFile(Player player) {
        Map<Integer, String> capturedItems = new HashMap<>();
        for (int i = 0; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                capturedItems.put(i, item.getType().name());
            }
        }

        Scheduler.runSequentialAsync(() -> {
            //Wipe Current YML.
            yml.setup();
            var config = yml.getConfig();
            for (String key : config.getKeys(false)) {
                config.set(key, null);
            }
            // Check for BTLoadout validity and post to file.
            for (Map.Entry<Integer, String> entry : capturedItems.entrySet()) {
                BTLoadout loadout = BTCacheManager.getBrushToolSQL().getBrushToolLoadoutSQL(player.getUniqueId(), entry.getValue());
                if (loadout != null && !loadout.isEmpty()) {
                    saveLoadout(entry.getKey(), loadout);
                }
            }
            yml.saveConfig();
        });
        loadedFromFile = false;
    }

    /**
     * Loads all brush/tool loadouts from the macro configuration file into memory.
     */
    public void loadFromFile() {
        yml.setup();
        brushToolLoadoutMap.clear();
        for (int i = 0; i <= 35; i++) {
            String material = yml.getConfig().getString("InventorySlot." + i + ".Material");
            if (material != null) {
                brushToolLoadoutMap.put(i, getLoadout(i));
            }
        }
        loadedFromFile = true;
    }

    /**
     * Checks whether this macro has already been loaded from the configuration file.
     * Upon new saveToFile call this will change to false.
     *
     * @return true if already loaded from file, false otherwise
     */
    public boolean alreadyLoadedFromFile() {
        return loadedFromFile;
    }

    /**
     * Checks whether the macro map contains any active loadouts.
     *
     * @return true if empty, false otherwise
     */
    public boolean isBrushToolLoadoutEmpty() {
        return brushToolLoadoutMap.isEmpty();
    }

    /**
     * Attempts to delete the macro configuration file from disk.
     *
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteBrushToolLoadoutMacro() {
        return yml.attemptFileDeletion();
    }

    /**
     * Applies all loadouts in this macro to the specified player by UUID across inventory slots 0-35.
     * Reports success or details any specific slots that failed to apply due to missing materials.
     *
     * @param playerUUID the target player's UUID
     */
    public void applyTo(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        if (brushToolLoadoutMap.isEmpty()) {
            player.sendMessage(PlayerMessages.Warning_BrushTool_Macro_Is_Empty.getMessageMacroName(macroName));
            return;
        }

        Utils.clearInventorySlotsFromTo(player, 0, 35);
        List<Integer> failures = new ArrayList<>();

        for (Map.Entry<Integer, BTLoadout> entry : brushToolLoadoutMap.entrySet()) {
            // applyToSpecifiedSlot returns false if it encountered a failure.
            if (!entry.getValue().applyToSpecifiedSlot(player, entry.getKey())) {
                failures.add(entry.getKey());
            }
        }

        if (failures.isEmpty()) {
            player.sendMessage(PlayerMessages.BrushTool_Macro_Applied.getMessageMacroName(macroName));
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < failures.size(); i++) {
                stringBuilder.append(failures.get(i));
                if (i < failures.size() - 1) {
                    stringBuilder.append("&7, &e");
                } else {
                    stringBuilder.append("&7.");
                }
            }
            player.sendMessage(PlayerMessages.BrushTool_Macro_Applied_With_Failures_Missing_Material
                    .getMessageMacroNameFailuresInventorySlots(macroName, failures.size(), stringBuilder.toString()));
        }

        if (DKFirstMacro.hasGlobalMask(player))
            player.sendMessage(PlayerMessages.Warning_GMask_Is_Enabled.getMessage());
    }

    /**
     * Saves a given loadout to the configuration file at the specified inventory slot.
     *
     * @param inventorySlot the inventory slot index
     * @param loadout       the loadout to save
     */
    private void saveLoadout(int inventorySlot, BTLoadout loadout) {
        String path = "InventorySlot." + inventorySlot + ".";
        var config = yml.getConfig();
        config.set(path + "Material", loadout.getMaterial());
        config.set(path + "Item", loadout.getCustomItem());
        config.set(path + "PrimaryCommand", loadout.getPrimaryCommand());
        config.set(path + "SecondaryCommand", loadout.getSecondaryCommand());
        saveBrushToolModifiers(inventorySlot, loadout.getModifiers());
    }

    /**
     * Serializes and saves brush tool modifiers in a system and human-readable format.
     *
     * @param inventorySlot      the inventory slot index
     * @param brushToolModifiers the list of modifier commands
     */
    private void saveBrushToolModifiers(int inventorySlot, List<String> brushToolModifiers) {
        if (brushToolModifiers.isEmpty()) return;

        List<String> brushToolModifiersProper = new ArrayList<>();
        for (String modifierRaw : brushToolModifiers) {
            BrushToolModifier modifierType = BrushToolCommandLogic.getStandardizedModifier(modifierRaw);
            if (modifierType != null)
                brushToolModifiersProper.add(modifierType.name() + BrushToolModifier.modifierSplitValue() + modifierRaw);
        }
        yml.getConfig().set("InventorySlot." + inventorySlot + ".Modifiers", brushToolModifiersProper);
    }

    /**
     * Retrieves loadout details from the configuration file and constructs a {@link BTLoadout} object.
     *
     * @param inventorySlot the inventory slot index
     * @return the constructed {@link BTLoadout}
     */
    private BTLoadout getLoadout(int inventorySlot) {
        var config = yml.getConfig();
        String path = "InventorySlot." + inventorySlot + ".";
        return new BTLoadout(
                config.getString(path + "Material"),
                config.getItemStack(path + "Item"),
                config.getString(path + "PrimaryCommand"),
                config.getString(path + "SecondaryCommand"),
                BrushToolModifier.removeSplitValuesInBrushToolModifiers(config.getStringList(path + "Modifiers"))
        );
    }
}
