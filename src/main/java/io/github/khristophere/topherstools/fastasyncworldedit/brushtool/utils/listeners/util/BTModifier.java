/*
 * Copyright (C) 2026 KhristopherE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Details on the below commands and aliases are in found in com.sk89q.worldedit.command.ToolUtilCommands

/**
 * Represents and brush / tool modifier definitions aliases, mappings, and prefix parsing.
 */
public enum BTModifier {

    MASK("mask", null, null),
    MATERIAL("material", List.of("pattern", "mat"), null),
    RANGE("range", null, Type.GLOBAL),
    SIZE("size", null, null),
    TRACEMASK("tracemask", List.of("tarmask", "targetmask", "tm"), Type.GLOBAL),
    TARGET("target", List.of("tar"), Type.GLOBAL),
    TARGETOFFSET("targetoffset", List.of("to"), Type.GLOBAL),
    SCROLL("scroll", null, null),
    SOURCEMASK("sourcemask", List.of("smask"), null),
    TRANSFORM("transform", null, null);

    public static final String MODIFIER_SPLIT_DELIMITER = ":";
    private static final Map<String, BTModifier> ALIAS_MAP = new HashMap<>();
    private static final Map<String, BTModifier> STANDARD_COMMAND_MAP = new HashMap<>();

    static {
        for (BTModifier modifier : values()) {
            if (modifier.aliases != null) modifier.aliases.forEach(alias -> ALIAS_MAP.put(alias, modifier));
            if (modifier.standardModifierCommand != null)
                STANDARD_COMMAND_MAP.put(modifier.standardModifierCommand, modifier);
        }
    }

    private final String standardModifierCommand;
    private final List<String> aliases;
    private final Type modifierType;

    /**
     * Constructs a new BTModifier enum constant.
     *
     * @param standardModifierCommand the standardized command for this modifier.
     * @param aliases                 the list of aliases, or null if none exist.
     * @param modifierType            the categorization of application for a brush / tool modifier to a brush / tool. Null for Dual.
     */
    BTModifier(@NotNull String standardModifierCommand, @Nullable List<String> aliases, @Nullable Type modifierType) {
        this.standardModifierCommand = standardModifierCommand;
        this.aliases = aliases;
        this.modifierType = modifierType;
    }

    /**
     * Scans existing modifiers by both standard and alias commands, returning a matching BTModifier if located.
     *
     * @param standardOrAliasCommand the command we're locating a matching modifier for.
     * @return the matching BTModifier, or null if no modifier is identified.
     */
    @Nullable
    public static BTModifier findToolModifier(@NotNull String standardOrAliasCommand) {
        if (standardOrAliasCommand.isEmpty()) return null;
        BTModifier lookup = findToolModifierByAlias(standardOrAliasCommand);
        if (lookup != null) return lookup;
        return findToolModifierByStandard(standardOrAliasCommand);
    }

    /**
     * Scans existing modifiers by standard commands only, returning a matching BTModifier if located.
     *
     * @param standardCommand the command we're locating a matching modifier for.
     * @return the matching BTModifier, or null if no modifier is identified.
     */
    @Nullable
    public static BTModifier findToolModifierByStandard(@NotNull String standardCommand) {
        if (standardCommand.isEmpty()) return null;
        return STANDARD_COMMAND_MAP.get(standardCommand.toLowerCase());
    }

    /**
     * Scans existing modifiers by alias commands only, returning a matching BTModifier if located.
     *
     * @param aliasCommand the command we're locating a matching modifier for.
     * @return the matching BTModifier, or null if no modifier is identified.
     */
    @Nullable
    public static BTModifier findToolModifierByAlias(@NotNull String aliasCommand) {
        if (aliasCommand.isEmpty()) return null;
        return ALIAS_MAP.get(aliasCommand.toLowerCase());
    }

    /**
     * Takes a list of raw modifier strings and removes the prefix and delimiter from the beginning of each entry.
     *
     * @param toolModifiers the list of raw tool modifiers.
     * @return a clean list of modifier commands with prefixes removed.
     */
    @NotNull
    public static List<String> removePrefixesInBTModifiers(@NotNull List<String> toolModifiers) {
        List<String> adjustedModifiers = new ArrayList<>();
        for (String modifierInReview : toolModifiers) {
            String[] removedSplitValueModifier = modifierInReview.split(MODIFIER_SPLIT_DELIMITER, 2);
            if (removedSplitValueModifier.length >= 2) {
                adjustedModifiers.add(removedSplitValueModifier[1]);
            }
        }
        return adjustedModifiers;
    }

    /**
     * Checks if this is a global modifier.
     *
     * @return true if global, false otherwise.
     */
    public boolean isGlobalModifier() {
        if (this.modifierType == null) return false;
        return this.modifierType.equals(Type.GLOBAL);
    }

    /**
     * Gets this modifier's type.
     *
     * @return the modifier type, or null if Dual.
     */
    @Nullable
    public Type getModifierType() {
        return this.modifierType;
    }

    /**
     * Represents the categorization of application for a brush / tool modifier to a brush / tool.
     * Those associated to null are Dual applicable to Primary & or Secondary.
     */
    public enum Type {
        PRIMARY(),
        SECONDARY(),
        GLOBAL()
    }

}
