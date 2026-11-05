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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config;

import io.github.khristophere.topherstools.data.management.yml.YMLManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Manages brush / tool configuration options.
 */
public class Config {

    // =========================================================================
    // SQL Data Settings
    // =========================================================================
    public static boolean isRemote;
    public static String host;
    public static int port;
    public static String databaseName;
    public static String username;
    public static String password;
    public static boolean useSSL;
    /**
     * The maximum number of recently used brush / tool loadout macros to retain in memory.
     */
    public static int maxCachedMacros;
    /**
     * Whether to silence FastAsyncWorldEdit feedback messages during macro execution.
     */
    public static boolean silenceFAWEFeedback;
    // =========================================================================
    // Cache Management Settings
    // =========================================================================
    // =========================================================================
    // Brush Tool Command Cooldowns
    // =========================================================================
    public static long CooldownBTClearAll;
    // =========================================================================
    // Brush Macro Settings
    // =========================================================================
    public static long CooldownBTGet;
    public static long CooldownBTMacroSave;
    public static long CooldownBTMacroLoad;
    public static long CooldownBTMacroDelete;
    public static long CooldownBTReturnAll;
    public static long CooldownBTShout;
    private static boolean hasStarted = false;
    private static YMLManager configYML;

    /**
     * Populates defaults if missing and loads all values into memory.
     */
    public static void startConfig() {
        if (hasStarted) return;
        hasStarted = true;
        configYML = new YMLManager("FAWE-BT-Utils", "Config", true);
        populateDefaults();
        loadConfigValues(true);
    }

    /**
     * Ensures all required configuration paths exist, injecting defaults for any missing keys without overwriting existing settings.
     */
    private static void populateDefaults() {
        boolean modified = false;

        List<String> header = List.of(
                "############################################################",
                "# +------------------------------------------------------+ #",
                "# |                 FAWE-BT-Utils Config                 | #",
                "# +------------------------------------------------------+ #",
                "############################################################");

        if (!configYML.getConfig().options().getHeader().equals(header)) {
            configYML.getConfig().options().setHeader(header);
            modified = true;
        }

        // SQL Data Defaults
        modified |= checkAndSet("SQLData.Remote.UseRemoteDatabase", false,
                List.of("False will utilize SQLite locally.",
                        "True is compatible with MySQL & MariaDB."));

        modified |= checkAndSet("SQLData.Remote.Settings.Host", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Port", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Database-Name", "TophersTools-FAWE-BT-Utils", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Username", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Password", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Use-SSL", false, null);

        // Cache Management Defaults
        modified |= checkAndSet("CacheManagement.MaxCachedMacros", 5,
                List.of("Maximum number of Brush/Tool macros to retain in active memory via the LRU cache."));

        // Brush Tool Macro Defaults
        modified |= checkAndSet("BrushTool.Macro.SilenceFAWEFeedback", true,
                List.of("Whether to silence FastAsyncWorldEdit (FAWE) feedback chat messages (errors are intended to pass through) when executing brush macros.",
                        "Highly recommended to keep as True, as the the chat messages can be excessive."));

        // Brush Tool Command Cooldowns Defaults
        modified |= checkAndSet("BrushTool.Commands.Cooldowns", null, List.of("Command cooldowns are in Milliseconds"));
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ClearAllBrushTools", 8000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.GetBrushTool", 2000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroSave", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroLoad", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroDelete", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ReturnAllBrushTools", 8000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ShoutBrushTool", 5000L, null);

        if (modified) configYML.saveConfig();
    }

    /**
     * Loads configuration values from the YML file into static memory fields.
     *
     * @param loadSQL whether to load SQL configuration settings.
     */
    public static void loadConfigValues(boolean loadSQL) {
        var config = configYML.getConfig();

        if (loadSQL) {
            // SQL Data
            isRemote = config.getBoolean("SQLData.Remote.UseRemoteDatabase", false);
            host = config.getString("SQLData.Remote.Settings.Host", "");
            port = config.getInt("SQLData.Remote.Settings.Port", 3306);
            databaseName = config.getString("SQLData.Remote.Settings.Database-Name", "TophersTools-FAWE-BT-Utils");
            username = config.getString("SQLData.Remote.Settings.Username", "");
            password = config.getString("SQLData.Remote.Settings.Password", "");
            useSSL = config.getBoolean("SQLData.Remote.Settings.Use-SSL", false);
        }

        // Cache Management
        maxCachedMacros = config.getInt("CacheManagement.MaxCachedMacros", 5);

        // Brush Macro
        silenceFAWEFeedback = config.getBoolean("BrushTool.Macro.SilenceFAWEFeedback", true);

        // Brush Tool Command Cooldowns
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.ClearAllBrushTools", 8000L);
        CooldownBTGet = config.getLong("BrushTool.Commands.Cooldowns.GetBrushTool", 2000L);
        CooldownBTMacroSave = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroSave", 5000L);
        CooldownBTMacroLoad = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroLoad", 5000L);
        CooldownBTMacroDelete = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroDelete", 5000L);
        CooldownBTReturnAll = config.getLong("BrushTool.Commands.Cooldowns.ReturnAllBrushTools", 8000L);
        CooldownBTShout = config.getLong("BrushTool.Commands.Cooldowns.ShoutBrushTool", 5000L);
    }

    /**
     * Helper method to check if a configuration path exists, and set its default value and comments if missing.
     *
     * @param path     the configuration path key.
     * @param value    the default value to apply if missing.
     * @param comments optional comments to attach to the node.
     * @return true if a default was added, false otherwise.
     */
    private static boolean checkAndSet(@NotNull String path, @Nullable Object value, @Nullable List<String> comments) {
        var config = configYML.getConfig();
        if (!config.contains(path)) {
            if (value != null) config.set(path, value);
            if (comments != null && !comments.isEmpty()) config.setComments(path, comments);
            return true;
        }
        return false;
    }

    /**
     * Reloads the configuration file from disk, updates all in-memory static fields (except SQL settings), and dynamically adjust the macro cache limit.
     */
    public static void reloadConfigSettings() {
        configYML.reloadConfig();
        populateDefaults();
        loadConfigValues(false);

        BTCacheManager.refreshMacroCache();
        Cooldown.refreshAllCooldowns();
    }

}
