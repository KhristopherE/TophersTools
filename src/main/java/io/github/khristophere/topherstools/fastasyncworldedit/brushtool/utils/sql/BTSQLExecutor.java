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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.khristophere.topherstools.TophersTools;
import io.github.khristophere.topherstools.data.management.sql.SQLManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

/**
 * Handles low-level database operations and execution logic for brush / tool data management.
 */
public class BTSQLExecutor {

    public static SQLManager sqlManager;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>() {
    }.getType();
    private final BTSQLRequests btsqlRequests;

    /**
     * Constructs a new BTSQLExecutor, initializes the underlying SQLManager for database executions,
     * and instantiates the BTSQLRequests handler for high-level requests.
     * <p>
     * If remote SQL is enabled in the config, provided arguments are disregarded in favor the config.
     * Unless the first connection attempt fails, then we default to local SQLite.
     *
     * @param filePath     the relative folder path within the plugin's data folder.
     * @param databaseName the name of the SQLite database file.
     */
    public BTSQLExecutor(@NotNull String filePath, @NotNull String databaseName) {
        if (!Config.isRemote) {
            sqlManager = new SQLManager(filePath, databaseName, 8);
        } else {
            sqlManager = new SQLManager(Config.host, Config.port, Config.databaseName, Config.username, Config.password, Config.useSSL, 8);
            try {
                sqlManager.getConnection();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to connect to Remote SQL Server, Defaulting to Local.", e);
                Config.isRemote = false;
                sqlManager = new SQLManager(filePath, databaseName, 8);
            }
        }
        this.btsqlRequests = new BTSQLRequests(this);
    }

    /**
     * Retrieves the BTSQLRequests handler for high-level requests.
     *
     * @return the BTSQLRequests instance
     */
    @NotNull
    public BTSQLRequests getBTSQLRequests() {
        return this.btsqlRequests;
    }

    /**
     * Initializes the required database tables and index if they do not already exist.
     */
    public void initializeTable() {
        boolean anyFail = false;
        try (Connection c = sqlManager.getConnection(); Statement s = c.createStatement()) {
            s.execute(BTSQLStatements.CREATE_PLAYERS_TABLE);
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Database Request Create Players Table Failed!", e);
            anyFail = true;
        }
        try (Connection c = sqlManager.getConnection(); Statement s = c.createStatement()) {
            s.execute(BTSQLStatements.CREATE_BRUSH_TOOL_BINDINGS_TABLE);
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Database Request Create BrushToolBindings Table Failed!", e);
            anyFail = true;
        }
        try (Connection c = sqlManager.getConnection(); Statement s = c.createStatement()) {
            s.execute(BTSQLStatements.CREATE_USERNAME_TO_UUID_INDEX());
        } catch (SQLException e) {
            if (e.getErrorCode() == 1061) return; // Duplicate Key Name error, disregard.
            TophersTools.getInstance().getSLF4JLogger().error("Database Request Create UsernamesToUUID Index Failed!", e);
            anyFail = true;
        }
        if (anyFail)
            TophersTools.getInstance().getSLF4JLogger().error("FAWE-BrushTools/SQLData/PlayerData.db has failed to initialize");
    }

