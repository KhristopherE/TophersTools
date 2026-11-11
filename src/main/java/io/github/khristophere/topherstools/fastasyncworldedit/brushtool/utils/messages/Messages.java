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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents standardized messages and message formatting utilities for brush / tool operations.
 */
public enum Messages {
    BrushTool_Not_Bound_Self(
            getHeader() + "&7There is &cno brush/tool &7currently bound to &cthis item&7."),
    BrushTool_Not_Bound_Specified_Player(
            getHeader() + "&7There is &cno brush/tool &7currently bound to &d%playerName%&7's &cheld item&7."),
    BrushTool_Not_Bound_Specified_Player_Specified_Material(
            getHeader() + "&7There is &cno brush/tool &7currently bound to &d%playerName%&7's &c%material%&7."),
    BrushTool_Info_Below_Self(
            getHeader() + "&7Your currently held &6brush/tool &7info is &6below&7."),
    BrushTool_Info_Below_Specified_Player(
            getHeader() + "&d%playerName%&7's currently held &6brush/tool &7info is &6below&7."),
    BrushTool_Info_Below_Specified_Player_Specified_Material(
            getHeader() + "&d%playerName%&7's &b%material% &6brush/tool &7info is &6below&7."),
    BrushTool_Applied(
            getHeader() + " &7Your &6brush/tool &7has been &aapplied&7."),
    BrushTool_Info_Inquiry_Player_Offline_Requesting_Specify_Material(
            getHeader() + " &7That player is &coffline&7. Please &cspecify a material&7."),
    BrushTool_Macro_Applied_DKInstant(
            getHeader() + "&7Your &6brush/tool macro &7has been &aapplied&7, p.s. &aI looove you &c<3&7."),
    Warning_GMask_Is_Enabled(
            getHeader() + "&8[&cWARNING&8] &7You currently have &eGMask &7enabled."),
    Warning_BrushTool_Is_Empty(
            getHeader() + "&8[&cWARNING&8] &7This &6brush/tool&7's &cdetails &7are &cempty&7."),
    BrushTool_Shared_With_Everyone(
            getHeader() + "&7This &6brush/tool &7has been shared with &aeveryone online&7."),
    BrushTool_Shared_From_Specified_Player(
            getHeader() + "&7This &6brush/tool &7has been shared with you by &d%playerName%&7."),
    BrushTools_Not_On_File(
            getHeader() + "&7You have &cno brushes/tools &7saved on file."),
    Warning_Unable_To_Apply_BrushTools_From_File(
            getHeader() + "&8[&cWARNING&8] &cUnable to apply brushes/tools from file."),
    BrushTools_ReApplied(
            getHeader() + "&7Your prior &6brushes/tools &7have been &are-applied&7, (&eMax of 36&7)."),
    BrushTools_Cleared(
            getHeader() + "&7All of your &6brushes/tools &7have been &ccleared &7from the file, & &cunbound&7 in FAWE."),
    BrushTool_Max_Length(
            getHeader() + "&7The max length for a &6brush/tool macro &ename &7is &e48 characters&7."),
    BrushTool_Macro_Saved(
            getHeader() + "&7Your &6brush/tool macro &7has been &asaved &7as &b%macroName%&7."),
    BrushTool_Macro_Already_Exists_Requesting_Override(
            getHeader() + "&7Macro &b%macroName% &calready exists&7. Add &e-o &7to &eoverride &7it."),
    BrushTool_Macro_Not_Exist(
            getHeader() + "&7Macro &b%macroName% &7does &cnot exist&7."),
    Warning_BrushTool_Macro_Is_Empty(
            getHeader() + "&8[&cWARNING&8] &7Macro &b%macroName% &7is &cempty&7."),
    BrushTool_Macro_Applied(
            getHeader() + "&7Your &6brush/tool macro&7, &b%macroName% &7has been &aapplied&7."),
    BrushTool_Macro_Applied_With_Failures_Missing_Material(
            getHeader() + "&7Your &6brush/tool macro&7, &b%macroName% &7has been &aapplied&7, with &c%integer% failures &7at inventory slot(s) &e%inventorySlots%&7."),
    BrushTool_Macro_Deleted(
            getHeader() + "&7The &6brush/tool macro &b%macroName% &7has been &cdeleted&7."),
    BrushTool_Macro_Deletion_Failed(
            getHeader() + "&7Deletion of macro &b%macroName% &7has &cfailed&7. Please contact an administrator & or report this issue."),
    BrushTool_Macro_Delete_Requesting_Confirmation(
            getHeader() + "&7Deletion request for macro &b%macroName% &7received. Add &econfirm &7to the end of your command, within &c15 seconds &7to finalize."),
    BrushTool_Macro_Delete_Requesting_Initial_Request_Before_Confirmation(
            getHeader() + "&7Deletion request for macro &b%macroName% &cnot received&7. Input &cwithout &econfirm &7first."),
    BrushTool_Macro_Delete_Confirmation_Window_Expired(
            getHeader() + "&7Deletion request &econfirmation &7for macro &b%macroName% &7has &cexpired&7. Please try again."),
    BrushTool_Macro_Help(
            getHeader() + "&7Please specify an action: &esave&7, &eload&7, or &edelete&7."),
    Config_Reloaded(
            getHeader() + "&eThe config has been reloaded."),
    Command_Reserved_For_Players(
            getHeader() + "&cThis command is reserved for players.");

