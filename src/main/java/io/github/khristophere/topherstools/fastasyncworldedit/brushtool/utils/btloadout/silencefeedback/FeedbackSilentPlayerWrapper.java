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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.silencefeedback;

import com.fastasyncworldedit.core.wrappers.AsyncPlayer;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.Component;

/**
 * Wraps an AsyncPlayer intended to suppress standard feedback messages while allowing error messages.
 */
public class FeedbackSilentPlayerWrapper extends AsyncPlayer {
    public FeedbackSilentPlayerWrapper(Player parent) {
        super(parent);
    }

    @Override
    public void print(Component component) {
        if (hasRedColor(component)) {
            this.getBasePlayer().print(component);
        }
    }

    /**
     * Checks if a toString of the component object contains "style=Style{color=red,"
     * This is not the most optimal method.
     *
     * @param component the component to check.
     * @return true if the text is present within, false otherwise.
     */
    private boolean hasRedColor(Component component) {
        if (component == null) return false;
        return component.toString().contains("style=Style{color=red,");

        /*
        Commented out to review later. The above works just fine for the purpose...
        If it ain't broke don't fix it. I cannot for the life of me figure out the Component system in FAWE.

        if (component.style().color() != null)
            //noinspection DataFlowIssue
            if (component.style().color().toString().equals("red")) return true;

        for (Component child : component.children())
            if (hasRedColor(child))
                return true;

        return false;
        */
    }

}