package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;

public class SQLQueries {

    public static final String requestCreatePlayersTable = """
            CREATE TABLE IF NOT EXISTS Players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL
            );
            """;

    public static final String requestCreateBrushToolBindingsTable = """
            CREATE TABLE IF NOT EXISTS BrushToolBindings (
                uuid VARCHAR(36) NOT NULL,
                material VARCHAR(64) NOT NULL,
                primaryCommand TEXT,
                secondaryCommand TEXT,
                modifierCommands TEXT,
                itemStack BLOB,
                PRIMARY KEY (uuid, material)
            );
            """;

    public static String requestCreateUsernameToUUIDIndex() {
        if (Config.isRemote) {
            return "CREATE INDEX IF NOT EXISTS UsernamesToUUID ON Players(username);";
        } else {
            return "CREATE INDEX IF NOT EXISTS UsernamesToUUID ON Players(username COLLATE NOCASE);";
        }
    }

    public static final String updateUsernameSQL = "REPLACE INTO Players (uuid, username) VALUES (?, ?);";

    public static final String getUsernameUUIDMapSQL = "SELECT uuid, username FROM Players;";

    public static final String getUUIDMaterialMapSQL = "SELECT uuid, material FROM BrushToolBindings;";

    public static final String getBrushToolLoadoutSQL = "SELECT primaryCommand, secondaryCommand, itemStack FROM BrushToolBindings WHERE uuid = ? AND material = ?;";

    public static final String deleteBrushToolLoadoutSQL = "DELETE FROM BrushToolBindings WHERE uuid = ? AND material = ?;";

    public static String updatePrimaryCommandSQL() {
        if (Config.isRemote) {
            return """
                INSERT INTO BrushToolBindings (uuid, material, primaryCommand, itemStack)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE primaryCommand = VALUES(primaryCommand);
                """;
        } else {
            return """
                INSERT INTO BrushToolBindings (uuid, material, primaryCommand, itemStack)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid, material)
                DO UPDATE SET primaryCommand = excluded.primaryCommand;
                """;
        }
    }

    public static String updateSecondaryCommandSQL() {
        if (Config.isRemote) {
            return """
                INSERT INTO BrushToolBindings (uuid, material, secondaryCommand, itemStack)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE secondaryCommand = VALUES(secondaryCommand);
                """;
        } else {
            return """
                INSERT INTO BrushToolBindings (uuid, material, secondaryCommand, itemStack)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid, material)
                DO UPDATE SET secondaryCommand = excluded.secondaryCommand;
                """;
        }
    }

    public static String updateModifierCommandsSQL() {
        if (Config.isRemote) {
            return """
                INSERT INTO BrushToolBindings (uuid, material, modifierCommands)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE modifierCommands = VALUES(modifierCommands);
                """;
        } else {
            return """
                INSERT INTO BrushToolBindings (uuid, material, modifierCommands)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid, material)
                DO UPDATE SET modifierCommands = excluded.modifierCommands;
                """;
        }
    }

    public static final String removeSecondaryCommandSQL = "UPDATE BrushToolBindings SET secondaryCommand = NULL WHERE uuid = ? AND material = ?;";

    public static final String removeModifierCommandsSQL = "UPDATE BrushToolBindings SET modifierCommands = ? WHERE uuid = ? AND material = ?;";

    public static final String getModifiersRawSQL = "SELECT modifierCommands FROM BrushToolBindings WHERE uuid = ? AND material = ?;";



}
