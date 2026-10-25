package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.silencefeedback;

import com.fastasyncworldedit.core.wrappers.AsyncPlayer;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.Component;

/**
 * Avoids printing messages except continues to print debug & error messages.
 */
public class FeedbackSilentPlayerWrapper extends AsyncPlayer {
    public FeedbackSilentPlayerWrapper(Player parent) {super(parent);}
    @Override public void print(String msg) {}
    @Override public void print(Component component) {}
    @Override public void printRaw(String msg) {}
}