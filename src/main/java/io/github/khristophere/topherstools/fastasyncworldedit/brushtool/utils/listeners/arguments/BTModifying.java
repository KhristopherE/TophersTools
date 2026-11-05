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
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.silencefeedback.FeedbackSilentPlayerWrapper;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTCommandLogic;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifierComparator;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier.Type.*;

/**
 * Handles intercepted modifying commands for FastAsyncWorldEdit brush / tool operations.
 */
public class BTModifying {

    /**
     * Processes a FastAsyncWorldEdit command event known to modify an existing brush / tool.
     *
     * @param player              the player executing the modifying command.
     * @param command             the command string being called.
     * @param silenceFAWEFeedback whether to suppress FAWE feedback.
     */
    public static void processCommand(@NotNull Player player, @NotNull String command, boolean silenceFAWEFeedback) {
        // Step 1 Gather Details.
        //noinspection DuplicatedCode - Duplicated to BTBinding.
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemType worldEditItemType = BukkitAdapter.asItemType(itemInHand.getType());
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        if (worldEditItemType == null) {
            // Command should not function, but allow FAWE to process as normal.
            BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command);
            return;
        }


        LocalSession worldEditSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        BaseItem worldEditBaseItem = worldEditItemType.getDefaultState();

        Tool previousTool = worldEditSession.getTool(worldEditBaseItem, worldEditPlayer);
        if (previousTool == null) {
            // Command does function in some fashion, but the result is useless. Allow FAWE to process as normal.
            BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command);
            return;
        }

        if (silenceFAWEFeedback) worldEditPlayer = new FeedbackSilentPlayerWrapper(worldEditPlayer);

        BTModifier BTModifier = BTCommandLogic.getStandardizedModifier(command);
        BTModifierComparator modifierChecker = new BTModifierComparator(previousTool, BTModifier);

        //Step 2. Process via FAWE.
        BTCommandLogic.performCommandOnCurrentThread(worldEditPlayer, command); // Have FAWE process normally.

        //Step 3. Compare.
        if (BTModifier == null) return; // Can't save something that can't be identified.
        LocalSession refreshedSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        Tool newTool = refreshedSession.getTool(worldEditBaseItem, worldEditPlayer);
        if (newTool == null) return; // Prior & resulting tool are useless disregard.

        // Identify for which, Primary or Secondary is the Modifier Applicable to.
        boolean isPrimary = BTCommandLogic.getModifyingContext(newTool) == PRIMARY;
        UUID uuid = player.getUniqueId();
        String materialName = itemInHand.getType().name();

        // Certain BrushToolModifiers are considered global, and not stored in the primary or secondary context, these will be saved separately.
        BTModifier.Type modifierType;
        if (!BTModifier.isGlobalModifier())
            modifierType = isPrimary ? PRIMARY : SECONDARY;
        else
            modifierType = GLOBAL;

        //Step 4. Save.
        if (modifierChecker.checkIfModified(newTool)) {
            Scheduler.runSequentialAsync(() ->
                    BTCacheManager.getBTSQLExecutor().getBTSQLRequests().saveBrushToolModifier(
                            uuid, materialName, BTModifier, command, modifierType)
            );
        }
    }

}
