package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
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
public class TesseractBlockEntity extends BaseBlockEntity {

    private TesseractReference reference;
    private final EnumMap<EnumChannelType,TransferState> transferState = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,Object> capabilities = new EnumMap<>(EnumChannelType.class);
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;
    /**
     * Counts recurrent calls inside the combined capabilities in order to prevent infinite loops
     */
    public int recurrentCalls = 0;

    private final Map<EnumFacing,Map<Capability<?>,Object>> surroundingCapabilities = new EnumMap<>(EnumFacing.class);

    public TesseractBlockEntity(){
        super(Tesseract.tesseract_tile);
        for(EnumChannelType type : EnumChannelType.values())
            this.transferState.put(type, TransferState.BOTH);
        for(EnumFacing facing : EnumFacing.values())
            this.surroundingCapabilities.put(facing, new HashMap<>());
    }

    public TesseractReference getReference(){
        if(this.reference == null)
            this.reference = TesseractTracker.getInstance(this.world).add(this);
        return this.reference;
    }

    public void invalidateReference(){
        this.reference = null;
    }

    public void channelChanged(EnumChannelType type){
        // Clear old capabilities
        this.capabilities.remove(type);
        this.notifyNeighbors();
    }

    public boolean renderOn(){
        return !this.isBlockedByRedstone();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.getChannel(EnumChannelType.ITEMS) != null) ||
            (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.getChannel(EnumChannelType.FLUID) != null) ||
            (capability == CapabilityEnergy.ENERGY && this.getChannel(EnumChannelType.ENERGY) != null) ||
            super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return (T)this.capabilities.computeIfAbsent(EnumChannelType.ITEMS, o -> {
                Channel channel = this.getChannel(EnumChannelType.ITEMS);
                return channel == null ? null : channel.getItemHandler(this);
            });
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return (T)this.capabilities.computeIfAbsent(EnumChannelType.FLUID, o -> {
                Channel channel = this.getChannel(EnumChannelType.FLUID);
                return channel == null ? null : channel.getFluidHandler(this);
            });
        }
        if(capability == CapabilityEnergy.ENERGY){
            return (T)this.capabilities.computeIfAbsent(EnumChannelType.ENERGY, o -> {
                Channel channel = this.getChannel(EnumChannelType.ENERGY);
                return channel == null ? null : channel.getEnergyStorage(this);
            });
        }
        return super.getCapability(capability, side);
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        if(this.world == null)
            return Collections.emptyList();

        ArrayList<T> list = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()){
            Object object = this.surroundingCapabilities.get(facing).computeIfAbsent(capability, o -> {
                TileEntity entity = this.world.getTileEntity(this.pos.offset(facing));
                if(entity != null && !(entity instanceof TesseractBlockEntity))
                    return entity.getCapability(capability, facing.getOpposite());
                return null;
            });
            if(object != null)
                //noinspection unchecked
                list.add((T)object);
        }
        return list;
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

    private Channel getChannel(EnumChannelType type){
        return this.getReference().getChannel(type);
    }

    public void onNeighborChanged(BlockPos neighbor){
        EnumFacing facing = EnumFacing.getFacingFromVector(neighbor.getX() - this.pos.getX(), neighbor.getY() - this.pos.getY(), neighbor.getZ() - this.pos.getZ());
        this.surroundingCapabilities.get(facing).clear();
    }

    private void notifyNeighbors(){
        this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockState().getBlock(), false);
    }

    private void updateReference(){
        TesseractReference reference = this.getReference();
        if(reference != null)
            reference.update(this);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = new NBTTagCompound();
        for(EnumChannelType type : EnumChannelType.values())
            compound.setString("transferState" + type.name(), this.transferState.get(type).name());
        compound.setString("redstoneState", this.redstoneState.name());
        compound.setBoolean("powered", this.redstone);
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        for(EnumChannelType type : EnumChannelType.values())
            if(compound.hasKey("transferState" + type.name()))
                this.transferState.put(type, TransferState.valueOf(compound.getString("transferState" + type.name())));
        if(compound.hasKey("redstoneState"))
            this.redstoneState = RedstoneState.valueOf(compound.getString("redstoneState"));
        if(compound.hasKey("powered"))
            this.redstone = compound.getBoolean("powered");
    }

    public void onReplaced(){
        if(!this.world.isRemote)
            TesseractTracker.SERVER.remove(this.world, this.pos);
    }
}
