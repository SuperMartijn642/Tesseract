package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        this.dimension = entity.getWorld().provider.getDimension();
        this.pos = entity.getPos();
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, entity.getChannelId(type));
            this.canSend.put(type, entity.canSend(type));
            this.canReceive.put(type, entity.canReceive(type));
        }
    }

    public TesseractReference(NBTTagCompound tag){
        this.dimension = tag.getInteger("dim");
        this.pos = new BlockPos(tag.getInteger("posx"), tag.getInteger("posy"), tag.getInteger("posz"));
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, tag.getInteger(type + "_channel"));
            this.canSend.put(type, tag.getBoolean(type + "_canSend"));
            this.canReceive.put(type, tag.getBoolean(type + "_canReceive"));
        }
    }

    public int getDimension(){
        return this.dimension;
    }

    public World getLevel(){
        return DimensionManager.getWorld(this.dimension);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World level = this.getLevel();
        boolean isValid = level != null && level.getBlockState(this.pos).getBlock() == Tesseract.tesseract && level.getTileEntity(this.pos) instanceof TesseractBlockEntity;

        if(!isValid)
            TesseractTracker.SERVER.remove(this.dimension, this.pos);

        return isValid;
    }

    public TesseractBlockEntity getTesseract(){
        return (TesseractBlockEntity)this.getLevel().getTileEntity(this.pos);
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

    public void update(TesseractBlockEntity entity){
        for(EnumChannelType type : EnumChannelType.values()){
            int channelId = entity.getChannelId(type);
            this.channels.put(type, channelId);
            this.canSend.put(type, entity.canSend(type));
            this.canReceive.put(type, entity.canReceive(type));
            if(channelId == -1){
                Channel channel = TesseractChannelManager.getInstance(entity.getWorld()).getChannelById(type, channelId);
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
