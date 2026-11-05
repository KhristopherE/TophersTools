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

package io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.btloadout;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.world.item.ItemType;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.config.Config;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTBinding;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTModifying;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.arguments.BTUnbinding;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.listeners.util.BTModifier;
import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.messages.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a configurable loadout for brush / tool commands, materials, and custom items.
 */
public class BTLoadout {

    private static final String NA = "N/A";
    private final String material;
    private final String primaryCommand;
    private final String secondaryCommand;
    private final List<String> primaryModifiers;
    private final List<String> secondaryModifiers;
    private final List<String> globalModifiers;
    private final @Nullable ItemStack itemStack;

    /**
     * Constructs a new BTLoadout ( brush / tool loadout ) with the specified material, item stack, commands, and modifiers.
     *
     * @param material           the material associated with the loadout, or null.
     * @param itemStack          the optional custom item stack, or null.
     * @param primaryCommand     the primary brush / tool command, or null.
     * @param secondaryCommand   the secondary brush / tool command, or null.
     * @param primaryModifiers   the primary brush / tool modifiers, or null.
     * @param secondaryModifiers the secondary brush / tool modifiers, or null.
     * @param globalModifiers    the global brush / tool modifiers, or null.
     */
    public BTLoadout(@Nullable String material,
                     @Nullable ItemStack itemStack,
                     @Nullable String primaryCommand,
                     @Nullable String secondaryCommand,
                     @Nullable List<String> primaryModifiers,
                     @Nullable List<String> secondaryModifiers,
                     @Nullable List<String> globalModifiers) {
        this.material = Objects.requireNonNullElse(material, NA);
        this.itemStack = itemStack != null ? itemStack.clone() : null;
        this.primaryCommand = Objects.requireNonNullElse(primaryCommand, NA);
        this.secondaryCommand = Objects.requireNonNullElse(secondaryCommand, NA);
        this.primaryModifiers = primaryModifiers != null ? List.copyOf(primaryModifiers) : List.of();
        this.secondaryModifiers = secondaryModifiers != null ? List.copyOf(secondaryModifiers) : List.of();
        this.globalModifiers = globalModifiers != null ? List.copyOf(globalModifiers) : List.of();
    }

    /**
     * Appends a comma-separated list of modifiers to the command list builder.
     *
     * @param commandListBuilder the string builder to append to.
     * @param modifiers          the list of modifiers.
     */
    public static void AppendCommaSeparatedList(@NotNull StringBuilder commandListBuilder, @NotNull List<String> modifiers) {
        for (int i = 0; i < modifiers.size(); i++) {
            if (i == modifiers.size() - 1) {
                commandListBuilder.append(modifiers.get(i)).append("&7.\n");
                break;
            }
            commandListBuilder.append(modifiers.get(i)).append("&7, &e");
        }
    }

    /**
     * Gets the material name associated with this loadout.
     *
     * @return the material name or "N/A" if there is not one.
     */
    @NotNull
    public String getMaterial() {
        return material;
    }

    /**
     * Gets the primary command associated with this loadout.
     *
     * @return the primary command or "N/A".
     */
    @NotNull
    public String getPrimaryCommand() {
        return primaryCommand;
    }

    /**
     * Gets the secondary command associated with this loadout.
     *
     * @return the secondary command or "N/A".
     */
    @NotNull
    public String getSecondaryCommand() {
        return secondaryCommand;
    }

    /**
     * Gets an unmodifiable list of modifiers associated with this loadout.
     *
     * @param modifierType the modifier type to target.
     * @return an unmodifiable list of modifier commands.
     */
    @NotNull
    public List<String> getModifiers(@NotNull BTModifier.Type modifierType) {
        return switch (modifierType) {
            case GLOBAL -> globalModifiers;
            case PRIMARY -> primaryModifiers;
            case SECONDARY -> secondaryModifiers;
        };
    }

    /**
     * Checks whether this brush / tool loadout is considered empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return NA.equals(primaryCommand) && NA.equals(secondaryCommand) && primaryModifiers.isEmpty() && secondaryModifiers.isEmpty() && globalModifiers.isEmpty();
    }

    /**
     * Checks if this loadout has a custom item stack.
     *
     * @return true if a custom item stack is present, false otherwise.
     */
    public boolean hasCustomItem() {
        return itemStack != null;
    }

    /**
     * Gets a clone of the custom item stack associated with this loadout, if present.
     *
     * @return a cloned custom ItemStack, or null if none is set.
     */
    @Nullable
    public ItemStack getCustomItem() {
        return itemStack != null ? itemStack.clone() : null;
    }

