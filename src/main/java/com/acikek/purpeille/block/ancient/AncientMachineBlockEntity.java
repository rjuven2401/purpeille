package com.acikek.purpeille.block.ancient;

import com.acikek.purpeille.Purpeille;
import com.acikek.purpeille.item.core.EncasedCore;
import lib.ImplementedInventory;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class AncientMachineBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory {

    public DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public AncientMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getItem() {
        return getStack(0);
    }

    /**
     * Sets the singular item within the machine.
     * This modifies the inventory directly and should be used for player actions.
     * To handle transfer interactions, override {@link net.minecraft.inventory.Inventory#setStack(int, ItemStack)}.
     * @param stack The stack to add. Will not be modified.
     */
    public void setItem(ItemStack stack) {
        ItemStack newStack = stack.copy();
        if (newStack.getCount() > 1) {
            newStack.setCount(1);
        }
        items.set(0, newStack);
    }

    public void setItem(Item item) {
        setItem(item.getDefaultStack());
    }

    /**
     * Removes the singular item within the machine.
     * This modifies the inventory directly and should be used for player actions.
     * To handle transfer interactions, override {@link net.minecraft.inventory.Inventory#removeStack(int, int)}.
     */
    public void removeItem() {
        setItem(ItemStack.EMPTY);
    }

    public EncasedCore getCore() {
        if (getItem().getItem() instanceof EncasedCore core) {
            return core;
        }
        return null;
    }

    public boolean checkCore(PlayerEntity player, Hand hand) {
        if (player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
            EncasedCore core = getCore();
            if (core != null) {
                MutableText text = new TranslatableText(core.getTranslationKey()).formatted(core.type.rarity.formatting);
                player.sendMessage(text, true);
            }
            return false;
        }
        return true;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[1];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        super.writeNbt(nbt);
    }

    public static <T extends BlockEntity> BlockEntityType<T> build(String id, FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        return Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                Purpeille.id(id),
                FabricBlockEntityTypeBuilder.create(factory, block)
                        .build(null)
        );
    }
}
