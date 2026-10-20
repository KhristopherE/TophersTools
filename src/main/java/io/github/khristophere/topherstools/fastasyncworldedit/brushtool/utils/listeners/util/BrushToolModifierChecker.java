package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util;

import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;

import java.util.Objects;

public class BrushToolModifierChecker {

    private final Tool originalTool;
    private final BrushToolModifier brushToolModifier;
    private String initialState;

    /**
     * An object that takes the state of the Original FAWE Brush/Tool & documents it's BrushToolModifier State.
     * Later allowing you to compare the state of the Original FAWE Brush/Tool to that of the New FAWE Brush/Tool under checkIfModified.
     * Utilized to determine if a command ran by a player was successful in binding, letting us know that we should save the command issued.
     * @param originalTool The Original FAWE Brush/Tool.
     * @param brushToolModifier The BrushToolModifier we are checking for changes on.
     */
    public BrushToolModifierChecker(Tool originalTool, BrushToolModifier brushToolModifier){
        this.originalTool = Objects.requireNonNull(originalTool);
        this.brushToolModifier = Objects.requireNonNull(brushToolModifier);
        documentInitialState();
    }

    /**
     * Documents the initial state of the Original FAWE Brush/Tool's BrushToolModifier.
     */
    private void documentInitialState(){
        try {
            initialState = String.valueOf(switch (brushToolModifier) {
                case BrushToolModifier.MASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.MATERIAL -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getMaterial().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.RANGE -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getRange();
                    else yield "NULL";
                }
                case BrushToolModifier.SIZE -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getSize();
                    else yield "NULL";
                }
                case BrushToolModifier.TRACEMASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTraceMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.TARGET -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTargetMode().name();
                    else yield "NULL";
                }
                case BrushToolModifier.TARGETOFFSET -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTargetOffset();
                    else yield "NULL";
                }
                case BrushToolModifier.SCROLL -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getContext().getScrollAction().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.SOURCEMASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getSourceMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.TRANSFORM -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTransform().toString();
                    else yield "NULL";
                }
            });
        } catch (Exception _){
            initialState = "NULL";
        }
    }

    /**
     * Takes the state of the Original FAWE Brush/Tool's BrushToolModifier State and compares it to that of the newTool.
     * @param newTool The new FAWE Brush/Tool we're comparing with.
     * @return True if there is a difference of values between the respective BrushToolModifiers.
     */
    public boolean checkIfModified(Tool newTool){
        String modifiedValue;
        try {
            modifiedValue = String.valueOf(switch (brushToolModifier) {
                case BrushToolModifier.MASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.MATERIAL -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getMaterial().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.RANGE -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getRange();
                    else yield "NULL";
                }
                case BrushToolModifier.SIZE -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getSize();
                    else yield "NULL";
                }
                case BrushToolModifier.TRACEMASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTraceMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.TARGET -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTargetMode().name();
                    else yield "NULL";
                }
                case BrushToolModifier.TARGETOFFSET -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTargetOffset();
                    else yield "NULL";
                }
                case BrushToolModifier.SCROLL -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getContext().getScrollAction().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.SOURCEMASK -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getSourceMask().toString();
                    else yield "NULL";
                }
                case BrushToolModifier.TRANSFORM -> {
                    if (originalTool instanceof BrushTool)
                        yield ((BrushTool) originalTool).getTransform().toString();
                    else yield "NULL";
                }
            });
        } catch (Exception _){
            modifiedValue = "NULL";
        }
        return !initialState.equals(modifiedValue);
    }

    /**
     * Provides the BrushToolModifier this BrushToolModifierChecker is comparing for.
     * @return The BrushToolModifier.
     */
    public BrushToolModifier getBrushToolModifier(){
        return brushToolModifier;
    }

}
