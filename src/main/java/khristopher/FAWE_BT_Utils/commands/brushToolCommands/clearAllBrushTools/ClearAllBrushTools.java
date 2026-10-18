package khristopher.FAWE_BT_Utils.commands.brushToolCommands.clearAllBrushTools;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.clearAllBrushTools.commandHandler.clearAllBrushToolsCommand;

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
