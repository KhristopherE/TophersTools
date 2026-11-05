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

package io.github.khristophere.topherstools.data.management.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.khristophere.topherstools.TophersTools;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQL database connections and initialization, supporting both local SQLite and remote MySQL/MariaDB servers.
 */
public class SQLManager {

    private final String filePath;
    private final String databaseName;
    private final boolean isRemote;
    private final HikariDataSource dataSource;

    /**
     * Constructs a local SQLite database manager with a HikariCP connection pool.
     *
     * @param filePath          the relative folder path within TophersTools data folder.
     * @param databaseName      the name of the SQLite database file.
     * @param maxConnectionPool the max number of connections to be available.
     */
    public SQLManager(@NotNull String filePath, @NotNull String databaseName, int maxConnectionPool) {
        this.databaseName = databaseName.toLowerCase().endsWith(".db") ? databaseName : databaseName + ".db";

        String normalizedPath = filePath.replace('\\', '/').replaceAll("^/+|/+$", "");
        Path pluginFolder = TophersTools.getInstance().getDataFolder().toPath();
        Path resolvedPath = normalizedPath.isEmpty() ? pluginFolder : pluginFolder.resolve(normalizedPath);

        this.filePath = resolvedPath.toAbsolutePath().toString();

        try {
            Files.createDirectories(resolvedPath);
            Path dbFilePath = resolvedPath.resolve(this.databaseName);
            if (Files.notExists(dbFilePath)) {
                Files.createFile(dbFilePath);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFilePath.toAbsolutePath());
            config.setMaximumPoolSize(maxConnectionPool);
            config.setPoolName("TophersTools-SQLite-" + databaseName + "-Pool");
            config.setMinimumIdle((maxConnectionPool / 2) + 1);

            this.dataSource = new HikariDataSource(config);
            this.isRemote = false;
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not create SQLite database file {} at {}", this.databaseName, resolvedPath, e);
            throw new RuntimeException("Failed to initialize SQLite database: " + this.databaseName
                    + ". TophersTools cannot run without this, please report this error.", e);
        }
        enableWAL();
    }

    /**
     * Constructs a remote MySQL/MariaDB database manager with a HikariCP connection pool.
     *
     * @param host              the database server host address.
     * @param port              the database server port.
     * @param databaseName      the name of the remote database schema.
     * @param username          the authentication username.
     * @param password          the authentication password.
     * @param useSSL            whether to use SSL encryption for the connection.
     * @param maxConnectionPool the max number of connections to be available.
     */
    public SQLManager(@NotNull String host, int port, @NotNull String databaseName, @Nullable String username, @Nullable String password, boolean useSSL, int maxConnectionPool) {
        this.filePath = "";
        this.databaseName = databaseName;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true",
                host, port, databaseName, useSSL
        ));
        config.setUsername(username != null ? username : "");
        config.setPassword(password != null ? password : "");
        config.setMaximumPoolSize(maxConnectionPool);
        config.setPoolName("TophersTools-MySQL-" + databaseName + "-Pool");
        config.setMinimumIdle((maxConnectionPool / 2) + 1);

        this.dataSource = new HikariDataSource(config);
        this.isRemote = true;
    }

    /**
     * Borrows an active connection from the HikariCP connection pool.
     *
     * @return a database Connection.
     * @throws SQLException if a connection cannot be obtained.
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    /**
     * Enables Write-Ahead Logging (WAL) for SQLite.
     */
    private void enableWAL() {
        if (isRemote) return;
        try (Connection c = getConnection();
             Statement s = c.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to set SQLite database Journal Mode to Write-Ahead Logging (WAL)", e);
        }
    }

    /**
     * Shuts down the connection pool.
     */
    public void closeConnectionPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Checks if this SQLManager is remote or local.
     *
     * @return true if this is remote, false otherwise.
     */
    public boolean isRemote() {
        return isRemote;
    }

    /**
     * Gets the absolute file path where the database is stored, if the database is local.
     *
     * @return the absolute file path string. Or "" if remote.
     */
    @NotNull
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the name of the database file or remote schema.
     *
     * @return the database name string.
     */
    @NotNull
    public String getDatabaseName() {
        return databaseName;
    }

}
