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
        if(this.pushRecurrentCall())
            return 0;

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

        this.popRecurrentCall();

        return slots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot){
        if(this.pushRecurrentCall())
            return ItemStack.EMPTY;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots()){
                            ItemStack stack = handler.getStackInSlot(slot - slots);
                            this.popRecurrentCall();
                            return stack;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
        if(this.pushRecurrentCall())
            return stack;

        if(!this.requester.canSend(EnumChannelType.ITEMS) || stack.isEmpty())
            return stack;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots()){
                            ItemStack leftOver = reference.canReceive(EnumChannelType.ITEMS) ? handler.insertItem(slot - slots, stack, simulate) : stack;
                            this.popRecurrentCall();
                            return leftOver;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate){
        if(this.pushRecurrentCall())
            return ItemStack.EMPTY;

        if(!this.requester.canReceive(EnumChannelType.ITEMS) || amount <= 0)
            return ItemStack.EMPTY;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots()){
                            ItemStack stack = reference.canSend(EnumChannelType.ITEMS) ? handler.extractItem(slot - slots, amount, simulate) : ItemStack.EMPTY;
                            this.popRecurrentCall();
                            return stack;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot){
        if(this.pushRecurrentCall())
            return 0;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots()){
                            int limit = handler.getSlotLimit(slot - slots);
                            this.popRecurrentCall();
                            return limit;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack){
        if(this.pushRecurrentCall())
            return false;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.isValid()){
                TesseractTile tile = reference.getTesseract();
                if(tile != this.requester){
                    for(IItemHandler handler : tile.getSurroundingCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)){
                        if(slot - slots < handler.getSlots()){
                            boolean valid = handler.isItemValid(slot - slots, stack);
                            this.popRecurrentCall();
                            return valid;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return false;
    }

    /**
     * Checks whether this is a recurrent call to this combined capability.
     * If not, it will just increase the recurrent call counter.
     */
    private boolean pushRecurrentCall(){
        if(this.requester.recurrentCalls >= 1)
            return true;
        this.requester.recurrentCalls++;
        return false;
    }

    private void popRecurrentCall(){
        this.requester.recurrentCalls--;
    }
}
