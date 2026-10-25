package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.brushToolBinding;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.brushToolModify;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.brushToolUnbind;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.PlayerMessages;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a configurable loadout for brush/tool commands, materials, and custom items.
 */
public class BTLoadout {

    private static final String NA = "N/A";
    private final String material;
    private final String primaryCommand;
    private final String secondaryCommand;
    private final List<String> modifiers;
    private final @Nullable ItemStack itemStack;

    /**
     * Constructs a new {@code BTLoadout} (brush/tool loadout) with the specified material, item stack,
     * commands, and modifier list.
     *
     * @param material         the material associated with the loadout, or null
     * @param itemStack        the optional custom item stack, or null
     * @param primaryCommand   the primary brush/tool command, or null
     * @param secondaryCommand the secondary brush/tool command, or null
     * @param modifiers        the list of modifier commands to apply
     */
    public BTLoadout(@Nullable String material, @Nullable ItemStack itemStack, @Nullable String primaryCommand, @Nullable String secondaryCommand, @Nullable List<String> modifiers) {
        this.material = Objects.requireNonNullElse(material, NA);
        this.itemStack = itemStack != null ? itemStack.clone() : null;
        this.primaryCommand = Objects.requireNonNullElse(primaryCommand, NA);
        this.secondaryCommand = Objects.requireNonNullElse(secondaryCommand, NA);
        this.modifiers = modifiers != null ? List.copyOf(modifiers) : List.of();
    }

    /**
     * Gets the material string associated with this loadout.
     *
     * @return the material name or "N/A"
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Gets the primary command associated with this loadout.
     *
     * @return the primary command or "N/A"
     */
    public String getPrimaryCommand() {
        return primaryCommand;
    }

    /**
     * Gets the secondary command associated with this loadout.
     *
     * @return the secondary command or "N/A"
     */
    public String getSecondaryCommand() {
        return secondaryCommand;
    }

    /**
     * Gets an unmodifiable list of modifiers associated with this loadout.
     *
     * @return an unmodifiable list of modifier strings
     */
    public List<String> getModifiers() {
        return modifiers;
    }

    /**
     * Checks whether this brush/tool loadout is considered empty
     * (i.e., no primary/secondary commands and no modifiers).
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return NA.equals(primaryCommand) && NA.equals(secondaryCommand) && modifiers.isEmpty();
    }

    /**
     * Checks if this loadout has a custom item stack defined.
     *
     * @return true if a custom item stack is present, false otherwise
     */
    public boolean hasCustomItem() {
        return itemStack != null;
    }

    /**
     * Gets a clone of the custom item stack associated with this loadout, if present.
     *
     * @return a cloned custom {@link ItemStack}, or null if none is set
     */
    @Nullable
    public ItemStack getCustomItem() {
        return itemStack != null ? itemStack.clone() : null;
    }

    /**
     * Applies this brush/tool loadout's bindings to the player with the specified UUID,
     * provided they are currently online.
     *
     * @param uuid the target player's UUID
     */
    public void applyTo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            bindingCommands(player);
        }
    }

    /**
     * Applies this brush/tool loadout with the custom item to the player with the specified UUID,
     * provided they are online. Temporarily handles the player's currently held item.
     *
     * @param uuid                     the target player's UUID
     * @param addBackCurrentlyHeldItem whether to return the previously held item to inventory
     */
    public void applyToWithCustomItem(UUID uuid, boolean addBackCurrentlyHeldItem) {
        if (itemStack == null) {
            applyTo(uuid);
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            ItemStack existingHeldItem = player.getInventory().getItemInMainHand();
            player.getInventory().setItemInMainHand(itemStack.clone());
            bindingCommands(player);
            if (addBackCurrentlyHeldItem && !existingHeldItem.getType().isAir()) {
                player.getInventory().addItem(existingHeldItem);
            }
        }
    }

    /**
     * Applies this loadout and its item to a specified inventory slot for the given player.
     *
     * @param player the target player
     * @param slot   the inventory slot index
     */
    public boolean applyToSpecifiedSlot(Player player, int slot) {
        if (NA.equals(material)) return false;
        ItemStack localStack = itemStack != null ? itemStack.clone() : null;
        if (localStack == null) {
            Material localMaterial = Material.matchMaterial(material);
            if (localMaterial == null) return false;
            localStack = new ItemStack(localMaterial);
        }
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, localStack);
        bindingCommands(player);
        if (slot != 0){
            ItemStack movedItem = player.getInventory().getItem(0);
            player.getInventory().setItem(slot, movedItem);
            player.getInventory().setItem(0, null);
        }
        return true;
    }

    /**
     * Internal helper to process and bind all commands and modifiers to the player.
     *
     * @param player the target player
     */
    private void bindingCommands(Player player) {
        brushToolUnbind.processCommand(player, "none", true);

        if (!NA.equals(primaryCommand))
            brushToolBinding.processCommand(player, primaryCommand, true);

        if (!NA.equals(secondaryCommand))
            brushToolBinding.processCommand(player, secondaryCommand, true);

        if (!modifiers.isEmpty())
            modifiers.forEach(modifier -> brushToolModify.processCommand(player, modifier, true));
    }


    /**
     * Generates a rich interactive Adventure component prompt message detailing the loadout
     * and allowing quick application via click events.
     *
     * @return the interactive component prompt message
     */
    public Component getLoadoutPromptMessage() {
        if (isEmpty())
            return PlayerMessages.Warning_BrushTool_Is_Empty.getMessage();

        StringBuilder commandListBuilder = new StringBuilder();
        if (!NA.equals(primaryCommand))
            commandListBuilder.append("&8[&aPRIMARY&8] &e").append(primaryCommand).append("\n");
        if (!NA.equals(secondaryCommand))
            commandListBuilder.append("&8[&bSECONDARY&8] &e").append(secondaryCommand).append("\n");
        if (!modifiers.isEmpty()) {
            commandListBuilder.append("&8[&eMODIFIERS&8] &e");
            for (int i = 0; i < modifiers.size(); i++) {
                if (i == modifiers.size() - 1) {
                    commandListBuilder.append(modifiers.get(i)).append("&7.\n");
                    break;
                }
                commandListBuilder.append(modifiers.get(i)).append("&7, &e");
            }
        }

        TextComponent commandListComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(commandListBuilder.toString());
        TextComponent pleaseComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7Please ");
        Component clickHereComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player player) {
                        applyTo(player.getUniqueId());
                        player.sendMessage(PlayerMessages.BrushTool_Applied.getMessage());
                    }
                }, options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
        TextComponent toApplyComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to your currently held item. &c(Expires in 3m, 3 uses)");

        if (hasCustomItem()) {
            TextComponent customItemOrComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("\n&7OR ");
            Component customItemClickHereComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof Player player) {
                            applyToWithCustomItem(player.getUniqueId(), true);
                            player.sendMessage(PlayerMessages.BrushTool_Applied.getMessage());
                        }
                    }, options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
            TextComponent customItemToApplyComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to a copy of the &6Original Item&7. &c(Expires in 3m, 3 uses)");
            toApplyComponent = toApplyComponent.append(customItemOrComponent).append(customItemClickHereComponent).append(customItemToApplyComponent);
        }

        return commandListComponent.append(pleaseComponent).append(clickHereComponent).append(toApplyComponent);
    }

}
