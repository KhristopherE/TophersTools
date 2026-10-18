package khristopher.FAWE_BT_Utils.listeners.fawetool.util;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;

public class BrushToolCommandLogic {

    /**
     * The list of known FAWE Brush/Tool Binding Commands.
     */
    private static final Set<String> bindingCommands = Set.of(
            "brush",
            "br",
            "tool",
            "primary",
            "secondary");

    /**
     * The list of known FAWE Brush/Tool Unbinding Commands.
     */
    private static final Set<String> unbindingCommands = Set.of(
            "none",
            "brush none",
            "brush unbind",
            "br none",
            "br unbind",
            "tool none",
            "tool unbind");

    /**
     * This function takes the dirty command and cleans the beginning slashes.
     * @param dirtyCommand The original command beginning with / or //+.
     * @return A clean command without the beginning slashes.
     */
    public static String cleanCommand(String dirtyCommand){
        return dirtyCommand.replaceFirst("^/+", "").toLowerCase();
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is FAWE Brush/Tool Binding, including Primary, Secondary or neither (Can still be binding).
     * <p>
     * IMPORTANT! FAWE treats these commands... interestingly. If the command is Primary OR Secondary binding, the opposing ordinal is unaffected.
     * YET! If the command is NOT Primary NOR Secondary binding, yet still binding, then both Primary & Secondary Bindings are revoked in place of the standard binding.
     * ANOTHER Interesting Scenario, if you utilize a Secondary binding command that is NOT successful in binding, your entire brush is no longer functional...
     * YET, Still considered a brush for some reason. We account for this in the brushToolBinding Class.
     *
     * @param command The command we're checking.
     * @return True if binding, False if Not.
     */
    public static boolean isCommandBinding(String command){
        String cleanCommand = cleanCommand(command);
        String[] splitWords = cleanCommand.split(" ");

        if (splitWords.length == 0) return false;
        String firstWord = splitWords[0];
        return bindingCommands.contains(firstWord);
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is FAWE Brush/Tool Primary Binding.
     * <p>
     * IMPORTANT! FAWE treats these commands... interestingly. If the command is Primary OR Secondary binding, the opposing ordinal is unaffected.
     * YET! If the command is NOT Primary NOR Secondary binding, yet still binding, then both Primary & Secondary Bindings are revoked in place of the standard binding.
     * ANOTHER Interesting Scenario, if you utilize a Secondary binding command that is NOT successful in binding, your entire brush is no longer functional...
     * YET, Still considered a brush for some reason. We account for this in the brushToolBinding Class.
     *
     * @param command The command we're checking.
     * @return True if Primary binding, False if Not.
     */
    public static boolean isCommandPrimaryBinding(String command) {
        return isCommandOrdinalBinding(command, "primary");
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is FAWE Brush/Tool Secondary Binding.
     * <p>
     * IMPORTANT! FAWE treats these commands... interestingly. If the command is Primary OR Secondary binding, the opposing ordinal is unaffected.
     * YET! If the command is NOT Primary NOR Secondary binding, yet still binding, then both Primary & Secondary Bindings are revoked in place of the standard binding.
     * ANOTHER Interesting Scenario, if you utilize a Secondary binding command that is NOT successful in binding, your entire brush is no longer functional...
     * YET, Still considered a brush for some reason. We account for this in the brushToolBinding Class.
     *
     * @param command The command we're checking.
     * @return True if Secondary binding, False if Not.
     */
    public static boolean isCommandSecondaryBinding(String command) {
        return isCommandOrdinalBinding(command, "secondary");
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is a FAWE Brush/Tool Matching Ordinal Binding to the Ordinal Provided.
     * @param command The command we're checking.
     * @return True if Matching Ordinal binding, False if Not.
     */
    private static boolean isCommandOrdinalBinding(String command, String ordinal){
        String cleanCommand = cleanCommand(command);
        String[] splitWords = cleanCommand.split(" ");
        if (splitWords.length == 0) return false;
        String firstWord = splitWords[0];
        if (!bindingCommands.contains(firstWord)) return false;
        if (firstWord.equalsIgnoreCase(ordinal)) return true;
        // We're checking both the first & second word as the commands can be utilized as such //(ordinal) sphere 3, or //brush (ordinal) sphere 3, or //brush sphere 3.
        if (splitWords.length == 1) return false;
        String secondWord = splitWords[1];
        return secondWord.equalsIgnoreCase(ordinal);
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is FAWE Brush/Tool Secondary Binding.
     * @param command The command we're checking.
     * @return True if unbinding, False if Not.
     */
    public static boolean isCommandUnbinding(String command) {
        String cleanCommand = cleanCommand(command);
        return unbindingCommands.contains(cleanCommand);
    }

    /**
     * Checks if the command provided no matter dirty or clean if it is a FAWE Brush/Tool Modifier Command.
     * Filters through the Enums of BrushToolModifier, each standard command & it's aliases.
     * @param command The command we're checking.
     * @return True if unbinding, False if Not.
     */
    public static boolean isCommandModifier(String command) {
        String cleanCommand = cleanCommand(command);
        String[] splitWords = cleanCommand.split(" ");

        if (splitWords.length == 0) return false;
        String firstWord = splitWords[0];
        BrushToolModifier modifier = BrushToolModifier.getToolModifier(firstWord);
        if (modifier != null) return true;

        // We're checking both the first & second word as the commands can be utilized as such /size sphere 3, or //brush size 3

        if (splitWords.length == 1) return false;
        String secondWord = splitWords[1];
        modifier = BrushToolModifier.getToolModifier(secondWord);
        return modifier != null;
    }

    /**
     * This function takes the command provided no matter dirty or clean, & strips the proceeding binding command //brush or any other variants.
     * @param command The original command.
     * @return A clean command without the beginning slashes & preceding binding portion.
     */
    public static String stripPrecedingBindingCommands(String command){
        if (!isCommandBinding(command)) return command;

        String cleanCommand = cleanCommand(command);
        String[] splitWords = cleanCommand.split(" ");

        if (splitWords.length <= 1) return command;

        // Rejoins this string/command, skipping the first word / instance (being the preceding binding command).
        return String.join(" ", Arrays.copyOfRange(splitWords, 1, splitWords.length));
    }

    /**
     * Takes an assumed Modifier Command and strips the preceding Binding Command if present, e.g. //brush size 3, removes //brush.
     * OR if it's a Non-Preceding Binding Command e.g. /size 3. only cleans the preceding slashes. Then identifies the matching BrushToolModifier if there is one.
     * @param command The command we're checking.
     * @return The matching BrushToolModifier if there is one, else Null.
     */
    @Nullable
    public static BrushToolModifier getStandardizedModifier(String command){
        command = cleanCommand(stripPrecedingBindingCommands(command));
        String[] splitWords = command.split(" ");
        if (splitWords.length >= 1){
            String firstWord = splitWords[0];
            return BrushToolModifier.getToolModifier(firstWord);
        }
        return null;
    }

}
