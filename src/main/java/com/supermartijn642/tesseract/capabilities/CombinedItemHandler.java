package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.TesseractLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedItemHandler implements IItemHandler {

    private final List<TesseractLocation> tesseracts;
    private final TesseractTile requester;

    public CombinedItemHandler(List<TesseractLocation> tesseracts, TesseractTile requester){
        this.tesseracts = tesseracts;
        this.requester = requester;
    }

    @Override
    public int getSlots(){
        int slots = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid()){
                for(IItemHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                    slots += handler.getSlots();
            }
        }
        return slots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot){
        int slots = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IItemHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                    if(slot - slots < handler.getSlots())
                        return handler.getStackInSlot(slot - slots);
                    else
                        slots += handler.getSlots();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
        if(!this.requester.canSend(EnumChannelType.ITEMS))
            return stack;
        int slots = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IItemHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                    if(slot - slots < handler.getSlots())
                        return location.canReceive(EnumChannelType.ITEMS) ? handler.insertItem(slot - slots, stack, simulate) : stack;
                    else
                        slots += handler.getSlots();
                }
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate){
        if(!this.requester.canReceive(EnumChannelType.ITEMS))
            return ItemStack.EMPTY;
        int slots = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IItemHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                    if(slot - slots < handler.getSlots())
                        return location.canSend(EnumChannelType.ITEMS) ? handler.extractItem(slot - slots, amount, simulate) : ItemStack.EMPTY;
                    else
                        slots += handler.getSlots();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot){
        int slots = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IItemHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                    if(slot - slots < handler.getSlots())
                        return handler.getSlotLimit(slot);
                    else
                        slots += handler.getSlots();
                }
            }
        }
        return 0;
    }

}
