package khristopher.FAWE_BT_Utils.commands.brushToolCommands.returnAllBrushTools;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.returnAllBrushTools.commandHandler.returnAllBrushToolsCommand;

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
