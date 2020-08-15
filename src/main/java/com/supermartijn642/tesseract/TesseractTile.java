package com.supermartijn642.tesseract;

import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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

        public boolean isItemValid(int slot, @Nonnull ItemStack stack){
            return true;
        }
    };

    private static final IFluidHandler EMPTY_FLUID_HANDLER = new IFluidHandler() {
        @Override
        public int getTanks(){
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank){
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank){
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action){
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action){
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action){
            return FluidStack.EMPTY;
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
    private final HashMap<EnumChannelType,TransferState> transferState = new HashMap<>();
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;

    public TesseractTile(){
        super(Tesseract.tesseract_tile);
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, -1);
            this.transferState.put(type, TransferState.BOTH);
        }
    }

    public void setChannel(EnumChannelType type, int channel){
        if(channel == this.channels.get(type))
            return;
        Channel oldChannel = this.getChannel(type);
        this.channels.put(type, channel);
        if(oldChannel != null)
            oldChannel.removeTesseract(this);
        Channel newChannel = this.getChannel(type);
        if(newChannel != null)
            newChannel.addTesseract(this);
        this.hasChanged = true;
        BlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 2);
        this.markDirty();
    }

    public boolean renderOn(){
        return this.redstoneState == RedstoneState.DISABLED || this.redstoneState == (this.redstone ? RedstoneState.HIGH : RedstoneState.LOW);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.ITEMS) == null)
                return LazyOptional.of(() -> EMPTY_ITEM_HANDLER).cast();
            return LazyOptional.of(() -> this.getChannel(EnumChannelType.ITEMS).getItemHandler(this)).cast();
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.FLUID) == null)
                return LazyOptional.of(() -> EMPTY_FLUID_HANDLER).cast();
            return LazyOptional.of(() -> this.getChannel(EnumChannelType.FLUID).getFluidHandler(this)).cast();
        }
        if(capability == CapabilityEnergy.ENERGY){
            if(this.getChannel(EnumChannelType.ENERGY) == null)
                return LazyOptional.of(() -> EMPTY_ENERGY_STORAGE).cast();
            return LazyOptional.of(() -> this.getChannel(EnumChannelType.ENERGY).getEnergyStorage(this)).cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
        return this.getCapability(cap);
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        if(this.world == null)
            return Collections.emptyList();
        ArrayList<T> list = new ArrayList<>();
        for(Direction facing : Direction.values()){
            TileEntity tile = this.world.getTileEntity(this.pos.offset(facing));
            if(tile != null && !(tile instanceof TesseractTile))
                tile.getCapability(capability, facing.getOpposite()).ifPresent(list::add);
        }
        return list;
    }

    public boolean canSend(EnumChannelType type){
        return this.transferState.get(type).canSend() &&
            this.redstoneState == RedstoneState.DISABLED || this.redstoneState == (this.redstone ? RedstoneState.HIGH : RedstoneState.LOW);
    }

    public boolean canReceive(EnumChannelType type){
        return this.transferState.get(type).canReceive() &&
            this.redstoneState == RedstoneState.DISABLED || this.redstoneState == (this.redstone ? RedstoneState.HIGH : RedstoneState.LOW);
    }

    public int getChannelId(EnumChannelType type){
        return this.channels.get(type);
    }

    public TransferState getTransferState(EnumChannelType type){
        return this.transferState.get(type);
    }

    public void cycleTransferState(EnumChannelType type){
        TransferState transferState = this.transferState.get(type);
        this.transferState.put(type, transferState == TransferState.BOTH ? TransferState.SEND : transferState == TransferState.SEND ? TransferState.RECEIVE : TransferState.BOTH);
        this.hasChanged = true;
        BlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 2);
        this.markDirty();
    }

    public RedstoneState getRedstoneState(){
        return this.redstoneState;
    }

    public void cycleRedstoneState(){
        this.redstoneState = this.redstoneState == RedstoneState.DISABLED ? RedstoneState.HIGH : this.redstoneState == RedstoneState.HIGH ? RedstoneState.LOW : RedstoneState.DISABLED;
        this.hasChanged = true;
        BlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 2);
        this.markDirty();
    }

    public void setPowered(boolean powered){
        if(this.redstone != powered){
            this.redstone = powered;

            this.hasChanged = true;
            BlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 2);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.put("data", this.getData());
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound){
        super.read(state, compound);
        if(compound.contains("data"))
            this.handleData(compound.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT compound = super.getUpdateTag();
        compound.put("data", this.getData());
        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT compound){
        super.handleUpdateTag(state, compound);
        if(compound.contains("data"))
            this.handleData(compound.getCompound("data"));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        if(this.hasChanged){
            this.hasChanged = false;
            return new SUpdateTileEntityPacket(this.pos, 0, this.getData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleData(pkt.getNbtCompound());
    }

    public CompoundNBT getData(){
        CompoundNBT compound = new CompoundNBT();
        for(EnumChannelType type : EnumChannelType.values()){
            compound.putInt(type.name(), this.channels.get(type));
            compound.putString("transferState" + type.name(), this.transferState.get(type).name());
        }
        compound.putString("redstoneState", this.redstoneState.name());
        compound.putBoolean("powered", this.redstone);
        return compound;
    }

    public void handleData(CompoundNBT compound){
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, compound.getInt(type.name()));
            if(compound.contains("transferState" + type.name()))
                this.transferState.put(type, TransferState.valueOf(compound.getString("transferState" + type.name())));
        }
        if(compound.contains("redstoneState"))
            this.redstoneState = RedstoneState.valueOf(compound.getString("redstoneState"));
        if(compound.contains("powered"))
            this.redstone = compound.getBoolean("powered");
    }

    private Channel getChannel(EnumChannelType type){
        if(this.channels.get(type) < 0 || this.world == null)
            return null;
        Channel channel = (this.world.isRemote ? TesseractChannelManager.CLIENT : TesseractChannelManager.SERVER).getChannelById(type, this.channels.get(type));
        if(channel == null && !this.world.isRemote){
            this.channels.put(type, -1);
            this.hasChanged = true;
            BlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 2);
            this.markDirty();
        }
        return channel;
    }
}
