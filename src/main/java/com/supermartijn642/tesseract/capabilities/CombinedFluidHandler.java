package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedFluidHandler implements IFluidHandler {

    private final Channel channel;
    private final TesseractReference requester;

    public CombinedFluidHandler(Channel channel, TesseractReference requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int getTanks(){
        return this.runSafe(0, () -> {
            int size = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER))
                        size += handler.getTanks();
                }
            }
            return size;
        });
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int index){
        if(index < 0)
            throw new IllegalArgumentException("Tank index must not be negative!");
        return this.runSafe(FluidStack.EMPTY, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(index - counter < handler.getTanks())
                            return handler.getFluidInTank(index - counter);
                        else
                            counter += handler.getTanks();
                    }
                }
            }
            return FluidStack.EMPTY;
        });
    }

    @Override
    public int getTankCapacity(int index){
        if(index < 0)
            throw new IllegalArgumentException("Tank index must not be negative!");
        return this.runSafe(0, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(index - counter < handler.getTanks()){
                            int capacity = handler.getTankCapacity(index - counter);
                            if(capacity < 0)
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + capacity + "' for #getTankCapacity()!");
                            return capacity;
                        }else
                            counter += handler.getTanks();
                    }
                }
            }
            return 0;
        });
    }

    @Override
    public boolean isFluidValid(int index, @Nonnull FluidStack resource){
        if(index < 0)
            throw new IllegalArgumentException("Tank index must not be negative!");
        if(resource.isEmpty())
            throw new IllegalArgumentException("Stack must not be empty!");
        return this.runSafe(true, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(index - counter < handler.getTanks())
                            return handler.isFluidValid(index, resource);
                        else
                            counter += handler.getTanks();
                    }
                }
            }
            return true;
        });
    }

    @Override
    public int fill(FluidStack resource, FluidAction action){
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            boolean copied = false;
            FluidStack leftOver = resource;
            int leftOverAmount = resource.getAmount();
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        int inserted = handler.fill(leftOver, action);
                        if(inserted < 0)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #fill()!");
                        if(leftOver.getAmount() != leftOverAmount)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #fill()!");
                        if(inserted > 0){
                            leftOverAmount -= inserted;
                            if(leftOverAmount <= 0)
                                return resource.getAmount();
                            if(!copied)
                                leftOver = leftOver.copy();
                            leftOver.shrink(inserted);
                        }
                    }
                }
            }
            return resource.getAmount() - leftOverAmount;
        });
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action){
        if(resource.isEmpty())
            throw new IllegalArgumentException("Stack must not be empty!");
        if(!this.requester.canReceive(this.channel.type))
            return FluidStack.EMPTY;
        return this.runSafe(FluidStack.EMPTY, () -> {
            boolean copied = false;
            FluidStack leftOver = resource;
            int leftOverAmount = resource.getAmount();
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        FluidStack extracted = handler.drain(leftOver, action);
                        if(leftOver.getAmount() != leftOverAmount)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #drain()!");
                        if(!extracted.isEmpty()){
                            if(!resource.isFluidEqual(extracted))
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned different fluid than was requested from #drain()!");
                            leftOverAmount -= extracted.getAmount();
                            if(leftOverAmount < 0)
                                return resource;
                            if(!copied)
                                leftOver = leftOver.copy();
                            leftOver.setAmount(leftOverAmount);
                        }
                    }
                }
            }
            if(leftOver == resource)
                return FluidStack.EMPTY;
            leftOver.setAmount(resource.getAmount() - leftOverAmount);
            return leftOver;
        });
    }

    @Nonnull
    @Override
    public FluidStack drain(int amount, FluidAction action){
        if(amount < 0)
            throw new IllegalArgumentException("Drain amount must not be negative!");
        if(!this.requester.canReceive(this.channel.type))
            return FluidStack.EMPTY;
        return this.runSafe(FluidStack.EMPTY, () -> {
            FluidStack resource = null;
            FluidStack leftOver = null;
            int leftOverAmount = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        // If nothing has been extracted yet, extract anything
                        if(resource == null){
                            FluidStack extracted = handler.drain(leftOverAmount, action);
                            if(!extracted.isEmpty()){
                                leftOverAmount -= extracted.getAmount();
                                if(leftOverAmount < 0)
                                    return extracted;
                                resource = extracted;
                                leftOver = resource.copy();
                                leftOver.setAmount(leftOverAmount);
                            }
                        }else{ // If fluid has been extracted, extract more of the same fluid
                            FluidStack extracted = handler.drain(leftOver, action);
                            if(leftOver.getAmount() != leftOverAmount)
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #drain()!");
                            if(!extracted.isEmpty()){
                                if(!resource.isFluidEqual(extracted))
                                    throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned different fluid than was requested from #drain()!");
                                leftOverAmount -= extracted.getAmount();
                                if(leftOverAmount < 0){
                                    leftOver.setAmount(amount);
                                    return leftOver;
                                }
                                leftOver.setAmount(leftOverAmount);
                            }
                        }
                    }
                }
            }
            if(resource == null)
                return FluidStack.EMPTY;
            leftOver.setAmount(amount - leftOverAmount);
            return leftOver;
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
