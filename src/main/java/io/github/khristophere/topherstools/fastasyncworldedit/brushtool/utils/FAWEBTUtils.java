package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall.BTClearAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget.BTGet;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.BTMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall.BTReturnAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout.BTShout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.reloadconfig.ReloadConfig;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.BrushToolListener;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql.BrushToolSQL;

public class FAWEBTUtils {

    /**
     *  Enables all FAWEBTUtils Functions.
     */
    public FAWEBTUtils(){
        new Config(); // Setup config.
        new ReloadConfig(); // Enables the /topherstools reload btutils command.
        new BTGet(); // Enables the /getbrush or /gb command & listener.
        new BTMacro(); // Enables the /brushmacro or / bm command.
        new BTShout(); // Enables the /shoutbrush or / sb command.
        new BTReturnAll(); // Enables the /returnallbrushes or /rab command. I prefer /RAT.
        new BTClearAll(); // Enables the /clearallbrushes or /cab command. I prefer /CAT.
        new BrushToolListener(); //Instantiates & registers the BrushToolListener.
    }

    public static void disable(){
        BrushToolSQL.sqlManager.closeConnectionPool();
    }

}
