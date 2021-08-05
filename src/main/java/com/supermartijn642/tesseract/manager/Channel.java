package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.capabilities.CombinedEnergyStorage;
import com.supermartijn642.tesseract.capabilities.CombinedFluidHandler;
import com.supermartijn642.tesseract.capabilities.CombinedItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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

    public Channel(int id, EnumChannelType type, CompoundTag compound){
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

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        compound.putUUID("creator", this.creator);
        compound.putBoolean("private", this.isPrivate);
        compound.putString("name", this.name);
        CompoundTag tesseractCompound = new CompoundTag();
        Iterator<TesseractReference> iterator = this.tesseracts.iterator();
        for(int i = 0; iterator.hasNext(); i++)
            tesseractCompound.put("tesseract" + i, TesseractTracker.SERVER.writeKey(iterator.next()));
        compound.put("references", tesseractCompound);
        return compound;
    }

    public void read(CompoundTag compound){
        this.creator = compound.getUUID("creator");
        this.isPrivate = compound.getBoolean("private");
        this.name = compound.getString("name");
        this.tesseracts.clear();
        this.sendingTesseracts.clear();
        this.receivingTesseracts.clear();
        CompoundTag tesseractCompound = compound.getCompound("references");
        for(String key : tesseractCompound.getAllKeys()){
            TesseractReference reference = TesseractTracker.SERVER.fromKey(tesseractCompound.getCompound(key));
            if(reference != null)
                this.addTesseract(reference);
        }

        if(compound.contains("tesseracts")){ // for older versions
            tesseractCompound = compound.getCompound("tesseracts");
            for(String key : tesseractCompound.getAllKeys()){
                CompoundTag compound2 = tesseractCompound.getCompound(key);
                String dimension = compound2.getString("dim");
                BlockPos pos = new BlockPos(compound2.getInt("posx"), compound2.getInt("posy"), compound2.getInt("posz"));
                TesseractReference reference = TesseractTracker.SERVER.tryAdd(dimension, pos);
                if(reference != null)
                    this.addTesseract(reference);
            }
        }
    }

    public CompoundTag writeClientChannel(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", this.id);
        tag.putInt("type", this.type.getIndex());
        tag.putUUID("creator", this.creator);
        tag.putBoolean("private", this.isPrivate);
        tag.putString("name", this.name);
        return tag;
    }

    public static Channel readClientChannel(CompoundTag tag){
        int id = tag.getInt("id");
        EnumChannelType type = EnumChannelType.byIndex(tag.getInt("type"));
        UUID creator = tag.getUUID("creator");
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
