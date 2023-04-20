package com.supermartijn642.tesseract.manager;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

/**
 * Created 3/22/2020 by SuperMartijn642
 */
public class TesseractReference {

    private final long index;
    private final int dimension;
    private final BlockPos pos;
    private final boolean isClientSide;
    private final EnumMap<EnumChannelType,Integer> channels = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,Boolean> canSend = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,Boolean> canReceive = new EnumMap<>(EnumChannelType.class);
    private WeakReference<TesseractBlockEntity> entity;

    public TesseractReference(long index, TesseractBlockEntity entity){
        this.index = index;
        this.dimension = entity.getWorld().provider.getDimension();
        this.pos = entity.getPos();
        this.isClientSide = entity.getWorld().isRemote;
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, -1);
            this.canSend.put(type, entity.canSend(type));
            this.canReceive.put(type, entity.canReceive(type));
        }
    }

    public TesseractReference(long index, NBTTagCompound tag, boolean isClientSide){
        this.index = index;
        this.dimension = tag.getInteger("dim");
        this.pos = new BlockPos(tag.getInteger("posx"), tag.getInteger("posy"), tag.getInteger("posz"));
        this.isClientSide = isClientSide;
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, tag.getInteger(type + "_channel"));
            this.canSend.put(type, tag.getBoolean(type + "_canSend"));
            this.canReceive.put(type, tag.getBoolean(type + "_canReceive"));
        }
    }

    public long getSaveIndex(){
        return this.index;
    }

    public int getDimension(){
        return this.dimension;
    }

    public World getLevel(){
        if(this.isClientSide)
            return ClientUtils.getWorld().provider.getDimension() == this.dimension ? ClientUtils.getWorld() : null;
        return DimensionManager.getWorld(this.dimension);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World level = this.getLevel();
        boolean isValid = level != null && level.getBlockState(this.pos).getBlock() == Tesseract.tesseract && level.getTileEntity(this.pos) instanceof TesseractBlockEntity;

        if(!isValid && !this.isClientSide)
            TesseractTracker.SERVER.remove(this.dimension, this.pos);

        return isValid;
    }

    /**
     * Checks whether the tesseract is loaded and valid
     */
    public boolean canBeAccessed(){
        World level = this.getLevel();
        return level != null && level.isBlockLoaded(this.pos) && this.isValid();
    }

    public TesseractBlockEntity getTesseract(){
        if(this.entity == null || this.entity.get() == null || this.entity.get().isInvalid() || !this.entity.get().getPos().equals(this.pos))
            this.entity = new WeakReference<>((TesseractBlockEntity)this.getLevel().getTileEntity(this.pos));
        return this.entity == null ? null : this.entity.get();
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dim", this.dimension);
        compound.setInteger("posx", this.pos.getX());
        compound.setInteger("posy", this.pos.getY());
        compound.setInteger("posz", this.pos.getZ());
        for(EnumChannelType type : EnumChannelType.values()){
            compound.setInteger(type + "_channel", this.channels.get(type));
            compound.setBoolean(type + "_canSend", this.canSend.get(type));
            compound.setBoolean(type + "_canReceive", this.canReceive(type));
        }
        return compound;
    }

    public boolean canSend(EnumChannelType type){
        return this.canSend.get(type);
    }

    public boolean canReceive(EnumChannelType type){
        return this.canReceive.get(type);
    }

    public int getChannelId(EnumChannelType type){
        return this.channels.get(type);
    }

    public Channel getChannel(EnumChannelType type){
        if(this.channels.get(type) < 0)
            return null;
        Channel channel = TesseractChannelManager.getInstance(this.isClientSide).getChannelById(type, this.channels.get(type));
        if(channel == null && !this.isClientSide){
            this.channels.put(type, -1);
            this.markDirty();
            if(this.canBeAccessed())
                this.getTesseract().channelChanged(type);
        }
        return channel;
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
        this.markDirty();
        if(this.canBeAccessed())
            this.getTesseract().channelChanged(type);
    }

    public void update(TesseractBlockEntity entity){
        for(EnumChannelType type : EnumChannelType.values()){
            boolean changed = false;
            boolean canSend = entity.canSend(type);
            //noinspection DataFlowIssue
            if(canSend != this.canSend.put(type, canSend))
                changed = true;
            boolean canReceive = entity.canReceive(type);
            //noinspection DataFlowIssue
            if(canReceive != this.canReceive.put(type, canReceive))
                changed = true;
            if(changed){
                Channel channel = this.getChannel(type);
                if(channel != null)
                    channel.updateTesseract(this);
                this.markDirty();
            }
        }
    }

    private void markDirty(){
        if(!this.isClientSide)
            TesseractTracker.SERVER.markDirty(this);
    }

    void delete(){
        for(EnumChannelType type : EnumChannelType.values()){
            Channel channel = this.getChannel(type);
            if(channel != null)
                channel.removeTesseract(this);
        }
    }

    @Override
    public boolean equals(Object o){
        return this == o;
    }

    @Override
    public int hashCode(){
        int result = this.dimension;
        result = 31 * result + (this.pos != null ? this.pos.hashCode() : 0);
        return result;
    }
}
