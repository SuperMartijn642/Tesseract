package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

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
    public int getTanks(){
        if(this.pushRecurrentCall())
            return 0;

        int tanks = 0;
        for(TesseractReference location : this.channel.tesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER))
                        tanks += Math.max(handler.getTanks(), 0);
                }
            }
        }

        this.popRecurrentCall();

        return tanks;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank){
        if(this.pushRecurrentCall())
            return FluidStack.EMPTY;

        FluidStack stack = FluidStack.EMPTY;
        int tanks = 0;
        loop:
        for(TesseractReference location : this.channel.tesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(tank - tanks < handler.getTanks()){
                            stack = handler.getFluidInTank(tank - tanks);
                            break loop;
                        }else
                            tanks += Math.max(handler.getTanks(), 0);
                    }
                }
            }
        }

        this.popRecurrentCall();

        return stack;
    }

    @Override
    public int getTankCapacity(int tank){
        if(this.pushRecurrentCall())
            return 0;

        int capacity = 0;
        int tanks = 0;
        loop:
        for(TesseractReference location : this.channel.tesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(tank - tanks < handler.getTanks()){
                            capacity = handler.getTankCapacity(tank - tanks);
                            break loop;
                        }else
                            tanks += Math.max(handler.getTanks(), 0);
                    }
                }
            }
        }

        this.popRecurrentCall();

        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
        if(this.pushRecurrentCall())
            return false;

        boolean valid = false;
        int tanks = 0;
        loop:
        for(TesseractReference location : this.channel.tesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        if(tank - tanks < handler.getTanks()){
                            valid = handler.isFluidValid(tank - tanks, stack);
                            break loop;
                        }else
                            tanks += Math.max(handler.getTanks(), 0);
                    }
                }
            }
        }

        this.popRecurrentCall();

        return valid;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canSend(EnumChannelType.FLUID) || resource.isEmpty()){
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
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
                        amount += handler.fill(fluid, action);
                        if(amount >= resource.getAmount())
                            break loop;
                        fluid.setAmount(resource.getAmount() - amount);
                    }
                }
            }
        }

        this.popRecurrentCall();

        return amount;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action){
        if(this.pushRecurrentCall())
            return FluidStack.EMPTY;

        if(!this.requester.canReceive(EnumChannelType.FLUID) || resource == null || resource.isEmpty()){
            this.popRecurrentCall();
            return FluidStack.EMPTY;
        }

        FluidStack fluid = resource.copy();

        loop:
        for(TesseractReference location : this.channel.sendingTesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
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
        }

        this.popRecurrentCall();

        if(fluid.getAmount() == resource.getAmount())
            return FluidStack.EMPTY;

        fluid.setAmount(resource.getAmount() - fluid.getAmount());
        return fluid;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action){
        if(this.pushRecurrentCall())
            return FluidStack.EMPTY;

        if(!this.requester.canReceive(EnumChannelType.FLUID) || maxDrain <= 0){
            this.popRecurrentCall();
            return FluidStack.EMPTY;
        }

        FluidStack fluid = null;

        loop:
        for(TesseractReference location : this.channel.sendingTesseracts){
            if(location.canBeAccessed()){
                TesseractBlockEntity entity = location.getTesseract();
                if(entity != this.requester){
                    for(IFluidHandler handler : entity.getSurroundingCapabilities(ForgeCapabilities.FLUID_HANDLER)){
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
        }

        this.popRecurrentCall();

        if(fluid == null)
            return FluidStack.EMPTY;

        fluid.setAmount(maxDrain - fluid.getAmount());
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
