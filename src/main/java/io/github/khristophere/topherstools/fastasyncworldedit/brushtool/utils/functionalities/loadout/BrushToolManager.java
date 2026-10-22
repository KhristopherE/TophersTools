package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout;

import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.SQL.BrushToolSQL;
import io.github.khristophere.topherstools.scheduler.Scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrushToolManager {

    public static BrushToolSQL brushToolSQL;
    public static Map<UUID, List<String>> allUUIDsToListMaterialsBound = new HashMap<>();
    public static Map<String, UUID> allPlayersInSQL = new HashMap<>();
    public static List<String> brushToolLoadoutMacrosAvailable = new ArrayList<>();

    public BrushToolManager(){
        //Instantiates the BrushToolSQL, and makes it public static accessible.
        brushToolSQL = new BrushToolSQL("FAWE-BT-Utils/SQLData", "PlayerBindings");
        //Makes an Async Call to Initialize the SQL Table, and get All Players in The SQL.
        Scheduler.runSequentialAsync( () -> {
            brushToolSQL.initializeTable();
            Map<UUID, List<String>> allUUIDsToListMaterialsBoundACM = brushToolSQL.getUUIDMaterialMapSQL();
            Map<String, UUID> allPlayersInSQLACM = brushToolSQL.getUsernameUUIDMapSQL();
            // Schedules it to run on the main thread to avoid concurrent modification.
            List<String> brushToolMacrosAvailableACM = collectAllBrushToolLoadoutMacros();
            Scheduler.runOnPrimaryThread(( () -> {
                allUUIDsToListMaterialsBound = allUUIDsToListMaterialsBoundACM;
                allPlayersInSQL = allPlayersInSQLACM;
                brushToolLoadoutMacrosAvailable = brushToolMacrosAvailableACM;
            }));
        });
    }

    /**
     * Adds the Player to Active Memory for Tab Complete Suggestions.
     * @param playerName Player Name.
     * @param uuid UUID.
     */
    public static void addPlayerToAllPlayersInMemory(String playerName, UUID uuid){
        allPlayersInSQL.put(playerName, uuid);
    }

    /**
     * This function will add the Material Bound to the allUUIDsToListMaterialsBound Map, this Maps purpose is
     * for the /GetBrush command to show a tab complete function for all the given players currently bound materials.
     * @param uuid Player UUID.
     * @param material The Material to Add.
     */
    public static void addToUUIDsToListMaterialsBoundInMemory(UUID uuid, String material){
        List<String> materialsBound = Optional.ofNullable(allUUIDsToListMaterialsBound.get(uuid)).orElse(new ArrayList<>());
        if (!materialsBound.contains(material)) {
            materialsBound.add(material);
            allUUIDsToListMaterialsBound.put(uuid, materialsBound);
        }
    }

    /**
     * This function will remove the Material Bound from the allUUIDsToListMaterialsBound Map, this Maps purpose is
     * for the /GetBrush command to show a tab complete function for all the given players currently bound materials.
     * @param uuid Player UUID.
     * @param material The Material to Remove.
     */
    public static void removeFromUUIDsToListMaterialsBoundInMemory(UUID uuid, String material){
        List<String> materialsBound = Optional.ofNullable(allUUIDsToListMaterialsBound.get(uuid)).orElse(new ArrayList<>());
        if (materialsBound.contains(material)) {
            materialsBound.remove(material);
            allUUIDsToListMaterialsBound.put(uuid, materialsBound);
        }
    }

    private List<String> collectAllBrushToolLoadoutMacros(){
        Path macroFolder = TophersTools.getInstance().getDataFolder().toPath().resolve("FAWE-BrushTools/BrushToolMacros");
        if (!Files.exists(macroFolder) || !Files.isDirectory(macroFolder)) {
            return new ArrayList<>();
        }
        try (Stream<Path> pathStream = Files.list(macroFolder)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(".yml"))
                    .map(name -> name.substring(0, name.length() - 4))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to scan {} for all Brush Tool Macros", macroFolder, e);
            return new ArrayList<>();
        }
    }

    public static void addBrushToolLoadoutMacroToMem(String macroName){
        if (!brushToolLoadoutMacrosAvailable.contains(macroName)) brushToolLoadoutMacrosAvailable.add(macroName);
    }

    public static void removeBrushToolLoadoutMacroFromMem(String macroName){
        brushToolLoadoutMacrosAvailable.remove(macroName);
    }

}
