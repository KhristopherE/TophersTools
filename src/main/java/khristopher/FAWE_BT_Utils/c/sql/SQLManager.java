package khristopher.FAWE_BT_Utils.c.sql;

import khristopher.a.main.TophersTools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
    }

    public SQLManager(String host, int port, String databaseName, String username, String password, boolean useSSL) {
        this.filePath = "";
        this.databaseName = "";
        this.isRemote = true;
        this.username = username;
        this.password = password;
        this.connectionURL = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=%b&autoReconnect=true&allowPublicKeyRetrieval=true",
                host, port, databaseName, useSSL
        );
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public boolean makeRequest(String sqlRequest) {
        try (Connection connection = DriverManager.getConnection(connectionURL);
             Statement statement = connection.createStatement()) {
            statement.execute(sqlRequest);
            return true;
        } catch (SQLException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Database Request Failed", e);
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (isRemote) {
            return java.sql.DriverManager.getConnection(connectionURL, username, password);
        } else {
            return java.sql.DriverManager.getConnection(connectionURL);
        }
    }

    public String getFilePath(){
        return filePath;
    }

    public String getDatabaseName(){
        return databaseName;
    }

}
