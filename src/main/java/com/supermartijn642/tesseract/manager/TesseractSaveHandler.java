package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.TesseractConfig;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.nio.file.Path;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class TesseractSaveHandler {

    private static long lastSaveTime = 0;

    public static void registerListeners(){
        MinecraftForge.EVENT_BUS.addListener(TesseractSaveHandler::onJoin);
        MinecraftForge.EVENT_BUS.addListener(TesseractSaveHandler::tick);
        MinecraftForge.EVENT_BUS.addListener(TesseractSaveHandler::save);
        MinecraftForge.EVENT_BUS.addListener(TesseractSaveHandler::load);
    }

    private static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(e.getEntity().getCommandSenderWorld().isClientSide)
            return;

        TesseractTracker.sendReferences(e.getPlayer());
        TesseractChannelManager.sendChannels(e.getPlayer());
    }

    private static void tick(TickEvent.WorldTickEvent e){
        if(e.world.isClientSide || e.phase != TickEvent.Phase.END || e.world.getDimension().getType() != DimensionType.OVERWORLD)
            return;

        if(System.currentTimeMillis() - lastSaveTime >= TesseractConfig.saveInterval.get() * 60000){
            Path saveDirectory = ((ServerWorld)e.world).getLevelStorage().getFolder().toPath();
            TesseractTracker.saveReferences(saveDirectory);
            TesseractChannelManager.saveChannels(saveDirectory);
            lastSaveTime = System.currentTimeMillis();
        }
    }

    private static void save(WorldEvent.Save e){
        if(e.getWorld().isClientSide() || !(e.getWorld() instanceof World) || e.getWorld().getDimension().getType() != DimensionType.OVERWORLD)
            return;

        Path saveDirectory = ((ServerWorld)e.getWorld()).getLevelStorage().getFolder().toPath();
        TesseractTracker.saveReferences(saveDirectory);
        TesseractChannelManager.saveChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }

    private static void load(WorldEvent.Load e){
        if(e.getWorld().isClientSide() || !(e.getWorld() instanceof World) || e.getWorld().getDimension().getType() != DimensionType.OVERWORLD)
            return;

        Path saveDirectory = ((ServerWorld)e.getWorld()).getLevelStorage().getFolder().toPath();
        TesseractTracker.loadReferences(saveDirectory);
        TesseractChannelManager.loadChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }
}
