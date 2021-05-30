package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseTileEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

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

    private final Map<EnumFacing,Map<Capability<?>,Object>> capabilities = new HashMap<>();

    public TesseractTile(){
        super();
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, -1);
            this.transferState.put(type, TransferState.BOTH);
        }
        for(EnumFacing facing : EnumFacing.values())
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

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.getChannel(EnumChannelType.ITEMS) != null) ||
            (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.getChannel(EnumChannelType.FLUID) != null) ||
            (capability == CapabilityEnergy.ENERGY && this.getChannel(EnumChannelType.ENERGY) != null) ||
            super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.ITEMS) == null)
                return null;
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.getChannel(EnumChannelType.ITEMS).getItemHandler(this));
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            if(this.getChannel(EnumChannelType.FLUID) == null)
                return null;
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.getChannel(EnumChannelType.FLUID).getFluidHandler(this));
        }
        if(capability == CapabilityEnergy.ENERGY){
            if(this.getChannel(EnumChannelType.ENERGY) == null)
                return null;
            return CapabilityEnergy.ENERGY.cast(this.getChannel(EnumChannelType.ENERGY).getEnergyStorage(this));
        }
        return super.getCapability(capability, facing);
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        if(this.world == null)
            return Collections.emptyList();

        ArrayList<T> list = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()){
            if(!this.capabilities.get(facing).containsKey(capability)){
                TileEntity tile = this.world.getTileEntity(this.pos.offset(facing));
                if(tile != null && !(tile instanceof TesseractTile)){
                    T object = tile.getCapability(capability, facing.getOpposite());
                    if(object != null){
                        this.capabilities.get(facing).put(capability, object);
                        list.add(object);
                    }
                }
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
        if(!this.world.isRemote)
            this.reference = TesseractTracker.SERVER.add(this);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = new NBTTagCompound();
        for(EnumChannelType type : EnumChannelType.values()){
            compound.setInteger(type.name(), this.channels.get(type));
            compound.setString("transferState" + type.name(), this.transferState.get(type).name());
        }
        compound.setString("redstoneState", this.redstoneState.name());
        compound.setBoolean("powered", this.redstone);
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, compound.getInteger(type.name()));
            if(compound.hasKey("transferState" + type.name()))
                this.transferState.put(type, TransferState.valueOf(compound.getString("transferState" + type.name())));
        }
        if(compound.hasKey("redstoneState"))
            this.redstoneState = RedstoneState.valueOf(compound.getString("redstoneState"));
        if(compound.hasKey("powered"))
            this.redstone = compound.getBoolean("powered");
    }

    private Channel getChannel(EnumChannelType type){
        if(this.channels.get(type) < 0 || this.world == null)
            return null;
        Channel channel = TesseractChannelManager.getInstance(this.world).getChannelById(type, this.channels.get(type));
        if(channel == null && !this.world.isRemote){
            this.channels.put(type, -1);
            this.updateReference();
            this.dataChanged();
        }
        return channel;
    }

    public void onNeighborChanged(BlockPos neighbor){
        EnumFacing facing = EnumFacing.getFacingFromVector(neighbor.getX() - this.pos.getX(), neighbor.getY() - this.pos.getY(), neighbor.getZ() - this.pos.getZ());
        this.capabilities.get(facing).clear();
    }

    @Override
    public void dataChanged(){
        super.dataChanged();
        this.world.notifyNeighborsOfStateChange(this.pos, this.blockType, false);
    }

    private void updateReference(){
        // Mekanism seems to interact with the tile entity before #onLoad is called somehow, thus this check is needed
        if(this.reference != null)
            this.reference.update(this);
    }

    public void onReplaced(){
        if(this.world.isRemote){
            for(EnumChannelType type : EnumChannelType.values()){
                Channel channel = this.getChannel(type);
                if(channel != null)
                    channel.removeTesseract(this.reference);
            }
            TesseractTracker.SERVER.remove(this.world, this.pos);
        }
    }
}