    /**
     * Applies this brush / tool loadout's bindings to the player with the specified UUID.
     *
     * @param uuid the target player's UUID.
     */
    public void applyTo(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) bindingCommands(player);
    }

    /**
     * Applies this brush / tool loadout with the custom item to the player with the specified UUID.
     *
     * @param uuid                     the target player's UUID.
     * @param addBackCurrentlyHeldItem whether to return the previously held item to inventory.
     */
    public void applyToWithCustomItem(@NotNull UUID uuid, boolean addBackCurrentlyHeldItem) {
        if (itemStack == null) {
            applyTo(uuid);
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            ItemStack existingHeldItem = player.getInventory().getItemInMainHand();
            player.getInventory().setItemInMainHand(itemStack.clone());
            bindingCommands(player);
            if (addBackCurrentlyHeldItem && !existingHeldItem.getType().isAir())
                player.getInventory().addItem(existingHeldItem);
        }
    }

    /**
     * Applies this loadout and its item to a specified inventory slot for the given player.
     *
     * @param player the target player.
     * @param slot   the inventory slot.
     * @return true if successfully applied, false otherwise.
     */
    public boolean applyToSpecifiedSlot(@NotNull Player player, int slot) {
        if (NA.equals(material)) return false;
        ItemStack localStack = itemStack != null ? itemStack.clone() : null;
        if (localStack == null) {
            Material localMaterial = Material.matchMaterial(material);
            if (localMaterial == null) return false;
            localStack = new ItemStack(localMaterial);
        }
        player.getInventory().setHeldItemSlot(0);
        ItemStack retainedItem = player.getInventory().getItem(0);
        player.getInventory().setItem(0, localStack);
        bindingCommands(player);
        if (slot != 0) {
            ItemStack movedItem = player.getInventory().getItem(0);
            player.getInventory().setItem(slot, movedItem);
            player.getInventory().setItem(0, retainedItem);
        }
        return true;
    }

    /**
     * Internal helper to process and bind all commands and modifiers to the player.
     *
     * @param player the target player.
     */
    private void bindingCommands(@NotNull Player player) {
        boolean silenceFAWEFeedback = Config.silenceFAWEFeedback;
        BTUnbinding.processCommand(player, "none", silenceFAWEFeedback);

        if (!NA.equals(primaryCommand))
            BTBinding.processCommand(player, primaryCommand, silenceFAWEFeedback);

        if (!primaryModifiers.isEmpty())
            primaryModifiers.forEach(modifier -> BTModifying.processCommand(player, modifier, silenceFAWEFeedback));

        if (!NA.equals(secondaryCommand))
            BTBinding.processCommand(player, secondaryCommand, silenceFAWEFeedback);

        setBindingContext(player, BTModifier.Type.SECONDARY);

        if (!secondaryModifiers.isEmpty())
            secondaryModifiers.forEach(modifier -> BTModifying.processCommand(player, modifier, silenceFAWEFeedback));

        if (!globalModifiers.isEmpty())
            globalModifiers.forEach(modifier -> BTModifying.processCommand(player, modifier, silenceFAWEFeedback));
    }

    /**
     * Sets the binding context for the brush / tools.
     *
     * @param player             the target player.
     * @param bindingContextType the to be bound modifier type.
     */
    @SuppressWarnings("SameParameterValue")
    private void setBindingContext(@NotNull Player player, @NotNull BTModifier.Type bindingContextType) {
        Material identifiedMaterial = Material.matchMaterial(this.material);
        if (identifiedMaterial == null) {
            if (itemStack == null) return;
            identifiedMaterial = itemStack.getType();
        }
        com.sk89q.worldedit.entity.Player worldEditPlayer = BukkitAdapter.adapt(player);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
        ItemType worldEditItemType = BukkitAdapter.asItemType(identifiedMaterial);
        if (worldEditItemType == null) return;
        Tool tool = localSession.getTool(worldEditItemType.getDefaultState(), worldEditPlayer);
        if (tool instanceof BrushTool brushTool) {
            if (bindingContextType == BTModifier.Type.PRIMARY) brushTool.setContext(brushTool.getPrimary());
            else if (bindingContextType == BTModifier.Type.SECONDARY) brushTool.setContext(brushTool.getSecondary());
            // If global, do nothing.
        }
    }

    /**
     * Generates a rich interactive component prompt message detailing the loadout.
     *
     * @return the interactive component prompt message.
     */
    @NotNull
    public Component getLoadoutPromptMessage() {
        if (isEmpty()) return Messages.Warning_BrushTool_Is_Empty.getMessage();

        StringBuilder commandListBuilder = new StringBuilder();

        if (!NA.equals(primaryCommand))
            commandListBuilder.append("&8[&aPRIMARY&8] &e").append(primaryCommand).append("\n");
        if (!primaryModifiers.isEmpty()) {
            commandListBuilder.append("&8[&aPRIMARY MODIFIERS&8] &e");
            AppendCommaSeparatedList(commandListBuilder, primaryModifiers);
        }

        if (!NA.equals(secondaryCommand))
            commandListBuilder.append("&8[&bSECONDARY&8] &e").append(secondaryCommand).append("\n");
        if (!secondaryModifiers.isEmpty()) {
            commandListBuilder.append("&8[&bSECONDARY MODIFIERS&8] &e");
            AppendCommaSeparatedList(commandListBuilder, secondaryModifiers);
        }

        if (!globalModifiers.isEmpty()) {
            commandListBuilder.append("&8[&6GLOBAL MODIFIERS&8] &e");
            AppendCommaSeparatedList(commandListBuilder, globalModifiers);
        }

        TextComponent commandListComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(commandListBuilder.toString());
        TextComponent pleaseComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7Please ");
        Component clickHereComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player player) {
                        applyTo(player.getUniqueId());
                        player.sendMessage(Messages.BrushTool_Applied.getMessage());
                    }
                }, options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
        TextComponent toApplyComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to your currently held item. &c(Expires in 3m, 3 uses)");

        if (hasCustomItem()) {
            TextComponent customItemOrComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("\n&7OR ");
            Component customItemClickHereComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&aClick Here&8] ")
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof Player player) {
                            applyToWithCustomItem(player.getUniqueId(), true);
                            player.sendMessage(Messages.BrushTool_Applied.getMessage());
                        }
                    }, options -> options.uses(3).lifetime(Duration.ofMinutes(3))));
            TextComponent customItemToApplyComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7to apply to a copy of the &6Original Item&7. &c(Expires in 3m, 3 uses)");
            toApplyComponent = toApplyComponent.append(customItemOrComponent).append(customItemClickHereComponent).append(customItemToApplyComponent);
        }

        return commandListComponent.append(pleaseComponent).append(clickHereComponent).append(toApplyComponent);
    }

}
