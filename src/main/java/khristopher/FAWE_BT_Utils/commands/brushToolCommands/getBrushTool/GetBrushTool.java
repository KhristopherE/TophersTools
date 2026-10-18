package khristopher.FAWE_BT_Utils.commands.brushToolCommands.getBrushTool;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.getBrushTool.commandHandler.getBrushToolCommand;

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
