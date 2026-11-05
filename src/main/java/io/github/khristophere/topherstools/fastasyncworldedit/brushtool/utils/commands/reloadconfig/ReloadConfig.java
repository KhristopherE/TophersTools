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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.reloadconfig;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

/**
 * Command handler for reloading FAWEBTUtils config.
 */
public class ReloadConfig {

    /**
     * Constructs a new ReloadConfig instance and registers it with the command manager.
     */
    public ReloadConfig() {
        Utils.registerClassWithCommandManager(this);
    }

    /**
     * Reloads the FAWEBTUtils configuration settings.
     *
     * @param sender the command sender executing the command.
     */
    @Command("topherstools reload btutils")
    @Command("tt r btu")
    @Permission("topherstools.reloadconfig.fawebtutils")
    private void reloadFAWEBTUtils(@NotNull Source sender) {
        Config.reloadConfigSettings();
        sender.source().sendMessage(Messages.Config_Reloaded.getMessage());
    }

}

