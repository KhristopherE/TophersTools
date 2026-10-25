package io.github.khristophere.topherstools.data.management.yml;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Manages custom YAML configuration files within the plugin's data folder.
 * Handles directory structuring, file creation, saving, reloading, and deletion.
 */
public class YMLManager {

    private final JavaPlugin tophersTools = TophersTools.getInstance();
    private final String fileName;

    private final File file;
    private FileConfiguration config;

    /**
     * Constructs a new YMLManager instance.
     *
     * @param filePath     The subdirectory path relative to the plugin data folder.
     *                     Leading/trailing slashes and backslashes are automatically normalized.
     * @param fileName     The name of the configuration file (automatically appends ".yml" if missing).
     * @param instantSetup Whether to immediately create the file and load its configuration.
     */
    public YMLManager(String filePath, String fileName, boolean instantSetup) {
        this.fileName = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";
        filePath = filePath.replace('\\', '/').replaceAll("^/+|/+$", "");

        File parentFolder = filePath.isEmpty()
                ? tophersTools.getDataFolder()
                : new File(tophersTools.getDataFolder(), filePath);
        this.file = new File(parentFolder, this.fileName);

        if (instantSetup)
            setup();
    }

    /**
     * Sets up parent directories, creates the file if it does not exist,
     * and loads the YAML configuration into memory.
     */
    public boolean setup() {
        try {
            File parentFolder = file.getParentFile();
            if (parentFolder != null && !parentFolder.exists())
                //noinspection ResultOfMethodCallIgnored
                parentFolder.mkdirs();
            if (!file.exists())
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            reloadConfig();
            return true;
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not create or initialize YAML file {} at {}", this.fileName, file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Loads / Reloads the configuration from the file on disk.
     */
    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Checks if the configuration file already exists on disk.
     *
     * @return True if the file exists, false otherwise.
     */
    public boolean fileExists() {
        return file.exists();
    }

    /**
     * Attempts to delete the configuration file from disk.
     *
     * @return True if the file was successfully deleted or did not exist, false otherwise.
     */
    public boolean deleteFile() {
        if (!file.exists()) return true;
        return file.delete();
    }

    /**
     * Retrieves the FileConfiguration instance.
     *
     * @return The FileConfiguration representation of this YAML file.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Attempts to save the current FileConfiguration state back to the file on disk.
     */
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not save YAML file {} at {}", this.fileName, file.getAbsolutePath(), e);
        }
    }

}
