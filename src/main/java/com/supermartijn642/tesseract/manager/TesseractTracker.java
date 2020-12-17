package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class TesseractTracker {

    public static MinecraftServer minecraftServer;

    public static final TesseractTracker SERVER = new TesseractTracker();
//    public static final TesseractTracker CLIENT = new TesseractTracker();

    public static TesseractTracker getInstance(World world){
        return world.isRemote ? null /*CLIENT*/ : SERVER;
    }

    private final HashMap<String,HashMap<BlockPos,TesseractReference>> tesseracts = new HashMap<>();

    public TesseractReference add(TesseractTile self){
        String dimension = self.getWorld().func_234923_W_().func_240901_a_().toString();
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        return this.tesseracts.get(dimension).computeIfAbsent(self.getPos(), key -> new TesseractReference(self));
    }

    @Deprecated
    public TesseractReference tryAdd(String dimension, BlockPos pos){
        if(minecraftServer == null)
            return null;

        RegistryKey<World> key = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(dimension));
        World world = TesseractChannelManager.minecraftServer.getWorld(key);
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TesseractTile ? this.add((TesseractTile)tile) : null;
    }

    public TesseractReference remove(World world, BlockPos pos){
        String dimension = world.func_234923_W_().func_240901_a_().toString();
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        return this.tesseracts.get(dimension).remove(pos);
    }

    public TesseractReference get(World world, BlockPos pos){
        String dimension = world.func_234923_W_().func_240901_a_().toString();
        return this.tesseracts.putIfAbsent(dimension, new HashMap<>()).get(pos);
    }

    public CompoundNBT writeKey(TesseractReference reference){
        CompoundNBT tag = new CompoundNBT();
        tag.putString("dimension", reference.getDimension());
        tag.putInt("posx", reference.getPos().getX());
        tag.putInt("posy", reference.getPos().getY());
        tag.putInt("posz", reference.getPos().getZ());
        return tag;
    }

    public TesseractReference fromKey(CompoundNBT key){
        String dimension = key.getString("dimension");
        BlockPos pos = new BlockPos(key.getInt("posx"), key.getInt("posy"), key.getInt("posz"));
        return this.tesseracts.containsKey(dimension) ?
            this.tesseracts.get(dimension).get(pos) : null;
    }

    @SubscribeEvent
    public static void onSave(WorldEvent.Save e){
        if(e.getWorld().isRemote() || !(e.getWorld() instanceof World) || ((World)e.getWorld()).func_234923_W_() != World.field_234918_g_)
            return;

        File directory = new File(((ServerWorld)e.getWorld()).getServer().func_240776_a_(FolderName.field_237253_i_).toFile(), "tesseract/tracking");
        int index = 0;
        for(Map.Entry<String,HashMap<BlockPos,TesseractReference>> dimensionEntry : SERVER.tesseracts.entrySet()){
            for(Map.Entry<BlockPos,TesseractReference> entry : dimensionEntry.getValue().entrySet()){
                File file = new File(directory, "tesseract" + index + ".nbt");
                try{
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    CompressedStreamTools.write(entry.getValue().write(), file);
                }catch(IOException ioException){
                    ioException.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLoad(WorldEvent.Load e){
        if(e.getWorld().isRemote() || !(e.getWorld() instanceof World) || ((World)e.getWorld()).func_234923_W_() != World.field_234918_g_)
            return;

        minecraftServer = ((ServerWorld)e.getWorld()).getServer();

        SERVER.tesseracts.clear();

        File directory = new File(((ServerWorld)e.getWorld()).getServer().func_240776_a_(FolderName.field_237253_i_).toFile(), "tesseract/tracking");
        File[] files = directory.listFiles();
        if(files != null){
            for(File file : directory.listFiles()){
                if(file.isFile() && file.getName().endsWith(".nbt")){
                    try{
                        CompoundNBT tag = CompressedStreamTools.read(file);
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
}
