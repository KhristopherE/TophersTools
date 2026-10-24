package io.github.khristophere.topherstools.data.management.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.khristophere.topherstools.TophersTools;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connections and initialization, supporting both local SQLite
 * file databases and remote MySQL/MariaDB database servers.
 */
public class SQLManager {

    private final String filePath;
    private final String databaseName;
    private final boolean isRemote;
    private final HikariDataSource dataSource;

    /**
     * Constructs a local SQLite database manager with a HikariCP connection pool.
     * automatically creating necessary parent directories, the database file,
     * and enabling Write-Ahead Logging (WAL).
     *
     * @param filePath     the relative folder path within the plugin's data folder
     * @param databaseName the name of the SQLite database file (e.g., "PlayerBindings")
     */
    public SQLManager(String filePath, String databaseName) {
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
            config.setMaximumPoolSize(10);
            config.setPoolName("TophersTools-SQLite-"+databaseName+"-Pool");

            this.dataSource = new HikariDataSource(config);
            this.isRemote = false;
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not create SQLite database file {} at {}", this.databaseName, resolvedPath, e);
            throw new RuntimeException("Failed to initialize SQLite database: " + this.databaseName, e);
        }

        enableWAL();
    }

    /**
     * Constructs a remote MySQL/MariaDB database manager with a HikariCP connection pool.
     *
     * @param host         the database server host address
     * @param port         the database server port
     * @param databaseName the name of the remote database schema
     * @param username     the authentication username
     * @param password     the authentication password
     * @param useSSL       whether to use SSL encryption for the connection
     */
    public SQLManager(String host, int port, String databaseName, @Nullable String username, @Nullable String password, boolean useSSL) {
        this.filePath = "";
        this.databaseName = databaseName;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true",
                host, port, databaseName, useSSL
        ));
        config.setUsername(username != null ? username : "");
        config.setPassword(password != null ? password : "");
        config.setMaximumPoolSize(10);
        config.setPoolName("TophersTools-MySQL-"+databaseName+"-Pool");

        this.dataSource = new HikariDataSource(config);
        this.isRemote = true;
    }

    /**
     * Borrows an active connection from the HikariCP connection pool.
     *
     * @return a database {@link Connection}
     * @throws SQLException if a connection cannot be obtained
     */
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
            TophersTools.getInstance().getSLF4JLogger().error("Failed to set SQLite database Journal Mode to WAL", e);
        }
    }

    /**
     * Gracefully shuts down the connection pool when the plugin disables.
     */
    public void closeConnectionPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Gets the absolute file path where the database is stored, if the database is local.
     *
     * @return the absolute file path string
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the name of the database file or remote schema.
     *
     * @return the database name string
     */
    public String getDatabaseName() {
        return databaseName;
    }

}
