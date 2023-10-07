package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlockEntity extends BaseBlockEntity {

    private TesseractReference reference;
    private final EnumMap<EnumChannelType,TransferState> transferState = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,LazyOptional<?>> capabilities = new EnumMap<>(EnumChannelType.class);
    private RedstoneState redstoneState = RedstoneState.DISABLED;
    private boolean redstone;
    /**
     * Counts recurrent calls inside the combined capabilities in order to prevent infinite loops
     */
    public int recurrentCalls = 0;

    private final Map<Direction,Map<Capability<?>,LazyOptional<?>>> surroundingCapabilities = new EnumMap<>(Direction.class);

    public TesseractBlockEntity(){
        super(Tesseract.tesseract_tile);
        for(EnumChannelType type : EnumChannelType.values())
            this.transferState.put(type, TransferState.BOTH);
        for(Direction facing : Direction.values())
            this.surroundingCapabilities.put(facing, new HashMap<>());
    }

    public TesseractReference getReference(){
        if(this.reference == null)
            this.reference = TesseractTracker.getInstance(this.level).add(this);
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
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return computeIfLazyAbsent(this.capabilities, EnumChannelType.ITEMS, o -> {
                Channel channel = this.getChannel(EnumChannelType.ITEMS);
                return channel == null ? LazyOptional.empty() : LazyOptional.of(() -> channel.getItemHandler(this));
            }).cast();
        }
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return computeIfLazyAbsent(this.capabilities, EnumChannelType.FLUID, o -> {
                Channel channel = this.getChannel(EnumChannelType.FLUID);
                return channel == null ? LazyOptional.empty() : LazyOptional.of(() -> channel.getFluidHandler(this));
            }).cast();
        }
        if(capability == CapabilityEnergy.ENERGY){
            return computeIfLazyAbsent(this.capabilities, EnumChannelType.ENERGY, o -> {
                Channel channel = this.getChannel(EnumChannelType.ENERGY);
                return channel == null ? LazyOptional.empty() : LazyOptional.of(() -> channel.getEnergyStorage(this));
            }).cast();
        }
        return super.getCapability(capability, side);
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        if(this.level == null)
            return Collections.emptyList();

        ArrayList<T> list = new ArrayList<>();
        for(Direction facing : Direction.values()){
            LazyOptional<?> optional = computeIfLazyAbsent(this.surroundingCapabilities.get(facing), capability, o -> {
                TileEntity entity = this.level.getBlockEntity(this.worldPosition.relative(facing));
                if(entity != null && !(entity instanceof TesseractBlockEntity))
                    return entity.getCapability(capability, facing.getOpposite());
                return LazyOptional.empty();
            });
            if(optional.isPresent())
                //noinspection unchecked,DataFlowIssue
                list.add((T)optional.orElseGet(() -> null));
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
        Direction facing = Direction.getNearest(neighbor.getX() - this.worldPosition.getX(), neighbor.getY() - this.worldPosition.getY(), neighbor.getZ() - this.worldPosition.getZ());
        this.surroundingCapabilities.get(facing).clear();
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
    protected CompoundNBT writeData(){
        CompoundNBT compound = new CompoundNBT();
        for(EnumChannelType type : EnumChannelType.values())
            compound.putString("transferState" + type.name(), this.transferState.get(type).name());
        compound.putString("redstoneState", this.redstoneState.name());
        compound.putBoolean("powered", this.redstone);
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        for(EnumChannelType type : EnumChannelType.values())
            if(compound.contains("transferState" + type.name()))
                this.transferState.put(type, TransferState.valueOf(compound.getString("transferState" + type.name())));
        if(compound.contains("redstoneState"))
            this.redstoneState = RedstoneState.valueOf(compound.getString("redstoneState"));
        if(compound.contains("powered"))
            this.redstone = compound.getBoolean("powered");
    }

    public void onReplaced(){
        if(!this.level.isClientSide)
            TesseractTracker.SERVER.remove(this.level, this.worldPosition);
    }

    @Override
    public void onChunkUnloaded(){
        super.onChunkUnloaded();
        // Invalidate capabilities
        this.capabilities.values().forEach(LazyOptional::invalidate);
    }

    /**
     * A replacement wrapper for {@link Map#computeIfAbsent(Object, Function)}
     * that can handle a {@link LazyOptional} being invalidated.
     * @param map             A mapping between a generic key and a value wrapped in a
     *                        LazyOptional.
     * @param key             The key to test for.
     * @param mappingFunction The mapping function to execute if the value
     *                        is missing or invalidated. This function should probably
     *                        not return null, instead it should probably return
     *                        {@link LazyOptional#empty}.
     * @param <K>             The generic key type.
     * @return The value associated with the key (either pre-existing, or
     * newly created if the value was previously missing or
     * invalidated) wrapped in a LazyOptional. This can be null, if
     * the mapping function returns a null, though it shouldn't.
     */
    private static <K> LazyOptional<?> computeIfLazyAbsent(Map<K,LazyOptional<?>> map, K key, Function<? super K,? extends LazyOptional<?>> mappingFunction){
        // If the value is fully missing, defer to the original functionality of Map.
        if(!map.containsKey(key)){
            return map.computeIfAbsent(key, mappingFunction);
        }

        LazyOptional<?> value = map.get(key);

        // If the value is null, defer to the original functionality of Map.
        if(value == null){
            return map.computeIfAbsent(key, mappingFunction);
        }

        // If the value is present, there is no need to perform the mapping.
        if(value.isPresent()){
            return value;
        }

        // Create the new value.
        value = mappingFunction.apply(key);

        // If the value is not null (which should always be true), store it into the map.
        if(value != null){
            map.put(key, value);
        }

        return value;
    }
}
