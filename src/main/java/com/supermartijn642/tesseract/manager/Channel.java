package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.capabilities.CombinedEnergyStorage;
import com.supermartijn642.tesseract.capabilities.CombinedFluidHandler;
import com.supermartijn642.tesseract.capabilities.CombinedItemHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class Channel {

    public final int id;
    public final EnumChannelType type;
    public UUID creator;
    public boolean isPrivate = false;

    public String name;

    public final Set<TesseractReference> tesseracts = new LinkedHashSet<>();
    public final Set<TesseractReference> sendingTesseracts = new LinkedHashSet<>();
    public final Set<TesseractReference> receivingTesseracts = new LinkedHashSet<>();

    public Channel(int id, EnumChannelType type, UUID creator, boolean isPrivate, String name){
        this.id = id;
        this.type = type;
        this.creator = creator;
        this.isPrivate = isPrivate;
        this.name = name;
    }

    public Channel(int id, EnumChannelType type, NBTTagCompound compound){
        this.id = id;
        this.type = type;
        this.read(compound);
    }

    public String getName(){
        return this.name;
    }

    public void addTesseract(TesseractReference tesseract){
        if(!this.tesseracts.contains(tesseract)){
            this.tesseracts.add(tesseract);
            if(tesseract.canSend(this.type))
                this.sendingTesseracts.add(tesseract);
            if(tesseract.canReceive(this.type))
                this.receivingTesseracts.add(tesseract);
            if(tesseract.getChannelId(this.type) != this.id)
                tesseract.getTesseract().setChannel(this.type, this.id);
        }
    }

    public void removeTesseract(TesseractReference tesseract){
        this.tesseracts.remove(tesseract);
        this.sendingTesseracts.remove(tesseract);
        this.receivingTesseracts.remove(tesseract);
    }

    public void updateTesseract(TesseractReference tesseract){
        if(tesseract.canSend(this.type))
            this.sendingTesseracts.add(tesseract);
        else
            this.sendingTesseracts.remove(tesseract);

        if(tesseract.canReceive(this.type))
            this.receivingTesseracts.add(tesseract);
        else
            this.receivingTesseracts.remove(tesseract);
    }

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setUniqueId("creator", this.creator);
        compound.setBoolean("private", this.isPrivate);
        compound.setString("name", this.name);
        NBTTagCompound tesseractCompound = new NBTTagCompound();
        Iterator<TesseractReference> iterator = this.tesseracts.iterator();
        for(int i = 0; iterator.hasNext(); i++)
            tesseractCompound.setTag("tesseract" + i, TesseractTracker.SERVER.writeKey(iterator.next()));
        compound.setTag("references", tesseractCompound);
        return compound;
    }

    public void read(NBTTagCompound compound){
        this.creator = compound.getUniqueId("creator");
        this.isPrivate = compound.getBoolean("private");
        this.name = compound.getString("name");
        this.tesseracts.clear();
        this.sendingTesseracts.clear();
        this.receivingTesseracts.clear();
        NBTTagCompound tesseractCompound = compound.getCompoundTag("references");
        for(String key : tesseractCompound.getKeySet()){
            TesseractReference reference = TesseractTracker.SERVER.fromKey(tesseractCompound.getCompoundTag(key));
            if(reference != null)
                this.addTesseract(reference);
        }

        if(compound.hasKey("tesseracts")){ // for older versions
            tesseractCompound = compound.getCompoundTag("tesseracts");
            for(String key : tesseractCompound.getKeySet()){
                NBTTagCompound compound2 = tesseractCompound.getCompoundTag(key);
                int dimension = compound2.getInteger("dim");
                BlockPos pos = new BlockPos(compound2.getInteger("posx"), compound2.getInteger("posy"), compound2.getInteger("posz"));
                TesseractReference reference = TesseractTracker.SERVER.tryAdd(dimension, pos);
                if(reference != null)
                    this.addTesseract(reference);
            }
        }
    }

    public NBTTagCompound writeClientChannel(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("id", this.id);
        tag.setInteger("type", this.type.getIndex());
        tag.setUniqueId("creator", this.creator);
        tag.setBoolean("private", this.isPrivate);
        tag.setString("name", this.name);
        return tag;
    }

    public static Channel readClientChannel(NBTTagCompound tag){
        int id = tag.getInteger("id");
        EnumChannelType type = EnumChannelType.byIndex(tag.getInteger("type"));
        UUID creator = tag.getUniqueId("creator");
        boolean isPrivate = tag.getBoolean("private");
        String name = tag.getString("name");
        return new Channel(id, type, creator, isPrivate, name);
    }

    public CombinedItemHandler getItemHandler(TesseractTile self){
        return new CombinedItemHandler(this, self);
    }

    public CombinedFluidHandler getFluidHandler(TesseractTile self){
        return new CombinedFluidHandler(this, self);
    }

    public CombinedEnergyStorage getEnergyStorage(TesseractTile self){
        return new CombinedEnergyStorage(this, self);
    }

    @Override
    public int hashCode(){
        return this.id + 31 * this.type.hashCode();
    }

    public void delete(){
        for(TesseractReference location : this.tesseracts)
            if(location.isValid())
                location.getTesseract().setChannel(this.type, -1);
    }
}
