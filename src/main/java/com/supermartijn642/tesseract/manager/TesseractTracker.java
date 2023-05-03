package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.packets.PacketAddTesseractReferences;
import com.supermartijn642.tesseract.packets.PacketRemoveTesseractReferences;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    public static TesseractTracker getInstance(Level level){
        return level.isClientSide ? CLIENT : SERVER;
    }

    public static void registerListeners(){
        MinecraftForge.EVENT_BUS.addListener(TesseractTracker::onTick);
    }

    private final HashMap<String,HashMap<BlockPos,TesseractReference>> tesseracts = new HashMap<>();
    private final Set<TesseractReference> dirtyReferences = new HashSet<>();
    private final Set<TesseractReference> referencesToBeRemoved = new HashSet<>();
    private final Set<TesseractReference> referencesToBeSaved = new HashSet<>();
    private final Set<Long> referencesToBeUnsaved = new HashSet<>();

    public TesseractReference add(TesseractBlockEntity self){
        String dimension = self.getLevel().dimension().location().toString();
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
            String dimension = reference.getDimension();
            BlockPos pos = reference.getPos();
            TesseractReference oldReference = this.tesseracts.computeIfAbsent(dimension, o -> new HashMap<>()).put(pos, reference);
            if(oldReference != null && oldReference != reference && oldReference.canBeAccessed())
                oldReference.getTesseract().invalidateReference();
        }
    }

    public TesseractReference getReference(String dimension, BlockPos pos){
        return this.tesseracts.containsKey(dimension) ? this.tesseracts.get(dimension).get(pos) : null;
    }

    public void remove(Level level, BlockPos pos){
        String dimension = level.dimension().location().toString();
        this.remove(dimension, pos);
    }

    public void remove(String dimension, BlockPos pos){
        if(this == SERVER){
            TesseractReference reference = this.getReference(dimension, pos);
            if(reference != null)
                this.referencesToBeRemoved.add(reference);
        }else if(this.tesseracts.containsKey(dimension))
            this.tesseracts.get(dimension).remove(pos);
    }

    void markDirty(TesseractReference reference){
        this.dirtyReferences.add(reference);
        this.referencesToBeSaved.add(reference);
    }

    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.world.isClientSide || e.phase != TickEvent.Phase.END || e.world.dimension() != Level.OVERWORLD)
            return;

        // Handle removed references
        if(!SERVER.referencesToBeRemoved.isEmpty()){
            for(TesseractReference reference : SERVER.referencesToBeRemoved){
                reference.delete();
                SERVER.tesseracts.get(reference.getDimension()).remove(reference.getPos());
                SERVER.dirtyReferences.remove(reference);
                SERVER.referencesToBeUnsaved.add(reference.getSaveIndex());
                SERVER.referencesToBeSaved.remove(reference);
            }
            Tesseract.CHANNEL.sendToAllPlayers(new PacketRemoveTesseractReferences(SERVER.referencesToBeRemoved));
            SERVER.referencesToBeRemoved.clear();
        }
        // Handle dirty references
        if(!SERVER.dirtyReferences.isEmpty()){
            Tesseract.CHANNEL.sendToAllPlayers(new PacketAddTesseractReferences(SERVER.dirtyReferences));
            SERVER.dirtyReferences.clear();
        }
    }

    public CompoundTag writeKey(TesseractReference reference){
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", reference.getDimension());
        tag.putInt("posx", reference.getPos().getX());
        tag.putInt("posy", reference.getPos().getY());
        tag.putInt("posz", reference.getPos().getZ());
        return tag;
    }

    public TesseractReference fromKey(CompoundTag key){
        String dimension = key.getString("dimension");
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
                NbtIo.write(reference.write(), output);
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
        SERVER.referencesToBeRemoved.clear();
        SERVER.referencesToBeSaved.clear();
        SERVER.referencesToBeUnsaved.clear();
        referenceIndexCounter = 0;

        Path directory = saveDirectory.resolve("tesseract/tracking");
        try(Stream<Path> files = Files.list(directory)){
            files.forEach(file -> {
                if(Files.isRegularFile(file)){
                    String fileName = file.getFileName().toString();
                    if(fileName.startsWith("tesseract") && fileName.endsWith(".nbt")){
                        try{
                            long index = Long.parseLong(file.getFileName().toString().substring("tesseract".length(), file.getFileName().toString().length() - ".nbt".length()));
                            if(index > referenceIndexCounter)
                                referenceIndexCounter = index + 1;
                            CompoundTag tag;
                            try(DataInputStream input = new DataInputStream(Files.newInputStream(file))){
                                tag = NbtIo.read(input);
                            }
                            TesseractReference reference = new TesseractReference(index, tag, false);
                            SERVER.tesseracts.putIfAbsent(reference.getDimension(), new HashMap<>());
                            SERVER.tesseracts.get(reference.getDimension()).put(reference.getPos(), reference);
                        }catch(IOException exception){
                            Tesseract.LOGGER.error("Failed to read tesseract data from file '~/tesseract/tracking/" + file.getFileName() + "':", exception);
                        }
                    }
                }
            });
        }catch(IOException e){
            Tesseract.LOGGER.error("Failed to load tesseract references!", e);
        }
        System.out.println("Loaded " + SERVER.tesseracts.values().stream().map(Map::values).mapToLong(Collection::size).sum() + " tesseract references!");
    }

    public static void sendReferences(Player player){
        Collection<TesseractReference> references = SERVER.tesseracts.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        Tesseract.CHANNEL.sendToPlayer(player, new PacketAddTesseractReferences(references));
    }
}
