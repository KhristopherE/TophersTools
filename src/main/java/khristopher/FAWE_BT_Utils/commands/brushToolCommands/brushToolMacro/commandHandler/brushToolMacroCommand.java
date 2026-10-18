package khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.commandHandler;

import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.b.utils.Utils;
import khristopher.FAWE_BT_Utils.commands.brushToolCommands.brushToolMacro.utils.DKInstantMacro.DKInstantMacro;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
import khristopher.FAWE_BT_Utils.functionalities.loadout.brushtoolmacro.BrushToolLoadoutMacro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class brushToolMacroCommand {

    /**
     * Instantiates the BrushToolMacro Command.
     */
    public brushToolMacroCommand(){
        Utils.registerClassWithCommandManager(this);
    }

    /**
     * Allows the player to save a current inventory from slots 0-35 with data from the current Brush/Tool Data base into a macro form.
     * This macro form is widely sharable, if it defers to any local files like schematics those would need shared as well.
     * @param sender The command sender.
     * @param macroName The name of the Macro to save.
     * @param override Override is in the event the file name already exists.
     */
    @Command("brushmacro save <macroname>")
    @Command("bm save <macroname>")
    @Command("toolmacro save <macroname>")
    @Command("tm save <macroname>")
    @Permission("topherstools.bt.macro.save")
    private void brushToolMacroSave(Source sender, @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName, @Flag(value = "o") boolean override) {
        if (sender.source() instanceof Player player) {
            String finalMacroName = sanitizeInput(macroName);
            BrushToolLoadoutMacro brushToolLoadoutMacro = new BrushToolLoadoutMacro(finalMacroName);
            if (brushToolLoadoutMacro.alreadyExists() && !override){
                player.sendMessage(PlayerMessages.BrushTool_Macro_Already_Exists_Requesting_Override.getMessageMacroName(finalMacroName));
                return;
            }
            player.sendMessage(PlayerMessages.BrushTool_Macro_Saved.getMessageMacroName(finalMacroName));
            brushToolLoadoutMacro.saveToFile(player);
            BrushToolManager.addBrushToolLoadoutMacroToMem(finalMacroName);
        }
    }

    /**
     * Allows the player to load a saved BrushToolMacro to their inventory.
     * This macro form is widely sharable, if it defers to any local files like schematics those would need shared as well.
     * @param sender The command sender.
     * @param macroName The name of the Macro to save.
     */
    @Command("brushmacro load <macroname>")
    @Command("bm load <macroname>")
    @Command("toolmacro load <macroname>")
    @Command("tm load <macroname>")
    @Permission("topherstools.bt.macro.load")
    private void brushToolMacroLoad(Source sender, @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName) {
        if (sender.source() instanceof Player player) {
            String finalMacroName = sanitizeInput(macroName);
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                BrushToolLoadoutMacro brushToolLoadoutMacro = new BrushToolLoadoutMacro(finalMacroName);
                if (!brushToolLoadoutMacro.alreadyExists()){
                    player.sendMessage(PlayerMessages.BrushTool_Macro_Not_Exist.getMessageMacroName(finalMacroName));
                    return;
                }
                brushToolLoadoutMacro.loadFromFile();
                if (brushToolLoadoutMacro.isBrushToolLoadoutsEmpty()){
                    player.sendMessage(PlayerMessages.Warning_BrushTool_Macro_Is_Empty.getMessageMacroName(finalMacroName));
                    return;
                }
                Bukkit.getScheduler().runTask(TophersTools.getInstance(), () -> brushToolLoadoutMacro.applyTo(player.getUniqueId()));
            });
        }
    }

    /**
     * Allows the player to delete a saved BrushToolMacro from the Files. This does require confirmation with a 15 second confirmation window.
     * @param sender The command sender.
     * @param macroName The name of the Macro to delete.
     * @param confirmation If "confirmation" is included in the command as the last argument.
     */
    @Command("brushmacro delete <macroname> [confirm]")
    @Command("bm delete <macroname> [confirm]")
    @Command("toolmacro delete <macroname> [confirm]")
    @Command("tm delete <macroname> [confirm]")
    @Permission("topherstools.bt.macro.delete")
    private void brushToolMacroDelete(Source sender, @Argument(value = "macroname", suggestions = "suggestedMacros") String macroName, @Argument(value = "confirm") String confirmation) {
        if (sender.source() instanceof Player player) {
            String finalMacroName = sanitizeInput(macroName);
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                BrushToolLoadoutMacro brushToolLoadoutMacro = new BrushToolLoadoutMacro(finalMacroName);
                if (!brushToolLoadoutMacro.alreadyExists()){
                    player.sendMessage(PlayerMessages.BrushTool_Macro_Not_Exist.getMessageMacroName(finalMacroName));
                } else {
                    if (confirmation != null && confirmation.equalsIgnoreCase("Confirm")){
                        if (pendingDeletions.containsKey(player.getUniqueId()) && pendingDeletions.get(player.getUniqueId()).macroName.equals(finalMacroName)){
                            if (!pendingDeletions.get(player.getUniqueId()).isExpired()){
                                if (brushToolLoadoutMacro.deleteBrushToolLoadoutMacro()){
                                    player.sendMessage(PlayerMessages.BrushTool_Macro_Deleted.getMessageMacroName(finalMacroName));
                                    BrushToolManager.removeBrushToolLoadoutMacroFromMem(finalMacroName);
                                } else {
                                    player.sendMessage(PlayerMessages.BrushTool_Macro_Deletion_Failed.getMessageMacroName(finalMacroName));
                                }
                            } else {
                                player.sendMessage(PlayerMessages.BrushTool_Macro_Delete_Confirmation_Window_Expired.getMessageMacroName(finalMacroName));
                            }
                        } else {
                            player.sendMessage(PlayerMessages.BrushTool_Macro_Delete_Requesting_Confirmation.getMessageMacroName(finalMacroName));
                            pendingDeletions.put(player.getUniqueId(), new PendingDeletion(finalMacroName));
                        }
                    } else {
                        player.sendMessage(PlayerMessages.BrushTool_Macro_Delete_Requesting_Confirmation.getMessageMacroName(finalMacroName));
                        pendingDeletions.put(player.getUniqueId(), new PendingDeletion(finalMacroName));
                    }
                }
            });
        }
    }

    private final Map<UUID, PendingDeletion> pendingDeletions = new ConcurrentHashMap<>();
    private static class PendingDeletion {
        private final String macroName;
        private final long timestamp;
        public PendingDeletion(String macroName) {
            this.macroName = macroName;
            this.timestamp = System.currentTimeMillis();
        }
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 15000;
        }
    }

    /**
     * Provides the sender provided they're a player, a help menu for the Brush Macro Commands.
     * Unless they're KhristopherE or TheWither_MAN, then a set of premade brush/tools (This was the original purpose / functionality of the plugin but has since expanded).
     * @param sender The command sender.
     */
    @Command("brushmacro")
    @Command("bm")
    @Command("toolmacro")
    @Command("tm")
    @Permission("topherstools.bt.macro")
    private void brushToolMacro(Source sender) {
        if (sender.source() instanceof Player player) {
            if (player.getUniqueId().equals(UUID.fromString("9e278e4b-5296-4313-8e05-feedaeaaa050")) || player.getUniqueId().equals(UUID.fromString("fbbfa97b-d2e2-47e1-8987-15e83d8e005c"))){
                DKInstantMacro.DKInstantMacro(player);
            } else {
                player.sendMessage(PlayerMessages.BrushTool_Macro_Help.getMessage());
            }
        }
    }

    /**
     * Provides a list of suggested brushToolMacros for a command.
     * @return A matching list of brushToolMacros.
     */
    @Suggestions("suggestedMacros")
    public List<String> getSuggestedBrushToolMacros(CommandContext<Source> context, String input) {
        return BrushToolManager.brushToolLoadoutMacrosAvailable;
    }

    public static String sanitizeInput(String input){
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

}
