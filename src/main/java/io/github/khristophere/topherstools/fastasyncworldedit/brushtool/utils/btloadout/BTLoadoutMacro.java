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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout;

import io.github.khristophere.topherstools.data.management.yml.YMLManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.dkmacros.DKFirstMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages a collection of brush / tool loadouts saved to a specific macro YAML file,
 * allowing players to save, load, and apply entire inventory loadouts.
 */
public class BTLoadoutMacro {

    private final YMLManager yml;
    private final Map<Integer, BTLoadout> btLoadoutMap = new HashMap<>();
    private final String macroName;
    private boolean loadedFromFile = false;

    /**
     * Constructs a new BTLoadoutMacro for the specified macro file name.
     *
     * @param fileName the name of the macro file.
     */
    public BTLoadoutMacro(@NotNull String fileName) {
        this.yml = new YMLManager("FAWE-BT-Utils/Macros", fileName, false);
        this.macroName = fileName;
    }

    /**
     * Checks whether the macro YAML file already exists on disk.
     *
     * @return true if the file exists, false otherwise.
     */
    public boolean alreadyExists() {
        return yml.fileExists();
    }

    /**
     * Saves all valid brush / tool loadouts from the player's inventory (slots 0-35) to file asynchronously.
     *
     * @param player the player whose inventory is being saved.
     */
    public void saveToFile(@NotNull Player player) {
        Map<Integer, String> capturedItems = new HashMap<>();
        for (int i = 0; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) capturedItems.put(i, item.getType().name());
        }

