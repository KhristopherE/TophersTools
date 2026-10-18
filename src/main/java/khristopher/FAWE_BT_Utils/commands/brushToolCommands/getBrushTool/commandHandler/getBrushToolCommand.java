package khristopher.FAWE_BT_Utils.commands.brushToolCommands.getBrushTool.commandHandler;

import khristopher.FAWE_BT_Utils.cooldown.CoolDownManager;
import khristopher.a.main.TophersTools;
import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.b.utils.Utils;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolLoadout;
import khristopher.FAWE_BT_Utils.functionalities.loadout.BrushToolManager;
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


public class getBrushToolCommand {

    /**
     * Instantiates the GetBrushTool Command.
     */
    public getBrushToolCommand(){
        Utils.registerClassWithCommandManager(this);
        this.coolDowns = new CoolDownManager(2000);
    }

    private final CoolDownManager coolDowns;

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
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                if (coolDowns.needsToWait(player)) return;
                BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(player.getUniqueId(), player.getInventory().getItemInMainHand().getType().name());
                if (loadout != null && !loadout.isBrushToolLoadoutEmpty()) {
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
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                if (coolDowns.needsToWait(player)) return;
                if (onlinePlayer == null) {
                    player.sendMessage(PlayerMessages.BrushTool_Info_Inquiry_Player_Offline_Requesting_Specify_Material.getMessage());
                } else {
                    BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(onlinePlayer.getUniqueId(), onlinePlayer.getInventory().getItemInMainHand().getType().name());
                    if (loadout != null && !loadout.isBrushToolLoadoutEmpty()) {
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
            Bukkit.getScheduler().runTaskAsynchronously(TophersTools.getInstance(), () -> {
                if (coolDowns.needsToWait(player)) return;
                UUID requestedPlayerUUID = BrushToolManager.allPlayersInSQL.get(requestedPlayer);
                BrushToolLoadout loadout = BrushToolManager.brushToolSQL.getBrushToolLoadoutSQL(requestedPlayerUUID, requestedMaterial.toUpperCase());
                if (loadout != null && !loadout.isBrushToolLoadoutEmpty()) {
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
     * Provides a list of suggested player names for a command.
     * @return A matching list of online and offline player names.
     */
    @Suggestions("suggestedPlayers")
    public List<String> getSuggestedPlayers(CommandContext<Source> context, String input) {
        return BrushToolManager.allPlayersInSQL.keySet().stream().toList();
    }

    /**
     * Provides a list of suggested materials for a command.
     * @return A matching list of materials that are both, items & not blocks.
     */
    @Suggestions("suggestedMaterials")
    public List<String> getSuggestedMaterials(CommandContext<CommandSender> context, String input) {
        String requestedPlayer = context.getOrDefault("player", null);
        if (requestedPlayer == null || requestedPlayer.isEmpty()) return Collections.emptyList();
        UUID targetUuid = BrushToolManager.allPlayersInSQL.get(requestedPlayer);
        if (targetUuid == null) return Collections.emptyList();
        List<String> targetMaterials = BrushToolManager.allUUIDsToListMaterialsBound.get(targetUuid);
        return targetMaterials != null ? targetMaterials : Collections.emptyList();
    }

}
