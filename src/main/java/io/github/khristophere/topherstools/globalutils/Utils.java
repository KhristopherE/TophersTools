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

package io.github.khristophere.topherstools.globalutils;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.setting.ManagerSetting;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

/**
 * A general utility class for commonly used functions.
 */
public class Utils {

    private static PaperCommandManager<Source> commandManager = null;

    /**
     * Clears the given player's inventory slots in a given range.
     *
     * @param player the player whose inventory slots will be cleared.
     * @param from   the inventory slot to start clearing from (inclusive).
     * @param to     the inventory slot to clear to (inclusive).
     */
    public static void clearInventorySlots(@NotNull Player player, int from, int to) {
        IntStream.rangeClosed(from, to).forEach(player.getInventory()::clear);
    }

    /**
     * Sets up and configures the shared command manager instance.
     */
    public static void setupCommandManager() {
        if (commandManager == null)
            commandManager = PaperCommandManager.builder(
                            PaperSimpleSenderMapper.simpleSenderMapper())
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(TophersTools.getInstance());
        commandManager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);
    }

    /**
     * Registers the provided class instance with Cloud Command Manager.
     *
     * @param commandClass the class to parse for command functionalities.
     */
    public static void registerClassWithCommandManager(@NotNull Object commandClass) {
        AnnotationParser<Source> annotationParser = new AnnotationParser<>(
                commandManager,
                Source.class
        );
        annotationParser.parse(commandClass);
    }

}
