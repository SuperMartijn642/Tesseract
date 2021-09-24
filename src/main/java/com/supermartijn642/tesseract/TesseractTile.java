package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseTileEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractTile extends BaseTileEntity {

    private TesseractReference reference;
    private final EnumMap<EnumChannelType,Integer> channels = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,TransferState> transferState = new EnumMap<>(EnumChannelType.class);
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;

    private final Map<Direction,Map<Capability<?>,Object>> capabilities = new HashMap<>();

    public TesseractTile(BlockPos pos, BlockState state){
        super(Tesseract.tesseract_tile, pos, state);
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, -1);
            this.transferState.put(type, TransferState.BOTH);
        }
        for(Direction facing : Direction.values())
            this.capabilities.put(facing, new HashMap<>());
    }

    public void setChannel(EnumChannelType type, int channel){
        if(channel == this.channels.get(type))
            return;
        Channel oldChannel = this.getChannel(type);
        this.channels.put(type, channel);
        if(oldChannel != null)
            oldChannel.removeTesseract(this.reference);
        Channel newChannel = this.getChannel(type);
        if(newChannel != null)
            newChannel.addTesseract(this.reference);
        this.updateReference();
        this.dataChanged();
    }

    public boolean renderOn(){
        return this.redstoneState == RedstoneState.DISABLED || this.redstoneState == (this.redstone ? RedstoneState.HIGH : RedstoneState.LOW);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.ITEMS) == null)
                return LazyOptional.empty();
            return LazyOptional.of(() -> this.getChannel(EnumChannelType.ITEMS).getItemHandler(this)).cast();
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.FLUID) == null)
                return LazyOptional.empty();
            return LazyOptional.of(() -> this.getChannel(EnumChannelType.FLUID).getFluidHandler(this)).cast();
        }
        if(capability == CapabilityEnergy.ENERGY){
            if(this.getChannel(EnumChannelType.ENERGY) == null)
                return LazyOptional.empty();
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
        if(this.level == null)
            return Collections.emptyList();

        ArrayList<T> list = new ArrayList<>();
        for(Direction facing : Direction.values()){
            if(!this.capabilities.get(facing).containsKey(capability)){
                BlockEntity tile = this.level.getBlockEntity(this.worldPosition.relative(facing));
                if(tile != null && !(tile instanceof TesseractTile))
                    tile.getCapability(capability, facing.getOpposite()).ifPresent(
                        object -> {
                            this.capabilities.get(facing).put(capability, object);
                            list.add(object);
                        }
                    );
            }else
                list.add((T)this.capabilities.get(facing).get(capability));
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

    @Override
    public void onLoad(){
        if(!this.level.isClientSide)
            this.reference = TesseractTracker.SERVER.add(this);
    }

    /**
     *  TODO: 9/24/2021  Remove this once https://github.com/MinecraftForge/MinecraftForge/issues/7926 is fixed
     */
    @Override
    public void clearRemoved(){
        if(!this.level.isClientSide)
            this.reference = TesseractTracker.SERVER.add(this);
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        for(EnumChannelType type : EnumChannelType.values()){
            compound.putInt(type.name(), this.channels.get(type));
            compound.putString("transferState" + type.name(), this.transferState.get(type).name());
        }
        compound.putString("redstoneState", this.redstoneState.name());
        compound.putBoolean("powered", this.redstone);
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
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
        if(this.channels.get(type) < 0 || this.level == null)
            return null;
        Channel channel = TesseractChannelManager.getInstance(this.level).getChannelById(type, this.channels.get(type));
        if(channel == null && !this.level.isClientSide){
            this.channels.put(type, -1);
            this.updateReference();
            this.dataChanged();
        }
        return channel;
    }

    public void onNeighborChanged(BlockPos neighbor){
        Direction facing = Direction.getNearest(neighbor.getX() - this.worldPosition.getX(), neighbor.getY() - this.worldPosition.getY(), neighbor.getZ() - this.worldPosition.getZ());
        this.capabilities.get(facing).clear();
    }

    @Override
    public void dataChanged(){
        super.dataChanged();
        this.level.blockUpdated(this.worldPosition, this.getBlockState().getBlock());
    }

    private void updateReference(){
        // Mekanism seems to interact with the tile entity before #onLoad is called somehow, thus this check is needed
        if(this.reference != null)
            this.reference.update(this);
    }

    public void onReplaced(){
        if(this.level.isClientSide){
            for(EnumChannelType type : EnumChannelType.values()){
                Channel channel = this.getChannel(type);
                if(channel != null)
                    channel.removeTesseract(this.reference);
            }
            TesseractTracker.SERVER.remove(this.level, this.worldPosition);
        }
    }
}
