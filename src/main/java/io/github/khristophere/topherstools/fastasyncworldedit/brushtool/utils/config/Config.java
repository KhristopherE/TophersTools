package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config;

import io.github.khristophere.topherstools.data.management.yml.YMLHandler;

public class Config {

    public static YMLHandler configYML;
    public Config(){
        configYML = new YMLHandler("", "FAWE_BT_Utils_config", true);
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
            configYML.getConfig().set("database.FAWEBTUtils.type.isremote", false);
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.host", "ExampleHost.Example");
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.port", 1234);
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.database-name", "database-name");
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.username", "username");
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.password", "password");
            configYML.getConfig().set("database.FAWEBTUtils.remote.settings.use-ssl", false);
            configYML.saveConfig();
        }
    }

    private void gatherDetails(){
        isRemote = configYML.getConfig().getBoolean("database.type.isremote");
        if (isRemote) {
            host = configYML.getConfig().getString("database.FAWEBTUtils.remote.settings.host");
            port = configYML.getConfig().getInt("database.FAWEBTUtils.remote.settings.port");
            databaseName = configYML.getConfig().getString("database.FAWEBTUtils.remote.settings.database-name");
            username = configYML.getConfig().getString("database.FAWEBTUtils.remote.settings.username");
            password = configYML.getConfig().getString("database.FAWEBTUtils.remote.settings.password");
            useSSL = configYML.getConfig().getBoolean("database.FAWEBTUtils.remote.settings.use-ssl");
        }
    }

}
