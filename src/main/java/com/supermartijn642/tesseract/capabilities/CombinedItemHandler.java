package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedItemHandler implements IItemHandler {

    private final Channel channel;
    private final TesseractBlockEntity requester;

    public CombinedItemHandler(Channel channel, TesseractBlockEntity requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int getSlots(){
        if(this.pushRecurrentCall())
            return 0;

        int slots = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER))
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

        ItemStack stack = ItemStack.EMPTY;
        int slots = 0;
        loop:
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(slot - slots < handler.getSlots()){
                            stack = handler.getStackInSlot(slot - slots);
                            break loop;
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
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
        if(this.pushRecurrentCall())
            return stack;

        if(!this.requester.canSend(EnumChannelType.ITEMS) || stack.isEmpty()){
            this.popRecurrentCall();
            return stack;
        }

        ItemStack leftOver = stack;
        int slots = 0;
        loop:
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(slot - slots < handler.getSlots()){
                            leftOver = reference.canReceive(EnumChannelType.ITEMS) ? handler.insertItem(slot - slots, stack, simulate) : stack;
                            break loop;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return leftOver;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate){
        if(this.pushRecurrentCall())
            return ItemStack.EMPTY;

        if(!this.requester.canReceive(EnumChannelType.ITEMS) || amount <= 0){
            this.popRecurrentCall();
            return ItemStack.EMPTY;
        }

        ItemStack stack = ItemStack.EMPTY;
        int slots = 0;
        loop:
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(slot - slots < handler.getSlots()){
                            stack = reference.canSend(EnumChannelType.ITEMS) ? handler.extractItem(slot - slots, amount, simulate) : ItemStack.EMPTY;
                            break loop;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return stack;
    }

    @Override
    public int getSlotLimit(int slot){
        if(this.pushRecurrentCall())
            return 0;

        int limit = 0;
        int slots = 0;
        loop:
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(slot - slots < handler.getSlots()){
                            limit = handler.getSlotLimit(slot - slots);
                            break loop;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return limit;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack){
        if(this.pushRecurrentCall())
            return false;

        boolean valid = false;
        int slots = 0;
        loop:
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(slot - slots < handler.getSlots()){
                            valid = handler.isItemValid(slot - slots, stack);
                            break loop;
                        }else
                            slots += handler.getSlots();
                    }
                }
            }
        }

        this.popRecurrentCall();

        return valid;
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
