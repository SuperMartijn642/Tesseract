package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class PacketRemoveTesseractReferences implements BasePacket {

    private Int2ObjectMap<List<BlockPos>> references;

    public PacketRemoveTesseractReferences(Collection<TesseractReference> references){
        this.references = new Int2ObjectArrayMap<>(4);
        for(TesseractReference reference : references)
            this.references.computeIfAbsent(reference.getDimension(), o -> new ArrayList<>()).add(reference.getPos());
    }

    public PacketRemoveTesseractReferences(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeInt(this.references.size());
        for(Int2ObjectMap.Entry<List<BlockPos>> entry : this.references.int2ObjectEntrySet()){
            buffer.writeVarInt(entry.getIntKey());
            List<BlockPos> positions = entry.getValue();
            buffer.writeInt(positions.size());
            for(BlockPos position : positions)
                buffer.writeBlockPos(position);
        }
    }

    @Override
    public void read(PacketBuffer buffer){
        int dimensions = buffer.readInt();
        this.references = new Int2ObjectArrayMap<>(Math.min(16, dimensions));
        for(int i = 0; i < dimensions; i++){
            int dimension = buffer.readVarInt();
            int numberOfPositions = buffer.readInt();
            Set<BlockPos> positions = new HashSet<>(Math.min(16, numberOfPositions));
            for(int j = 0; j < numberOfPositions; j++)
                positions.add(buffer.readBlockPos());
            this.references.put(dimension, new ArrayList<>(positions));
        }
    }

    @Override
    public void handle(PacketContext context){
        for(Int2ObjectMap.Entry<List<BlockPos>> entry : this.references.int2ObjectEntrySet()){
            int dimension = entry.getIntKey();
            for(BlockPos pos : entry.getValue())
                TesseractTracker.CLIENT.remove(dimension, pos);
        }
    }
}
