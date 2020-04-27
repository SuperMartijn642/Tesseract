package com.supermartijn642.tesseract;

import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractTile extends TileEntity {

    private static final IItemHandler EMPTY_ITEM_HANDLER = new IItemHandler() {
        public int getSlots(){
            return 0;
        }

        public ItemStack getStackInSlot(int slot){
            return ItemStack.EMPTY;
        }

        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
            return stack;
        }

        public ItemStack extractItem(int slot, int amount, boolean simulate){
            return ItemStack.EMPTY;
        }

        public int getSlotLimit(int slot){
            return 0;
        }
    };
    private static final IFluidHandler EMPTY_FLUID_HANDLER = new IFluidHandler() {
        public IFluidTankProperties[] getTankProperties(){
            return new IFluidTankProperties[0];
        }

        public int fill(FluidStack resource, boolean doFill){
            return 0;
        }

        public FluidStack drain(FluidStack resource, boolean doDrain){
            return null;
        }

        public FluidStack drain(int maxDrain, boolean doDrain){
            return null;
        }
    };
    private static final IEnergyStorage EMPTY_ENERGY_STORAGE = new IEnergyStorage() {
        public int receiveEnergy(int maxReceive, boolean simulate){
            return 0;
        }

        public int extractEnergy(int maxExtract, boolean simulate){
            return 0;
        }

        public int getEnergyStored(){
            return 0;
        }

        public int getMaxEnergyStored(){
            return 0;
        }

        public boolean canExtract(){
            return true;
        }

        public boolean canReceive(){
            return true;
        }
    };

    private final HashMap<EnumChannelType,Integer> channels = new HashMap<>();
    private boolean hasChanged = false;

    public TesseractTile(){
        for(EnumChannelType type : EnumChannelType.values())
            this.channels.put(type, -1);
    }

    public void setChannel(EnumChannelType type, int channel){
        if(channel == this.channels.get(type))
            return;
        this.hasChanged = true;
        Channel oldChannel = this.getChannel(type);
        this.channels.put(type, channel);
        if(oldChannel != null)
            oldChannel.removeTesseract(this);
        Channel newChannel = this.getChannel(type);
        if(newChannel != null)
            newChannel.addTesseract(this);
        IBlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 2);
    }

    public boolean renderOn(){
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
            capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.ITEMS) == null)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(EMPTY_ITEM_HANDLER);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.getChannel(EnumChannelType.ITEMS).getItemHandler(this));
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.FLUID) == null)
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(EMPTY_FLUID_HANDLER);
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.getChannel(EnumChannelType.FLUID).getFluidHandler(this));
        }
        if(capability == CapabilityEnergy.ENERGY){
            if(this.getChannel(EnumChannelType.ENERGY) == null)
                return CapabilityEnergy.ENERGY.cast(EMPTY_ENERGY_STORAGE);
            return CapabilityEnergy.ENERGY.cast(this.getChannel(EnumChannelType.ENERGY).getEnergyStorage(this));
        }
        return super.getCapability(capability, facing);
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        ArrayList<T> list = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()){
            TileEntity tile = this.getWorld().getTileEntity(this.pos.offset(facing));
            if(tile != null && !(tile instanceof TesseractTile) && tile.hasCapability(capability, facing.getOpposite())){
                T handler = tile.getCapability(capability, facing.getOpposite());
                if(handler != null)
                    list.add(handler);
            }
        }
        return list;
    }

    public boolean canSend(EnumChannelType type){
        return true;
    }

    public boolean canReceive(EnumChannelType type){
        return true;
    }

    public int getChannelId(EnumChannelType type){
        return this.channels.get(type);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setTag("data", this.getData());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        if(compound.hasKey("data"))
            this.handleData(compound.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound compound = super.getUpdateTag();
        compound.setTag("data", this.getData());
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound){
        super.handleUpdateTag(compound);
        if(compound.hasKey("data"))
            this.handleData(compound.getCompoundTag("data"));
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        if(this.hasChanged){
            this.hasChanged = false;
            return new SPacketUpdateTileEntity(this.pos, 0, this.getData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleData(pkt.getNbtCompound());
    }

    public NBTTagCompound getData(){
        NBTTagCompound compound = new NBTTagCompound();
        for(EnumChannelType type : EnumChannelType.values())
            compound.setInteger(type.name(), this.channels.get(type));
        return compound;
    }

    public void handleData(NBTTagCompound compound){
        for(EnumChannelType type : EnumChannelType.values())
            this.channels.put(type, compound.getInteger(type.name()));
    }

    private Channel getChannel(EnumChannelType type){
        if(this.channels.get(type) < 0)
            return null;
        Channel channel = (this.world.isRemote ? TesseractChannelManager.CLIENT : TesseractChannelManager.SERVER).getChannelById(type, this.channels.get(type));
        if(channel == null && !this.world.isRemote)
            this.channels.put(type, -1);
        return channel;
    }
}
