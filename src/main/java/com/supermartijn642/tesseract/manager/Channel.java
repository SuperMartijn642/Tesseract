package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.capabilities.CombinedEnergyStorage;
import com.supermartijn642.tesseract.capabilities.CombinedFluidHandler;
import com.supermartijn642.tesseract.capabilities.CombinedItemHandler;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
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

    public final Set<TesseractReference> tesseracts = new ReferenceArraySet<>(); // Iteration is most important, insertion/removal is sparse and only by direct user action
    public final Set<TesseractReference> sendingTesseracts = new ReferenceArraySet<>();
    public final Set<TesseractReference> receivingTesseracts = new ReferenceArraySet<>();

    /**
     * Counts recurrent calls inside the combined capabilities in order to prevent infinite loops
     */
    public int recurrentCalls = 0;

    public Channel(int id, EnumChannelType type, UUID creator, boolean isPrivate, String name){
        this.id = id;
        this.type = type;
        this.creator = creator;
        this.isPrivate = isPrivate;
        this.name = name;
    }

    public Channel(int id, EnumChannelType type, CompoundNBT compound){
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
                tesseract.setChannel(this.type, this.id);
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

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        compound.putUUID("creator", this.creator);
        compound.putBoolean("private", this.isPrivate);
        compound.putString("name", this.name);
        CompoundNBT tesseractCompound = new CompoundNBT();
        Iterator<TesseractReference> iterator = this.tesseracts.iterator();
        for(int i = 0; iterator.hasNext(); i++)
            tesseractCompound.put("tesseract" + i, TesseractTracker.SERVER.writeKey(iterator.next()));
        compound.put("references", tesseractCompound);
        return compound;
    }

    public void read(CompoundNBT compound){
        this.creator = compound.getUUID("creator");
        this.isPrivate = compound.getBoolean("private");
        this.name = compound.getString("name");
        this.tesseracts.clear();
        this.sendingTesseracts.clear();
        this.receivingTesseracts.clear();
        CompoundNBT tesseractCompound = compound.getCompound("references");
        for(String key : tesseractCompound.getAllKeys()){
            TesseractReference reference = TesseractTracker.SERVER.fromKey(tesseractCompound.getCompound(key));
            if(reference != null)
                this.addTesseract(reference);
        }

        if(compound.contains("tesseracts")){ // for older versions
            tesseractCompound = compound.getCompound("tesseracts");
            for(String key : tesseractCompound.getAllKeys()){
                CompoundNBT compound2 = tesseractCompound.getCompound(key);
                int dimension = compound2.getInt("dim");
                BlockPos pos = new BlockPos(compound2.getInt("posx"), compound2.getInt("posy"), compound2.getInt("posz"));
                TesseractReference reference = TesseractTracker.SERVER.getReference(dimension, pos);
                if(reference != null)
                    this.addTesseract(reference);
            }
        }
    }

    public void writeClientChannel(PacketBuffer buffer){
        buffer.writeInt(this.id);
        buffer.writeEnum(this.type);
        buffer.writeUUID(this.creator);
        buffer.writeBoolean(this.isPrivate);
        buffer.writeUtf(this.name);
    }

    public static Channel readClientChannel(PacketBuffer buffer){
        int id = buffer.readInt();
        EnumChannelType type = buffer.readEnum(EnumChannelType.class);
        UUID creator = buffer.readUUID();
        boolean isPrivate = buffer.readBoolean();
        String name = buffer.readUtf(32767);
        return new Channel(id, type, creator, isPrivate, name);
    }

    public CombinedItemHandler getItemHandler(TesseractBlockEntity self){
        return new CombinedItemHandler(this, self.getReference());
    }

    public CombinedFluidHandler getFluidHandler(TesseractBlockEntity self){
        return new CombinedFluidHandler(this, self.getReference());
    }

    public CombinedEnergyStorage getEnergyStorage(TesseractBlockEntity self){
        return new CombinedEnergyStorage(this, self.getReference());
    }

    @Override
    public int hashCode(){
        return this.id + 31 * this.type.hashCode();
    }

    public void delete(){
        for(TesseractReference location : this.tesseracts)
            location.setChannel(this.type, -1);
    }
}
