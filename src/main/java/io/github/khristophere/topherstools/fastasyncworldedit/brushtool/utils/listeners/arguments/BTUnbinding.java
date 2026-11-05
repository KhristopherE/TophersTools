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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.silencefeedback.FeedbackSilentPlayerWrapper;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles intercepted unbinding commands for FastAsyncWorldEdit brush / tool operations.
 */
public class BTUnbinding {

    /**
     * Processes a FastAsyncWorldEdit command event known to unbind an existing brush / tool.
     *
     * @param player              the player executing the unbinding command.
     * @param command             the command string being called.
     * @param silenceFAWEFeedback whether to suppress FAWE feedback.
     */
    public static void processCommand(@NotNull Player player, @NotNull String command, boolean silenceFAWEFeedback) {
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        if (silenceFAWEFeedback) worldEditPlayer = new FeedbackSilentPlayerWrapper(worldEditPlayer);

        BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command);

        UUID uuid = player.getUniqueId();
        String material = player.getInventory().getItemInMainHand().getType().name();

        BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteBrushTool(uuid, material);
        BTCacheManager.removeUUIDMaterialBindFromCache(uuid, material);
    }

}
