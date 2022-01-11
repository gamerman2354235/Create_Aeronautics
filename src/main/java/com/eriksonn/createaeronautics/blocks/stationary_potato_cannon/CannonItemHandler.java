package com.eriksonn.createaeronautics.blocks.stationary_potato_cannon;

import com.simibubi.create.content.curiosities.weapons.PotatoProjectileTypeManager;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;

public class CannonItemHandler implements IItemHandlerModifiable {
    private StationaryPotatoCannonTileEntity te;
    //private ItemStack currentStack;
    public CannonItemHandler(StationaryPotatoCannonTileEntity te) {
        this.te = te;
        //currentStack=te.currentStack;
    }
    public int getSlots() {
        return 1;
    }

    public ItemStack getStackInSlot(int slot) {
        return this.te.currentStack;
    }
    public void set(ItemStack stack) {
        if (this.te.currentStack != null) {
            if (!this.te.getLevel().isClientSide) {
                this.te.currentStack=stack;
                this.te.currentStack=stack;
                this.te.setChanged();
                this.te.sendData();
            }
        }
    }
    public ItemStack getHeld() {
        return this.te.currentStack == null ? ItemStack.EMPTY : this.te.currentStack;
    }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack held = this.getHeld();
        if (!this.isItemValid(slot, stack)) {
            return stack;
        }
        else if (held.isEmpty()) {
            if (!simulate) {
                this.te.ItemTimer=0;
                if(stack.getCount()>16)
                {
                    ItemStack remainder = stack.copy();
                    ItemStack split = remainder.split(16);
                    this.set(split);
                    return remainder;
                }
                this.set(stack);

            }
            return ItemStack.EMPTY;
        } else if (!ItemHandlerHelper.canItemStacksStack(held, stack)) {
            return stack;
        } else {
            int space = Math.min(held.getMaxStackSize(),16) - held.getCount();
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
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        } else {
            ItemStack extractedFromOverflow = ItemStack.EMPTY;
            ItemStack returnToOverflow = ItemStack.EMPTY;
            //Iterator iterator = this.te.overflowItems.iterator();

            ItemStack toReturn;




            if (!extractedFromOverflow.isEmpty()) {
                return extractedFromOverflow;
            } else {
                ItemStack held = this.getHeld();
                if (amount != 0 && !held.isEmpty()) {
                    //if (!this.te.filtering.getFilter().isEmpty() && this.te.filtering.test(held)) {
                        //return ItemStack.EMPTY;
                    //} else
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
    }
    public int getSlotLimit(int slot) {
        int a = 16;
        if(!this.te.currentStack.isEmpty())
            a= Math.min(this.getHeld().getMaxStackSize(), 16);
        return a;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        Optional<ItemStack> found = PotatoProjectileTypeManager.getTypeForStack(stack).map(($) -> {
            return stack;
        });
        if(!found.isPresent())
            return false;

        FilteringBehaviour filteringBehaviour = (FilteringBehaviour)this.te.getBehaviour(FilteringBehaviour.TYPE);
        return filteringBehaviour == null || filteringBehaviour.test(stack);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        this.set(stack);
    }
}
