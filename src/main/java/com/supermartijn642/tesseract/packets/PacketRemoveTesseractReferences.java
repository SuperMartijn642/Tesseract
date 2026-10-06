package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class PacketRemoveTesseractReferences implements BasePacket {

    private Map<ResourceLocation,List<BlockPos>> references;

    public PacketRemoveTesseractReferences(Collection<TesseractReference> references){
        this.references = new HashMap<>(4);
        for(TesseractReference reference : references)
            this.references.computeIfAbsent(reference.getDimension(), o -> new ArrayList<>()).add(reference.getPos());
    }

    public PacketRemoveTesseractReferences(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.references.size());
        for(Map.Entry<ResourceLocation,List<BlockPos>> entry : this.references.entrySet()){
            buffer.writeResourceLocation(entry.getKey());
            List<BlockPos> positions = entry.getValue();
            buffer.writeInt(positions.size());
            for(BlockPos position : positions)
                buffer.writeBlockPos(position);
        }
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        int dimensions = buffer.readInt();
        this.references = new HashMap<>(Math.min(16, dimensions));
        for(int i = 0; i < dimensions; i++){
            ResourceLocation dimension = buffer.readResourceLocation();
            int numberOfPositions = buffer.readInt();
            Set<BlockPos> positions = new HashSet<>(Math.min(16, numberOfPositions));
            for(int j = 0; j < numberOfPositions; j++)
                positions.add(buffer.readBlockPos());
            this.references.put(dimension, List.copyOf(positions));
        }
    }

    @Override
    public void handle(PacketContext context){
        if(context.getHandlingSide().isServer())
            return;
        for(Map.Entry<ResourceLocation,List<BlockPos>> entry : this.references.entrySet()){
            ResourceLocation dimension = entry.getKey();
            for(BlockPos pos : entry.getValue())
                TesseractTracker.CLIENT.remove(dimension, pos);
        }
    }
}
