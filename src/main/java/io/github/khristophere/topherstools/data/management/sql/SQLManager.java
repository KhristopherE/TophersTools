package io.github.khristophere.topherstools.data.management.sql;

import io.github.khristophere.topherstools.TophersTools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLManager {

    private final String connectionURL;
    private final String filePath;
    private final String databaseName;
    private String username = "";
    private String password = "";
    private boolean isRemote = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SQLManager(String filePath, String databaseName) {
        //If the file name does NOT end with ".db" add ".db" to the end.
        this.databaseName = databaseName.toLowerCase().endsWith(".db") ? databaseName : databaseName + ".db";

        //Replaces all "\" with "/" as "\" don't work on Linux & Mac
        //Replaces all leading & trailing "/" with nothing.
        filePath = filePath
                .replace('\\', '/')
                .replaceAll("^/+|/+$", "");

        //If the path is empty no changes are made, if the path contains anything 'prepend' a "/"
        filePath = filePath.isEmpty() ? "" : "/" + filePath;
        this.filePath = TophersTools.getInstance().getDataFolder().getAbsolutePath() + filePath;

        File parentFolder = new File((this.filePath));
        parentFolder.mkdirs();
        File dbFile = new File(this.filePath, this.databaseName);
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                TophersTools.getInstance().getLogger().severe("Could not create Database / DB file " + this.databaseName + " at " + dbFile.getPath());
            }
        }
        this.connectionURL = "jdbc:sqlite:" +  dbFile;
        enableWAL();
    }

    public SQLManager(String host, int port, String databaseName, String username, String password, boolean useSSL) {
        this.filePath = "";
        this.databaseName = "";
        this.isRemote = true;
        this.username = username;
        this.password = password;
        this.connectionURL = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true",
                host, port, databaseName, useSSL
        );
    }

    public Connection getConnection() throws SQLException {
        if (isRemote) {
            return java.sql.DriverManager.getConnection(connectionURL, username, password);
        } else {
            return java.sql.DriverManager.getConnection(connectionURL);
        }
    }

    private void enableWAL(){
        try (Statement statement = getConnection().createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Failed to set Database Journal Mode to WAL", e);
        }
    }

    public String getFilePath(){
        return filePath;
    }

    public String getDatabaseName(){
        return databaseName;
    }

}
