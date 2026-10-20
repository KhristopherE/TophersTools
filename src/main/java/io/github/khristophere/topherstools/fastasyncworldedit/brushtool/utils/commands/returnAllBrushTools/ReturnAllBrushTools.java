package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.returnAllBrushTools;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.returnAllBrushTools.commandHandler.returnAllBrushToolsCommand;

public class ReturnAllBrushTools {

    public static returnAllBrushToolsCommand getReturnAllBrushToolsCommand;

    /**
     * Instantiates the entirety of the ReturnAllBrushTools functionalities.
     */
    public ReturnAllBrushTools(){
        //Instantiates & registers the returnAllBrushToolsCommand and makes it public static accessible.
        getReturnAllBrushToolsCommand = new returnAllBrushToolsCommand();
    }

}
