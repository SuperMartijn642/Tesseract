package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
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

    public TesseractReference(TesseractTile tile){
        this.dimension = tile.getWorld().dimension.getType().getId();
        this.pos = tile.getPos();
        for(EnumChannelType type : EnumChannelType.values()){
            this.channels.put(type, tile.getChannelId(type));
            this.canSend.put(type, tile.canSend(type));
            this.canReceive.put(type, tile.canReceive(type));
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

    public World getWorld(){
        DimensionType type = DimensionType.getById(this.dimension);
        if(type == null || TesseractChannelManager.minecraftServer == null)
            return null;
        return DimensionManager.getWorld(TesseractChannelManager.minecraftServer, type, false, true);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World world = this.getWorld();
        boolean isValid = world != null && world.getBlockState(this.pos).getBlock() == Tesseract.tesseract && world.getTileEntity(this.pos) instanceof TesseractTile;

        if(!isValid)
            TesseractTracker.SERVER.remove(this.dimension, this.pos);

        return isValid;
    }

    public TesseractTile getTesseract(){
        return (TesseractTile)this.getWorld().getTileEntity(this.pos);
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

    public void update(TesseractTile tile){
        for(EnumChannelType type : EnumChannelType.values()){
            int channelId = tile.getChannelId(type);
            this.channels.put(type, channelId);
            this.canSend.put(type, tile.canSend(type));
            this.canReceive.put(type, tile.canReceive(type));
            if(channelId == -1){
                Channel channel = TesseractChannelManager.getInstance(tile.getWorld()).getChannelById(type, channelId);
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
