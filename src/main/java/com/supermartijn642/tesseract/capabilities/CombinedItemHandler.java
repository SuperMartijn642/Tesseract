package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedItemHandler implements IItemHandler {

    private final Channel channel;
    private final TesseractTile requester;

    public CombinedItemHandler(Channel channel, TesseractTile requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int getSlots(){
        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                        slots += handler.getSlots();
                }
            }
        }
        return slots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot){
        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots())
                            return handler.getStackInSlot(slot - slots);
                        else
                            slots += handler.getSlots();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
        if(!this.requester.canSend(EnumChannelType.ITEMS) || stack.isEmpty())
            return stack;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots())
                            return reference.canReceive(EnumChannelType.ITEMS) ? handler.insertItem(slot - slots, stack, simulate) : stack;
                        else
                            slots += handler.getSlots();
                    }
                }
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate){
        if(!this.requester.canReceive(EnumChannelType.ITEMS) || amount <= 0)
            return ItemStack.EMPTY;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots())
                            return reference.canSend(EnumChannelType.ITEMS) ? handler.extractItem(slot - slots, amount, simulate) : ItemStack.EMPTY;
                        else
                            slots += handler.getSlots();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot){
        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots())
                            return handler.getSlotLimit(slot - slots);
                        else
                            slots += handler.getSlots();
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack){
        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots())
                            return handler.isItemValid(slot - slots, stack);
                        else
                            slots += handler.getSlots();
                    }
                }
            }
        }
        return false;
    }
}
