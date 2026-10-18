package khristopher.FAWE_BT_Utils.functionalities.loadout;

import khristopher.FAWE_BT_Utils.b.messages.FAWEBrushTool.PlayerMessages;
import khristopher.FAWE_BT_Utils.listeners.fawetool.arguments.brushToolBinding;
import khristopher.FAWE_BT_Utils.listeners.fawetool.arguments.brushToolModify;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BrushToolLoadout {

    private final String material;
    private final String primaryCommand;
    private final String secondaryCommand;
    private final List<String> modifiers;
    private final ItemStack itemStack;

    /**
     * Returns a class with the given players brush/tool details for a given material. With which you can apply to another player.
     */
    public BrushToolLoadout(String material, ItemStack itemStack, String primaryCommand, String secondaryCommand, List<String> modifiers){
        this.material = Optional.ofNullable(material).orElse("N/A");
        this.primaryCommand = Optional.ofNullable(primaryCommand).orElse("N/A");
        this.secondaryCommand = Optional.ofNullable(secondaryCommand).orElse("N/A");
        if (modifiers == null) modifiers = new ArrayList<>();
        this.modifiers = modifiers;
        this.itemStack = itemStack;
    }

    public String getMaterial(){ return this.material; }
    public String getPrimaryCommand(){ return this.primaryCommand; }
    public String getSecondaryCommand(){ return this.secondaryCommand; }
    public List<String> getModifiers(){ return this.modifiers; }

    /**
     * Checks if the Brush Loadout Values all Equal N/A = Empty Loadout.
     * @return True if Empty, False if Not.
     */
    public boolean isBrushToolLoadoutEmpty(){
        return primaryCommand.equals("N/A") && secondaryCommand.equals("N/A") && modifiers.isEmpty();
    }

    /**
     * Checks if the Brush Loadout has a Custom Item.
     * @return true if it has custom ItemStack, false if not.
     */
    public boolean hasCustomItem(){
        return itemStack != null;
    }

    /**
     * Returns the Custom Item Stack associated.
     * @return the Custom Item Stack, if present, else null.
     */
    @Nullable
    public ItemStack getCustomItem(){
        return itemStack;
    }

    /**
     * Applies this BrushToolLoadout to the given player. Provided they're online.
     * @param uuid Player UUID to apply the brush/tool loadout to.
     */
    public void applyTo(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()){
            if (!primaryCommand.equals("N/A")) brushToolBinding.processCommand(player, primaryCommand);
            if (!secondaryCommand.equals("N/A")) brushToolBinding.processCommand(player, secondaryCommand);
            if (!modifiers.isEmpty()) modifiers.forEach(modifier -> brushToolModify.processCommand(player, modifier));
        }
    }

    /**
     * Applies this BrushToolLoadout with the Custom Item (if present, if not defaults to applyTo(uuid)) to the given player. Provided they're online.
     * This will take the players currently held item, temporarily remove it, then add it back to their inventory.
     * @param uuid Player UUID to apply the brush/tool loadout to.
     */
    public void applyToWithCustomItem(UUID uuid, boolean addBackCurrentlyHeldItem){
        if (itemStack == null) {
            applyTo(uuid);
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()){
            ItemStack existingHeldItem = player.getInventory().getItemInMainHand();
            player.getInventory().setItemInMainHand(itemStack);
            if (!primaryCommand.equals("N/A")) brushToolBinding.processCommand(player, primaryCommand);
            if (!secondaryCommand.equals("N/A")) brushToolBinding.processCommand(player, secondaryCommand);
            if (!modifiers.isEmpty()) modifiers.forEach(modifier -> brushToolModify.processCommand(player, modifier));
            if (addBackCurrentlyHeldItem) player.getInventory().addItem(existingHeldItem);
        }
    }

    public void applyToSpecifiedSlot(Player player, int slot){
        if (material.equals("N/A")) return;
        ItemStack localStack = itemStack;
        Material localMaterial = Material.matchMaterial(material);
        if (localMaterial == null && localStack == null) return;
        if (localStack == null) localStack = new ItemStack(localMaterial);
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, localStack);
        if (!primaryCommand.equals("N/A")) brushToolBinding.processCommand(player, primaryCommand);
        if (!secondaryCommand.equals("N/A")) brushToolBinding.processCommand(player, secondaryCommand);
        if (!modifiers.isEmpty()) modifiers.forEach(modifier -> brushToolModify.processCommand(player, modifier));
        if (slot != 0){
            ItemStack movedItem = player.getInventory().getItem(0);
            player.getInventory().setItem(slot, movedItem);
            player.getInventory().setItem(0, null);
        }
    }

    /**
     * Provides a clickable component message the player can use to apply the brush/tool commands.
     * @return Clickable component message.
     */
    public Component getLoadoutPromptMessage(){
        String commandList = "";
        if (!primaryCommand.equals("N/A")) commandList = "&8[&aPRIMARY&8] &e" + primaryCommand + "\n";
        if (!secondaryCommand.equals("N/A")) commandList = commandList.concat("&8[&bSECONDARY&8] &e" + secondaryCommand + "\n");
        if (!modifiers.isEmpty()) {
            commandList = commandList.concat("&8[&eMODIFIERS&8] &e");
            for (int i = 0; i < modifiers.size(); i++) {
                if (i == modifiers.size() - 1) {
                    commandList = commandList.concat(modifiers.get(i) + "&7.\n");
                    break;
                }
                commandList = commandList.concat(modifiers.get(i) + "&7, &e");
            }
        }
        if (commandList.isEmpty())
            return PlayerMessages.Warning_BrushTool_Is_Empty.getMessage();
        TextComponent CommandListComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(commandList);
        TextComponent Please = LegacyComponentSerializer.legacyAmpersand().deserialize("&7Please ");
        Component ClickHere = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                                .clickEvent(ClickEvent.callback(audience -> {
                                    if (audience instanceof Player player) {
                                        applyTo(player.getUniqueId());
                                        player.sendMessage(PlayerMessages.BrushTool_Applied.getMessage());
                                    }
                                },
                        options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
        TextComponent ToApply = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to your currently held item. &c(Expires in 3m, 3 uses)");
        if (hasCustomItem()){
            TextComponent customItemOR = LegacyComponentSerializer.legacyAmpersand().deserialize("\n&7OR ");
            Component customItemClickHere = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                    .clickEvent(ClickEvent.callback(audience -> {
                                if (audience instanceof Player player) {
                                    applyToWithCustomItem(player.getUniqueId(), true);
                                    player.sendMessage(PlayerMessages.BrushTool_Applied.getMessage());
                                }
                            },
                            options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
            TextComponent customItemToApply = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to a copy of the &6Original Item&7. &c(Expires in 3m, 3 uses)");
            ToApply = ToApply.append(customItemOR).append(customItemClickHere).append(customItemToApply);
        }
        return CommandListComponent.append(Please).append(ClickHere).append(ToApply);
    }

}
