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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.dkmacros;

import com.fastasyncworldedit.core.command.tool.brush.CommandBrush;
import com.fastasyncworldedit.core.command.tool.brush.ScatterBrush;
import com.fastasyncworldedit.core.command.tool.brush.ScatterOverlayBrush;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for creating default developer brush / tool macros and setup configurations. Retained for as a tribute as this was the original purpose.
 * It is messy, inefficient and mostly done improperly. I apologize in advance.
 */
public class DKFirstMacro {

    /**
     * Creates a command brush / tool for the given player to be placed in a specific slot.
     *
     * @param player         the requested player.
     * @param brushToolItem  the item the command is bound to.
     * @param inventorySlot  the inventory slot the item will be placed in.
     * @param command        the command to be run upon interaction.
     * @param bindingCommand the command needed to get the resulting in-game brush / tool.
     */
    public static void createCommandBrush(@NotNull Player player, @NotNull ItemStack brushToolItem, int inventorySlot, @NotNull String command, @NotNull String bindingCommand) {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);

        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());
        BrushTool brushTool;

        if (itemType == null) return; // Returns for failure, as the itemType resulting from brushToolItem was null.

        worldEditSession.setTool(itemType.getDefaultState(), null, worldEditPlayer); // Unbind anything existing.
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteBrushTool(player.getUniqueId(), brushToolItem.getType().name());

        brushTool = worldEditSession.getBrushTool(itemType.getDefaultState(), worldEditPlayer, true);

        brushTool.setBrush(new CommandBrush(command, true), "worldedit.brush.command");

        worldEditSession.setTool(itemType.getDefaultState(), brushTool);

        player.getInventory().setItem(inventorySlot, brushToolItem);
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().saveBrushToolCommandSelfIdentifyOrdinal(player.getUniqueId(), brushToolItem, bindingCommand);
        BTCacheManager.addUUIDMaterialBindToCache(player.getUniqueId(), brushToolItem.getType().name());
    }

    /**
     * Provides the requested player the standard wand item.
     *
     * @param player         the requested player.
     * @param itemCustomName the custom name for the item using color codes.
     * @param inventorySlot  the inventory slot to be placed in.
     */
    public static void giveStandardWand(@NotNull Player player, @NotNull String itemCustomName, int inventorySlot) {
        Material wandMaterial = Material.matchMaterial(WorldEdit.getInstance().getConfiguration().wandItem);
        ItemStack wandItem = createNamedItem(wandMaterial, itemCustomName);
        player.getInventory().setItem(inventorySlot, wandItem);
    }

    /**
     * Creates a sphere brush for the given player to be placed in a specific slot.
     *
     * @param player                         the requested player.
     * @param brushToolItem                  the item the brush / tool is bound to.
     * @param inventorySlot                  the inventory slot the item will be placed in.
     * @param brushToolSize                  the size of the brush / tool.
     * @param materialToRespectivePercentage a map of materials linked to percentage values.
     * @param useMask                        whether to use a mask.
     * @param maskedMaterials                list of materials to mask if useMask is true.
     * @param bindingCommand                 the command needed to get the resulting in-game brush / tool.
     */
    public static void createSphereBrush(
            @NotNull Player player,
            @NotNull ItemStack brushToolItem,
            int inventorySlot,
            int brushToolSize,
            @NotNull Map<Material, Double> materialToRespectivePercentage,
            boolean useMask,
            @NotNull List<Material> maskedMaterials,
            @NotNull String bindingCommand) {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        //noinspection DuplicatedCode - 2x in this class.
        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());

        if (itemType == null) return; // Returns for failure, as the itemType resulting from brushToolItem was null.

        worldEditSession.setTool(itemType.getDefaultState(), null, worldEditPlayer); // Unbind anything existing.
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteBrushTool(player.getUniqueId(), brushToolItem.getType().name());

        BrushTool brushTool = worldEditSession.getBrushTool(
                itemType.getDefaultState(),
                worldEditPlayer,
                true);

        // Checking that the percentage total = exactly 100%.
        BigDecimal percentageTotal = BigDecimal.ZERO;
        for (Double eachPercentage : materialToRespectivePercentage.values()) {
            if (eachPercentage != null) percentageTotal = percentageTotal.add(BigDecimal.valueOf(eachPercentage));
        }
        if (percentageTotal.compareTo(new BigDecimal("100.00")) != 0)
            return; // Returns for failure, as the percentageTotal was NOT equal to exactly 100%.

        RandomPattern randomPattern = new RandomPattern();
        materialToRespectivePercentage.forEach((material, percentage) -> {
            BlockType blockType = BukkitAdapter.asBlockType(material);
            if (blockType != null) {
                randomPattern.add(blockType.getDefaultState(), percentage);
            }
        });

        brushTool.setBrush(new SphereBrush(), "worldedit.brush.sphere");
        brushTool.setFill(randomPattern);
        brushTool.setSize(brushToolSize);

        if (useMask && !maskedMaterials.isEmpty()) {
            Set<BlockType> maskBlockTypes = maskedMaterials.stream()
                    .map(BukkitAdapter::asBlockType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            brushTool.setMask(new BlockTypeMask(worldEditPlayer.getExtent(), maskBlockTypes));
        }

        worldEditSession.setTool(itemType.getDefaultState(), brushTool);

        player.getInventory().setItem(inventorySlot, brushToolItem);
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().saveBrushToolCommandSelfIdentifyOrdinal(
                player.getUniqueId(),
                brushToolItem,
                bindingCommand
        );
        BTCacheManager.addUUIDMaterialBindToCache(player.getUniqueId(), brushToolItem.getType().name());
    }

    /**
     * Creates a scattering schematic brush for the given player to be placed in a specific slot.
     *
     * @param player            the requested player.
     * @param brushToolItem     the item the brush / tool is bound to.
     * @param inventorySlot     the inventory slot the item will be placed in.
     * @param schematicLocation the location of the schematic.
     * @param randomlyRotate    whether to randomly rotate the schematic upon placement.
     * @param bindingCommand    the command needed to get the resulting in-game brush / tool.
     */
    public static void createScatterSchematicBrush(
            @NotNull Player player,
            @NotNull ItemStack brushToolItem,
            int inventorySlot,
            @NotNull String schematicLocation,
            boolean randomlyRotate,
            @NotNull String bindingCommand) {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        ParserContext context = new ParserContext();
        context.setActor(worldEditPlayer);
        context.setWorld(worldEditPlayer.getWorld());
        context.setSession(worldEditSession);

        String patternString = "#fullcopy[" + schematicLocation.toLowerCase() + "][" + randomlyRotate + "]";
        Pattern pattern;
        try {
            pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(patternString, context);
        } catch (Exception e) {
            TophersTools.getInstance().getLogger().severe("§cFailed to parse scatter schematic brush :" + patternString);
            TophersTools.getInstance().getLogger().severe("Error: " + e.getMessage());
            return; // Failure likely due to schematicLocation.
        }

        //noinspection DuplicatedCode - 2x in this class.
        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());
        if (itemType == null) return; // Failure due to brushToolItem.

        worldEditSession.setTool(itemType.getDefaultState(), null, worldEditPlayer); // Unbind anything existing.
        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteBrushTool(player.getUniqueId(), brushToolItem.getType().name());

        BrushTool brushTool = worldEditSession.getBrushTool(itemType.getDefaultState(), worldEditPlayer, true);

        ScatterBrush scatterBrush = new ScatterOverlayBrush(1, 1);

        brushTool.setBrush(scatterBrush, "worldedit.brush.scatter");
        brushTool.setFill(pattern);
        brushTool.setSourceMask(new ExistingBlockMask(worldEditPlayer.getWorld()));

        worldEditSession.setTool(itemType.getDefaultState(), brushTool);
        player.getInventory().setItem(inventorySlot, brushToolItem);

        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().saveBrushToolCommandSelfIdentifyOrdinal(player.getUniqueId(), brushToolItem, bindingCommand);
        BTCacheManager.addUUIDMaterialBindToCache(player.getUniqueId(), brushToolItem.getType().name());
    }

    /**
     * Maps a list of materials to a map of respective percentage doubles.
     *
     * @param materials   the list of materials to map.
     * @param percentages the list of percentages to map.
     * @return the resulting material-to-percentage map.
     */
    @NotNull
    public static Map<Material, Double> mapMaterialToPercentage(@NotNull List<Material> materials, @NotNull List<Double> percentages) {
        return IntStream.range(0, Math.min(materials.size(), percentages.size()))
                .boxed().collect(Collectors.toMap(materials::get, percentages::get));
    }

    /**
     * Checks if the player has FAWE global mask enabled.
     *
     * @param player the player we're checking.
     * @return true if global mask is enabled, false otherwise.
     */
    public static boolean hasGlobalMask(@NotNull Player player) {
        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        LocalSession session = sessionManager.get(BukkitAdapter.adapt(player));
        if (session == null) return false;
        return session.getMask() != null;
    }

    /**
     * Provides an instant set of brush / tool macros (reliant on Schematics Saved to the Local Server) to the player.
     * Intended for KhristopherE or TheWither_MAN.
     *
     * @param player the player we're applying this to.
     */
    public static void DKInstantMacro(@NotNull Player player) {
        Utils.clearInventorySlots(player, 0, 35);
        DKFirstMacro.giveStandardWand(player, "&dWand", 0);

        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.STONE_AXE, "&3Outer | Small Tree"),
                9,
                "Trees/Outer/Small",
                true,
                "//br scatter #fullcopy[Trees/Outer/Small][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.COPPER_AXE, "&3Outer | Medium Tree"),
                10,
                "Trees/Outer/Medium",
                true,
                "//br scatter #fullcopy[Trees/Outer/Medium][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.IRON_AXE, "&3Outer | Large Tree"),
                11,
                "Trees/Outer/Large",
                true,
                "//br scatter #fullcopy[Trees/Outer/Large][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.GOLDEN_AXE, "&6Inner | Small Tree"),
                18,
                "Trees/Inner/Small",
                true,
                "//br scatter #fullcopy[Trees/Inner/Small][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.DIAMOND_AXE, "&6Inner | Medium Tree"),
                19,
                "Trees/Inner/Medium",
                true,
                "//br scatter #fullcopy[Trees/Inner/Medium][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.NETHERITE_AXE, "&6Inner | Large Tree"),
                20,
                "Trees/Inner/Large",
                true,
                "//br scatter #fullcopy[Trees/Inner/Large][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.GOLDEN_HOE, "&5Small Bush"),
                27,
                "Bush/Small",
                true,
                "//br scatter #fullcopy[Bush/Small][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.DIAMOND_HOE, "&5Medium Bush"),
                28,
                "Bush/Medium",
                true,
                "//br scatter #fullcopy[Bush/Medium][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.NETHERITE_HOE, "&5Large Bush"),
                29,
                "Bush/Large",
                true,
                "//br scatter #fullcopy[Bush/Large][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.STONE_PICKAXE, "&9Dry | &3Small Rock"),
                15,
                "Rocks/Dry/Small",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Small][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.COPPER_PICKAXE, "&9Dry | &3Medium Rock"),
                16,
                "Rocks/Dry/Medium",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Medium][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.IRON_PICKAXE, "&9Dry | &3Large Rock"),
                17,
                "Rocks/Dry/Large",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Large][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.GOLDEN_PICKAXE, "&3Wet | &3Small Rock"),
                24,
                "Rocks/Wet/Small",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Small][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.DIAMOND_PICKAXE, "&3Wet | &3Medium Rock"),
                25,
                "Rocks/Wet/Medium",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Medium][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.NETHERITE_PICKAXE, "&3Wet | &3Large Rock"),
                26,
                "Rocks/Wet/Large",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Large][true] 1 1 1 -o"
        );
        DKFirstMacro.createScatterSchematicBrush(player,
                createNamedItem(Material.IRON_SWORD, "&2Birch Tree"),
                31,
                "Trees/Birch",
                true,
                "//br scatter #fullcopy[Trees/Birch][true] 1 1 1 -o"
        );
        DKFirstMacro.createSphereBrush(
                player,
                createNamedItem(Material.WOODEN_SHOVEL, "&3Wet &6Dirt"),
                33,
                3,
                DKFirstMacro.mapMaterialToPercentage(
                        List.of(Material.PODZOL, Material.BROWN_CONCRETE_POWDER),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere podzol,brown_concrete_powder 3");
        DKFirstMacro.createSphereBrush(player,
                createNamedItem(Material.COPPER_SHOVEL, "&6Dry &6Dirt"),
                34,
                3,
                DKFirstMacro.mapMaterialToPercentage(
                        List.of(Material.COARSE_DIRT, Material.ROOTED_DIRT),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere coarse_dirt,rooted_dirt 3");
        DKFirstMacro.createSphereBrush(player,
                createNamedItem(Material.DIAMOND_SHOVEL, "&2Grass"),
                35,
                3,
                DKFirstMacro.mapMaterialToPercentage(
                        List.of(Material.GREEN_CONCRETE_POWDER, Material.GRASS_BLOCK),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere green_concrete_powder,grass_block 3");
        DKFirstMacro.createCommandBrush(player,
                createNamedItem(Material.MACE, "&9Undo Brush"),
                22,
                "/undo",
                "//br command 1 undo"
        );
        DKFirstMacro.createCommandBrush(player,
                createNamedItem(Material.TRIDENT, "&9Undo Brush"),
                13,
                "/undo",
                "//br command 1 undo"
        );

        player.sendMessage(Messages.BrushTool_Macro_Issued_DKInstant.getMessage());
        player.sendMessage(Messages.recommendedBrushToolMacroGMaskMessage());
        if (DKFirstMacro.hasGlobalMask(player)) player.sendMessage(Messages.Warning_GMask_Is_Enabled.getMessage());
    }

    /**
     * Creates an item stack with custom display name using color codes.
     *
     * @param material   the material type.
     * @param customName the custom display name using color codes.
     * @return the resulting ItemStack.
     */
    @NotNull
    public static ItemStack createNamedItem(@NotNull Material material, @NotNull String customName) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(customName)));
        return item;
    }

}
