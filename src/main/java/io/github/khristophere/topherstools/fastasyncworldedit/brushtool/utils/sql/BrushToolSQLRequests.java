package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BrushToolManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BrushToolModifier;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BrushToolSQLRequests {

    private final BrushToolSQL brushToolSQL;

    public BrushToolSQLRequests(BrushToolSQL brushToolSQL){
        this.brushToolSQL = brushToolSQL;
    }

    /**
     * Saves the Player's current name to the YML File.
     * @param playerUUID The requested Player's UUID.
     * @param playerName The requested Player's Name.
     */
    public void updatePlayerName(UUID playerUUID, String playerName){
        brushToolSQL.updateUsernameSQL(playerUUID, playerName);
        BrushToolManager.addPlayerToAllPlayersInMemory(playerName, playerUUID);
    }

    /**
     * Removes the requested Player's Secondary Brush/Tool Command associated to a given Material.
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's Name.
     */
    public void removeSecondaryBrushToolCommand(UUID playerUUID, String materialName){
        brushToolSQL.removeSecondaryCommandSQL(playerUUID, materialName);
    }

    /**
     * Saves the requested Player's to be identified Ordinal Brush/Tool Command associated to a given Material.
     * @param playerUUID The requested Player's UUID.
     * @param itemStack The requested ItemStack.
     * @param brushToolCommand The new to be identified Ordinal Brush/Tool Command.
     */
    public void saveBrushToolCommandSelfIdentifyOrdinal(UUID playerUUID, ItemStack itemStack, String brushToolCommand){
        if (BrushToolCommandLogic.isCommandSecondaryBinding(brushToolCommand)) {
            brushToolSQL.updateSecondaryCommandSQL(playerUUID, itemStack, brushToolCommand);
        } else {
            brushToolSQL.updatePrimaryCommandSQL(playerUUID, itemStack, brushToolCommand);
        }
    }

    /**
     * Deletes the Brush/Tool in its entirety, including Primary, Secondary & Modifier Commands from the YML File.
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's name.
     */
    public void deleteBrushTool(UUID playerUUID, String materialName){
        brushToolSQL.deleteBrushToolLoadoutSQL(playerUUID, materialName);
    }

    /**
     * Saves the requested Player's BrushToolModifier command associated to a given Material.
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's name.
     * @param brushToolModifier The requested BrushToolModifier.
     * @param toolModifierCommand The new BrushToolModifier Command.
     */
    public void saveBrushToolModifier(UUID playerUUID, String materialName, BrushToolModifier brushToolModifier, String toolModifierCommand){
        if (brushToolModifier == null) return;
        List<String> preExistingToolModifiers = getAllBrushToolModifiersRaw(playerUUID, materialName);
        List<String> existingModifierToRemove = new ArrayList<>();
        preExistingToolModifiers
                .forEach(modifierInReview -> {
                    if (modifierInReview.startsWith(brushToolModifier.name() + BrushToolModifier.modifierSplitValue()))
                        existingModifierToRemove.add(modifierInReview);
                });
        existingModifierToRemove.forEach(preExistingToolModifiers::remove);
        preExistingToolModifiers.add(brushToolModifier.name() + BrushToolModifier.modifierSplitValue() + toolModifierCommand);
        brushToolSQL.updateModifierCommandsSQL(playerUUID, materialName, preExistingToolModifiers);
    }

    /**
     * Deletes all of the BrushToolModifier Commands associated to this Player's given Material in entirety from the YML File.
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's name.
     */
    public void deleteAllBrushToolModifiers(UUID playerUUID, String materialName){
        brushToolSQL.removeModifierCommandsSQL(playerUUID, materialName);
    }

    /**
     * Class Public Version of getAllBrushToolModifiersRaw.
     * Looks up all of the requested Player's BrushToolModifier Commands associated to a given Material. (Removes the split values for easy reading / processing of each command)
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's name.
     * @return All of the requested Player's BrushToolModifier Commands associated to the given Material Name, if any present, else Null. (Removes the split values for easy reading / processing of each command)
     */
    public List<String> getAllBrushToolModifiers(UUID playerUUID, String materialName){
        return BrushToolModifier.removeSplitValuesInBrushToolModifiers(getAllBrushToolModifiersRaw(playerUUID, materialName));
    }

    /**
     * Class Private Version of getAllBrushToolModifiers.
     * Looks up all of the requested Player's BrushToolModifier Commands associated to a given Material. (Does NOT remove the split values)
     * @param playerUUID The requested Player's UUID.
     * @param materialName The requested Material's name.
     * @return All of the requested Player's BrushToolModifier Commands associated to the given Material Name, if any present, else Null. (Does NOT remove the split values)
     */
    private List<String> getAllBrushToolModifiersRaw(UUID playerUUID, String materialName){
        return brushToolSQL.getModifiersRawSQL(playerUUID, materialName);
    }

}
