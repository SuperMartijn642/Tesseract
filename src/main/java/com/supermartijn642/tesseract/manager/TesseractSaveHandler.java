package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.TesseractConfig;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.nio.file.Path;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class TesseractSaveHandler {

    private static long lastSaveTime = 0;

    public static void registerListeners(){
        MinecraftForge.EVENT_BUS.register(TesseractSaveHandler.class);
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(e.player.getEntityWorld().isRemote)
            return;

        TesseractTracker.sendReferences(e.player);
        TesseractChannelManager.sendChannels(e.player);
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent e){
        if(e.world.isRemote || e.phase != TickEvent.Phase.END || e.world.provider.getDimension() != 0)
            return;

        if(System.currentTimeMillis() - lastSaveTime >= TesseractConfig.saveInterval.get() * 60000){
            Path saveDirectory = DimensionManager.getCurrentSaveRootDirectory().toPath();
            TesseractTracker.saveReferences(saveDirectory);
            TesseractChannelManager.saveChannels(saveDirectory);
            lastSaveTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public static void save(WorldEvent.Save e){
        if(e.getWorld().isRemote || e.getWorld().provider.getDimension() != 0)
            return;

        Path saveDirectory = DimensionManager.getCurrentSaveRootDirectory().toPath();
        TesseractTracker.saveReferences(saveDirectory);
        TesseractChannelManager.saveChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void load(WorldEvent.Load e){
        if(e.getWorld().isRemote || e.getWorld().provider.getDimension() != 0)
            return;

        Path saveDirectory = DimensionManager.getCurrentSaveRootDirectory().toPath();
        TesseractTracker.loadReferences(saveDirectory);
        TesseractChannelManager.loadChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }
}
