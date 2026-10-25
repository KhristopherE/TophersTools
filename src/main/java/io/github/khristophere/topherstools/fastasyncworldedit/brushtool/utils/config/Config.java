package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config;

import io.github.khristophere.topherstools.data.management.yml.YMLManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Manages FAWE BrushTool Utils configuration options.
 */
public class Config {

    private static YMLManager configYML;

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

    // =========================================================================
    // Cache Management Settings
    // =========================================================================
    /** The maximum number of recently used BTLoadoutMacros to retain in memory. */
    public static int maxCachedMacros;

    // =========================================================================
    // Brush Macro Settings
    // =========================================================================
    /** Whether to silence FAWE feedback messages during macro execution. */
    public static boolean silenceFAWEFeedback;

    // =========================================================================
    // Brush Tool Command Cooldowns
    // =========================================================================
    /** Whether to silence FAWE feedback messages during macro execution. */
    public static long CooldownBTClearAll;
    public static long CooldownBTGet;
    public static long CooldownBTMacroSave;
    public static long CooldownBTMacroLoad;
    public static long CooldownBTMacroDelete;
    public static long CooldownBTReturnAll;
    public static long CooldownBTShout;

    /**
     * Constructs and initializes the configuration manager, populating defaults if missing
     * and loading all values into memory.
     */
    public Config() {
        configYML = new YMLManager("FAWE-BT-Utils", "config", true);
        populateDefaults();
        loadConfigValues(true);
    }

    /**
     * Ensures all required configuration paths exist, injecting defaults for any
     * missing keys without overwriting existing administrator settings.
     */
    private static void populateDefaults() {
        var config = configYML.getConfig();
        boolean modified = false;

        // SQL Data Defaults
        modified |= checkAndSet("SQLData.Remote.UseRemoteDatabase", false,
                List.of("UseRemoteDatabase: False will utilize SQLite locally. True is compatible with MySQL & MariaDB"));
        modified |= checkAndSet("SQLData.Remote.Settings.Host", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Port", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Database-Name", "TophersTools-FAWE-BT-Utils", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Username", "", null);
        modified |= checkAndSet("SQLData.Remote.Settings.Password", "", null);
        modified |= checkAndSet( "SQLData.Remote.Settings.Use-SSL", false, null);

        // Cache Management Defaults
        modified |= checkAndSet( "CacheManagement.MaxCachedMacros", 5,
                List.of("Maximum number of BT Macros to retain in active memory via the LRU cache."));

        // Brush Tool Macro Defaults
        modified |= checkAndSet("BrushTool.Macro.SilenceFAWEFeedback", true,
                List.of("Whether to silence FastAsyncWorldEdit (FAWE) feedback chat messages when executing brush macros, if false causes allot of clutter in chat."));

        // Brush Tool Command Cooldowns Defaults
        modified |= checkAndSet("BrushTool.Commands.Cooldowns", null, List.of("Cooldowns are in Milliseconds, 1000 Milliseconds = 1 Second"));
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ClearAllBrushTools", 8000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.GetBrushTool", 2000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroSave", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroLoad", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.BrushToolMacroDelete", 5000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ReturnAllBrushTools", 8000L, null);
        modified |= checkAndSet("BrushTool.Commands.Cooldowns.ShoutBrushTool", 5000L, null);

        // Only save to disk if a new default was actually added
        if (modified) {
            configYML.saveConfig();
        }
    }

    /**
     * Loads configuration values from the YML file into static memory fields.
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
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.GetBrushTool", 2000L);
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroSave", 5000L);
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroLoad", 5000L);
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.BrushToolMacroDelete", 5000L);
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.ReturnAllBrushTools", 8000L);
        CooldownBTClearAll = config.getLong("BrushTool.Commands.Cooldowns.ShoutBrushTool", 5000L);
    }

    /**
     * Helper method to check if a configuration path exists, and set its default value and comments if missing.
     *
     * @param path     the configuration path key
     * @param value    the default value to apply if missing
     * @param comments optional comments to attach to the node
     * @return true if a default was added, false otherwise
     */
    private static boolean checkAndSet(String path, @Nullable Object value, @Nullable List<String> comments) {
        var config = configYML.getConfig();
        if (!config.contains(path)) {
            if (value != null) config.set(path, value);
            if (comments != null && !comments.isEmpty()) config.setComments(path, comments);
            return true;
        }
        return false;
    }

    /**
     * Reloads the configuration file from disk, updates all in-memory static fields,
     * and refreshes the macro cache limit.
     */
    public static void reloadConfigSettings() {
        configYML.reloadConfig();
        populateDefaults();
        loadConfigValues(false);

        BTCacheManager.refreshMacroCache();
        Cooldown.refreshAllCooldowns();
    }

}
