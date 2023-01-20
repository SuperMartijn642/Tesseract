package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class TesseractTracker {

    public static MinecraftServer minecraftServer;

    public static final TesseractTracker SERVER = new TesseractTracker();
//    public static final TesseractTracker CLIENT = new TesseractTracker();

    public static TesseractTracker getInstance(Level world){
        return world.isClientSide ? null /*CLIENT*/ : SERVER;
    }

    private final HashMap<String,HashMap<BlockPos,TesseractReference>> tesseracts = new HashMap<>();
    private final Set<TesseractReference> toBeRemoved = new HashSet<>();

    public TesseractReference add(TesseractBlockEntity self){
        String dimension = self.getLevel().dimension().location().toString();
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        return this.tesseracts.get(dimension).computeIfAbsent(self.getBlockPos(), key -> new TesseractReference(self));
    }

    @Deprecated
    public TesseractReference tryAdd(String dimension, BlockPos pos){
        if(minecraftServer == null)
            return null;

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
        Level level = TesseractChannelManager.minecraftServer.getLevel(key);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof TesseractBlockEntity ? this.add((TesseractBlockEntity)entity) : null;
    }

    public void remove(Level level, BlockPos pos){
        String dimension = level.dimension().location().toString();
        this.remove(dimension, pos);
    }

    public void remove(String dimension, BlockPos pos){
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        TesseractReference reference = this.tesseracts.get(dimension).get(pos);
        if(reference != null)
            this.toBeRemoved.add(reference);
    }

    private void removeAndUpdate(TesseractReference reference){
        reference.delete();
        this.tesseracts.putIfAbsent(reference.getDimension(), new HashMap<>());
        this.tesseracts.get(reference.getDimension()).remove(reference.getPos());
    }

    public TesseractReference get(Level level, BlockPos pos){
        String dimension = level.dimension().location().toString();
        return this.tesseracts.putIfAbsent(dimension, new HashMap<>()).get(pos);
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
        return this.tesseracts.containsKey(dimension) ?
            this.tesseracts.get(dimension).get(pos) : null;
    }

    @SubscribeEvent
    public static void onSave(LevelEvent.Save e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        File directory = new File(((ServerLevel)e.getLevel()).getServer().getWorldPath(LevelResource.ROOT).toFile(), "tesseract/tracking");
        int index = 0;
        for(Map.Entry<String,HashMap<BlockPos,TesseractReference>> dimensionEntry : SERVER.tesseracts.entrySet()){
            for(Map.Entry<BlockPos,TesseractReference> entry : dimensionEntry.getValue().entrySet()){
                File file = new File(directory, "tesseract" + index++ + ".nbt");
                try{
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    NbtIo.write(entry.getValue().write(), file);
                }catch(IOException ioException){
                    ioException.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        minecraftServer = ((ServerLevel)e.getLevel()).getServer();

        SERVER.tesseracts.clear();

        File directory = new File(((ServerLevel)e.getLevel()).getServer().getWorldPath(LevelResource.ROOT).toFile(), "tesseract/tracking");
        File[] files = directory.listFiles();
        if(files != null){
            for(File file : directory.listFiles()){
                if(file.isFile() && file.getName().endsWith(".nbt")){
                    try{
                        CompoundTag tag = NbtIo.read(file);
                        TesseractReference location = new TesseractReference(tag);
                        SERVER.tesseracts.putIfAbsent(location.getDimension(), new HashMap<>());
                        SERVER.tesseracts.get(location.getDimension()).put(location.getPos(), location);
                    }catch(IOException ioException){
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent e){
        if(e.level.isClientSide || e.phase != TickEvent.Phase.END || e.level.dimension() != Level.OVERWORLD)
            return;

        SERVER.toBeRemoved.forEach(SERVER::removeAndUpdate);
        SERVER.toBeRemoved.clear();
    }
}
