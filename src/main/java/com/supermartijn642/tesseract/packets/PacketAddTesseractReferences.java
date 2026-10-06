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
    private boolean clear;

    public PacketAddTesseractReferences(Collection<TesseractReference> references, boolean clearExisting){
        this.references = references;
        this.clear = clearExisting;
    }

    public PacketAddTesseractReferences(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.clear);
        buffer.writeInt(this.references.size());
        for(TesseractReference reference : this.references)
            TesseractReference.encode(reference, buffer);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.clear = buffer.readBoolean();
        int size = buffer.readInt();
        this.references = new ArrayList<>(Math.min(256, size));
        for(int i = 0; i < size; i++)
            this.references.add(TesseractReference.decode(buffer));
    }

    @Override
    public void handle(PacketContext context){
        if(this.clear)
            TesseractTracker.CLIENT.clear();
        for(TesseractReference reference : this.references)
            TesseractTracker.CLIENT.add(reference);
    }
}