        UUID uuid = player.getUniqueId();
        Scheduler.runSequentialAsync(() -> {
            //Wipe Current YML.
            yml.setup();
            yml.getConfig().set("Slot", null);

            // Check for BTLoadout validity and post to file.
            for (Map.Entry<Integer, String> entry : capturedItems.entrySet()) {
                BTLoadout loadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(uuid, entry.getValue());
                if (loadout != null && !loadout.isEmpty()) saveLoadout(entry.getKey(), loadout);
            }
            yml.saveConfig();
        });
        loadedFromFile = false;
    }

    // All calls to this function call from Async Thread

    /**
     * Loads all brush / tool loadouts from the macro YAML file into memory.
     */
    public void loadFromFile() {
        yml.setup();
        btLoadoutMap.clear();
        for (int i = 0; i <= 35; i++) {
            String material = yml.getConfig().getString("Slot." + i + ".Material");
            if (material != null) {
                btLoadoutMap.put(i, getLoadout(i));
            }
        }
        loadedFromFile = true;
    }

    /**
     * Checks whether this macro has already been loaded from the YAML file.
     *
     * @return true if already loaded from file, false otherwise.
     */
    public boolean alreadyLoadedFromFile() {
        return loadedFromFile;
    }

    /**
     * Checks whether the macro map contains any active loadouts.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isBrushToolLoadoutEmpty() {
        return btLoadoutMap.isEmpty();
    }

    /**
     * Attempts to delete the macro YAML file from disk.
     *
     * @return true if successfully deleted, false otherwise.
     */
    public boolean deleteBrushToolLoadoutMacro() {
        return yml.deleteFile();
    }

    /**
     * Applies all loadouts in this macro to the specified player by UUID across inventory slots 0-35.
     *
     * @param playerUUID the target player's UUID.
     */
    public void applyTo(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        if (btLoadoutMap.isEmpty()) {
            player.sendMessage(Messages.Warning_BrushTool_Macro_Is_Empty.getMessageMacroName(macroName));
            return;
        }

        Utils.clearInventorySlots(player, 0, 35);
        List<Integer> failures = new ArrayList<>();

        for (Map.Entry<Integer, BTLoadout> entry : btLoadoutMap.entrySet())
            // applyToSpecifiedSlot returns false if it encountered a failure.
            if (!entry.getValue().applyToSpecifiedSlot(player, entry.getKey()))
                failures.add(entry.getKey());

        if (failures.isEmpty()) {
            player.sendMessage(Messages.BrushTool_Macro_Applied.getMessageMacroName(macroName));
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < failures.size(); i++) {
                stringBuilder.append(failures.get(i));
                if (i < failures.size() - 1)
                    stringBuilder.append("&7, &e");
                else
                    stringBuilder.append("&7.");
            }
            player.sendMessage(Messages.BrushTool_Macro_Applied_With_Failures_Missing_Material
                    .getMessageMacroNameFailuresInventorySlots(macroName, failures.size(), stringBuilder.toString()));
        }

        if (DKFirstMacro.hasGlobalMask(player))
            player.sendMessage(Messages.Warning_GMask_Is_Enabled.getMessage());
    }

    /**
     * Saves a given loadout to the YAML file at the specified inventory slot.
     *
     * @param inventorySlot the inventory slot.
     * @param loadout       the loadout to save.
     */
    private void saveLoadout(int inventorySlot, @NotNull BTLoadout loadout) {
        String path = "Slot." + inventorySlot + ".";
        var config = yml.getConfig();
        config.set(path + "Material", loadout.getMaterial());
        config.set(path + "Item", loadout.getCustomItem());
        config.set(path + "Primary-Command", loadout.getPrimaryCommand());
        config.set(path + "Secondary-Command", loadout.getSecondaryCommand());
        saveBrushToolModifiers(inventorySlot, loadout.getModifiers(BTModifier.Type.PRIMARY), BTModifier.Type.PRIMARY);
        saveBrushToolModifiers(inventorySlot, loadout.getModifiers(BTModifier.Type.SECONDARY), BTModifier.Type.SECONDARY);
        saveBrushToolModifiers(inventorySlot, loadout.getModifiers(BTModifier.Type.GLOBAL), BTModifier.Type.GLOBAL);
    }

    /**
     * Serializes and saves brush / tool modifiers to the YAML in a system and human-readable format.
     *
     * @param inventorySlot      the inventory slot index.
     * @param brushToolModifiers the list of modifier commands.
     * @param modifierType       the modifier type to target.
     */
    private void saveBrushToolModifiers(int inventorySlot, @NotNull List<String> brushToolModifiers, @NotNull BTModifier.Type modifierType) {
        if (brushToolModifiers.isEmpty()) return;

        List<String> brushToolModifiersProper = new ArrayList<>();
        for (String modifierRaw : brushToolModifiers) {
            BTModifier modifier = BTCommandLogic.getStandardizedModifier(modifierRaw);
            if (modifier != null)
                brushToolModifiersProper.add(modifier.name() + BTModifier.MODIFIER_SPLIT_DELIMITER + modifierRaw);
        }
        String path = "Slot." + inventorySlot + ".";
        switch (modifierType) {
            case GLOBAL -> yml.getConfig().set(path + "Global-Modifiers", brushToolModifiersProper);
            case PRIMARY -> yml.getConfig().set(path + "Primary-Modifiers", brushToolModifiersProper);
            case SECONDARY -> yml.getConfig().set(path + "Secondary-Modifiers", brushToolModifiersProper);
        }
    }

    /**
     * Retrieves loadout details from the YAML file and constructs a {@link BTLoadout}.
     *
     * @param inventorySlot the inventory slot.
     * @return the constructed BTLoadout.
     */
    @NotNull
    private BTLoadout getLoadout(int inventorySlot) {
        var config = yml.getConfig();
        String path = "Slot." + inventorySlot + ".";
        return new BTLoadout(
                config.getString(path + "Material"),
                config.getItemStack(path + "Item"),
                config.getString(path + "Primary-Command"),
                config.getString(path + "Secondary-Command"),
                BTModifier.removePrefixesInBTModifiers(config.getStringList(path + "Primary-Modifiers")),
                BTModifier.removePrefixesInBTModifiers(config.getStringList(path + "Secondary-Modifiers")),
                BTModifier.removePrefixesInBTModifiers(config.getStringList(path + "Global-Modifiers"))
        );
    }
}