    private final String message;

    /**
     * Constructs a new Messages enum constant with the specified message string.
     *
     * @param message the raw message string.
     */
    Messages(@NotNull String message) {
        this.message = message;
    }

    /**
     * Gets the standardized header for messages.
     *
     * @return the standardized header for messages.
     */
    private static String getHeader() {
        return "&8(&3&oBT&8-&2&oUtils&8) ";
    }

    /**
     * Returns the provided message formatted into a text component converting the & symbol & code into the associated color code.
     *
     * @param message expected to contain & symbol color codes.
     * @return the provided message formatted into a text component.
     */
    @NotNull
    private static TextComponent colorCode(@NotNull String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Returns the recommended brush / tool macro GMask options message.
     *
     * @return the recommended brush / tool macro GMask component.
     */
    @NotNull
    public static Component recommendedBrushToolMacroGMaskMessage() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize("&7You may want to utilize either option below.\n")
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/gmask Grass_Block & Green_Concrete_Powder ")
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8]\n")
                                .clickEvent(ClickEvent.runCommand("/gmask grass_block,green_concrete_powder")))
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&2OR\n"))
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/gmask Grass_Block ")
                                .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8]")
                                        .clickEvent(ClickEvent.runCommand("/gmask grass_block")))));
    }

    /**
     * Returns the bugged state secondary brush invalid message.
     *
     * @return the bugged state secondary brush invalid component.
     */
    @NotNull
    public static Component getBuggedStateSecondaryBrushInvalidMessage() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(
                        getHeader() + "&8[&cWARNING&8] &7Detected &cbugged brush/tool state&7. Use the link above to &areapply &7your brush.")
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                " &7[&eHover for details&7]")
                        .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                """
                                        &eConditions&7: Having a valid brush/tool and running a &e//secondary or equivalent command &7with invalid arguments breaks the brush state.
                                        &7To handle this, as the &6Brush/Tool &7is no longer valid in FAWE, it has been deleted from the Brush/Tool retention file.
                                        &7Although a &aclickable message &7has been generated above to &areapply &7your &6Brush/Tool&7.
                                        """
                        ))));
    }

    /**
     * Returns the requested message component.
     *
     * @return the requested text component message.
     */
    @NotNull
    public TextComponent getMessage() {
        return colorCode(message);
    }

    /**
     * Replaces %playerName% placeholders in the message with the provided string.
     *
     * @param playerName the player name to replace %playerName%.
     * @return the message component with replaced placeholders.
     */
    @NotNull
    public TextComponent getMessage(@NotNull String playerName) {
        return colorCode(message.replaceAll("%playerName%", playerName));
    }

    /**
     * Replaces %macroName% placeholders in the message with the provided string.
     *
     * @param macroName the macro name to replace %macroName%.
     * @return the message component with replaced placeholders.
     */
    @NotNull
    public TextComponent getMessageMacroName(@NotNull String macroName) {
        return colorCode(message.replaceAll("%macroName%", macroName));
    }

    /**
     * Replaces %macroName%, %integer%, and %inventorySlots% placeholders in the message with the provided values.
     *
     * @param macroName      the macro name to replace %macroName%.
     * @param failures       the number of failures.
     * @param inventorySlots the inventory slots string.
     * @return the message component with replaced placeholders.
     */
    @NotNull
    public TextComponent getMessageMacroNameFailuresInventorySlots(@NotNull String macroName, int failures, @NotNull String inventorySlots) {
        return colorCode(message.replaceAll("%macroName%", macroName).replaceAll("%integer%", String.valueOf(failures)).replaceAll("%inventorySlots%", inventorySlots));
    }

    /**
     * Replaces %playerName% and %material% placeholders in the message with the provided strings.
     *
     * @param playerName   the player name to replace %playerName%.
     * @param materialName the material name.
     * @return the message component with replaced placeholders.
     */
    @NotNull
    public TextComponent getMessage(@NotNull String playerName, @NotNull String materialName) {
        return colorCode(message.replaceAll("%playerName%", playerName).replaceAll("%material%", materialName.toLowerCase()));
    }

}
