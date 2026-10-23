package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public enum PlayerMessages {
    BrushTool_Not_Bound_Self(
            "&7There is &cno brush/tool &7currently bound to &cthis item&7."),
    BrushTool_Not_Bound_Specified_Player(
            "&7There is &cno brush/tool &7currently bound to &d%playerName%&7's held item."),
    BrushTool_Not_Bound_Specified_Player_Specified_Material(
            "&7There is &cno brush/tool &7currently bound to &d%playerName%&7's &b%material%&7."),
    BrushTool_Info_Below_Self(
            "\n&7Your currently held &6brush/tool &7info is below:\n"),
    BrushTool_Info_Below_Specified_Player(
            "\n&d%playerName%&7's currently held &6brush/tool &7info is below:\n"),
    BrushTool_Info_Below_Specified_Player_Specified_Material(
            "\n&d%playerName%&7's &b%material% &6brush/tool &7info is below:\n"),
    BrushTool_Applied(
            "\n&7Your &6brush/tool &7has been &aapplied&7."),
    BrushTool_Info_Inquiry_Player_Offline_Requesting_Specify_Material(
            "&7That player is &coffline&7. Please add an &aitem name/material &7as a third argument."),
    BrushTool_Macro_Issued_DKInstant(
            "\n&7Your &6brush/tool &7macro has been &aissued&7, P.S. &aI looove you &c<3&7.\n"),
    Warning_GMask_Is_Enabled(
            "&8[&cWARNING&8] &7You currently have a &eGMask &7enabled."),
    Warning_BrushTool_Is_Empty(
            "&8[&cWARNING&8] &7This &6brush/tool&7's details are &cempty&7."),
    BrushTool_Shared_With_Everyone(
            "\n&7This &6brush/tool &7has been shared with &aeveryone online&7.\n"),
    BrushTool_Shared_From_Specified_Player(
            "&7This &6brush/tool &7has been shared with you by &d%playerName%&7."),
    BrushTools_Not_On_File(
            "&7You have &cno brushes/tools &7saved on file."),
    Warning_Unable_To_Apply_BrushTools_From_File(
            "&8[&cWARNING&8] &cUnable to apply brushes/tools from file."),
    BrushTools_ReApplied(
            "&7Your prior &6brushes/tools &7have been &are-applied&7."),
    BrushTools_Cleared(
            "&7All of your &6brushes/tools &7have been &ccleared &7from the file, & &cunbound&7 in FAWE."),
    BrushTool_Max_Length(
            "&7The max length for a &6brush/tool &7macro &ename &7is &e48 characters&7."),
    BrushTool_Macro_Saved(
            "&7Your &6brush/tool &7macro has been &asaved &7as &b%macroName%&7."),
    BrushTool_Macro_Already_Exists_Requesting_Override(
            "&7Macro &b%macroName% &7already &cexists&7. Add &e-o &7to &eoverride &7it."),
    BrushTool_Macro_Not_Exist(
            "&7Macro &b%macroName% &7does &cnot exist&7."),
    Warning_BrushTool_Macro_Is_Empty(
            "&8[&cWARNING&8] &7Macro &b%macroName% &7is &cempty&7."),
    BrushTool_Macro_Applied(
            "&7Your &6brush/tool &7macro &b%macroName% &7has been &aapplied&7."),
    BrushTool_Macro_Applied_With_Failures_Missing_Material(
            "&7Your &6brush/tool &7macro &b%macroName% &7has been &aapplied&7, with &c%integer% failures &7at inventory slot(s) &e%inventorySlots%&7."),
    BrushTool_Macro_Deleted(
            "&7The &6brush/tool &7macro &b%macroName% &7has been &cdeleted&7."),
    BrushTool_Macro_Deletion_Failed(
            "&7Deletion of macro &b%macroName% &7has &cfailed&7. Please contact an admin."),
    BrushTool_Macro_Delete_Requesting_Confirmation(
            "&7Deletion request for macro &b%macroName% &7received. Add &econfirm &7to the end of your command, within &c15 seconds &7to finalize."),
    BrushTool_Macro_Delete_Requesting_Initial_Request_Before_Confirmation(
            "&7Deletion request for macro &b%macroName% &cnot &7received. Input &cwithout &econfirm &7first."),
    BrushTool_Macro_Delete_Confirmation_Window_Expired(
            "&7Deletion request &econfirmation &7for macro &b%macroName% &7has &cexpired&7. Please try again."),
    BrushTool_Macro_Help(
            "&7Please specify an action: &esave&7, &eload&7, or &edelete&7."),
    ;

    private final String message;
    PlayerMessages(String message) {
        this.message = message;
    }

    /**
     * Returns the requested PlayerMessage's standardized message for players.
     * @return The requested PlayerMessage's standardized message for players.
     */
    public TextComponent getMessage(){
        return colorCode(message);
    }

    /**
     * Returns the requested PlayerMessage's standardized message without any color code application. It will contain raw & symbols and the associated code.
     * @return The requested PlayerMessage's standardized message without any color code application. It will contain raw & symbols and the associated code.
     */
    public String asString(){
        return message;
    }

    /**
     * If the requested PlayerMessage has %playerName% this will replace those strings with the provided string.
     * @param playerName The Player Name to replace %playerName%.
     * @return This PlayerMessage's message replacing the %value%'s with the provided String.
     */
    public TextComponent getMessage(String playerName){
        return colorCode(message.replaceAll("%playerName%", playerName));
    }

    /**
     * If the requested PlayerMessage has %macroName% this will replace those strings with the provided string.
     * @param macroName The Macro Name to replace %macroName%.
     * @return This PlayerMessage's message replacing the %value%'s with the provided String.
     */
    public TextComponent getMessageMacroName(String macroName){
        return colorCode(message.replaceAll("%macroName%", macroName));
    }

    /**
     * If the requested PlayerMessage has %macroName% this will replace those strings with the provided string.
     * @param macroName The Macro Name to replace %macroName%.
     * @return This PlayerMessage's message replacing the %value%'s with the provided String.
     */
    public TextComponent getMessageMacroNameFailuresInventorySlots(String macroName, int failures, String inventorySlots){
        return colorCode(message.replaceAll("%macroName%", macroName).replaceAll("%integer%", String.valueOf(failures).replaceAll("%inventorySlots%", inventorySlots)));
    }

    /**
     * If the requested PlayerMessage has %playerName% & %material% this will replace those strings with the provided strings.
     * @param playerName The Player Name to replace %playerName%.
     * @param material The Material to replace %material.
     * @return This PlayerMessage's message replacing the %value%'s with the provided String.
     */
    public TextComponent getMessage(String playerName, String material){
        return colorCode(message.replaceAll("%playerName%", playerName).replaceAll("%material%", material.toLowerCase()));
    }

    /**
     * Returns the provided message formated into a Text Component converting the & symbol into the associated color code.
     * @param message expected to contain & symbol color codes.
     * @return the provided message formated into a Text Component converting the & symbol into the associated color code.
     */
    private static TextComponent colorCode(String message){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    /**
     * Returns the Recommended BrushTool Macro GMask Options in a format to be sent to players.
     * @return the Recommended BrushTool Macro GMask Options in a format to be sent to players.
     */
    public static Component recommendedBrushToolMacroGMaskMessage(){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(
            "&7You may want to utilize either option below:\n")
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/gmask GrassBlock & GreenConcretePowder &8[&aClick Here&8]\n")
                    .clickEvent(ClickEvent.runCommand("/gmask grass_block,green_concrete_powder")))
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&2OR\n"))
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/gmask GrassBlock &8[&aClick Here&8]"))
                    .clickEvent(ClickEvent.runCommand("/gmask grass_block"));
    }

    /**
     * Returns the Bugged State Secondary Brush Invalid Message
     * @return the Bugged State Secondary Brush Invalid Message in a format to be sent to players.
     */
    public static Component getBuggedStateSecondaryBrushInvalidMessage(){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(
                "\n&8[&cWARNING&8] \n&7Detected &cbugged brush/tool state&7. Use the link above to &areapply &7your brush.")
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(
                "&7[&eHover for details&7]")
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        """
                                &eConditions&7: Having a valid brush/tool and running a &e//secondary or equivalent command &7with invalid arguments breaks the brush state.
                                &7To handle this, as the &6Brush/Tool &7is no longer valid in FAWE, it has been deleted from the Brush/Tool retention file.
                                &7Although a &aclickable message &7has been generated above to &areapply &7your &6Brush/Tool&7.
                                """
                ))));
    }

}