    /**
     * Asynchronously updates a player's username in the database.
     *
     * @param uuid     the player's unique identifier.
     * @param username the new username.
     */
    public void updateUsername(@NotNull UUID uuid, @NotNull String username) {
        Scheduler.runSequentialAsync(() -> {
            String request = BTSQLStatements.UPSERT_USERNAME;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, username);
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to update FAWE-BrushTools/SQLData/PlayerData.db with Username: {}, with UUID: {}", username, uuid, e);
            }
        });
    }

    /**
     * Retrieves a mapping of all player usernames to their respective unique identifiers from the database.
     *
     * @return a map containing username-to-uuid associations
     */
    @NotNull
    public Map<String, UUID> getUsernameUUIDMap() {
        Map<String, UUID> usernameUUIDMap = new HashMap<>();
        String request = BTSQLStatements.SELECT_ALL_USERNAMES_TO_UUIDS;
        try (Connection c = sqlManager.getConnection();
             Statement sqlRequest = c.createStatement();
             ResultSet resultSet = sqlRequest.executeQuery(request)) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String username = resultSet.getString("username");
                usernameUUIDMap.put(username, uuid);
            }
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to get Username UUID Map from FAWE-BrushTools/SQLData/PlayerData.db", e);
        }
        return usernameUUIDMap;
    }

    /**
     * Retrieves a mapping of player unique identifiers to a list of their bound materials from the database.
     *
     * @return a map containing uuid-to-material list associations
     */
    @NotNull
    public Map<UUID, List<String>> getUUIDMaterialMap() {
        Map<UUID, List<String>> UUIDMaterialMap = new HashMap<>();
        String request = BTSQLStatements.SELECT_ALL_UUIDS_TO_MATERIALS;
        try (Connection c = sqlManager.getConnection();
             Statement sqlRequest = c.createStatement();
             ResultSet resultSet = sqlRequest.executeQuery(request)) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String material = resultSet.getString("material");
                UUIDMaterialMap.computeIfAbsent(uuid, k -> new ArrayList<>()).add(material);
            }
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to get UUID Material Map from FAWE-BrushTools/SQLData/PlayerData.db", e);
        }
        return UUIDMaterialMap;
    }

    /**
     * Retrieves the complete loadout for a specific brush / tool material binding, or returns null if none exists.
     *
     * @param uuid         the player's unique identifier.
     * @param materialName the material name.
     * @return the resulting BTLoadout instance, or null if not found
     */
    @Nullable
    public BTLoadout getBrushToolLoadout(@NotNull UUID uuid, @NotNull String materialName) {
        String request = BTSQLStatements.SELECT_BRUSH_TOOL_BINDING_ORDINAL_COMMANDS_ITEMSTACK;
        try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
            sqlRequest.setString(1, uuid.toString());
            sqlRequest.setString(2, materialName.toUpperCase());
            try (ResultSet resultSet = sqlRequest.executeQuery()) {
                if (resultSet.next()) {
                    String primaryCommand = resultSet.getString("primaryCommand");
                    List<String> primaryModifierCommands = btsqlRequests.getAllBrushToolModifiers(uuid, materialName, BTModifier.Type.PRIMARY);
                    String secondaryCommand = resultSet.getString("secondaryCommand");
                    List<String> secondaryModifierCommands = btsqlRequests.getAllBrushToolModifiers(uuid, materialName, BTModifier.Type.SECONDARY);
                    List<String> globalModifierCommands = btsqlRequests.getAllBrushToolModifiers(uuid, materialName, BTModifier.Type.GLOBAL);
                    byte[] itemBytes = resultSet.getBytes("itemStack");
                    ItemStack itemStack = null;
                    if (itemBytes != null) {
                        itemStack = ItemStack.deserializeBytes(itemBytes);
                    }
                    return new BTLoadout(materialName, itemStack, primaryCommand, secondaryCommand, primaryModifierCommands, secondaryModifierCommands, globalModifierCommands);
                }
            }
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to get BTLoadout for UUID: {}, for Material: {} from FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), e);
        }
        return null;
    }

    /**
     * Asynchronously deletes a player's brush / tool loadout binding from the database.
     *
     * @param uuid         the player's unique identifier.
     * @param materialName the material name.
     */
    public void deleteBrushToolLoadout(@NotNull UUID uuid, @NotNull String materialName) {
        Scheduler.runSequentialAsync(() -> {
            String request = BTSQLStatements.DELETE_BRUSH_TOOL_BINDING;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, materialName.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to delete BTLoadout for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), e);
            }
        });
    }

    /**
     * Asynchronously updates or inserts a brush / tool primary command binding in the database.
     *
     * @param uuid           the player's unique identifier.
     * @param itemStack      the item stack associated with the brush / tool.
     * @param primaryCommand the primary command string.
     */
    public void updatePrimaryCommand(@NotNull UUID uuid, @NotNull ItemStack itemStack, @NotNull String primaryCommand) {
        Scheduler.runSequentialAsync(() -> {
            String request = BTSQLStatements.UPSERT_PRIMARY_COMMAND();
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, itemStack.getType().name());
                sqlRequest.setString(3, primaryCommand);
                sqlRequest.setBytes(4, itemStack.serializeAsBytes());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to update Primary Command for UUID: {}, for Material: {}, for Primary Command:\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid, itemStack.getType().name(), primaryCommand, e);
            }
        });
    }

    /**
     * Asynchronously updates or inserts a brush / tool secondary command binding in the database.
     *
     * @param uuid             the player's unique identifier.
     * @param itemStack        the item stack associated with the brush / tool.
     * @param secondaryCommand the secondary command string.
     */
    public void updateSecondaryCommand(@NotNull UUID uuid, @NotNull ItemStack itemStack, @NotNull String secondaryCommand) {
        Scheduler.runSequentialAsync(() -> {
            String request = BTSQLStatements.UPSERT_SECONDARY_COMMAND();
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, itemStack.getType().name());
                sqlRequest.setString(3, secondaryCommand);
                sqlRequest.setBytes(4, itemStack.serializeAsBytes());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to update Primary Command for UUID: {}, for Material: {}, for Secondary Command:\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid, itemStack.getType().name(), secondaryCommand, e);
            }
        });
    }

    /**
     * Updates a player's brush / tool modifier commands in the database.
     *
     * @param uuid             the player's unique identifier.
     * @param materialName     the material name.
     * @param modifierCommands the list of modifier command strings.
     * @param modifierType     the modifier type to target.
     */
    public void updateModifierCommands(@NotNull UUID uuid, @NotNull String materialName, @Nullable List<String> modifierCommands, @NotNull BTModifier.Type modifierType) {
        String request = BTSQLStatements.UPSERT_MODIFIER_COMMANDS(modifierType);
        try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
            sqlRequest.setString(1, uuid.toString());
            sqlRequest.setString(2, materialName.toUpperCase());
            sqlRequest.setString(3, gson.toJson(modifierCommands != null ? modifierCommands : Collections.emptyList()));
            sqlRequest.executeUpdate();
        } catch (SQLException e) {
            String modifierCommandsConcatenated = modifierCommands != null ? String.join(", ", modifierCommands) : "";
            TophersTools.getInstance().getSLF4JLogger().error("Failed to update Modifier Commands for UUID: {}, for Material: {}, for Modifier Command(s):\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), modifierCommandsConcatenated, e);
        }
    }

    /**
     * Asynchronously removes a player's brush / tool secondary command while leaving other bindings intact.
     *
     * @param uuid         the player's unique identifier.
     * @param materialName the material name.
     */
    public void removeSecondaryCommand(@NotNull UUID uuid, @NotNull String materialName) {
        Scheduler.runSequentialAsync(() -> {
            String request = BTSQLStatements.DELETE_SECONDARY_BINDING;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, materialName.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to delete Secondary Command for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), e);
            }
        });
    }

    /**
     * Asynchronously clears a player's brush / tool modifier commands while leaving other bindings intact.
     *
     * @param uuid         the player's unique identifier.
     * @param materialName the material name.
     * @param modifierType the modifier type to target.
     */
    public void removeModifierCommands(@NotNull UUID uuid, @NotNull String materialName, @NotNull BTModifier.Type modifierType) {
        Scheduler.runSequentialAsync(() -> {
            String sql = switch (modifierType) {
                case GLOBAL -> BTSQLStatements.DELETE_GLOBAL_MODIFIER_COMMANDS;
                case PRIMARY -> BTSQLStatements.DELETE_PRIMARY_MODIFIER_COMMANDS;
                case SECONDARY -> BTSQLStatements.DELETE_SECONDARY_MODIFIER_COMMANDS;
            };
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(sql)) {
                sqlRequest.setString(1, gson.toJson(Collections.emptyList()));
                sqlRequest.setString(2, uuid.toString());
                sqlRequest.setString(3, materialName.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                TophersTools.getInstance().getSLF4JLogger().error("Failed to delete Modifier Commands for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), e);
            }
        });
    }

    /**
     * Retrieves the raw stored modifier commands for a given brush / tool material.
     *
     * @param uuid         the player's unique identifier.
     * @param materialName the material name.
     * @param modifierType the modifier type to target.
     * @return a list of raw modifier strings, or an empty list if none exist
     */
    public List<String> getModifiersRaw(@NotNull UUID uuid, @NotNull String materialName, @NotNull BTModifier.Type modifierType) {
        String sql = switch (modifierType) {
            case GLOBAL -> BTSQLStatements.SELECT_GLOBAL_MODIFIER_COMMANDS;
            case PRIMARY -> BTSQLStatements.SELECT_PRIMARY_MODIFIER_COMMANDS;
            case SECONDARY -> BTSQLStatements.SELECT_SECONDARY_MODIFIER_COMMANDS;
        };
        try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(sql)) {
            sqlRequest.setString(1, uuid.toString());
            sqlRequest.setString(2, materialName.toUpperCase());
            try (ResultSet resultSet = sqlRequest.executeQuery()) {
                if (resultSet.next()) {
                    String jsonModifierCommands = resultSet.getString(
                            switch (modifierType) {
                                case GLOBAL -> "globalModifierCommands";
                                case PRIMARY -> "primaryModifierCommands";
                                case SECONDARY -> "secondaryModifierCommands";
                            });
                    return (jsonModifierCommands != null && !jsonModifierCommands.isEmpty())
                            ? gson.fromJson(jsonModifierCommands, listType)
                            : new ArrayList<>();
                }
            }
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to get Raw Modifier Commands for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid, materialName.toUpperCase(), e);
        }
        return new ArrayList<>();
    }

}
