package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.TesseractLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
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
    public int getTanks(){
        int tanks = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
                    tanks += Math.max(handler.getTanks(), 0);
            }
        }
        return tanks;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank){
        int tanks = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    if(tank - tanks < handler.getTanks())
                        return handler.getFluidInTank(tank - tanks);
                    else
                        tanks += Math.max(handler.getTanks(), 0);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank){
        int tanks = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    if(tank - tanks < handler.getTanks())
                        return handler.getTankCapacity(tank - tanks);
                    else
                        tanks += Math.max(handler.getTanks(), 0);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
        int tanks = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    if(tank - tanks < handler.getTanks())
                        return handler.isFluidValid(tank - tanks, stack);
                    else
                        tanks += Math.max(handler.getTanks(), 0);
                }
            }
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action){
        if(!this.requester.canSend(EnumChannelType.FLUID) || resource.isEmpty())
            return 0;
        FluidStack fluid = resource.copy();
        int amount = 0;
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canReceive(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    amount += handler.fill(fluid, action);
                    if(amount >= resource.getAmount())
                        break loop;
                    fluid.setAmount(resource.getAmount() - amount);
                }
            }
        }
        return amount;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action){
        if(!this.requester.canReceive(EnumChannelType.FLUID) || resource == null || resource.isEmpty())
            return FluidStack.EMPTY;
        FluidStack fluid = resource.copy();
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canSend(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    FluidStack stack = handler.drain(fluid.copy(), FluidAction.SIMULATE);
                    if(!stack.isEmpty() && resource.isFluidEqual(stack)){
                        if(action.execute())
                            handler.drain(fluid.copy(), FluidAction.EXECUTE);
                        fluid.setAmount(fluid.getAmount() - stack.getAmount());
                    }
                    if(fluid.isEmpty())
                        break loop;
                }
            }
        }
        if(fluid.getAmount() == resource.getAmount())
            return FluidStack.EMPTY;
        fluid.setAmount(resource.getAmount() - fluid.getAmount());
        return fluid;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action){
        if(!this.requester.canReceive(EnumChannelType.FLUID) || maxDrain <= 0)
            return FluidStack.EMPTY;
        FluidStack fluid = null;
        loop: for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canSend(EnumChannelType.FLUID)){
                for(IFluidHandler handler : location.getTesseract().getSurroundingCapabilities(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)){
                    if(fluid == null){
                        fluid = handler.drain(maxDrain, action);
                        if(fluid.isEmpty())
                            fluid = null;
                        else
                            fluid.setAmount(maxDrain - fluid.getAmount());
                    }else{
                        FluidStack stack = handler.drain(fluid.copy(), FluidAction.SIMULATE);
                        if(!stack.isEmpty() && fluid.isFluidEqual(stack)){
                            if(action.execute())
                                handler.drain(fluid.copy(), FluidAction.EXECUTE);
                            fluid.setAmount(fluid.getAmount() - stack.getAmount());
                        }
                        if(fluid.isEmpty())
                            break loop;
                    }
                }
            }
        }
        if(fluid == null)
            return FluidStack.EMPTY;
        fluid.setAmount(maxDrain - fluid.getAmount());
        return fluid;
    }
}
