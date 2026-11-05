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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.event.platform.CommandEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;

import static io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier.Type.PRIMARY;
import static io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier.Type.SECONDARY;

/**
 * Utility logic for parsing, cleaning, validating, and executing FastAsyncWorldEdit brush / tool binding, unbinding, and modifier commands.
 */
public class BTCommandLogic {

    /**
     * The set of known FAWE brush / tool binding commands.
     */
    private static final Set<String> bindingCommands = Set.of(
            "brush",
            "br",
            "tool",
            "primary",
            "secondary");

    /**
     * The set of known FAWE brush / tool unbinding commands.
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
     * Strips leading forward slashes from the given string and converts the resulting text to lowercase.
     *
     * @param string the input string to process.
     * @return the cleaned, lowercase string.
     */
    @NotNull
    public static String stripLeadingSlashesLowerCase(@NotNull String string) {
        return string.replaceFirst("^/+", "").toLowerCase();
    }

    /**
     * Checks whether a string is considered a FAWE brush / tool binding command.
     *
     * @param command the command string to check.
     * @return true if it is a binding command, false otherwise.
     */
    public static boolean isCommandBinding(@NotNull String command) {
        command = stripLeadingSlashesLowerCase(command);
        String[] splitCommand = command.split(" ");

        if (splitCommand.length == 0) return false;
        return bindingCommands.contains(splitCommand[0]);
    }

    /**
     * Checks if the provided string is considered a FAWE brush / tool primary binding command.
     *
     * @param command the command string to check.
     * @return true if it is a primary binding command, false otherwise.
     */
    public static boolean isCommandPrimaryBinding(@NotNull String command) {
        return isCommandOrdinalBinding(command, "primary");
    }

    /**
     * Checks if the provided string is considered a FAWE brush / tool secondary binding command.
     *
     * @param command the command string to check.
     * @return true if it is a secondary binding command, false otherwise.
     */
    public static boolean isCommandSecondaryBinding(@NotNull String command) {
        return isCommandOrdinalBinding(command, "secondary");
    }

    /**
     * Checks if the provided command is considered a specific ordinal of FAWE brush / tool binding.
     *
     * @param command the command string to check.
     * @param ordinal the ordinal type.
     * @return true if it matches the ordinal binding, false otherwise.
     */
    private static boolean isCommandOrdinalBinding(@NotNull String command, @NotNull String ordinal) {
        command = stripLeadingSlashesLowerCase(command);
        String[] splitCommand = command.split(" ");

        if (splitCommand.length == 0) return false;

        String initialCommand = splitCommand[0];
        if (!bindingCommands.contains(initialCommand)) return false;
        if (initialCommand.equalsIgnoreCase(ordinal)) return true;

        // Check both the initial command, and first argument, to support variations of the command.
        // Ex. //(Ordinal) sphere 3, OR //brush (Ordinal) sphere 3.
        if (splitCommand.length == 1) return false;

        String firstArg = splitCommand[1];
        return firstArg.equalsIgnoreCase(ordinal);
    }

    /**
     * Checks if the provided string is considered a FAWE brush / tool unbinding command.
     *
     * @param command the command string to check.
     * @return true if it is an unbinding command, false otherwise.
     */
    public static boolean isCommandUnbinding(@NotNull String command) {
        command = stripLeadingSlashesLowerCase(command);
        return unbindingCommands.contains(command);
    }

    /**
     * Checks if the provided string is considered a FAWE brush / tool unbinding command.
     *
     * @param command the command string to check.
     * @return true if it is an unbinding command, false otherwise.
     */
    public static boolean isCommandModifying(@NotNull String command) {
        command = stripLeadingSlashesLowerCase(command);
        String[] splitCommand = command.split(" ");

        if (splitCommand.length == 0) return false;

        String initialCommand = splitCommand[0];
        BTModifier identifiedModifier = BTModifier.findToolModifier(initialCommand);
        if (identifiedModifier != null) return true;

        // Check both the initial command, and first argument, to support variations of the command.
        // Ex. //(Modifier) 3, OR //brush (Modifier) 3.
        if (splitCommand.length == 1) return false;

        String firstArg = splitCommand[1];
        identifiedModifier = BTModifier.findToolModifier(firstArg);
        return identifiedModifier != null;
    }

