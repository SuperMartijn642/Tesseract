package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.Arrays;
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
    public IFluidTankProperties[] getTankProperties(){
        return this.runSafe(new IFluidTankProperties[0], () -> {
            ArrayList<IFluidTankProperties> tanks = new ArrayList<>();
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingFluidCapabilities()){
                        IFluidTankProperties[] handlerTanks = handler.getTankProperties();
                        if(handlerTanks != null)
                            tanks.addAll(Arrays.asList(handlerTanks));
                    }
                }
            }
            return tanks.toArray(new IFluidTankProperties[0]);
        });
    }

    @Override
    public int fill(FluidStack resource, boolean doFill){
        if(resource == null)
            throw new IllegalArgumentException("Fluid stack must not be null!");
        if(resource.amount < 0)
            throw new IllegalArgumentException("Fluid stack amount must not be negative!");
        if(resource.amount == 0 || !this.requester.canSend(EnumChannelType.FLUID))
            return 0;
        return this.runSafe(0, () -> {
            boolean copied = false;
            FluidStack leftOver = resource;
            int leftOverAmount = resource.amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingFluidCapabilities()){
                        int inserted = handler.fill(leftOver, doFill);
                        if(inserted < 0)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #fill()!");
                        if(leftOver.amount != leftOverAmount)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #fill()!");
                        if(inserted > 0){
                            leftOverAmount -= inserted;
                            if(leftOverAmount <= 0)
                                return resource.amount;
                            if(!copied)
                                leftOver = leftOver.copy();
                            leftOver.amount = leftOverAmount;
                        }
                    }
                }
            }
            return resource.amount - leftOverAmount;
        });
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain){
        if(resource == null)
            throw new IllegalArgumentException("Fluid stack must not be null!");
        if(resource.amount < 0)
            throw new IllegalArgumentException("Fluid stack amount must not be negative!");
        if(resource.amount == 0 || !this.requester.canReceive(EnumChannelType.FLUID))
            return null;
        return this.runSafe(null, () -> {
            boolean copied = false;
            FluidStack leftOver = resource;
            int leftOverAmount = resource.amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingFluidCapabilities()){
                        FluidStack extracted = handler.drain(leftOver, doDrain);
                        if(leftOver.amount != leftOverAmount)
                            throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #drain()!");
                        if(extracted != null){
                            if(extracted.amount < 0)
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned fluid stack with negative amount for #drain()!");
                            if(!resource.isFluidEqual(extracted))
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned different fluid than was requested from #drain()!");
                            if(extracted.amount > 0){
                                leftOverAmount -= extracted.amount;
                                if(leftOverAmount < 0)
                                    return resource;
                                if(!copied)
                                    leftOver = leftOver.copy();
                                leftOver.amount = leftOverAmount;
                            }
                        }
                    }
                }
            }
            if(leftOver == resource)
                return null;
            leftOver.amount = resource.amount - leftOverAmount;
            return leftOver;
        });
    }

    @Override
    public FluidStack drain(int amount, boolean doDrain){
        if(amount < 0)
            throw new IllegalArgumentException("Drain amount must not be negative!");
        if(amount == 0 || !this.requester.canReceive(this.channel.type))
            return null;
        return this.runSafe(null, () -> {
            FluidStack resource = null;
            FluidStack leftOver = null;
            int leftOverAmount = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IFluidHandler handler : entity.getSurroundingFluidCapabilities()){
                        // If nothing has been extracted yet, extract anything
                        if(resource == null){
                            FluidStack extracted = handler.drain(leftOverAmount, doDrain);
                            if(extracted != null){
                                if(extracted.amount < 0)
                                    throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned fluid stack with negative amount for #drain()!");
                                if(extracted.amount > 0){
                                    leftOverAmount -= extracted.amount;
                                    if(leftOverAmount < 0)
                                        return extracted;
                                    resource = extracted;
                                    leftOver = resource.copy();
                                    leftOver.amount = leftOverAmount;
                                }
                            }
                        }else{ // If fluid has been extracted, extract more of the same fluid
                            FluidStack extracted = handler.drain(leftOver, doDrain);
                            if(leftOver.amount != leftOverAmount)
                                throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' modified fluid stack argument in #drain()!");
                            if(extracted != null){
                                if(extracted.amount < 0)
                                    throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned fluid stack with negative amount for #drain()!");
                                if(!resource.isFluidEqual(extracted))
                                    throw new IllegalStateException("Fluid handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned different fluid than was requested from #drain()!");
                                if(extracted.amount > 0){
                                    leftOverAmount -= extracted.amount;
                                    if(leftOverAmount < 0){
                                        leftOver.amount = amount;
                                        return leftOver;
                                    }
                                    leftOver.amount = leftOverAmount;
                                }
                            }
                        }
                    }
                }
            }
            if(resource == null)
                return null;
            leftOver.amount = amount - leftOverAmount;
            return leftOver;
        });
    }

    /**
     * Checks whether this is a recurrent call to this combined capability.
     * If not, it will just increase the recurrent call counter.
     */
    private boolean pushRecurrentCall(){
        if(this.channel.recurrentCalls >= 1)
            return true;
        this.channel.recurrentCalls++;
        return false;
    }

    private void popRecurrentCall(){
        this.channel.recurrentCalls--;
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
