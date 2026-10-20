package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.shoutBrushTool;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.shoutBrushTool.commandHandler.shoutBrushToolCommand;

public class ShoutBrushTool {

    public static shoutBrushToolCommand getShoutBrushToolCommand;

    /**
     * Instantiates the entirety of the ShoutBrushTool functionalities.
     */
    public ShoutBrushTool(){
        //Instantiates & registers the shoutBrushToolCommand and makes it public static accessible.
        getShoutBrushToolCommand = new shoutBrushToolCommand();
    }

}
