package me.imbanana.whatwemissed.screen.minecartfurnace;

import me.imbanana.whatwemissed.screen.ModMenuTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MinecartFurnaceMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public MinecartFurnaceMenu(final int containerId, final Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1), new SimpleContainerData(3));
    }

    public MinecartFurnaceMenu(final int containerId, final Inventory inventory, final Container container, final ContainerData data) {
        super(ModMenuTypes.MINECART_FURNACE.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;

        this.addSlot(new FuelSlot(this.container, 0, 80, 16));
        this.addStandardInventorySlots(inventory, 8, 61);

        this.addDataSlots(this.data);
    }

    public boolean isLit() {
        return this.data.get(2) == 1;
    }

    public float getFuelProgress() {
        int totalFuel = this.data.get(1);
        int currentFuel = this.data.get(0);

        return Math.clamp(currentFuel / (float) totalFuel, 0f, 1f);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();

        if (slotIndex < this.container.getContainerSize()) {
            if (!this.moveItemStackTo(stack, this.container.getContainerSize(), this.container.getContainerSize() + Inventory.INVENTORY_SIZE, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return clicked;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public static class FuelSlot extends Slot {
        public FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return itemStack.has(DataComponents.COOKING_FUEL);
        }

        @Override
        public int getMaxStackSize(ItemStack itemStack) {
            return itemStack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(itemStack);
        }
    }
}
