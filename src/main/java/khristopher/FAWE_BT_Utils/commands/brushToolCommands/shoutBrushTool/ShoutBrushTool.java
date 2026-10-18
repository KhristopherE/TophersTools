package khristopher.FAWE_BT_Utils.commands.brushToolCommands.shoutBrushTool;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.shoutBrushTool.commandHandler.shoutBrushToolCommand;

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
