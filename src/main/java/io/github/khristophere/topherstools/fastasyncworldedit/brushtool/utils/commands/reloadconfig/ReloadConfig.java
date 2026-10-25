package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.reloadconfig;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import io.github.khristophere.topherstools.globalutils.Utils;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

public class ReloadConfig {

    public ReloadConfig(){
        Utils.registerClassWithCommandManager(this);
    }

    /**
     * Reloads the FAWEBTUtils Config.
     * @param sender The command sender.
     */
    @Command("topherstools reload btutils")
    @Command("tt r btu")
    @Permission("topherstools.reloadconfig.fawebtutils")
    private void reloadFAWEBTUtils(Source sender) {
        Config.reloadConfigSettings();
        sender.source().sendMessage(Messages.Config_Reloaded.getMessage());
    }
}

