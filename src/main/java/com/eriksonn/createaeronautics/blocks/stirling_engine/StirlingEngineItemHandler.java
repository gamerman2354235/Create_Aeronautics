package com.eriksonn.createaeronautics.blocks.stirling_engine;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class StirlingEngineItemHandler  implements IItemHandlerModifiable {
    private StirlingEngineTileEntity te;
    public StirlingEngineItemHandler(StirlingEngineTileEntity te)
    {
        this.te=te;
    }
    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        set(stack);
    }
    public void set(ItemStack stack) {
        if (this.te.currentStack != null) {
            if (!this.te.getLevel().isClientSide) {
                this.te.currentStack=stack;
                this.te.setChanged();
                this.te.sendData();
            }
        }
    }
    @Override
    public int getSlots() {
        return 1;
    }
    public ItemStack getHeld() {
        return this.te.currentStack == null ? ItemStack.EMPTY : this.te.currentStack;
    }
    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.te.currentStack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack held = this.getHeld();
        if (!this.isItemValid(slot, stack)) {
            return stack;
        }
        if (held.isEmpty()) {
            if (!simulate) {
                this.set(stack);
            }
            return ItemStack.EMPTY;
        }
        if (!ItemHandlerHelper.canItemStacksStack(held, stack))
            return stack;
        int space = held.getMaxStackSize() - held.getCount();
        ItemStack remainder = stack.copy();
        ItemStack split = remainder.split(space);
        if (space == 0) {
            return stack;
        } else {
            if (!simulate) {
                held = held.copy();
                held.setCount(held.getCount() + split.getCount());
                this.set(held);
            }

            return remainder;
        }

    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack extractedFromOverflow = ItemStack.EMPTY;
        ItemStack returnToOverflow = ItemStack.EMPTY;
        //Iterator iterator = this.te.overflowItems.iterator();
        ItemStack toReturn;

        if (!extractedFromOverflow.isEmpty()) {
            return extractedFromOverflow;
        } else {
            ItemStack held = this.getHeld();
            if (!held.isEmpty()) {

                if (simulate) {
                    return held.copy().split(amount);
                } else {
                    toReturn = held.split(amount);
                    this.te.setChanged();
                    this.te.sendData();
                    return toReturn;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

    }

    @Override
    public int getSlotLimit(int slot) {
        if(!this.te.currentStack.isEmpty())
            return this.getHeld().getMaxStackSize();
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, null)>0;
    }
}
