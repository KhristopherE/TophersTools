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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Handles high level data requests and logic for brush / tool commands in the SQL
 */
public class BTSQLRequests {

    private final BTSQLExecutor btsqlExecutor;

    /**
     * Constructs a new brush / tool SQL request handler.
     *
     * @param btsqlExecutor the underlying database execution handler.
     */
    public BTSQLRequests(@NotNull BTSQLExecutor btsqlExecutor) {
        this.btsqlExecutor = btsqlExecutor;
    }

    /**
     * Updates a player's username in the database.
     *
     * @param playerUUID the player's unique identifier.
     * @param playerName the player's username.
     */
    public void updatePlayerName(@NotNull UUID playerUUID, @NotNull String playerName) {
        btsqlExecutor.updateUsername(playerUUID, playerName);
    }

    /**
     * Removes a player's secondary brush / tool command associated with a specific material.
     *
     * @param playerUUID   the player's unique identifier.
     * @param materialName the material name.
     */
    public void removeSecondaryBrushToolCommand(@NotNull UUID playerUUID, @NotNull String materialName) {
        btsqlExecutor.removeSecondaryCommand(playerUUID, materialName);
    }

    /**
     * Saves a player's brush / tool command as either primary or secondary based on its identified binding type.
     *
     * @param playerUUID       the player's unique identifier.
     * @param itemStack        the item stack to be associated with the brush / tool.
     * @param brushToolCommand the brush / tool command string to save.
     */
    public void saveBrushToolCommandSelfIdentifyOrdinal(@NotNull UUID playerUUID, @NotNull ItemStack itemStack, @NotNull String brushToolCommand) {
        if (BTCommandLogic.isCommandSecondaryBinding(brushToolCommand)) {
            btsqlExecutor.updateSecondaryCommand(playerUUID, itemStack, brushToolCommand);
        } else {
            btsqlExecutor.updatePrimaryCommand(playerUUID, itemStack, brushToolCommand);
        }
    }

    /**
     * Deletes the brush / tool in its entirety, including primary, secondary, and all modifier commands.
     *
     * @param playerUUID   the player's unique identifier.
     * @param materialName the material name.
     */
    public void deleteBrushTool(@NotNull UUID playerUUID, @NotNull String materialName) {
        btsqlExecutor.deleteBrushToolLoadout(playerUUID, materialName);
    }

    /**
     * Saves a player's brush / tool modifier command associated to a given material.
     *
     * @param playerUUID               the player's unique identifier.
     * @param materialName             the material name.
     * @param BTModifier               the brush / tool modifier type.
     * @param brushToolModifierCommand the new brush / tool modifier command.
     * @param modifierType             the modifier type to target.
     */
    public void saveBrushToolModifier(@NotNull UUID playerUUID,
                                      @NotNull String materialName,
                                      @NotNull BTModifier BTModifier,
                                      @NotNull String brushToolModifierCommand,
                                      @NotNull BTModifier.Type modifierType) {
        var rawModifiers = getAllBrushToolModifiersRaw(playerUUID, materialName, modifierType);
        var prefix = BTModifier.name() + io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier.MODIFIER_SPLIT_DELIMITER;
        rawModifiers.removeIf(modifier -> modifier.startsWith(prefix));
        rawModifiers.add(prefix + brushToolModifierCommand);
        btsqlExecutor.updateModifierCommands(playerUUID, materialName, rawModifiers, modifierType);
    }

    /**
     * Deletes all brush / tool modifier commands associated with a player's given material.
     *
     * @param playerUUID   the player's unique identifier.
     * @param materialName the material name.
     * @param modifierType the modifier type to target.
     */
    public void deleteAllBrushToolModifiers(@NotNull UUID playerUUID, @NotNull String materialName, @NotNull BTModifier.Type modifierType) {
        btsqlExecutor.removeModifierCommands(playerUUID, materialName, modifierType);
    }

    /**
     * Looks up all of a player's brush / tool modifier commands associated with a given material with the prefixes removed.
     *
     * @param playerUUID   the player's unique identifier.
     * @param materialName the material name.
     * @param modifierType the modifier type to target.
     * @return a list of clean modifier commands, or an empty list if none are present
     */
    public @NotNull List<String> getAllBrushToolModifiers(@NotNull UUID playerUUID, @NotNull String materialName, @NotNull BTModifier.Type modifierType) {
        return BTModifier.removePrefixesInBTModifiers(getAllBrushToolModifiersRaw(playerUUID, materialName, modifierType));
    }

    /**
     * Looks up all of a player's raw brush / tool modifier commands associated with a given material preserving prefixes.
     *
     * @param playerUUID   the player's unique identifier.
     * @param materialName the material name.
     * @param modifierType the modifier type to target.
     * @return a list of raw stored modifier strings, or an empty list if none are present
     */
    private @NotNull List<String> getAllBrushToolModifiersRaw(@NotNull UUID playerUUID, @NotNull String materialName, @NotNull BTModifier.Type modifierType) {
        return btsqlExecutor.getModifiersRaw(playerUUID, materialName, modifierType);
    }

}
