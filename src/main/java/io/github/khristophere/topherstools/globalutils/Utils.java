package io.github.khristophere.topherstools.globalutils;

import io.github.khristophere.topherstools.TophersTools;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.stream.IntStream;

public class Utils {

    /**
     * Clears the given players inventory slots in a given range.
     * @param player The Bukkit Player Entity.
     * @param from The inventory slot to start deleting from.
     * @param to The inventory slot to delete to.
     */
    public static void clearInventorySlotsFromTo (Player player, int from, int to){
        IntStream.rangeClosed(from, to).forEach(player.getInventory()::clear);
    }

    /**
     * Provides an ItemStack with 1 item, with the Custom Name. Using '&' Color Codes.
     * @param material Bukkit Material Type
     * @param customName Custom Display Name using Minecraft Color Codes with the '&' symbol.
     * @return The new ItemStack resulting.
     */
    public static ItemStack createNamedItem(Material material, String customName){
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(customName)));
        return item;
    }

    /**
     * Registers the provided class instance with Cloud Command Manager.
     * @param commandClass The class for which we will parse to enable the command's functionalities.
     */
    public static void registerClassWithCommandManager(Object commandClass){
        PaperCommandManager<Source> commandManager = PaperCommandManager.builder(
                PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(TophersTools.getInstance());
        AnnotationParser<Source> annotationParser = new AnnotationParser<>(
                commandManager,
                Source.class
        );
        annotationParser.parse(commandClass);
    }

}
