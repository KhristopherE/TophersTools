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

package io.github.khristophere.topherstools.data.management.yml;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Manages custom YAML configuration files within the plugin's data folder.
 */
public class YMLManager {

    private final String fileName;

    private final File file;
    private FileConfiguration config;
    private boolean hasBeenSetup = false;

    /**
     * Constructs a new YMLManager instance.
     *
     * @param filePath     the subdirectory path relative to the plugins data folder (leading/trailing (back)slashes are normalized), if empty places in data folder directly.
     * @param fileName     the name of the configuration file (appends ".yml" if missing).
     * @param instantSetup whether to immediately create the file and load its configuration.
     */
    public YMLManager(@NotNull String filePath, @NotNull String fileName, boolean instantSetup) {
        this.fileName = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";
        filePath = filePath.replace('\\', '/').replaceAll("^/+|/+$", "");

        var tophersTools = TophersTools.getInstance();
        File parentFolder = filePath.isEmpty()
                ? tophersTools.getDataFolder()
                : new File(tophersTools.getDataFolder(), filePath);
        this.file = new File(parentFolder, this.fileName);

        if (instantSetup) setup();
    }

    /**
     * Sets up parent directories if necessary, creates the file if it does not exist,
     * and loads the YAML configuration into memory.
     *
     * @return true if the file was successfully setup and loaded, false otherwise.
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
            hasBeenSetup = true;
            return true;
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not create or initialize YAML file {} at {}", this.fileName, file.getAbsolutePath(), e);
            hasBeenSetup = false;
            return false;
        }
    }

    /**
     * Loads or reloads the configuration from the file on disk.
     */
    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Checks if the configuration file already exists on disk.
     *
     * @return true if the file exists, false otherwise.
     */
    public boolean fileExists() {
        return file.exists();
    }

    /**
     * Attempts to delete the configuration file from disk.
     *
     * @return true if the file was successfully deleted or did not exist, false otherwise.
     */
    public boolean deleteFile() {
        if (!file.exists()) return true;
        return file.delete();
    }

    /**
     * Retrieves the FileConfiguration instance.
     *
     * @return the FileConfiguration representation of this YAML file.
     */
    @NotNull
    public FileConfiguration getConfig() {
        if (!hasBeenSetup) setup();
        return config;
    }

    /**
     * Attempts to save the current FileConfiguration state back to the file on disk.
     *
     * @return true if the file was successfully saved, false otherwise.
     */
    public boolean saveConfig() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            TophersTools.getInstance().getSLF4JLogger().error("Could not save YAML file {} at {}", this.fileName, file.getAbsolutePath(), e);
            return false;
        }
    }

}
