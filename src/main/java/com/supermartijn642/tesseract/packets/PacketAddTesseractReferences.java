package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class PacketAddTesseractReferences implements BasePacket {

    private Collection<TesseractReference> references;

    public PacketAddTesseractReferences(Collection<TesseractReference> references){
        this.references = references;
    }

    public PacketAddTesseractReferences(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.references.size());
        for(TesseractReference reference : this.references)
            buffer.writeNbt(reference.write());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        int size = buffer.readInt();
        this.references = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            try{
                TesseractReference reference = new TesseractReference(0, buffer.readNbt(), true);
                this.references.add(reference);
            }catch(Exception e){
                throw new RuntimeException("Received invalid tesseract reference data!", e);
            }
        }
    }

    @Override
    public void handle(PacketContext context){
        for(TesseractReference reference : this.references)
            TesseractTracker.CLIENT.add(reference);
    }
}
