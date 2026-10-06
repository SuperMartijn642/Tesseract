package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.capabilities.CombinedEnergyStorage;
import com.supermartijn642.tesseract.capabilities.CombinedFluidHandler;
import com.supermartijn642.tesseract.capabilities.CombinedItemHandler;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class Channel {

    public static final int CHANNEL_MIN_CHARACTERS = 3;
    public static final int CHANNEL_MAX_CHARACTERS = 19;

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
                TesseractReference reference = TesseractTracker.SERVER.getReference(dimension, pos);
                if(reference != null)
                    this.addTesseract(reference);
            }
        }
    }

    public void writeClientChannel(PacketBuffer buffer){
        buffer.writeInt(this.id);
        buffer.writeEnumValue(this.type);
        buffer.writeUniqueId(this.creator);
        buffer.writeBoolean(this.isPrivate);
        buffer.writeString(this.name);
    }

    public static Channel readClientChannel(PacketBuffer buffer){
        int id = buffer.readInt();
        EnumChannelType type = buffer.readEnumValue(EnumChannelType.class);
        UUID creator = buffer.readUniqueId();
        boolean isPrivate = buffer.readBoolean();
        String name = buffer.readString(Channel.CHANNEL_MAX_CHARACTERS + 1);
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