    /**
     * Strips the preceding binding portion of a command from a string.
     * (e.g., //brush, //tool, or other variants)
     *
     * @param command the command string.
     * @return the command string without preceding slashes and preceding binding command.
     */
    @NotNull
    private static String stripPrecedingBindingCommands(@NotNull String command) {
        command = stripLeadingSlashesLowerCase(command);
        if (!isCommandBinding(command)) return command;

        String[] splitCommand = command.split(" ");
        if (splitCommand.length <= 1) return command;

        // Rejoin the string skipping the first word (the binding portion)
        return String.join(" ", Arrays.copyOfRange(splitCommand, 1, splitCommand.length));
    }

    /**
     * Strips preceding binding commands and identifies any matching brush tool modifier.
     * (e.g., //brush size 3 (identifies "size"), or //size 4 (identifies "size")
     *
     * @param command the command string to check.
     * @return the matching BTModifier if found, or null otherwise.
     */
    @Nullable
    public static BTModifier getStandardizedModifier(@NotNull String command) {
        command = stripPrecedingBindingCommands(command);
        String[] splitCommand = command.split(" ");
        if (splitCommand.length >= 1) return BTModifier.findToolModifier(splitCommand[0]);
        return null;
    }

    /**
     * Executes a FastAsyncWorldEdit command on the current thread for the specified player.
     * (Avoids asynchronous processing delays, helpful for immediate processing & comparisons)
     *
     * @param worldEditPlayer the adapted WorldEdit player executing the command.
     * @param command         the raw command string to be cleaned and executed (stripLeadingSlashesLowerCase)
     */
    public static void performCommandOnCurrentThread(com.sk89q.worldedit.entity.Player worldEditPlayer, String command) {
        WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().handleCommandOnCurrentThread(
                new CommandEvent(worldEditPlayer, BTCommandLogic.stripLeadingSlashesLowerCase(command)));
    }

    /**
     * Determines the modifier context type (primary or secondary) for the given brush / tool.
     *
     * @param tool the tool to check.
     * @return the BTModifierType representing the context.
     */
    @NotNull
    public static BTModifier.Type getModifyingContext(@NotNull Tool tool) {
        if (tool instanceof BrushTool brushTool) {
            var secondary = brushTool.getSecondary();
            if (brushTool.getPrimary() == secondary) return PRIMARY;
            return (brushTool.getContext() != secondary) ? PRIMARY : SECONDARY;
        } else return PRIMARY;
    }

    /**
     * Compares pre-command state and post-command state identifying strings to determine if a binding was successful.
     *
     * @param hadPreviousTool    whether the player had a tool bound prior to execution.
     * @param newToolString      the string representation of the tool after execution.
     * @param previousToolString the string representation of the tool before execution.
     * @return true if binding was successful, false otherwise.
     */
    public static boolean isBindingSuccessful(boolean hadPreviousTool, @NotNull String newToolString, @NotNull String previousToolString) {
        /*
         * We don't need to run these comparisons if the below states return true when determined and if all else return false. But they're helpful to understand the logic.
         * if (!hadPreviousTool && newToolString.isEmpty()) bindingSuccessful = false; // Did not have tool, still no tool. bindingSuccessful is False.
         * if (hadPreviousTool && previousToolString.equals(newToolString)) bindingSuccessful = false; // Had tool, new tool is same tool. bindingSuccessful is False.
         */
        if (!hadPreviousTool && !newToolString.isEmpty()) return true;
        return hadPreviousTool && !previousToolString.equals(newToolString) && !newToolString.isEmpty();
    }

}
