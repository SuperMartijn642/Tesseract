package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created 3/22/2020 by SuperMartijn642
 */
public class TesseractReference {

    private final int dimension;
    private final BlockPos pos;
    private final EnumMap<EnumChannelType,Integer> channels = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,Boolean> canSend = new EnumMap<>(EnumChannelType.class);
    private final EnumMap<EnumChannelType,Boolean> canReceive = new EnumMap<>(EnumChannelType.class);

    public TesseractReference(TesseractBlockEntity entity){
        this.dimension = entity.getLevel().dimension.getType().getId();
        this.pos = entity.getBlockPos();
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, entity.getChannelId(type));
            this.canSend.put(type, entity.canSend(type));
            this.canReceive.put(type, entity.canReceive(type));
        }
    }

    public TesseractReference(CompoundNBT tag){
        this.dimension = tag.getInt("dim");
        this.pos = new BlockPos(tag.getInt("posx"), tag.getInt("posy"), tag.getInt("posz"));
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, tag.getInt(type + "_channel"));
            this.canSend.put(type, tag.getBoolean(type + "_canSend"));
            this.canReceive.put(type, tag.getBoolean(type + "_canReceive"));
        }
    }

    public int getDimension(){
        return this.dimension;
    }

    public World getLevel(){
        DimensionType type = DimensionType.getById(this.dimension);
        if(type == null || TesseractChannelManager.minecraftServer == null)
            return null;
        return DimensionManager.getWorld(TesseractChannelManager.minecraftServer, type, false, true);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World level = this.getLevel();
        boolean isValid = level != null && level.getBlockState(this.pos).getBlock() == Tesseract.tesseract && level.getBlockEntity(this.pos) instanceof TesseractBlockEntity;

        if(!isValid)
            TesseractTracker.SERVER.remove(this.dimension, this.pos);

        return isValid;
    }

    public TesseractBlockEntity getTesseract(){
        return (TesseractBlockEntity)this.getLevel().getBlockEntity(this.pos);
    }

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("dim", this.dimension);
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

    public void update(TesseractBlockEntity entity){
        for(EnumChannelType type : EnumChannelType.values()){
            int channelId = entity.getChannelId(type);
            this.channels.put(type, channelId);
            this.canSend.put(type, entity.canSend(type));
            this.canReceive.put(type, entity.canReceive(type));
            if(channelId == -1){
                Channel channel = TesseractChannelManager.getInstance(entity.getLevel()).getChannelById(type, channelId);
                if(channel != null)
                    channel.updateTesseract(this);
            }
        }
    }

    public void delete(){
        for(Map.Entry<EnumChannelType,Integer> entry : this.channels.entrySet()){
            Channel channel = TesseractChannelManager.SERVER.getChannelById(entry.getKey(), entry.getValue());
            if(channel != null)
                channel.removeTesseract(this);
        }
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof TesseractReference)) return false;
        TesseractReference that = (TesseractReference)o;
        return that.dimension == this.dimension && Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode(){
        int result = this.dimension;
        result = 31 * result + (this.pos != null ? this.pos.hashCode() : 0);
        return result;
    }
}
