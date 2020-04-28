package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Objects;

/**
 * Created 3/22/2020 by SuperMartijn642
 */
public class TesseractLocation {

    private final int dimension;
    private final BlockPos pos;

    public TesseractLocation(int dimension, BlockPos pos){
        this.dimension = dimension;
        this.pos = pos;
    }

    public TesseractLocation(World world, BlockPos pos){
        this(world.provider.getDimension(), pos);
    }

    public TesseractLocation(NBTTagCompound compound){
        this.dimension = compound.getInteger("dim");
        this.pos = new BlockPos(compound.getInteger("posx"),compound.getInteger("posy"),compound.getInteger("posz"));
    }

    public int getDimension(){
        return this.dimension;
    }

    public World getWorld(){
        return DimensionManager.getWorld(this.dimension);
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public boolean isValid(){
        World world = this.getWorld();
        return world != null && world.getBlockState(this.pos).getBlock() == Tesseract.tesseract;
    }

    public TesseractTile getTesseract(){
        return (TesseractTile)this.getWorld().getTileEntity(this.pos);
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dim",this.dimension);
        compound.setInteger("posx",this.pos.getX());
        compound.setInteger("posy",this.pos.getY());
        compound.setInteger("posz",this.pos.getZ());
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
        int result = dimension;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        return result;
    }
}
