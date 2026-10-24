package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.btget;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTLoadout;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.commands.Cooldown;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout.BTCacheManager;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
import io.github.khristophere.topherstools.globalutils.Utils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class BTGet {

    public BTGet(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new Cooldown(2000);
    }
    private final Cooldown coolDowns;

    /**
     * No argument GetBrushTool command, provided the sender is a player, provides the
     * currently held items' bound Brush/Tool command and modifiers in a click to apply format.
     * @param sender The command sender.
     */
    @Command("getbrush")
    @Command("gb")
    @Command("gettool")
    @Command("gt")
    @Permission("topherstools.bt.get.self")
    private void getBrushTool(Source sender) {
        if (sender.source() instanceof Player player) {
            Scheduler.runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                BTLoadout loadout = BTCacheManager.getBrushToolSQL().getBrushToolLoadoutSQL(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
                if (loadout != null && !loadout.isEmpty()) {
                    player.sendMessage(PlayerMessages.BrushTool_Info_Below_Self.getMessage());
                    player.sendMessage(loadout.getLoadoutPromptMessage());
                } else {
                    player.sendMessage(PlayerMessages.BrushTool_Not_Bound_Self.getMessage());
                }
                coolDowns.placeCooldown(player);
            });
        }
    }

    /**
     * Single argument GetBrushTool command, the sender provides a requested player name, this then provides the
     * requested player's currently held items' bound Brush/Tool command and modifiers in a click to apply format.
     * @param sender The command sender.
     * @param requestedPlayer The requested player's name.
     */
    @Command("getbrush <player>")
    @Command("gb <player>")
    @Command("gettool <player>")
    @Command("gt <player>")
    @Permission("topherstools.bt.get.specified.player")
    private void getBrushToolByPlayer(Source sender, @Argument(value = "player", suggestions = "suggestedPlayers") String requestedPlayer) {
        if (sender.source() instanceof Player player) {
            Player onlinePlayer = Bukkit.getPlayer(requestedPlayer);
            Scheduler.runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                if (onlinePlayer == null) {
                    player.sendMessage(PlayerMessages.BrushTool_Info_Inquiry_Player_Offline_Requesting_Specify_Material.getMessage());
                } else {
                    BTLoadout loadout = BTCacheManager.getBrushToolSQL().getBrushToolLoadoutSQL(onlinePlayer.getUniqueId(), onlinePlayer.getInventory().getItemInMainHand().getType().name());
                    if (loadout != null && !loadout.isEmpty()) {
                        player.sendMessage(PlayerMessages.BrushTool_Info_Below_Specified_Player.getMessage(onlinePlayer.getName()));
                        player.sendMessage(loadout.getLoadoutPromptMessage());
                    } else {
                        player.sendMessage(PlayerMessages.BrushTool_Not_Bound_Specified_Player.getMessage(onlinePlayer.getName()));
                    }
                }
                coolDowns.placeCooldown(player);
            });
        }
    }

    /**
     * Double argument GetBrushTool command, the sender provides a requested player name & requested material, this then
     * provides the requested player's bound Brush/Tool command and modifiers to that material in a click to apply format.
     * @param sender The command sender.
     * @param requestedPlayer The requested player's name.
     * @param requestedMaterial The requested player's requested material.
     */
    @Command("getbrush <player> <material>")
    @Command("gb <player> <material>")
    @Command("gettool <player> <material>")
    @Command("gt <player> <material>")
    @Permission("topherstools.bt.get.specified.player.specified.material")
    private void getBrushToolByPlayerByMaterial(Source sender, @Argument(value = "player", suggestions = "suggestedPlayers") String requestedPlayer, @Argument(value = "material", suggestions = "suggestedMaterials") String requestedMaterial) {
        if (sender.source() instanceof Player player) {
            Scheduler.runSequentialAsync( () -> {
                if (coolDowns.needsToWait(player)) return;
                UUID requestedPlayerUUID = BTCacheManager.getUsernameUUIDMapInCache().get(requestedPlayer);
                BTLoadout loadout = BTCacheManager.getBrushToolSQL().getBrushToolLoadoutSQL(requestedPlayerUUID, requestedMaterial.toUpperCase());
                if (loadout != null && !loadout.isEmpty()) {
                    player.sendMessage(PlayerMessages.BrushTool_Info_Below_Specified_Player_Specified_Material.getMessage(requestedPlayer, requestedMaterial));
                    player.sendMessage(loadout.getLoadoutPromptMessage());
                } else {
                    player.sendMessage(PlayerMessages.BrushTool_Not_Bound_Specified_Player_Specified_Material.getMessage(requestedPlayer, requestedMaterial));
                }
                coolDowns.placeCooldown(player);
            });
        }
    }

    /**
     * Provides a filtered and limited list of suggested player names.
     *
     * @param context The command context.
     * @param input   The current string the user has typed so far.
     * @return A filtered, capped list of matching usernames.
     */
    @Suggestions("suggestedPlayers")
    public List<String> getSuggestedPlayers(CommandContext<Source> context, String input) {
        return BTCacheManager.getUsernameUUIDMapInCache().keySet().stream()
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .limit(25)
                .toList();
    }

    /**
     * Provides a filtered and limited list of suggested materials for a target player.
     *
     * @param context The command context.
     * @param input   The current string the user has typed so far.
     * @return A filtered, capped list of matching materials.
     */
    @Suggestions("suggestedMaterials")
    public List<String> getSuggestedMaterials(CommandContext<CommandSender> context, String input) {
        String requestedPlayer = context.getOrDefault("player", null);
        if (requestedPlayer == null || requestedPlayer.isEmpty())
            return Collections.emptyList();
        UUID targetUUID = BTCacheManager.getUsernameUUIDMapInCache().get(requestedPlayer);
        if (targetUUID == null)
            return Collections.emptyList();
        List<String> targetMaterials = BTCacheManager.getUUIDMaterialBindsInCache().get(targetUUID);
        if (targetMaterials == null || targetMaterials.isEmpty())
            return Collections.emptyList();
        return targetMaterials.stream()
                .filter(material -> material.toLowerCase().contains(input.toLowerCase()))
                .limit(25)
                .toList();
    }

}
