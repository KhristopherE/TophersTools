package khristopher.FAWE_BT_Utils.listeners.fawetool.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//NOTE: Details on the below commands and aliases are in > com.sk89q.worldedit.command.ToolUtilCommands
public enum BrushToolModifier {
    MASK("mask", null),
    MATERIAL("material", List.of("pattern", "mat")),
    RANGE("range", null),
    SIZE("size", null),
    TRACEMASK("tracemask", List.of("tarmask", "targetmask", "tm")),
    TARGET("target", List.of("tar")),
    TARGETOFFSET("targetoffset", List.of("to")),
    SCROLL("scroll", null),
    SOURCEMASK("sourcemask", List.of("smask")),
    TRANSFORM("transform", null);

    private static final Map<String, BrushToolModifier> mapOfAliases = new HashMap<>();
    private static final Map<String, BrushToolModifier> mapOfStandardModifierCommand = new HashMap<>();

    /*
      On initialization runs a for loop for every BrushToolModifier and if the aliases is not equal to null, for every string alias place them in
      a map with their associated BrushToolModifier to later be accessed to search by alias. The same is performed for the Standard Modifier Command.
     */
    static {
        for (BrushToolModifier modifier : values()) {
            if (modifier.aliases != null) {
                modifier.aliases.forEach(alias -> mapOfAliases.put(alias, modifier));
            }
            if (modifier.standardModifierCommand != null) {
                mapOfStandardModifierCommand.put(modifier.standardModifierCommand, modifier);
            }
        }
    }

    private final String standardModifierCommand;
    private final List<String> aliases;

    /**
     * Instantiates the Enumerator.
     * @param standardModifierCommand The standardized command for this modifier.
     * @param aliases Either null for no aliases, or a List of String(s) of the aliases.
     */
    BrushToolModifier(String standardModifierCommand, List<String> aliases) {
        this.standardModifierCommand = standardModifierCommand;
        this.aliases = aliases;
    }

    /**
     * Returns the standardized command for this modifier.
     * @return A string command for this modifier without the preceding slash/.
     */
    public String getStandardCommand() {
        return this.standardModifierCommand;
    }

    /**
     * Returns a list of aliases for this command. If present.
     * @return Either null for no aliases, or a list of string(s) with the aliases.
     */
    @Nullable
    public List<String> getAliases() {
        return this.aliases;
    }

    /**
     * Scans existing ToolModifiers by both Standard & Alias Commands, returning a matching BrushToolModifier, if located.
     * @param standardOrAliasCommand The command we're locating a matching BrushToolModifier for.
     * @return Either null for no modifier identified. Or a matching BrushToolModifier.
     */
    @Nullable
    public static BrushToolModifier getToolModifier(String standardOrAliasCommand) {
        if (standardOrAliasCommand == null || standardOrAliasCommand.isEmpty()) return null;
        BrushToolModifier lookup = getToolModifierAlias(standardOrAliasCommand);
        if (lookup != null) return lookup;
        return getToolModifierByStandard(standardOrAliasCommand);
    }

    /**
     * Scans existing ToolModifiers by only Standard Commands, returning a matching BrushToolModifier, if located.
     * @param standardCommand The command we're locating a matching BrushToolModifier for.
     * @return Either null for no modifier identified. Or a matching BrushToolModifier.
     */
    @Nullable
    public static BrushToolModifier getToolModifierByStandard(String standardCommand) {
        if (standardCommand == null || standardCommand.isEmpty()) return null;
        return mapOfStandardModifierCommand.get(standardCommand.toLowerCase());
    }

    /**
     * Scans existing ToolModifiers by only Alias Commands, returning a matching BrushToolModifier, if located.
     * @param aliasCommand The command we're locating a matching BrushToolModifier for.
     * @return Either null for no modifier identified. Or a matching BrushToolModifier.
     */
    @Nullable
    public static BrushToolModifier getToolModifierAlias(String aliasCommand) {
        if (aliasCommand == null || aliasCommand.isEmpty()) return null;
        return mapOfAliases.get(aliasCommand.toLowerCase());
    }

    /**
     * Takes the List of Strings and removes the BrushToolModifier Standard Name and Standardized Split Value from the beginning of the string.
     * Example... "SIZE:/Size 3" is modified to "/Size 3", the String is split at and only at the first instance of a ":" which is
     * expected to always be formatted as "(BrushToolModifier Name):(Command)" At the time of writing ":" was the designated Split Value.
     * @param toolModifiers The list of Raw Tool Modifiers.
     * @return A clean list of strings by the above description.
     */
    public static List<String> removeSplitValuesInBrushToolModifiers(List<String> toolModifiers){
        List<String> adjustedModifiers = new ArrayList<>();
        for (String modifierInReview : toolModifiers) {
            String[] removedSplitValueModifier = modifierInReview.split(modifierSplitValue(), 2);
            if (removedSplitValueModifier.length >= 2){
                adjustedModifiers.add(removedSplitValueModifier[1]);
            }
        }
        return adjustedModifiers;
    }

    /**
     * Provides the standardized split value utilized for Tool Modifiers.
     * @return The standardized split value utilized for Tool Modifiers.
     */
    public static String modifierSplitValue(){
        return ":";
    }

}
