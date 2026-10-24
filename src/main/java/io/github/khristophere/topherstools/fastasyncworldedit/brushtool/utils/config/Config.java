package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config;

import io.github.khristophere.topherstools.data.management.yml.YMLManager;

import java.util.List;
import java.util.Optional;

public class Config {

    public static YMLManager configYML;
    public Config(){
        configYML = new YMLManager("FAWE-BT-Utils", "config", true);
        checkIfNeedsDefaults();
        gatherDetails();
    }

    public static boolean isRemote;
    public static String host;
    public static int port;
    public static String databaseName;
    public static String username;
    public static String password;
    public static boolean useSSL;

    private void checkIfNeedsDefaults(){
        if (configYML.getConfig().getKeys(false).isEmpty()){
            configYML.getConfig().setComments("SQLData.Remote", List.of("Use Remote Database: False will utilize SQLite locally. True is compatible with MySQL & MariaDB"));
            configYML.getConfig().set("SQLData.Remote.UseRemoteDatabase", false);
            configYML.getConfig().set("SQLData.Remote.Settings.Host", "");
            configYML.getConfig().set("SQLData.Remote.Settings.Port", "");
            configYML.getConfig().set("SQLData.Remote.Settings.Database-Name", "");
            configYML.getConfig().set("SQLData.Remote.Settings.Username", "");
            configYML.getConfig().set("SQLData.Remote.Settings.Password", "");
            configYML.getConfig().set("SQLData.Remote.Settings.Use-SSL", false);
            configYML.saveConfig();
        }
    }

    private void gatherDetails(){
        isRemote = configYML.getConfig().getBoolean("SQLData.Remote.UseRemoteDatabase");
        if (isRemote) {
            host = configYML.getConfig().getString("SQLData.Remote.Settings.Host");
            port = configYML.getConfig().getInt("SQLData.Remote.Settings.Port");
            databaseName = configYML.getConfig().getString("SQLData.Remote.Settings.Database-Name");
            username = configYML.getConfig().getString("SQLData.Remote.Settings.Username");
            password = Optional.ofNullable(configYML.getConfig().getString("SQLData.Remote.Settings.Password")).orElse("");
            useSSL = configYML.getConfig().getBoolean("SQLData.Remote.Settings.Use-SSL");
        }
    }

}
