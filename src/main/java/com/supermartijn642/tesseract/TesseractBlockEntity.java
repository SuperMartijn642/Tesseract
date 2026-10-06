package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.util.PerChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlockEntity extends BaseBlockEntity {

    private TesseractReference reference;
    private final PerChannel<TransferState> transferState = new PerChannel<>(TransferState.BOTH);
    private final PerChannel<LazyOptional<?>> capabilities = new PerChannel<>();
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;

    private final Map<Direction,PerChannel<LazyOptional<?>>> surroundingCapabilities = new EnumMap<>(Direction.class);

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
        LazyOptional<?> optional = this.capabilities.remove(type);
        if(optional != null)
            optional.invalidate();
        this.notifyNeighbors();
    }

    public boolean renderOn(){
        return !this.isBlockedByRedstone();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side){
        if(capability == ForgeCapabilities.ITEM_HANDLER){
            Channel channel = this.getChannel(EnumChannelType.ITEMS);
            if(channel == null)
                return LazyOptional.empty();
            return this.capabilities.computeIfAbsent(EnumChannelType.ITEMS, () -> LazyOptional.of(() -> channel.getItemHandler(this))).cast();
        }
        if(capability == ForgeCapabilities.FLUID_HANDLER){
            Channel channel = this.getChannel(EnumChannelType.FLUID);
            if(channel == null)
                return LazyOptional.empty();
            return this.capabilities.computeIfAbsent(EnumChannelType.FLUID, () -> LazyOptional.of(() -> channel.getFluidHandler(this))).cast();
        }
        if(capability == ForgeCapabilities.ENERGY){
            Channel channel = this.getChannel(EnumChannelType.ENERGY);
            if(channel == null)
                return LazyOptional.empty();
            return this.capabilities.computeIfAbsent(EnumChannelType.FLUID, () -> LazyOptional.of(() -> channel.getEnergyStorage(this))).cast();
        }
        return super.getCapability(capability, side);
    }

    public List<IItemHandler> getSurroundingItemCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.ITEMS, ForgeCapabilities.ITEM_HANDLER);
    }

    public List<IFluidHandler> getSurroundingFluidCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.FLUID, ForgeCapabilities.FLUID_HANDLER);
    }

    public List<IEnergyStorage> getSurroundingEnergyCapabilities(){
        return this.getSurroundingCapabilities(EnumChannelType.ENERGY, ForgeCapabilities.ENERGY);
    }

    private <T> List<T> getSurroundingCapabilities(EnumChannelType type, Capability<T> api){
        if(this.level == null)
            return Collections.emptyList();

        ArrayList<Object> capabilities = new ArrayList<>();
        for(Direction side : Direction.values()){
            LazyOptional<?> optional = this.surroundingCapabilities.get(side).get(type);
            if(optional != null && !optional.isPresent()){
                this.surroundingCapabilities.get(side).remove(type);
                optional = null;
            }
            if(optional == null){
                BlockEntity entity = this.level.getBlockEntity(this.worldPosition.relative(side));
                if(entity != null && !(entity instanceof TesseractBlockEntity)){
                    optional = entity.getCapability(api, side.getOpposite());
                    if(optional.isPresent())
                        this.surroundingCapabilities.get(side).set(type, optional);
                    else
                        optional = null;
                }
            }
            if(optional != null)
                capabilities.add(optional.orElse(null));
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

    public void onNeighborChanged(BlockPos neighbor, Direction side){
        this.surroundingCapabilities.get(side).reset();
    }

    private void notifyNeighbors(){
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
    }

    private void updateReference(){
        TesseractReference reference = this.getReference();
        if(reference != null)
            reference.update(this);
    }

    @Override
    protected void writeData(ValueOutput output){
        for(EnumChannelType type : EnumChannelType.values())
            output.putString("transferState" + type.name(), this.transferState.get(type).name());
        output.putString("redstoneState", this.redstoneState.name());
        output.putBoolean("powered", this.redstone);
    }

    @Override
    protected void readData(ValueInput input){
        for(EnumChannelType type : EnumChannelType.values())
            this.transferState.set(type, input.getString("transferState" + type.name()).map(TransferState::valueOf).orElse(TransferState.BOTH));
        this.redstoneState = input.getString("redstoneState").map(RedstoneState::valueOf).orElse(RedstoneState.DISABLED);
        this.redstone = input.getBooleanOr("powered", false);
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state){
        super.preRemoveSideEffects(pos, state);
        if(!this.level.isClientSide())
            TesseractTracker.SERVER.remove(this.level, this.worldPosition);
    }

    @Override
    public void onChunkUnloaded(){
        super.onChunkUnloaded();
        // Invalidate capabilities
        this.capabilities.values().forEach(LazyOptional::invalidate);
        this.capabilities.reset();
    }
}
