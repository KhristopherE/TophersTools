package khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.utils.DKInstantMacro;

import com.fastasyncworldedit.core.command.tool.brush.BrushSettings;
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
import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.b.utils.Utils;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DKInstantMacro {

    /**
     * Creates a Command Brush/Tool for the given player to be placed in a specific slot.
     * @param player The requested player.
     * @param brushToolItem The item the command is bound to.
     * @param inventorySlot The inventory slot the item will be placed in.
     * @param command The command to be run upon interaction.
     * @param bindingCommand This is what command would need to be run by a player to get the same in game resulting command brush/tool. It's saved to the PlayerLastBrush.YML.
     * @return True if Successful, False if Failure, failure resulting from brushToolItem being Null.
     */
    @SuppressWarnings("UnusedReturnValue") // Suppress warning that the boolean is not actually used anywhere.
    public static boolean createCommandBrush(Player player, ItemStack brushToolItem, int inventorySlot, String command, String bindingCommand) {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);

        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());
        BrushTool brushTool;

        if (itemType == null) return false; // Returns false for failure, as the itemType resulting from brushToolItem was null.

        brushTool = worldEditSession.getBrushTool(itemType.getDefaultState(), worldEditPlayer, true);

        brushTool.setBrush(new CommandBrush(command, true), "worldedit.brush.command");

        brushTool.setSecondary(new BrushSettings());
        BrushToolManager.brushToolSQL.brushToolSQLRequests.removeSecondaryBrushToolCommand(player.getUniqueId(), brushToolItem.getType().name());

        worldEditSession.setTool(itemType.getDefaultState(), brushTool);

        player.getInventory().setItem(inventorySlot, brushToolItem);
        BrushToolManager.brushToolSQL.brushToolSQLRequests.saveBrushToolCommandSelfIdentifyOrdinal(player.getUniqueId(), brushToolItem, bindingCommand);
        BrushToolManager.addToUUIDsToListMaterialsBoundInMemory(player.getUniqueId(), brushToolItem.getType().name());
        return true; // Returns true for successful run.
    }

    /**
     * Provides the requested player the standard wand item for the server.
     * @param player The requested player.
     * @param itemCustomName The custom name for the item using '&' color codes.
     * @param inventorySlot The inventory slot to be placed in.
     */
    public static void giveStandardWand(Player player, String itemCustomName, int inventorySlot){
        Material wandMaterial = Material.matchMaterial(WorldEdit.getInstance().getConfiguration().wandItem);
        ItemStack wandItem = Utils.createNamedItem(wandMaterial, itemCustomName);
        player.getInventory().setItem(inventorySlot, wandItem);
    }

    /**
     * Creates a Sphere Brush for the given player to be placed in a specific slot.
     * @param player The requested player.
     * @param brushToolItem The item the brush/tool is bound to.
     * @param inventorySlot The inventory slot the item will be placed in.
     * @param brushToolSize The size of the brush/tool.
     * @param materialToRespectivePercentage a Map of Materials linked to a Double indicating their given % value, the total of all doubles should be 100.00
     * @param useMask Boolean if we're using a mask.
     * @param maskedMaterials If useMask is true, we should provide a list of materials to mask, meaning the brush/tool will only interact with those materials.
     * @param bindingCommand This is what command would need to be run by a player to get the same in game resulting command brush/tool. It's saved to the PlayerLastBrush.YML.
     * @return True if Successful, False if Failure, Failure resulting from brushToolItem being Null,
     * & or the doubles mapped to materials in materialToRespectivePercentage did NOT equal exactly 100.00%
     */
    @SuppressWarnings("UnusedReturnValue") // Suppress warning that the boolean is not actually used anywhere.
    public static boolean createSphereBrush(
            Player player,
            ItemStack brushToolItem,
            int inventorySlot,
            int brushToolSize,
            Map<Material, Double> materialToRespectivePercentage,
            boolean useMask,
            List<Material> maskedMaterials,
            String bindingCommand)
    {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());

        if (itemType == null) return false; // Returns false for failure, as the itemType resulting from brushToolItem was null.

        BrushTool brushTool = worldEditSession.getBrushTool(
                itemType.getDefaultState(),
                worldEditPlayer,
                true);

        // Checking that the percentage total = exactly 100%.
        BigDecimal percentageTotal = BigDecimal.ZERO;
        for (Double eachPercentage : materialToRespectivePercentage.values()){
            if (eachPercentage != null) percentageTotal = percentageTotal.add(BigDecimal.valueOf(eachPercentage));
        }
        if (percentageTotal.compareTo(new BigDecimal("100.00")) != 0) return false; // Returns false for failure, as the percentageTotal was NOT equal to exactly 100%.

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

        if (useMask && maskedMaterials != null && !maskedMaterials.isEmpty()) {
            Set<BlockType> maskBlockTypes = maskedMaterials.stream()
                    .map(BukkitAdapter::asBlockType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            brushTool.setMask(new BlockTypeMask(worldEditPlayer.getExtent(), maskBlockTypes));
        }

        brushTool.setSecondary(new BrushSettings());
        BrushToolManager.brushToolSQL.brushToolSQLRequests.removeSecondaryBrushToolCommand(player.getUniqueId(), brushToolItem.getType().name());

        worldEditSession.setTool(itemType.getDefaultState(), brushTool);

        player.getInventory().setItem(inventorySlot, brushToolItem);
        BrushToolManager.brushToolSQL.brushToolSQLRequests.saveBrushToolCommandSelfIdentifyOrdinal(
                player.getUniqueId(),
                brushToolItem,
                bindingCommand
        );
        BrushToolManager.addToUUIDsToListMaterialsBoundInMemory(player.getUniqueId(), brushToolItem.getType().name());
        return true; // Returns true for successful run.
    }

    /**
     * Creates a Scattering Schematic Brush for the given player to be placed in a specific slot.
     * @param player The requested player.
     * @param brushToolItem The item the brush/tool is bound to.
     * @param inventorySlot The inventory slot the item will be placed in.
     * @param schematicLocation The location of the schematic stored within 'plugins\FastAsyncWorldEdit\schematics\' does NOT start with '\' or '/'.
     * @param randomlyRotate If we will randomly rotate the schematic upon placement.
     * @param bindingCommand This is what command would need to be run by a player to get the same in game resulting command brush/tool. It's saved to the PlayerLastBrush.YML.
     * @return True if Successful, False if Failure, Failure resulting from brushToolItem being Null,
     * & or schematicLocation being invalid or unparsable.
     */
    @SuppressWarnings("UnusedReturnValue") // Suppress warning that the boolean is not actually used anywhere.
    public static boolean createScatterSchematicBrush(
            Player player,
            ItemStack brushToolItem,
            int inventorySlot,
            String schematicLocation,
            boolean randomlyRotate,
            String bindingCommand)
    {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        ParserContext context = new ParserContext();
        context.setActor(worldEditPlayer);
        context.setWorld(worldEditPlayer.getWorld());
        context.setSession(localSession);

        String patternString = "#fullcopy["+ schematicLocation + "]["+randomlyRotate+"]";
        Pattern pattern;
        try {
            pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(patternString, context);
        } catch (Exception e) {
            TophersTools.getInstance().getLogger().severe("§cFailed to parse scatter schematic brush :" + patternString);
            TophersTools.getInstance().getLogger().severe("Error: " + e.getMessage());
            return false; // Failure likely due to schematicLocation.
        }

        ItemType itemType = BukkitAdapter.asItemType(brushToolItem.getType());
        if (itemType == null) return false; // Failure due to brushToolItem.
        BrushTool brushTool = localSession.getBrushTool(itemType.getDefaultState(), worldEditPlayer, true);

        ScatterBrush scatterBrush = new ScatterOverlayBrush(1, 1);

        brushTool.setBrush(scatterBrush, "worldedit.brush.scatter");
        brushTool.setFill(pattern);
        brushTool.setSourceMask(new ExistingBlockMask(worldEditPlayer.getWorld()));

        brushTool.setSecondary(new BrushSettings());
        BrushToolManager.brushToolSQL.brushToolSQLRequests.removeSecondaryBrushToolCommand(player.getUniqueId(), brushToolItem.getType().name());

        localSession.setTool(itemType.getDefaultState(), brushTool);
        player.getInventory().setItem(inventorySlot, brushToolItem);

        BrushToolManager.brushToolSQL.brushToolSQLRequests.saveBrushToolCommandSelfIdentifyOrdinal(player.getUniqueId(), brushToolItem, bindingCommand);
        BrushToolManager.addToUUIDsToListMaterialsBoundInMemory(player.getUniqueId(), brushToolItem.getType().name());
        return true; // Returns true for successful run.
    }

    /**
     * Utility function to map a list of Materials to a map of Doubles (being the percentage).
     * @param materials List of materials to map to the respective percentage.
     * @param percentages List of percentages to map to the respective material.
     * @return The Map :)
     */
    public static Map<Material, Double> mapMaterialToPercentage(List<Material> materials, List<Double> percentages){
        return IntStream.range(0, Math.min(materials.size(), percentages.size()))
                .boxed().collect(Collectors.toMap(materials::get, percentages::get));
    }

    /**
     * Checks if the player has FAWE Global Mask enabled.
     * @param player The player we're checking.
     * @return boolean.
     */
    public static boolean hasGlobalMask(Player player){
        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        LocalSession session = sessionManager.get(BukkitAdapter.adapt(player));
        if (session == null) return false;
        return session.getMask() != null;
    }

    /**
     * Intended for KhristopherE or TheWither_MAN to provide an instant set of Brush/Tools mostly reliant on Schematic Files Saved to the Local Server.
     * @param player The Player we're applying this to.
     */
    public static void DKInstantMacro(Player player){
        Utils.clearInventorySlotsFromTo(player, 0, 35);
        DKInstantMacro.giveStandardWand(player, "&dWand", 0);

        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.STONE_AXE, "&3Outer | Small Tree"),
                9,
                "Trees/Outer/Small",
                true,
                "//br scatter #fullcopy[Trees/Outer/Small][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.COPPER_AXE, "&3Outer | Medium Tree"),
                10,
                "Trees/Outer/Medium",
                true,
                "//br scatter #fullcopy[Trees/Outer/Medium][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.IRON_AXE, "&3Outer | Large Tree"),
                11,
                "Trees/Outer/Large",
                true,
                "//br scatter #fullcopy[Trees/Outer/Large][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.GOLDEN_AXE, "&6Inner | Small Tree"),
                18,
                "Trees/Inner/Small",
                true,
                "//br scatter #fullcopy[Trees/Inner/Small][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.DIAMOND_AXE, "&6Inner | Medium Tree"),
                19,
                "Trees/Inner/Medium",
                true,
                "//br scatter #fullcopy[Trees/Inner/Medium][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.NETHERITE_AXE, "&6Inner | Large Tree"),
                20,
                "Trees/Inner/Large",
                true,
                "//br scatter #fullcopy[Trees/Inner/Large][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.GOLDEN_HOE, "&5Small Bush"),
                27,
                "Bush/Small",
                true,
                "//br scatter #fullcopy[Bush/Small][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.DIAMOND_HOE, "&5Medium Bush"),
                28,
                "Bush/Medium",
                true,
                "//br scatter #fullcopy[Bush/Medium][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.NETHERITE_HOE, "&5Large Bush"),
                29,
                "Bush/Large",
                true,
                "//br scatter #fullcopy[Bush/Large][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.STONE_PICKAXE, "&9Dry | &3Small Rock"),
                15,
                "Rocks/Dry/Small",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Small][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.COPPER_PICKAXE, "&9Dry | &3Medium Rock"),
                16,
                "Rocks/Dry/Medium",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Medium][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.IRON_PICKAXE, "&9Dry | &3Large Rock"),
                17,
                "Rocks/Dry/Large",
                true,
                "//br scatter #fullcopy[Rocks/Dry/Large][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.GOLDEN_PICKAXE, "&3Wet | &3Small Rock"),
                24,
                "Rocks/Wet/Small",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Small][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.DIAMOND_PICKAXE, "&3Wet | &3Medium Rock"),
                25,
                "Rocks/Wet/Medium",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Medium][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.NETHERITE_PICKAXE, "&3Wet | &3Large Rock"),
                26,
                "Rocks/Wet/Large",
                true,
                "//br scatter #fullcopy[Rocks/Wet/Large][true] 1 1 1 -o"
        );
        DKInstantMacro.createScatterSchematicBrush(player,
                Utils.createNamedItem(Material.IRON_SWORD, "&2Birch Tree"),
                31,
                "Trees/Birch",
                true,
                "//br scatter #fullcopy[Trees/Birch][true] 1 1 1 -o"
        );
        DKInstantMacro.createSphereBrush(
                player,
                Utils.createNamedItem(Material.WOODEN_SHOVEL, "&3Wet &6Dirt"),
                33,
                3,
                DKInstantMacro.mapMaterialToPercentage(
                        List.of(Material.PODZOL, Material.BROWN_CONCRETE_POWDER),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere podzol,brown_concrete_powder 3");
        DKInstantMacro.createSphereBrush(player,
                Utils.createNamedItem(Material.COPPER_SHOVEL, "&6Dry &6Dirt"),
                34,
                3,
                DKInstantMacro.mapMaterialToPercentage(
                        List.of(Material.COARSE_DIRT, Material.ROOTED_DIRT),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere coarse_dirt,rooted_dirt 3");
        DKInstantMacro.createSphereBrush(player,
                Utils.createNamedItem(Material.DIAMOND_SHOVEL, "&2Grass"),
                35,
                3,
                DKInstantMacro.mapMaterialToPercentage(
                        List.of(Material.GREEN_CONCRETE_POWDER, Material.GRASS_BLOCK),
                        List.of(50.0, 50.0)),
                true,
                List.of(Material.GRASS_BLOCK, Material.GREEN_CONCRETE_POWDER),
                "//brush sphere green_concrete_powder,grass_block 3");
        DKInstantMacro.createCommandBrush(player,
                Utils.createNamedItem(Material.MACE, "&9Undo Brush"),
                22,
                "/undo",
                "//br command 1 undo"
        );
        DKInstantMacro.createCommandBrush(player,
                Utils.createNamedItem(Material.TRIDENT, "&9Undo Brush"),
                13,
                "/undo",
                "//br command 1 undo"
        );

        player.sendMessage(PlayerMessages.BrushTool_Macro_Issued_DKInstant.getMessage());
        player.sendMessage(PlayerMessages.recommendedBrushToolMacroGMaskMessage());
        if (DKInstantMacro.hasGlobalMask(player)) player.sendMessage(PlayerMessages.Warning_GMask_Is_Enabled.getMessage());
    }

}
