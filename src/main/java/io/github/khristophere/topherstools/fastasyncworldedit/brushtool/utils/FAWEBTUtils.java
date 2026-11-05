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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btclearall.BTClearAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget.BTGet;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btmacro.BTMacro;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btreturnall.BTReturnAll;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btshout.BTShout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.reloadconfig.ReloadConfig;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.BTListener;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.sql.BTSQLExecutor;
import io.github.khristophere.topherstools.globalutils.Utils;

/**
 * Separates Fast Async World Edit brush / tool utils from the TophersTools plugin, in future intended providing options
 * to enable or disable individual features and commands.
 */
public class FAWEBTUtils {

    // TODO: Retain Instantiated Classes to Later Reference & Shut down functions, un-register commands / disable listeners.

    /**
     * Enables all functions of FAWEBTUtils.
     */
    public static void enable() {
        Utils.setupCommandManager();
        Config.startConfig();

        // Enables Commands & Listeners.
        new ReloadConfig();
        new BTGet();
        new BTMacro();
        new BTShout();
        new BTReturnAll();
        new BTClearAll();
        new BTListener();
    }

    /**
     * Disables all functions of FAWEBTUtils.
     */
    public static void disable() {
        BTSQLExecutor.sqlManager.closeConnectionPool();
    }

}
