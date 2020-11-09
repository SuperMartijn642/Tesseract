package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * Created 3/22/2020 by SuperMartijn642
 */
public class TesseractLocation {

    private final String dimension;
    private final BlockPos pos;

    public TesseractLocation(String dimension, BlockPos pos){
        this.dimension = dimension;
        this.pos = pos;
    }

    public TesseractLocation(World world, BlockPos pos){
        this(world.getDimensionKey().getLocation().toString(), pos);
    }

    public TesseractLocation(CompoundNBT compound){
        this(compound.getString("dim"), new BlockPos(compound.getInt("posx"), compound.getInt("posy"), compound.getInt("posz")));
    }

    public String getDimension(){
        return this.dimension;
    }

    public World getWorld(){
        if(TesseractChannelManager.minecraftServer == null)
            return null;
        RegistryKey<World> key = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(this.dimension));
        return TesseractChannelManager.minecraftServer.getWorld(key);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World world = this.getWorld();
        return world != null && world.getBlockState(this.pos).getBlock() == Tesseract.tesseract && world.getTileEntity(pos) instanceof TesseractTile;
    }

    public TesseractTile getTesseract(){
        return (TesseractTile)this.getWorld().getTileEntity(this.pos);
    }

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putString("dim", this.dimension);
        compound.putInt("posx", this.pos.getX());
        compound.putInt("posy", this.pos.getY());
        compound.putInt("posz", this.pos.getZ());
        return compound;
    }

    public boolean canSend(EnumChannelType type){
        return this.getTesseract().canSend(type);
    }

    public boolean canReceive(EnumChannelType type){
        return this.getTesseract().canReceive(type);
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof TesseractLocation)) return false;
        TesseractLocation that = (TesseractLocation)o;
        return that.dimension == this.dimension && Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode(){
        int result = dimension.hashCode();
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        return result;
    }
}
