package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout;

import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql.BrushToolSQL;
import io.github.khristophere.topherstools.scheduler.Scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BTCacheManager {

    private static BrushToolSQL brushToolSQL;

    private static final Map<UUID, List<String>> UUID_TO_MATERIALS_MAP = new ConcurrentHashMap<>();
    private static final Map<String, UUID> USERNAME_TO_UUID_MAP = new ConcurrentHashMap<>();
    private static final List<String> AVAILABLE_MACROS = new CopyOnWriteArrayList<>();

    public BTCacheManager() {
        brushToolSQL = new BrushToolSQL("FAWE-BT-Utils/SQLData", "PlayerBindings");
        Scheduler.runSequentialAsync(() -> {
            brushToolSQL.initializeTable();
            Map<UUID, List<String>> fetchedMaterialsMap = brushToolSQL.getUUIDMaterialMapSQL();
            Map<String, UUID> fetchedPlayersMap = brushToolSQL.getUsernameUUIDMapSQL();
            List<String> fetchedMacros = collectBTLoadoutMacrosInFolder();
            UUID_TO_MATERIALS_MAP.clear();
            if (fetchedMaterialsMap != null)
                UUID_TO_MATERIALS_MAP.putAll(fetchedMaterialsMap);
            USERNAME_TO_UUID_MAP.clear();
            if (fetchedPlayersMap != null)
                USERNAME_TO_UUID_MAP.putAll(fetchedPlayersMap);
            AVAILABLE_MACROS.clear();
            if (fetchedMacros != null)
                AVAILABLE_MACROS.addAll(fetchedMacros);
        });
    }

    /**
     * Gets the active SQL handler instance.
     *
     * @return the {@link BrushToolSQL} instance
     */
    public static BrushToolSQL getBrushToolSQL() {
        return brushToolSQL;
    }

    /**
     * Gets an unmodifiable view of all available brush tool macros in memory.
     *
     * @return a list of macro names
     */
    public static List<String> getBTLoadoutMacrosInCache() {
        return Collections.unmodifiableList(AVAILABLE_MACROS);
    }

    /**
     * Gets an unmodifiable view of the mapping between player UUIDs and their bound materials.
     *
     * @return an unmodifiable map of player UUIDs to list of materials
     */
    public static Map<UUID, List<String>> getUUIDMaterialBindsInCache() {
        return Collections.unmodifiableMap(UUID_TO_MATERIALS_MAP);
    }

    /**
     * Wipes a player's bound materials list, resetting it to a brand-new, empty ArrayList.
     *
     * @param uuid the player's unique identifier
     */
    public static void emptyUUIDMaterialBindsInCache(UUID uuid) {
        if (uuid == null) return;
        UUID_TO_MATERIALS_MAP.put(uuid, new ArrayList<>());
    }

    /**
     * Gets an unmodifiable view of the mapping between player usernames and their UUIDs.
     *
     * @return an unmodifiable map of usernames to UUIDs
     */
    public static Map<String, UUID> getUsernameUUIDMapInCache() {
        return Collections.unmodifiableMap(USERNAME_TO_UUID_MAP);
    }

    /**
     * Adds a player's name and UUID to active memory for tab-completion support.
     *
     * @param playerName player name the player's username
     * @param uuid       the player's unique identifier
     */
    public static void addPlayerToCache(String playerName, UUID uuid) {
        if (playerName != null && uuid != null) {
            USERNAME_TO_UUID_MAP.put(playerName, uuid);
        }
    }

    /**
     * Adds a newly created macro name to the active in-memory cache.
     *
     * @param macroName the name of the macro
     */
    public static void addBTLoadoutMacroToCache(String macroName) {
        if (macroName != null && !AVAILABLE_MACROS.contains(macroName)) {
            AVAILABLE_MACROS.add(macroName);
        }
    }

    /**
     * Removes a deleted macro name from the active in-memory cache.
     *
     * @param macroName the name of the macro
     */
    public static void removeBTLoadoutMacroFromCache(String macroName) {
        AVAILABLE_MACROS.remove(macroName);
    }

    /**
     * Adds a bound material to a player's cached profile for tab-completion in commands like {@code /GetBrush}.
     *
     * @param uuid     the player's unique identifier
     * @param material the material name to add
     */
    public static void addUUIDMaterialBindToCache(UUID uuid, String material) {
        if (uuid == null || material == null) return;
        UUID_TO_MATERIALS_MAP.compute(uuid, (key, existingList) -> {
            List<String> materialsBound = existingList != null ? existingList : new ArrayList<>();
            if (!materialsBound.contains(material)) {
                materialsBound.add(material);
            }
            return materialsBound;
        });
    }

    /**
     * Removes a bound material from a player's cached profile.
     *
     * @param uuid     the player's unique identifier
     * @param material the material name to remove
     */
    public static void removeUUIDMaterialBindFromCache(UUID uuid, String material) {
        if (uuid == null || material == null) return;
        UUID_TO_MATERIALS_MAP.computeIfPresent(uuid, (key, materialsBound) -> {
            materialsBound.remove(material);
            return materialsBound.isEmpty() ? null : materialsBound;
        });
    }

    /**
     * Scans the macro directory on disk to collect all available brush tool macro file names.
     *
     * @return a list of macro names without file extensions
     */
    private List<String> collectBTLoadoutMacrosInFolder() {
        Path macroFolder = TophersTools.getInstance().getDataFolder().toPath().resolve("FAWE-BT-Utils/Macros");
        if (!Files.exists(macroFolder) || !Files.isDirectory(macroFolder))
            return new ArrayList<>();
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

}
