package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.TesseractLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedFluidHandler implements IFluidHandler {

    private final List<TesseractLocation> tesseracts;
    private final TesseractTile requester;

    public CombinedFluidHandler(List<TesseractLocation> tesseracts, TesseractTile requester){
        this.tesseracts = tesseracts;
        this.requester = requester;
    }

    @Override
    public IFluidTankProperties[] getTankProperties(){
        ArrayList<IFluidTankProperties[]> list = new ArrayList<>(this.tesseracts.size());
        int size = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    IFluidTankProperties[] properties = handler.getTankProperties();
                    if(properties != null){
                        list.add(properties);
                        size += properties.length;
                    }
                }
            }
        }
        IFluidTankProperties[] properties = new IFluidTankProperties[size];
        int index = 0;
        for(IFluidTankProperties[] arr : list){
            for(IFluidTankProperties tank : arr){
                properties[index] = tank;
                index++;
            }
        }
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill){
        if(!this.requester.canSend(EnumChannelType.FLUID))
            return 0;
        FluidStack fluid = resource.copy();
        int amount = 0;
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canReceive(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    amount += handler.fill(fluid, doFill);
                    if(amount >= resource.amount)
                        break loop;
                    fluid.amount = resource.amount - amount;
                }
            }
        }
        return amount;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain){
        if(!this.requester.canReceive(EnumChannelType.FLUID) || resource == null || resource.amount <= 0)
            return null;
        FluidStack fluid = resource.copy();
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canSend(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    FluidStack stack = handler.drain(fluid.copy(), false);
                    if(stack != null && stack.amount > 0 && resource.isFluidEqual(stack)){
                        if(doDrain)
                            handler.drain(fluid.copy(), true);
                        fluid.amount -= stack.amount;
                    }
                    if(fluid.amount <= 0)
                        break loop;
                }
            }
        }
        if(fluid.amount == resource.amount)
            return null;
        fluid.amount = resource.amount - fluid.amount;
        return fluid;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain){
        if(!this.requester.canReceive(EnumChannelType.FLUID) || maxDrain <= 0)
            return null;
        FluidStack fluid = null;
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canSend(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    if(fluid == null){
                        fluid = handler.drain(maxDrain, true);
                        if(fluid != null){
                            if(fluid.amount <= 0)
                                fluid = null;
                            else
                                fluid.amount = maxDrain - fluid.amount;
                        }
                    }else{
                        FluidStack stack = handler.drain(fluid.copy(), false);
                        if(stack != null && stack.amount > 0 && fluid.isFluidEqual(stack)){
                            if(doDrain)
                                handler.drain(fluid.copy(), true);
                            fluid.amount -= stack.amount;
                        }
                        if(fluid.amount <= 0)
                            break loop;
                    }
                }
            }
        }
        if(fluid == null)
            return null;
        fluid.amount = maxDrain - fluid.amount;
        return fluid;
    }
}
