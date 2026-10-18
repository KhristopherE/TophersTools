package khristopher.a.main;

import khristopher.FAWE_BT_Utils.FAWEBTUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class TophersTools extends JavaPlugin {

    private static TophersTools tophersToolsInstance;

    /**
     *  This is where the plugin starts, when the server calls to enable the plugin it calls to enable all the below functionalities.
     */
    @Override
    public void onEnable() {
        tophersToolsInstance = this; // Sets up accessible plugin instance.
        new FAWEBTUtils();
    }

    /**
     * Provides the current instance of TophersTools
     * @return TophersTools Plugin Instance.
     */
    public static TophersTools getInstance(){
        return tophersToolsInstance;
    }

}


