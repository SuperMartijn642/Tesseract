package com.supermartijn642.tesseract.manager;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.tesseract.TesseractConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;

import java.nio.file.Path;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class TesseractSaveHandler {

    private static long lastSaveTime = 0;

    public static void registerListeners(){
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(TesseractSaveHandler::onJoin);
        TickEvent.LevelTickEvent.Post.BUS.addListener(TesseractSaveHandler::tick);
        LevelEvent.Save.BUS.addListener(TesseractSaveHandler::save);
        LevelEvent.Load.BUS.addListener(TesseractSaveHandler::load);
    }

    private static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity().level().isClientSide)
            return;

        TesseractTracker.sendReferences(e.getEntity());
        TesseractChannelManager.sendChannels(e.getEntity());
    }

    private static void tick(TickEvent.LevelTickEvent.Post e){
        if(e.level.isClientSide || e.level.dimension() != Level.OVERWORLD)
            return;

        if(System.currentTimeMillis() - lastSaveTime >= TesseractConfig.saveInterval.get() * 60000){
            Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
            TesseractTracker.saveReferences(saveDirectory);
            TesseractChannelManager.saveChannels(saveDirectory);
            lastSaveTime = System.currentTimeMillis();
        }
    }

    private static void save(LevelEvent.Save e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
        TesseractTracker.saveReferences(saveDirectory);
        TesseractChannelManager.saveChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }

    private static void load(LevelEvent.Load e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
        TesseractTracker.loadReferences(saveDirectory);
        TesseractChannelManager.loadChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }
}
