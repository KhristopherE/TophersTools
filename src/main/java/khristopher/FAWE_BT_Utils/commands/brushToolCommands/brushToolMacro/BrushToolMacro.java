package khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.commandHandler.brushToolMacroCommand;

public class BrushToolMacro {

    public static brushToolMacroCommand getBrushToolMacroCommand;

    /**
     * Instantiates the entirety of the BrushToolMacro functionalities.
     */
    public BrushToolMacro(){
        //Instantiates & registers the brushToolMacroCommand and makes it public static accessible.
        getBrushToolMacroCommand = new brushToolMacroCommand();
    }

}
