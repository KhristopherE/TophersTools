package khristopher.FAWE_BT_Utils;

import khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.BrushToolMacro;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.clearAllBrushTools.ClearAllBrushTools;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.getBrushTool.GetBrushTool;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.returnAllBrushTools.ReturnAllBrushTools;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.shoutBrushTool.ShoutBrushTool;
import khristopher.FAWE_BT_Utils.config.ConfigYML;
import khristopher.FAWE_BT_Utils.listeners.fawetool.BrushToolListener;

public class FAWEBTUtils {

    /**
     *  Enables all FAWEBTUtils Functions.
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    public FAWEBTUtils(){
        new ConfigYML(); // Setup config.
        new GetBrushTool(); // Enables the /getbrush or /gb command & listener.
        new BrushToolMacro(); // Enables the /brushmacro or / bm command.
        new ShoutBrushTool(); // Enables the /shoutbrush or / sb command.
        new ReturnAllBrushTools(); // Enables the /returnallbrushes or /rab command. I prefer /RAT.
        new ClearAllBrushTools(); // Enables the /clearallbrushes or /cab command. I prefer /CAT.
        new BrushToolListener(); //Instantiates & registers the BrushToolListener.
    }

}
