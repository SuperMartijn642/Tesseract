package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.util.PerChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlockEntity extends BaseBlockEntity {

    private TesseractReference reference;
    private final PerChannel<TransferState> transferState = new PerChannel<>(TransferState.BOTH);
    private final PerChannel<Object> capabilities = new PerChannel<>();
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;

    private final Map<Direction,PerChannel<BlockCapabilityCache<?,Direction>>> surroundingCapabilities = new HashMap<>();

    public TesseractBlockEntity(BlockPos pos, BlockState state){
        super(Tesseract.tesseract_tile, pos, state);
        for(Direction facing : Direction.values())
            this.surroundingCapabilities.put(facing, new PerChannel<>());
    }

    public TesseractReference getReference(){
        if(this.reference == null){
            this.reference = TesseractTracker.getInstance(this.level).add(this);
            this.reference.update(this);
        }
        return this.reference;
    }

    public void invalidateReference(){
        this.reference = null;
    }

    public void channelChanged(EnumChannelType type){
        // Clear old capabilities
        this.capabilities.remove(type);
        this.invalidateCapabilities();
        this.notifyNeighbors();
    }

    public boolean renderOn(){
        return !this.isBlockedByRedstone();
    }

    public IItemHandler getItemCapability(){
        return (IItemHandler)this.capabilities.computeIfAbsent(EnumChannelType.ITEMS, () -> {
            Channel channel = this.getChannel(EnumChannelType.ITEMS);
            return channel == null ? null : channel.getItemHandler(this);
        });
    }

    public IFluidHandler getFluidCapability(){
        return (IFluidHandler)this.capabilities.computeIfAbsent(EnumChannelType.FLUID, () -> {
            Channel channel = this.getChannel(EnumChannelType.FLUID);
            return channel == null ? null : channel.getFluidHandler(this);
        });
    }

    public IEnergyStorage getEnergyCapability(){
        return (IEnergyStorage)this.capabilities.computeIfAbsent(EnumChannelType.ENERGY, () -> {
            Channel channel = this.getChannel(EnumChannelType.ENERGY);
            return channel == null ? null : channel.getEnergyStorage(this);
        });
    }

    public List<IItemHandler> getSurroundingItemCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.ITEMS, Capabilities.ItemHandler.BLOCK);
    }

    public List<IFluidHandler> getSurroundingFluidCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.FLUID, Capabilities.FluidHandler.BLOCK);
    }

    public List<IEnergyStorage> getSurroundingEnergyCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.ENERGY, Capabilities.EnergyStorage.BLOCK);
    }

    private <T> List<T> getSurroundingCapabilities(EnumChannelType type, BlockCapability<T,Direction> api){
        if(this.level == null)
            return Collections.emptyList();

        ArrayList<Object> capabilities = new ArrayList<>(6);
        for(Direction side : Direction.values()){
            Object capability = this.surroundingCapabilities.get(side)
                .computeIfAbsent(type, () -> this.surroundingCapabilities.get(side).computeIfAbsent(type, () -> BlockCapabilityCache.create(api, (ServerLevel)this.level, this.worldPosition.relative(side), side.getOpposite(), () -> !this.remove, () -> {})))
                .getCapability();
            if(capability != null)
                capabilities.add(capability);
        }
        //noinspection unchecked
        return (List<T>)capabilities;
    }

    public boolean canSend(EnumChannelType type){
        return this.transferState.get(type).canSend() && !this.isBlockedByRedstone();
    }

    public boolean canReceive(EnumChannelType type){
        return this.transferState.get(type).canReceive() && !this.isBlockedByRedstone();
    }

    public boolean isBlockedByRedstone(){
        return this.redstoneState != RedstoneState.DISABLED && this.redstoneState == (this.redstone ? RedstoneState.LOW : RedstoneState.HIGH);
    }

    public int getChannelId(EnumChannelType type){
        return this.getReference().getChannelId(type);
    }

    public TransferState getTransferState(EnumChannelType type){
        return this.transferState.get(type);
    }

    public void cycleTransferState(EnumChannelType type){
        TransferState transferState = this.transferState.get(type);
        this.transferState.set(type, transferState == TransferState.BOTH ? TransferState.SEND : transferState == TransferState.SEND ? TransferState.RECEIVE : TransferState.BOTH);
        this.updateReference();
        this.dataChanged();
    }

    public RedstoneState getRedstoneState(){
        return this.redstoneState;
    }

    public void cycleRedstoneState(){
        this.redstoneState = this.redstoneState == RedstoneState.DISABLED ? RedstoneState.HIGH : this.redstoneState == RedstoneState.HIGH ? RedstoneState.LOW : RedstoneState.DISABLED;
        this.updateReference();
        this.dataChanged();
    }

    public void setPowered(boolean powered){
        if(this.redstone != powered){
            this.redstone = powered;
            this.updateReference();
            this.dataChanged();
        }
    }

    private Channel getChannel(EnumChannelType type){
        return this.getReference().getChannel(type);
    }

    public void onNeighborChanged(BlockPos neighbor){
        Direction facing = Direction.getNearest(neighbor.getX() - this.worldPosition.getX(), neighbor.getY() - this.worldPosition.getY(), neighbor.getZ() - this.worldPosition.getZ());
        this.surroundingCapabilities.get(facing).reset();
    }

    private void notifyNeighbors(){
        this.level.blockUpdated(this.worldPosition, this.getBlockState().getBlock());
    }

    private void updateReference(){
        TesseractReference reference = this.getReference();
        if(reference != null)
            reference.update(this);
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        for(EnumChannelType type : EnumChannelType.values())
            compound.putString("transferState" + type.name(), this.transferState.get(type).name());
        compound.putString("redstoneState", this.redstoneState.name());
        compound.putBoolean("powered", this.redstone);
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        for(EnumChannelType type : EnumChannelType.values())
            if(compound.contains("transferState" + type.name()))
                this.transferState.set(type, TransferState.valueOf(compound.getString("transferState" + type.name())));
        if(compound.contains("redstoneState"))
            this.redstoneState = RedstoneState.valueOf(compound.getString("redstoneState"));
        if(compound.contains("powered"))
            this.redstone = compound.getBoolean("powered");
    }

    @Override
    public void setLevel(Level level){
        super.setLevel(level);
        if(!this.remove)
            this.updateReference();
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        if(this.level != null)
            this.updateReference();
    }

    public void onReplaced(){
        if(!this.level.isClientSide)
            TesseractTracker.SERVER.remove(this.level, this.worldPosition);
    }
}
