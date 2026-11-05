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

import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Compares and tracks state changes for brush / tool modifiers.
 */
public class BTModifierComparator {

    private final Tool originalTool;
    private final BTModifier BTModifier;
    private String initialState;

    /**
     * Constructs a new BTModifierComparator to track changes for a specific brush / tool modifier.
     *
     * @param originalTool the original FAWE brush / tool.
     * @param BTModifier   the brush / tool modifier we are checking for changes in.
     */

    public BTModifierComparator(Tool originalTool, BTModifier BTModifier) {
        this.originalTool = Objects.requireNonNull(originalTool);
        this.BTModifier = Objects.requireNonNull(BTModifier);
        documentInitialState();
    }

    /**
     * Documents the initial state of the original FAWE brush / tool's modifier.
     */
    private void documentInitialState() {
        initialState = (originalTool instanceof BrushTool brushTool) ? collectDetails(brushTool) : "NULL";
    }

    /**
     * Takes the state of the original FAWE brush / tool's modifier state and compares it to that of the new brush / tool.
     *
     * @param newTool the new FAWE brush / tool we're comparing with.
     * @return true if there is a difference of values between the respective brush / tool modifiers, false otherwise.
     */
    public boolean checkIfModified(@NotNull Tool newTool) {
        return !initialState.equals((newTool instanceof BrushTool brushTool) ? collectDetails(brushTool) : "NULL");
    }

    /**
     * Collects a string representation for the BTModifier this was initialized with for the specified brush / tool.
     *
     * @param brushTool the brush / tool to collect details from.
     * @return the string representation of the modifier value.
     */
    @NotNull
    private String collectDetails(@NotNull BrushTool brushTool) {
        return String.valueOf(switch (BTModifier) {
            case MASK -> brushTool.getContext().getMask().toString();
            case MATERIAL -> brushTool.getContext().getMaterial().toString();
            case RANGE -> brushTool.getRange();
            case SIZE -> brushTool.getContext().getSize();
            case TRACEMASK -> brushTool.getTraceMask() != null ? brushTool.getTraceMask().toString() : "NULL";
            case TARGET -> brushTool.getTargetMode().name();
            case TARGETOFFSET -> brushTool.getTargetOffset();
            case SCROLL -> brushTool.getContext().getScrollAction().toString();
            case SOURCEMASK -> brushTool.getContext().getSourceMask().toString();
            case TRANSFORM -> brushTool.getContext().getTransform().toString();
        });
    }

    /**
     * Provides the brush / tool modifier this BTModifierComparator is comparing for.
     *
     * @return the brush / tool modifier.
     */
    @NotNull
    public BTModifier getBrushToolModifier() {
        return BTModifier;
    }

}
