package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.getBrushTool;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.getBrushTool.commandHandler.getBrushToolCommand;

public class GetBrushTool {

    public static getBrushToolCommand getBrushToolCommand;

    /**
     * Instantiates the entirety of the GetBrushTool functionalities.
     */
    public GetBrushTool(){
        //Instantiates & registers the getBrushToolCommand and makes it public static accessible.
        getBrushToolCommand = new getBrushToolCommand();
    }

}
