package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.clearAllBrushTools;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.clearAllBrushTools.commandHandler.clearAllBrushToolsCommand;

public class ClearAllBrushTools {

    public static clearAllBrushToolsCommand getClearAllBrushToolsCommand;

    /**
     * Instantiates the entirety of the ClearAllBrushTools functionalities.
     */
    public ClearAllBrushTools(){
        //Instantiates & registers the clearAllBrushToolsCommand and makes it public static accessible.
        getClearAllBrushToolsCommand = new clearAllBrushToolsCommand();
    }

}
