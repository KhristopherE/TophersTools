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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * A centralized repository of standardized SQL queries and dialect-aware statement generators
 * supporting both SQLite (local) and MySQL / MariaDB (remote) databases for FAWEBTUtils.
 */
public class BTSQLStatements {

    /**
     * SQL statement to create the player to UUID table.
     */
    @Language("SQL")
    public static final String CREATE_PLAYERS_TABLE = """
            CREATE TABLE IF NOT EXISTS Players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL
            );
            """;

    /**
     * SQL statement to create the brush tool bindings table storing player tool commands, and serialized item stacks.
     */
    @Language("SQL")
    public static final String CREATE_BRUSH_TOOL_BINDINGS_TABLE = """
            CREATE TABLE IF NOT EXISTS BrushToolBindings (
                uuid VARCHAR(36) NOT NULL,
                material VARCHAR(64) NOT NULL,
                primaryCommand TEXT,
                primaryModifierCommands TEXT,
                secondaryCommand TEXT,
                secondaryModifierCommands TEXT,
                globalModifierCommands TEXT,
                itemStack BLOB,
                PRIMARY KEY (uuid, material)
            );
            """;
    /**
     * SQL statement to insert or replace a player's username.
     */
    @Language("SQL")
    public static final String UPSERT_USERNAME = "REPLACE INTO Players (uuid, username) VALUES (?, ?);";
    /**
     * SQL statement to retrieve all stored UUID-to-username mappings.
     */
    @Language("SQL")
    public static final String SELECT_ALL_USERNAMES_TO_UUIDS = "SELECT uuid, username FROM Players;";
    /**
     * SQL statement to retrieve all stored binding key pairs (UUID and material).
     */
    @Language("SQL")
    public static final String SELECT_ALL_UUIDS_TO_MATERIALS = "SELECT uuid, material FROM BrushToolBindings;";
    /**
     * SQL statement to fetch a specific brush tool's primary command, secondary command, itemStack by UUID & material.
     */
    @Language("SQL")
    public static final String SELECT_BRUSH_TOOL_BINDING_ORDINAL_COMMANDS_ITEMSTACK = "SELECT primaryCommand, secondaryCommand, itemStack FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to delete a specific brush tool binding entry.
     */
    @Language("SQL")
    public static final String DELETE_BRUSH_TOOL_BINDING = "DELETE FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to remove a secondary command from a binding.
     */
    @Language("SQL")
    public static final String DELETE_SECONDARY_BINDING = "UPDATE BrushToolBindings SET secondaryCommand = NULL WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to remove a primary modifier commands from a binding.
     */
    @Language("SQL")
    public static final String DELETE_PRIMARY_MODIFIER_COMMANDS = "UPDATE BrushToolBindings SET primaryModifierCommands = ? WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to remove a secondary modifier commands from a binding.
     */
    @Language("SQL")
    public static final String DELETE_SECONDARY_MODIFIER_COMMANDS = "UPDATE BrushToolBindings SET secondaryModifierCommands = ? WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to remove a global modifier commands from a binding.
     */
    @Language("SQL")
    public static final String DELETE_GLOBAL_MODIFIER_COMMANDS = "UPDATE BrushToolBindings SET globalModifierCommands = ? WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to fetch raw primary modifier commands.
     */
    @Language("SQL")
    public static final String SELECT_PRIMARY_MODIFIER_COMMANDS = "SELECT primaryModifierCommands FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to fetch raw secondary modifier commands.
     */
    @Language("SQL")
    public static final String SELECT_SECONDARY_MODIFIER_COMMANDS = "SELECT secondaryModifierCommands FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
    /**
     * SQL statement to fetch raw global modifier commands.
     */
    @Language("SQL")
    public static final String SELECT_GLOBAL_MODIFIER_COMMANDS = "SELECT globalModifierCommands FROM BrushToolBindings WHERE uuid = ? AND material = ?;";

    /**
     * @return an SQL statement to create an index for player usernames.
     */
    public static String CREATE_USERNAME_TO_UUID_INDEX() {
        if (Config.isRemote) {
            // language=MySQL
            return "CREATE INDEX UsernamesToUUID ON Players(username);";
        } else {
            // language=SQLite
            return "CREATE INDEX IF NOT EXISTS UsernamesToUUID ON Players(username COLLATE NOCASE);";
        }
    }

    /**
     * @return an SQL statement to insert or replace a primary command binding.
     */
    public static String UPSERT_PRIMARY_COMMAND() {
        if (Config.isRemote) {
            // language=MySQL
            return """
                    INSERT INTO BrushToolBindings (uuid, material, primaryCommand, itemStack)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE primaryCommand = VALUES(primaryCommand);
                    """;
        } else {
            // language=SQLite
            return """
                    INSERT INTO BrushToolBindings (uuid, material, primaryCommand, itemStack)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(uuid, material)
                    DO UPDATE SET primaryCommand = excluded.primaryCommand;
                    """;
        }
    }

    /**
     * @return an SQL statement to insert or replace a secondary command binding.
     */
    @NotNull
    public static String UPSERT_SECONDARY_COMMAND() {
        if (Config.isRemote) {
            // language=MySQL
            return """
                    INSERT INTO BrushToolBindings (uuid, material, secondaryCommand, itemStack)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE secondaryCommand = VALUES(secondaryCommand);
                    """;
        } else {
            // language=SQLite
            return """
                    INSERT INTO BrushToolBindings (uuid, material, secondaryCommand, itemStack)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT(uuid, material)
                    DO UPDATE SET secondaryCommand = excluded.secondaryCommand;
                    """;
        }
    }

    /**
     * SQL statement to insert or replace the specified ordinal / global modifier commands bound.
     *
     * @param modifierType the modifier type to target.
     * @return an SQL statement to insert or replace the specified ordinal / global modifier commands bound.
     */
    @NotNull
    public static String UPSERT_MODIFIER_COMMANDS(@NotNull BTModifier.Type modifierType) {
        if (Config.isRemote) {
            // language=MySQL
            return switch (modifierType) {
                case GLOBAL -> """
                        INSERT INTO BrushToolBindings (uuid, material, globalModifierCommands)
                        VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE globalModifierCommands = VALUES(globalModifierCommands);
                        """;
                case PRIMARY -> """
                        INSERT INTO BrushToolBindings (uuid, material, primaryModifierCommands)
                        VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE primaryModifierCommands = VALUES(primaryModifierCommands);
                        """;
                case SECONDARY -> """
                        INSERT INTO BrushToolBindings (uuid, material, secondaryModifierCommands)
                        VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE secondaryModifierCommands = VALUES(secondaryModifierCommands);
                        """;
            };
        } else {
            // language=SQLite
            return switch (modifierType) {
                case GLOBAL -> """
                        INSERT INTO BrushToolBindings (uuid, material, globalModifierCommands)
                        VALUES (?, ?, ?)
                        ON CONFLICT(uuid, material)
                        DO UPDATE SET globalModifierCommands = excluded.globalModifierCommands;
                        """;
                case PRIMARY -> """
                        INSERT INTO BrushToolBindings (uuid, material, primaryModifierCommands)
                        VALUES (?, ?, ?)
                        ON CONFLICT(uuid, material)
                        DO UPDATE SET primaryModifierCommands = excluded.primaryModifierCommands;
                        """;
                case SECONDARY -> """
                        INSERT INTO BrushToolBindings (uuid, material, secondaryModifierCommands)
                        VALUES (?, ?, ?)
                        ON CONFLICT(uuid, material)
                        DO UPDATE SET secondaryModifierCommands = excluded.secondaryModifierCommands;
                        """;
            };
        }
    }

}
