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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.world.item.ItemType;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.silencefeedback.FeedbackSilentPlayerWrapper;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles intercepted binding commands for FastAsyncWorldEdit brush / tool operations.
 */
public class BTBinding {

    /**
     * Processes a FastAsyncWorldEdit command event known to bind a brush / tool.
     *
     * @param player              the player executing the binding command.
     * @param command             the command string being called.
     * @param silenceFAWEFeedback whether to suppress FAWE feedback.
     */
    public static void processCommand(@NotNull Player player, @NotNull String command, boolean silenceFAWEFeedback) {
        // Step 1 Gather Details.
        //noinspection DuplicatedCode - Duplicated to BTModifying.
        ItemStack minecraftItemInHand = player.getInventory().getItemInMainHand();
        ItemType worldEditItemInHand = BukkitAdapter.asItemType(minecraftItemInHand.getType());
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        if (worldEditItemInHand == null) {
            // Command should not function, but allow FAWE to process as normal.
            BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command);
            return;
        }

        if (silenceFAWEFeedback) worldEditPlayer = new FeedbackSilentPlayerWrapper(worldEditPlayer);
        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        BaseItem worldEditBaseItemInHand = worldEditItemInHand.getDefaultState();

        Tool previousTool = worldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        boolean hadPreviousTool = previousTool != null;
        String previousToolString = hadPreviousTool ? previousTool.toString() : "";

        //Step 2. Process via FAWE.
        BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command);

        //Step 3. Compare.
        LocalSession refreshWorldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);

        Tool newTool = refreshWorldEditSession.getTool(worldEditBaseItemInHand, worldEditPlayer);
        String newToolString = newTool != null ? newTool.toString() : "";

        boolean wasBindingSuccessful = BTCommandLogic.isBindingSuccessful(hadPreviousTool, newToolString, previousToolString);
        boolean isPrimaryBinding = BTCommandLogic.isCommandPrimaryBinding(command);
        boolean isSecondaryBinding = BTCommandLogic.isCommandSecondaryBinding(command);

        UUID uuid = player.getUniqueId();
        String material = minecraftItemInHand.getType().name();

        /*
         * TODO: FAWE Bugged State, if this bug is ever resolved, change this. Certainly possible that it's not a bug and I'm just an idiot...
         *
         * The below checks for a bugged state in FAWE, where if you have a valid brush/tool, and utilized the "//(brush/tool) secondary"
         * or "//secondary" command with invalid arguments, it causes the brush/tool to become entirely invalid.
         *
         * To account for this, we will follow how FAWE behaves, unfortunately unbinding the brush.
         * Although, before that we will provide the player with a clickable chat message to reapply all the prior brush commands.
         */
        if (hadPreviousTool && isSecondaryBinding && newToolString.isEmpty()) {
            Scheduler.runSequentialAsync(() -> {
                BTLoadout oldLoadout = BTCacheManager.getBTSQLExecutor().getBrushToolLoadout(uuid, material);
                if (oldLoadout != null && !oldLoadout.isEmpty()) {
                    player.sendMessage(oldLoadout.getLoadoutPromptMessage());
                    player.sendMessage(Messages.getBuggedStateSecondaryBrushInvalidMessage());
                }
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteBrushTool(uuid, material);
            });
            BTCacheManager.removeUUIDMaterialBindFromCache(uuid, material);
            return;
        }

        // Step 4. Save.

        /*
         * This checks for a specific condition that results in your secondary brush/tool being overridden by a brush/tool command that did not
         * specify primary OR secondary. To comply with the functionality of FAWE, we will also delete the secondary functionality from the save file.
         */
        if (!isPrimaryBinding && !isSecondaryBinding && wasBindingSuccessful) {
            BTCacheManager.getBTSQLExecutor().getBTSQLRequests().removeSecondaryBrushToolCommand(uuid, player.getInventory().getItemInMainHand().getType().name());
        }

        if (wasBindingSuccessful) {
            if (!isPrimaryBinding && !isSecondaryBinding) {
                // If it is binding in neither ordinal all modifiers are removed in FAWE.
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteAllBrushToolModifiers(uuid, material, BTModifier.Type.PRIMARY);
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteAllBrushToolModifiers(uuid, material, BTModifier.Type.SECONDARY);
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteAllBrushToolModifiers(uuid, material, BTModifier.Type.GLOBAL);
            } else {
                // The modifiers for this binding type will be removed in FAWE as will globals.
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteAllBrushToolModifiers(uuid, material, isPrimaryBinding ? BTModifier.Type.PRIMARY : BTModifier.Type.SECONDARY);
                BTCacheManager.getBTSQLExecutor().getBTSQLRequests().deleteAllBrushToolModifiers(uuid, material, BTModifier.Type.GLOBAL);
            }
            BTCacheManager.getBTSQLExecutor().getBTSQLRequests().saveBrushToolCommandSelfIdentifyOrdinal(uuid, minecraftItemInHand, command);
            BTCacheManager.addUUIDMaterialBindToCache(uuid, material);
        }
    }

}
