package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.brushToolMacro;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.brushToolMacro.commandHandler.brushToolMacroCommand;

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
