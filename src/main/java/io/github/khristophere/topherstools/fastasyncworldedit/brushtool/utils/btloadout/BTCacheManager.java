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

import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql.BTSQLExecutor;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages in-memory caching for player bindings, usernames, and brush / tool macros.
 */
public class BTCacheManager {

    private static final BTSQLExecutor BTSQLExecutor = new BTSQLExecutor("FAWE-BT-Utils/Local-SQLData", "Brush-Tool-Bindings");

    private static final Map<UUID, List<String>> UUID_TO_MATERIALS_MAP = new ConcurrentHashMap<>();
    private static final Map<String, UUID> USERNAME_TO_UUID_MAP = new ConcurrentHashMap<>();
    private static final List<String> AVAILABLE_MACROS = new CopyOnWriteArrayList<>();
    // Thread-safe LinkedHashMap configured as an LRU (Least Recently Used) cache (accessOrder = true)
    private static Map<String, BTLoadoutMacro> macroCache = null;

    /**
     * Constructs a new BTCacheManager instance and initializes data caches asynchronously.
     */
    public BTCacheManager() {
        refreshMacroCache();
        Scheduler.runSequentialAsync(() -> {
            BTSQLExecutor.initializeTable();
            UUID_TO_MATERIALS_MAP.clear();
            UUID_TO_MATERIALS_MAP.putAll(BTSQLExecutor.getUUIDMaterialMap());
            USERNAME_TO_UUID_MAP.clear();
            USERNAME_TO_UUID_MAP.putAll(BTSQLExecutor.getUsernameUUIDMap());
            AVAILABLE_MACROS.clear();
            AVAILABLE_MACROS.addAll(collectBTLoadoutMacrosInFolder());
        });
    }

    /**
     * Gets the active SQL handler instance.
     *
     * @return the BTSQLExecutor instance.
     */
    @NotNull
    public static BTSQLExecutor getBTSQLExecutor() {
        return BTSQLExecutor;
    }

    /**
     * Gets an unmodifiable view of all available brush / tool macros in memory.
     *
     * @return a list of macro names.
     */
    @NotNull
    public static List<String> getBTLoadoutMacroNamesInCache() {
        return Collections.unmodifiableList(AVAILABLE_MACROS);
    }

    /**
     * Gets an unmodifiable view of the mapping between player UUIDs and their bound materials.
     *
     * @return an unmodifiable map of player UUIDs to list of materials.
     */
    @NotNull
    public static Map<UUID, List<String>> getUUIDMaterialBindsInCache() {
        return Collections.unmodifiableMap(UUID_TO_MATERIALS_MAP);
    }

    /**
     * Wipes a player's bound materials list, resetting it to an empty list.
     *
     * @param uuid the player's unique identifier.
     */
    public static void emptyUUIDMaterialBindsInCache(UUID uuid) {
        if (uuid == null) return;
        UUID_TO_MATERIALS_MAP.put(uuid, new ArrayList<>());
    }

    /**
     * Gets an unmodifiable view of the mapping between player usernames and their UUIDs.
     *
     * @return an unmodifiable map of usernames to UUIDs.
     */
    @NotNull
    public static Map<String, UUID> getUsernameUUIDMapInCache() {
        return Collections.unmodifiableMap(USERNAME_TO_UUID_MAP);
    }

    /**
     * Adds a player's name and UUID to active memory for tab-completion support.
     *
     * @param uuid       the player's unique identifier.
     * @param playerName the player's username.
     */
    public static void addPlayerToCache(@NotNull UUID uuid, @NotNull String playerName) {
        USERNAME_TO_UUID_MAP.put(playerName, uuid);
    }

    /**
     * Adds a macro name to the active in-memory cache.
     *
     * @param macroName the name of the macro.
     */
    public static void addBTLoadoutMacroNameToCache(@NotNull String macroName) {
        if (!AVAILABLE_MACROS.contains(macroName)) AVAILABLE_MACROS.add(macroName);
    }

    /**
     * Removes a macro name from the active in-memory cache.
     *
     * @param macroName the name of the macro.
     */
    public static void removeBTLoadoutMacroNameFromCache(@NotNull String macroName) {
        AVAILABLE_MACROS.remove(macroName);
    }

    /**
     * Adds a bound material to a player's cached profile for tab-completion.
     *
     * @param uuid     the player's unique identifier.
     * @param material the material name to add.
     */
    public static void addUUIDMaterialBindToCache(@NotNull UUID uuid, @NotNull String material) {
        UUID_TO_MATERIALS_MAP.compute(uuid, (key, existingList) -> {
            List<String> materialsBound = existingList != null ? existingList : new ArrayList<>();
            if (!materialsBound.contains(material)) materialsBound.add(material);
            return materialsBound;
        });
    }

    /**
     * Removes a bound material from a player's cached profile.
     *
     * @param uuid     the player's unique identifier.
     * @param material the material name to remove.
     */
    public static void removeUUIDMaterialBindFromCache(@NotNull UUID uuid, @NotNull String material) {
        UUID_TO_MATERIALS_MAP.computeIfPresent(uuid, (key, materialsBound) -> {
            materialsBound.remove(material);
            return materialsBound.isEmpty() ? null : materialsBound;
        });
    }

    /**
     * Refreshes the macro cache, updating its maximum capacity from the configuration file.
     */
    public static void refreshMacroCache() {
        Map<String, BTLoadoutMacro> oldEntries = null;
        if (macroCache != null) {
            oldEntries = new LinkedHashMap<>(macroCache);
        }
        macroCache = Collections.synchronizedMap(
                new LinkedHashMap<>(Config.maxCachedMacros, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, BTLoadoutMacro> eldest) {
                        return size() > Config.maxCachedMacros;
                    }
                }
        );
        if (oldEntries != null && !oldEntries.isEmpty()) {
            macroCache.putAll(oldEntries);
        }
    }

    /**
     * Retrieves a macro by name, utilizing an LRU memory cache or loading from disk and placing it in the cache.
     *
     * @param macroName the name of the macro.
     * @return the BTLoadoutMacro instance, or null if it doesn't exist on disk.
     */
    @NotNull
    public static BTLoadoutMacro getBTLoadoutMacroFromCacheOrFile(@NotNull String macroName) {
        BTLoadoutMacro cachedMacro = macroCache.get(macroName);
        if (cachedMacro != null)
            return cachedMacro;
        BTLoadoutMacro newMacro = new BTLoadoutMacro(macroName);
        if (newMacro.alreadyExists()) {
            newMacro.loadFromFile();
            macroCache.put(macroName, newMacro);
            return newMacro;
        }
        return newMacro;
    }

    /**
     * Removes the specified brush / tool loadout macro from the in-memory LRU cache.
     *
     * @param macroName the name of the macro to remove from cache.
     */
    public static void removeBTLoadoutMacroFromCache(@NotNull String macroName) {
        macroCache.remove(macroName);
    }

    /**
     * Scans the macro directory on disk to collect all available brush / tool macro file names.
     *
     * @return a list of macro names without file extensions.
     */
    @NotNull
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
