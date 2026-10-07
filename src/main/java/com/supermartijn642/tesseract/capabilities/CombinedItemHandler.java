package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedItemHandler implements IItemHandler {

    private final Channel channel;
    private final TesseractReference requester;

    public CombinedItemHandler(Channel channel, TesseractReference requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int getSlots(){
        return this.runSafe(0, () -> {
            int size = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER))
                        size += handler.getSlots();
                }
            }
            return size;
        });
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int index){
        if(index < 0)
            throw new IllegalArgumentException("Slot index must not be negative!");
        return this.runSafe(ItemStack.EMPTY, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(index - counter < handler.getSlots())
                            return handler.getStackInSlot(index - counter);
                        else
                            counter += handler.getSlots();
                    }
                }
            }
            return ItemStack.EMPTY;
        });
    }

    @Override
    public int getSlotLimit(int index){
        if(index < 0)
            throw new IllegalArgumentException("Slot index must not be negative!");
        return this.runSafe(0, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(index - counter < handler.getSlots()){
                            int capacity = handler.getSlotLimit(index - counter);
                            if(capacity < 0)
                                throw new IllegalStateException("Item handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + capacity + "' for #getSlotLimit()!");
                            return capacity;
                        }else
                            counter += handler.getSlots();
                    }
                }
            }
            return 0;
        });
    }

    @Override
    public boolean isItemValid(int index, @Nonnull ItemStack stack){
        if(index < 0)
            throw new IllegalArgumentException("Slot index must not be negative!");
        if(stack.isEmpty())
            throw new IllegalArgumentException("Stack must not be empty!");
        return this.runSafe(true, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(index - counter < handler.getSlots())
                            return handler.isItemValid(index, stack);
                        else
                            counter += handler.getSlots();
                    }
                }
            }
            return true;
        });
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int index, @Nonnull ItemStack stack, boolean simulate){
        if(index < 0)
            throw new IllegalArgumentException("Slot index must not be negative!");
        if(!this.requester.canSend(EnumChannelType.ITEMS))
            return stack;
        return this.runSafe(stack, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(index - counter < handler.getSlots()){
                            if(!reference.canReceive(this.channel.type))
                                return stack;
                            return handler.insertItem(index - counter, stack, simulate);
                        }else
                            counter += handler.getSlots();
                    }
                }
            }
            return stack;
        });
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate){
        if(index < 0)
            throw new IllegalArgumentException("Slot index must not be negative!");
        if(!this.requester.canSend(EnumChannelType.ITEMS))
            return ItemStack.EMPTY;
        return this.runSafe(ItemStack.EMPTY, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IItemHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.ITEM_HANDLER)){
                        if(index - counter < handler.getSlots()){
                            if(!reference.canSend(this.channel.type))
                                return ItemStack.EMPTY;
                            return handler.extractItem(index - counter, amount, simulate);
                        }else
                            counter += handler.getSlots();
                    }
                }
            }
            return ItemStack.EMPTY;
        });
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

    private <T> T runSafe(T defaultValue, Supplier<T> supplier){
        if(this.pushRecurrentCall())
            return defaultValue;
        try{
            return supplier.get();
        }finally{
            this.popRecurrentCall();
        }
    }
}
