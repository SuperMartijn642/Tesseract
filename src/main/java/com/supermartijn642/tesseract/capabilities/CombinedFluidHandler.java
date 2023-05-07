package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedFluidHandler implements IFluidHandler {

    private final Channel channel;
    private final TesseractBlockEntity requester;

    public CombinedFluidHandler(Channel channel, TesseractBlockEntity requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public IFluidTankProperties[] getTankProperties(){
        if(this.pushRecurrentCall())
            return new IFluidTankProperties[0];

        ArrayList<IFluidTankProperties[]> list = new ArrayList<>();
        int size = 0;
        for(TesseractReference reference : this.channel.tesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                        IFluidTankProperties[] properties = handler.getTankProperties();
                        if(properties != null){
                            list.add(properties);
                            size += properties.length;
                        }
                    }
                }
            }
        }
        IFluidTankProperties[] properties = new IFluidTankProperties[size];
        int index = 0;
        for(IFluidTankProperties[] arr : list){
            System.arraycopy(arr, 0, properties, index, arr.length);
            index += arr.length;
        }

        this.popRecurrentCall();

        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canSend(EnumChannelType.FLUID) || resource == null || resource.amount <= 0){
            this.popRecurrentCall();
            return 0;
        }

        FluidStack fluid = resource.copy();
        int amount = 0;

        loop:
        for(TesseractReference location : this.channel.receivingTesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                        amount += handler.fill(fluid, doFill);
                        if(amount >= resource.amount)
                            break loop;
                        fluid.amount = resource.amount - amount;
                    }
                }
            }
        }

        this.popRecurrentCall();

        return amount;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain){
        if(this.pushRecurrentCall())
            return null;

        if(!this.requester.canReceive(EnumChannelType.FLUID) || resource == null || resource.amount <= 0){
            this.popRecurrentCall();
            return null;
        }

        FluidStack fluid = resource.copy();

        loop:
        for(TesseractReference location : this.channel.sendingTesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                        FluidStack stack = handler.drain(fluid.copy(), true);
                        if(fluid.amount > 0 && resource.isFluidEqual(stack)){
                            if(doDrain)
                                handler.drain(fluid.copy(), true);
                            fluid.amount = fluid.amount - stack.amount;
                        }
                        if(fluid.amount <= 0)
                            break loop;
                    }
                }
            }
        }

        this.popRecurrentCall();

        if(fluid.amount == resource.amount)
            return null;

        fluid.amount = resource.amount - fluid.amount;
        return fluid;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain){
        if(this.pushRecurrentCall())
            return null;

        if(!this.requester.canReceive(EnumChannelType.FLUID) || maxDrain <= 0){
            this.popRecurrentCall();
            return null;
        }

        FluidStack fluid = null;

        loop:
        for(TesseractReference location : this.channel.sendingTesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                        if(fluid == null){
                            fluid = handler.drain(maxDrain, doDrain);
                            if(fluid == null || fluid.amount <= 0)
                                fluid = null;
                            else
                                fluid.amount = maxDrain - fluid.amount;
                        }else{
                            FluidStack stack = handler.drain(fluid.copy(), false);
                            if(fluid != null && fluid.amount > 0 && fluid.isFluidEqual(stack)){
                                if(doDrain)
                                    handler.drain(fluid.copy(), true);
                                fluid.amount = fluid.amount - stack.amount;
                            }
                            if(fluid.amount <= 0)
                                break loop;
                        }
                    }
                }
            }
        }

        this.popRecurrentCall();

        if(fluid == null)
            return null;

        fluid.amount = maxDrain - fluid.amount;
        return fluid;
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
