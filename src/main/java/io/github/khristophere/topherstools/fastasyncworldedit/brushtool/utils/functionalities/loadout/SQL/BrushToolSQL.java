package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.SQL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.filemanagement.sql.SQLManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.ConfigYML;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.functionalities.loadout.BrushToolLoadout;
import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BrushToolSQL {

    public static SQLManager sqlManager;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>() {}.getType();
    public final BrushToolSQLRequests brushToolSQLRequests;

    /**
     * Instantiates this class & the SQLManager for our BrushToolSQL.
     */
    public BrushToolSQL(String filePath, String databaseName){
        if (!ConfigYML.isRemote) {
            sqlManager = new SQLManager(filePath, databaseName);
        } else {
            sqlManager = new SQLManager(ConfigYML.host, ConfigYML.port, ConfigYML.databaseName, ConfigYML.username, ConfigYML.password, ConfigYML.useSSL);
        }
        this.brushToolSQLRequests = new BrushToolSQLRequests(this);
    }

    /**
     * Initializes the SQL Tables if they do not already exist.
     * A table for UUID's to Player Usernames and an Index for Username to UUID (Index is not Currently Used).
     * A table for the Brush Bindings, UUID, Material with it's associated Primary & OR Secondary Commands, & Modifiers.
     */
    public void initializeTable(){
        String requestCreatePlayersTable = """
            CREATE TABLE IF NOT EXISTS Players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL
            );
            """;
        String requestCreateBrushToolBindingsTable = """
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
        String requestCreateUsernameToUUIDIndex = """
                CREATE INDEX IF NOT EXISTS UsernamesToUUID ON Players(username COLLATE NOCASE);
                """;
        boolean rCPT = sqlManager.makeRequest(requestCreatePlayersTable);
        boolean rCBT = sqlManager.makeRequest(requestCreateBrushToolBindingsTable);
        boolean rCUTUI = sqlManager.makeRequest(requestCreateUsernameToUUIDIndex);
        if (!rCPT) TophersTools.getInstance().getLogger().severe("Database Request Create Players Table Failed!");
        if (!rCBT) TophersTools.getInstance().getLogger().severe("Database Request Create BrushToolBindings Table Failed!");
        if (!rCUTUI) TophersTools.getInstance().getLogger().severe("Database Request Create UsernamesToUUID Index Failed!");
        if (!rCPT && !rCBT && !rCUTUI) TophersTools.getInstance().getLogger().severe("FAWE-BrushTools/SQLData/PlayerData.db has failed to initialize");
    }

    /**
     * Updates the Requested UUID's Username (In the case the username has changed since last login.)
     * @param uuid Requested UUID.
     * @param username New Username.
     */
    public void updateUsernameSQL(UUID uuid, String username) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = "INSERT OR REPLACE INTO Players (uuid, username) VALUES (?, ?);";
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, username);
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to update FAWE-BrushTools/SQLData/PlayerData.db with Username: {}, with UUID: {}", username, uuid.toString(), e);}
        });
    }

    /**
     * Gets a Map of Usernames to UUID's from the SQL File.
     * @return a Map of Usernames to UUID's from the SQL File.
     */
    public Map<String, UUID> getUsernameUUIDMapSQL() {
        Map<String, UUID> usernameUUIDMap = new HashMap<>();
        String request = "SELECT uuid, username FROM Players;";
        try (Connection c = sqlManager.getConnection();
             Statement sqlRequest = c.createStatement();
             ResultSet resultSet = sqlRequest.executeQuery(request)) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String username = resultSet.getString("username");
                usernameUUIDMap.put(username, uuid);
            }
        } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to get Username UUID Map from FAWE-BrushTools/SQLData/PlayerData.db", e);}
        return usernameUUIDMap;
    }

    /**
     * Gets a Map of UUID's to Materials from the SQL File.
     * @return a Map of UUID's to Materials from the SQL File.
     */
    public Map<UUID, List<String>> getUUIDMaterialMapSQL() {
        Map<UUID, List<String>> UUIDMaterialMap = new HashMap<>();
        String request = "SELECT uuid, material FROM BrushToolBindings;";
        try (Connection c = sqlManager.getConnection();
             Statement sqlRequest = c.createStatement();
             ResultSet resultSet = sqlRequest.executeQuery(request)) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String material = resultSet.getString("material");
                UUIDMaterialMap.computeIfAbsent(uuid, k -> new ArrayList<>()).add(material);
            }
        } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to get UUID Material Map from FAWE-BrushTools/SQLData/PlayerData.db", e);}
        return UUIDMaterialMap;
    }

    /**
     * Gets either the BrushToolLoadout found, or null if nothing was found.
     * @param uuid Requested Players UUID.
     * @param material Material Bound.
     * @return either a BrushToolLoadout or Null if there is no BrushTool found.
     */
    @Nullable
    public BrushToolLoadout getBrushToolLoadoutSQL(UUID uuid, String material) {
        if (uuid == null || material == null) return null;
        String request = "SELECT primaryCommand, secondaryCommand, itemStack FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
        try (Connection conn = sqlManager.getConnection(); PreparedStatement sqlRequest = conn.prepareStatement(request)) {
            sqlRequest.setString(1, uuid.toString());
            sqlRequest.setString(2, material.toUpperCase());
            try (ResultSet resultSet = sqlRequest.executeQuery()) {
                if (resultSet.next()) {
                    String primaryCommand = resultSet.getString("primaryCommand");
                    String secondaryCommand = resultSet.getString("secondaryCommand");
                    List<String> modifierCommands = brushToolSQLRequests.getAllBrushToolModifiers(uuid, material);
                    byte[] itemBytes = resultSet.getBytes("itemStack");
                    ItemStack itemStack = null;
                    if (itemBytes != null) {
                        itemStack = ItemStack.deserializeBytes(itemBytes);
                    }
                    return new BrushToolLoadout(material, itemStack, primaryCommand, secondaryCommand, modifierCommands);
                }
            }
        } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to get BrushToolLoadout for UUID: {}, for Material: {} from FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), e);}
        return null;
    }

    /**
     * Deletes the requested UUID's Material's BrushToolLoadout from the SQL File.
     * @param uuid Requested Players UUID.
     * @param material Material Bound.
     */
    public void deleteBrushToolLoadoutSQL(UUID uuid, String material) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = "DELETE FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, material.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to delete BrushToolLoadout for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), e);}
        });
    }

    /**
     * Updates the requested UUID's Material's Primary Command in the SQL File.
     * @param uuid Requested Players UUID.
     * @param itemStack Requested ItemStack.
     * @param primaryCommand New Primary Command.
     */
    public void updatePrimaryCommandSQL(UUID uuid, ItemStack itemStack, String primaryCommand) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = """
            INSERT INTO BrushToolBindings (uuid, material, primaryCommand, itemStack)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid, material)
            DO UPDATE SET primaryCommand = excluded.primaryCommand;
            """;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, itemStack.getType().name());
                sqlRequest.setString(3, primaryCommand);
                sqlRequest.setBytes(4, itemStack.serializeAsBytes());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to update Primary Command for UUID: {}, for Material: {}, for Primary Command:\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), itemStack.getType().name(), primaryCommand, e);}
        });
    }

    /**
     * Updates the requested UUID's Material's Secondary Command in the SQL File.
     * @param uuid Requested Players UUID.
     * @param itemStack Requested ItemStack.
     * @param secondaryCommand New Primary Command.
     */
    public void updateSecondaryCommandSQL(UUID uuid, ItemStack itemStack, String secondaryCommand) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = """
            INSERT INTO BrushToolBindings (uuid, material, secondaryCommand, itemStack)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid, material)
            DO UPDATE SET secondaryCommand = excluded.secondaryCommand;
            """;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, itemStack.getType().name());
                sqlRequest.setString(3, secondaryCommand);
                sqlRequest.setBytes(4, itemStack.serializeAsBytes());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to update Primary Command for UUID: {}, for Material: {}, for Secondary Command:\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), itemStack.getType().name(), secondaryCommand, e);}
        });
    }

    /**
     * Updates the requested UUID's Material's Modifier Commands in the SQL File.
     * @param uuid Requested Players UUID.
     * @param material Requested Material.
     * @param modifierCommands New Primary Command.
     */
    public void updateModifierCommandsSQL(UUID uuid, String material, List<String> modifierCommands) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = """
            INSERT INTO BrushToolBindings (uuid, material, modifierCommands)
            VALUES (?, ?, ?)
            ON CONFLICT(uuid, material)
            DO UPDATE SET modifierCommands = excluded.modifierCommands;
            """;
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, material.toUpperCase());
                sqlRequest.setString(3, gson.toJson(modifierCommands != null ? modifierCommands : Collections.emptyList()));
                sqlRequest.executeUpdate();
            } catch (SQLException e) {
                AtomicReference<String> modifierCommandsConcatenated = new AtomicReference<>("");
                if (modifierCommands != null) modifierCommands.forEach(mC -> modifierCommandsConcatenated.set(modifierCommandsConcatenated + mC + ", "));
                modifierCommandsConcatenated.set(modifierCommandsConcatenated.get().replaceAll(",\\s*$", "."));
                TophersTools.getInstance().getSLF4JLogger().error("Failed to update Modifier Commands for UUID: {}, for Material: {}, for Modifier Command:\"{}\" in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), modifierCommandsConcatenated.get(), e);
            }
        });
    }

    /**
     * Removes the requested UUID's Material's Secondary Command, Leaving the Remainder intact, from the SQL File.
     * @param uuid Requested Players UUID.
     * @param material Material Bound.
     */
    public void removeSecondaryCommandSQL(UUID uuid, String material) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String request = "UPDATE BrushToolBindings SET secondaryCommand = NULL WHERE uuid = ? AND material = ?;";
            try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(request)) {
                sqlRequest.setString(1, uuid.toString());
                sqlRequest.setString(2, material.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to delete Secondary Command for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), e);}
        });
    }

    /**
     * Removes the requested UUID's Material's Modifier Commands, Leaving the Remainder intact, from the SQL File.
     * @param uuid Requested Players UUID.
     * @param material Material Bound.
     */
    public void removeModifierCommandsSQL(UUID uuid, String material) {
        TophersTools.getInstance().runSequentialAsync( () -> {
            String sql = "UPDATE BrushToolBindings SET modifierCommands = ? WHERE uuid = ? AND material = ?;";
            try (Connection conn = sqlManager.getConnection(); PreparedStatement sqlRequest = conn.prepareStatement(sql)) {
                sqlRequest.setString(1, gson.toJson(Collections.emptyList()));
                sqlRequest.setString(2, uuid.toString());
                sqlRequest.setString(3, material.toUpperCase());
                sqlRequest.executeUpdate();
            } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to delete Modifier Commands for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), e);}
        });
    }

    /**
     * Gathers the Raw Modifier Commands for a given Material.
     * @param uuid Requested Players UUID.
     * @param material Material Bound.
     * @return Either a list of those Raw Modifiers [ModifierType:ModifierCommand] or an Empty List.
     */
    public List<String> getModifiersRawSQL(UUID uuid, String material) {
        if (uuid == null || material == null) return new ArrayList<>();
        String sql = "SELECT modifierCommands FROM BrushToolBindings WHERE uuid = ? AND material = ?;";
        try (Connection c = sqlManager.getConnection(); PreparedStatement sqlRequest = c.prepareStatement(sql)) {
            sqlRequest.setString(1, uuid.toString());
            sqlRequest.setString(2, material.toUpperCase());
            try (ResultSet resultSet = sqlRequest.executeQuery()) {
                if (resultSet.next()) {
                    String jsonModifierCommands = resultSet.getString("modifierCommands");
                    return (jsonModifierCommands != null && !jsonModifierCommands.isEmpty())
                            ? gson.fromJson(jsonModifierCommands, listType)
                            : new ArrayList<>();
                }
            }
        } catch (SQLException e) {TophersTools.getInstance().getSLF4JLogger().error("Failed to get Raw Modifier Commands for UUID: {}, for Material: {} in FAWE-BrushTools/SQLData/PlayerData.db", uuid.toString(), material.toUpperCase(), e);}
        return new ArrayList<>();
    }

}
