package io.github.khristophere.topherstools.data.management.yml;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class YMLHandler {

    private final JavaPlugin tophersTools = TophersTools.getInstance();
    private final String filePath;
    private final String fileName;

    private File file;
    private FileConfiguration config;

    /**
     * Creates a new YML at the given destination with the given file name, within the TophersTools Directory.
     * @param filePath The path after the TophersTools directed (automatically added). This does NOT need (doesn't hurt either way) to begin with '/' nor end with '/' this is automatically handled.
     * @param fileName The name of the given YML file. This does NOT need (doesn't hurt either way) to end with ".yml" it's automatically handled.
     */
    public YMLHandler(String filePath, String fileName, boolean instantSetup) {
        //If the file name does NOT end with ".yml" add ".yml" to the end.
        this.fileName = fileName.toLowerCase().endsWith(".yml") ? fileName : fileName + ".yml";

        //Replaces all "\" with "/" as "\" don't work on Linux & Mac
        //Replaces all leading & trailing "/" with nothing.
        filePath = filePath
                .replace('\\', '/')
                .replaceAll("^/+|/+$", "");

        //If the path is empty no changes are made, if the path contains anything 'prepend' a "/"
        this.filePath = filePath.isEmpty() ? "" : "/" + filePath;

        if (instantSetup) setup();
    }

    /**
     * Set's up the parent folders & creates the file. Then loads the config.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // Suppresses suggestion to utilize boolean response on call "parentFolder.mkdirs();"
    public void setup() {
        File parentFolder = new File(tophersTools.getDataFolder().getAbsolutePath() + filePath);
        parentFolder.mkdirs();
        file = new File(parentFolder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                tophersTools.getLogger().severe("Could not create YML file " + fileName + " at " + file.getPath());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Checks if the File already exists.
     * @return boolean.
     */
    public boolean doesFileAlreadyExist(){
        return new File(new File(tophersTools.getDataFolder().getAbsolutePath() + filePath), fileName).exists();
    }

    /**
     * Attempts to delete the YML File.
     * @return boolean.
     */
    public boolean attemptFileDeletion(){
        setup();
        if (!file.exists()) return true;
        return file.delete();
    }

    /**
     * Returns this YML Config File to make modifications to, ensure we utilize saveConfig() after changes are made.
     * @return FileConfiguration in YML format.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Attempts to save the YML Config File.
     */
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            // Logs to console the given error along with the File Name & location.
            tophersTools.getLogger().severe("Could not save/update YML File " + fileName + " at " + tophersTools.getDataFolder() + filePath);
        }
    }

}
