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
import net.minecraftforge.fml.common.gameevent.TickEvent;

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

    public static final TesseractTracker SERVER = new TesseractTracker();
//    public static final TesseractTracker CLIENT = new TesseractTracker();

    public static TesseractTracker getInstance(World world){
        return world.isRemote ? null /*CLIENT*/ : SERVER;
    }

    private final IntObjectMap<HashMap<BlockPos,TesseractReference>> tesseracts = new IntObjectHashMap<>();
    private final Set<TesseractReference> toBeRemoved = new HashSet<>();

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

    public void remove(World world, BlockPos pos){
        int dimension = world.provider.getDimension();
        this.remove(dimension, pos);
    }

    public void remove(int dimension, BlockPos pos){
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

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.world.isRemote || e.phase != TickEvent.Phase.END || e.world.provider.getDimensionType() != DimensionType.OVERWORLD)
            return;

        SERVER.toBeRemoved.forEach(SERVER::removeAndUpdate);
        SERVER.toBeRemoved.clear();
    }
}
