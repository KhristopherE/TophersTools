package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.brushToolMacro.BrushToolMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.clearAllBrushTools.ClearAllBrushTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.getBrushTool.GetBrushTool;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.returnAllBrushTools.ReturnAllBrushTools;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.shoutBrushTool.ShoutBrushTool;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.ConfigYML;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.BrushToolListener;

public class FAWEBTUtils {

    /**
     *  Enables all FAWEBTUtils Functions.
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    public FAWEBTUtils(){
        new ConfigYML(); // Setup config.
        new GetBrushTool(); // Enables the /getbrush or /gb command & listener.
        new BrushToolMacro(); // Enables the /brushmacro or / bm command.
        new ShoutBrushTool(); // Enables the /shoutbrush or / sb command.
        new ReturnAllBrushTools(); // Enables the /returnallbrushes or /rab command. I prefer /RAT.
        new ClearAllBrushTools(); // Enables the /clearallbrushes or /cab command. I prefer /CAT.
        new BrushToolListener(); //Instantiates & registers the BrushToolListener.
    }

}
