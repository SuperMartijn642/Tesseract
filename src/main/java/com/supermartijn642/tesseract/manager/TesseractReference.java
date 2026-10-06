package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.util.PerChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Created 3/22/2020 by SuperMartijn642
 */
public final class TesseractReference {

    /**
     * Encoding of tesseract reference for server->client networking.
     */
    public static void encode(TesseractReference reference, FriendlyByteBuf buffer){
        buffer.writeResourceLocation(reference.dimension);
        buffer.writeBlockPos(reference.pos);
        for(EnumChannelType type : EnumChannelType.BY_INDEX){
            buffer.writeVarInt(reference.channels.get(type));
            buffer.writeBoolean(reference.canSend.get(type));
            buffer.writeBoolean(reference.canReceive.get(type));
        }
    }

    public static TesseractReference decode(FriendlyByteBuf buffer){
        TesseractReference reference = new TesseractReference(
            0,
            buffer.readResourceLocation(),
            buffer.readBlockPos(),
            true
        );
        for(EnumChannelType type : EnumChannelType.BY_INDEX){
            reference.channels.set(type, buffer.readVarInt());
            reference.canSend.set(type, buffer.readBoolean());
            reference.canReceive.set(type, buffer.readBoolean());
        }
        return reference;
    }

    private final long index;
    private final ResourceLocation dimension;
    private final BlockPos pos;
    private final boolean isClientSide;
    private final PerChannel<Integer> channels = new PerChannel<>(-1);
    private final PerChannel<Boolean> canSend = new PerChannel<>(true);
    private final PerChannel<Boolean> canReceive = new PerChannel<>(true);
    private @Nullable TesseractBlockEntity entity;

    private TesseractReference(long index, ResourceLocation dimension, BlockPos pos, boolean isClientSide){
        this.index = index;
        this.dimension = dimension;
        this.pos = pos;
        this.isClientSide = isClientSide;
    }

    TesseractReference(long index, TesseractBlockEntity entity){
        this(index, entity.getLevel().dimension().location(), entity.getBlockPos(), entity.getLevel().isClientSide());
        for(EnumChannelType type : EnumChannelType.values()){
            this.canSend.set(type, entity.canSend(type));
            this.canReceive.set(type, entity.canReceive(type));
        }
        this.entity = entity;
    }

    public TesseractReference(long index, CompoundTag tag, boolean isClientSide){
        this(
            index,
            ResourceLocation.parse(tag.getStringOr("dim", "")),
            new BlockPos(tag.getIntOr("posx", 0), tag.getIntOr("posy", 0), tag.getIntOr("posz", 0)),
            isClientSide
        );
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.set(type, tag.getIntOr(type + "_channel", 0));
            this.canSend.set(type, tag.getBooleanOr(type + "_canSend", true));
            this.canReceive.set(type, tag.getBooleanOr(type + "_canReceive", true));
        }
    }

    public long getSaveIndex(){
        return this.index;
    }

    public ResourceLocation getDimension(){
        return this.dimension;
    }

    public BlockPos getPos(){
        return this.pos;
    }

    /**
     * Checks whether the tesseract is loaded and valid
     */
    public boolean canBeAccessed(){
        return this.getTesseract() != null;
    }

    @Nullable
    public TesseractBlockEntity getTesseract(){
        if(this.entity != null && (this.entity.isRemoved() || !this.entity.getBlockPos().equals(this.pos)))
            this.entity = null;
        return this.entity;
    }

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        compound.putString("dim", this.dimension.toString());
        compound.putInt("posx", this.pos.getX());
        compound.putInt("posy", this.pos.getY());
        compound.putInt("posz", this.pos.getZ());
        for(EnumChannelType type : EnumChannelType.values()){
            compound.putInt(type + "_channel", this.channels.get(type));
            compound.putBoolean(type + "_canSend", this.canSend.get(type));
            compound.putBoolean(type + "_canReceive", this.canReceive(type));
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
            this.channels.set(type, -1);
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
        this.channels.set(type, channel);
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
        this.entity = entity;
        for(EnumChannelType type : EnumChannelType.values()){
            boolean changed = false;
            boolean canSend = entity.canSend(type);
            if(canSend != this.canSend.set(type, canSend))
                changed = true;
            boolean canReceive = entity.canReceive(type);
            if(canReceive != this.canReceive.set(type, canReceive))
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
}
