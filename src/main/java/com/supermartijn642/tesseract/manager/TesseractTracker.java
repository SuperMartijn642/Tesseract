package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.packets.PacketAddTesseractReferences;
import com.supermartijn642.tesseract.packets.PacketRemoveTesseractReferences;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class TesseractTracker {

    public static final TesseractTracker SERVER = new TesseractTracker();
    public static final TesseractTracker CLIENT = new TesseractTracker();

    private static long referenceIndexCounter = 0;

    public static TesseractTracker getInstance(World level){
        return level.isClientSide ? CLIENT : SERVER;
    }

    public static void registerListeners(){
        MinecraftForge.EVENT_BUS.addListener(TesseractTracker::onTick);
    }

    private final IntObjectMap<HashMap<BlockPos,TesseractReference>> tesseracts = new IntObjectHashMap<>();
    private final Set<TesseractReference> dirtyReferences = new HashSet<>();
    private final Set<TesseractReference> removedReferences = new HashSet<>();
    private final Set<TesseractReference> referencesToBeSaved = new HashSet<>();
    private final Set<Long> referencesToBeUnsaved = new HashSet<>();

    public TesseractReference add(TesseractBlockEntity self){
        int dimension = self.getLevel().getDimension().getType().getId();
        BlockPos pos = self.getBlockPos();
        TesseractReference reference = this.getReference(dimension, pos);
        if(reference != null)
            return reference;
        // Create a new reference
        reference = new TesseractReference(referenceIndexCounter++, self);
        if(this == SERVER)
            this.markDirty(reference);
        this.tesseracts.computeIfAbsent(dimension, o -> new HashMap<>()).put(pos, reference);
        return reference;
    }

    public void add(TesseractReference reference){
        if(this == CLIENT){
            int dimension = reference.getDimension();
            BlockPos pos = reference.getPos();
            TesseractReference oldReference = this.tesseracts.computeIfAbsent(dimension, o -> new HashMap<>()).put(pos, reference);
            if(oldReference != null && oldReference != reference && oldReference.canBeAccessed())
                oldReference.getTesseract().invalidateReference();
        }
    }

    public TesseractReference getReference(int dimension, BlockPos pos){
        return this.tesseracts.containsKey(dimension) ? this.tesseracts.get(dimension).get(pos) : null;
    }

    public void remove(World level, BlockPos pos){
        int dimension = level.dimension.getType().getId();
        this.remove(dimension, pos);
    }

    public void remove(int dimension, BlockPos pos){
        if(this == SERVER){
            TesseractReference reference = this.getReference(dimension, pos);
            if(reference != null){
                reference.delete();
                this.tesseracts.get(dimension).remove(pos);
                this.removedReferences.add(reference);
                this.dirtyReferences.remove(reference);
                this.referencesToBeUnsaved.add(reference.getSaveIndex());
                this.referencesToBeSaved.remove(reference);
            }
        }else if(this.tesseracts.containsKey(dimension))
            this.tesseracts.get(dimension).remove(pos);
    }

    void markDirty(TesseractReference reference){
        this.dirtyReferences.add(reference);
        this.referencesToBeSaved.add(reference);
    }

    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.world.isClientSide || e.phase != TickEvent.Phase.END || e.world.getDimension().getType() != DimensionType.OVERWORLD)
            return;

        // Handle dirty references
        if(!SERVER.dirtyReferences.isEmpty()){
            Tesseract.CHANNEL.sendToAllPlayers(new PacketAddTesseractReferences(SERVER.dirtyReferences));
            SERVER.dirtyReferences.clear();
        }
        // Handle removed references
        if(!SERVER.removedReferences.isEmpty()){
            Tesseract.CHANNEL.sendToAllPlayers(new PacketRemoveTesseractReferences(SERVER.removedReferences));
            SERVER.removedReferences.clear();
        }
    }

    public CompoundNBT writeKey(TesseractReference reference){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("dimension", reference.getDimension());
        tag.putInt("posx", reference.getPos().getX());
        tag.putInt("posy", reference.getPos().getY());
        tag.putInt("posz", reference.getPos().getZ());
        return tag;
    }

    public TesseractReference fromKey(CompoundNBT key){
        int dimension = key.getInt("dimension");
        BlockPos pos = new BlockPos(key.getInt("posx"), key.getInt("posy"), key.getInt("posz"));
        return this.getReference(dimension, pos);
    }

    public static void saveReferences(Path saveDirectory){
        Path directory = saveDirectory.resolve("tesseract/tracking");
        try{
            Files.createDirectories(directory);
        }catch(IOException e){
            Tesseract.LOGGER.error("Failed to create tesseract reference save directory!", e);
            return;
        }

        for(TesseractReference reference : SERVER.referencesToBeSaved){
            Path file = directory.resolve("tesseract" + reference.getSaveIndex() + ".nbt");
            try(DataOutputStream output = new DataOutputStream(Files.newOutputStream(file))){
                CompressedStreamTools.write(reference.write(), output);
            }catch(IOException e){
                Tesseract.LOGGER.error("Failed to save tesseract reference file '" + file + "'!", e);
            }
        }
        for(Long index : SERVER.referencesToBeUnsaved){
            Path file = directory.resolve("tesseract" + index + ".nbt");
            try{
                Files.deleteIfExists(file);
            }catch(IOException e){
                Tesseract.LOGGER.error("Failed to remove tesseract reference file '" + file + "'!", e);
            }
        }
        SERVER.referencesToBeSaved.clear();
        SERVER.referencesToBeUnsaved.clear();
    }

    public static void loadReferences(Path saveDirectory){
        SERVER.tesseracts.clear();
        SERVER.dirtyReferences.clear();
        SERVER.removedReferences.clear();
        SERVER.referencesToBeSaved.clear();
        SERVER.referencesToBeUnsaved.clear();
        referenceIndexCounter = 0;

        Path directory = saveDirectory.resolve("tesseract/tracking");
        try(Stream<Path> files = Files.list(directory)){
            files.forEach(file -> {
                if(Files.isRegularFile(file) && file.getFileName().startsWith("tesseract") && file.getFileName().endsWith(".nbt")){
                    try{
                        long index = Long.parseLong(file.getFileName().toString().substring("tesseract".length(), file.getFileName().toString().length() - ".nbt".length()));
                        if(index > referenceIndexCounter)
                            referenceIndexCounter = index + 1;
                        CompoundNBT tag;
                        try(DataInputStream input = new DataInputStream(Files.newInputStream(file))){
                            tag = CompressedStreamTools.read(input);
                        }
                        TesseractReference reference = new TesseractReference(index, tag, false);
                        SERVER.tesseracts.putIfAbsent(reference.getDimension(), new HashMap<>());
                        SERVER.tesseracts.get(reference.getDimension()).put(reference.getPos(), reference);
                    }catch(IOException exception){
                        Tesseract.LOGGER.error("Failed to read tesseract data from file '~/tesseract/tracking/" + file.getFileName() + "':", exception);
                    }
                }
            });
        }catch(IOException e){
            Tesseract.LOGGER.error("Failed to load tesseract references!", e);
        }
    }

    public static void sendReferences(PlayerEntity player){
        Collection<TesseractReference> references = SERVER.tesseracts.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        Tesseract.CHANNEL.sendToPlayer(player, new PacketAddTesseractReferences(references));
    }
}
