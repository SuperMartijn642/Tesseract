package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.TesseractTile;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class TesseractTracker {

    public static final TesseractTracker SERVER = new TesseractTracker();
//    public static final TesseractTracker CLIENT = new TesseractTracker();

    public static TesseractTracker getInstance(World world){
        return world.isRemote ? null /*CLIENT*/ : SERVER;
    }

    private final IntObjectMap<HashMap<BlockPos,TesseractReference>> tesseracts = new IntObjectHashMap<>();

    public TesseractReference add(TesseractTile self){
        int dimension = self.getWorld().provider.getDimension();
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        return this.tesseracts.get(dimension).computeIfAbsent(self.getPos(), key -> new TesseractReference(self));
    }

    @Deprecated
    public TesseractReference tryAdd(int dimension, BlockPos pos){
        World world = DimensionManager.getWorld(dimension);
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TesseractTile ? this.add((TesseractTile)tile) : null;
    }

    public TesseractReference remove(World world, BlockPos pos){
        int dimension = world.provider.getDimension();
        this.tesseracts.putIfAbsent(dimension, new HashMap<>());
        return this.tesseracts.get(dimension).remove(pos);
    }

    public TesseractReference get(World world, BlockPos pos){
        int dimension = world.provider.getDimension();
        return this.tesseracts.putIfAbsent(dimension, new HashMap<>()).get(pos);
    }

    public NBTTagCompound writeKey(TesseractReference reference){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", reference.getDimension());
        tag.setInteger("posx", reference.getPos().getX());
        tag.setInteger("posy", reference.getPos().getY());
        tag.setInteger("posz", reference.getPos().getZ());
        return tag;
    }

    public TesseractReference fromKey(NBTTagCompound key){
        int dimension = key.getInteger("dimension");
        BlockPos pos = new BlockPos(key.getInteger("posx"), key.getInteger("posy"), key.getInteger("posz"));
        return this.tesseracts.containsKey(dimension) ?
            this.tesseracts.get(dimension).get(pos) : null;
    }

    @SubscribeEvent
    public static void onSave(WorldEvent.Save e){
        World world = e.getWorld();

        if(world != null && !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD){
            File directory = new File(DimensionManager.getCurrentSaveRootDirectory(), "tesseract/tracking");

            int index = 0;
            for(Map.Entry<Integer,HashMap<BlockPos,TesseractReference>> dimensionEntry : SERVER.tesseracts.entrySet()){
                for(Map.Entry<BlockPos,TesseractReference> entry : dimensionEntry.getValue().entrySet()){
                    File file = new File(directory, "tesseract" + index++ + ".nbt");
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
    }

    @SubscribeEvent
    public static void onLoad(WorldEvent.Load e){
        World world = e.getWorld();
        if(world != null && !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD){
            SERVER.tesseracts.clear();

            File directory = new File(DimensionManager.getCurrentSaveRootDirectory(), "tesseract/tracking");
            File[] files = directory.listFiles();
            if(files != null){
                for(File file : directory.listFiles()){
                    if(file.isFile() && file.getName().endsWith(".nbt")){
                        try{
                            NBTTagCompound tag = CompressedStreamTools.read(file);
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
}
