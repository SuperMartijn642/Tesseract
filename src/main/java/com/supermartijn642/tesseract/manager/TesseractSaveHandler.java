package com.supermartijn642.tesseract.manager;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.tesseract.TesseractConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class TesseractSaveHandler {

    private static long lastSaveTime = 0;

    public static void registerListeners(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(TesseractSaveHandler::tick);
        ServerWorldEvents.LOAD.register(TesseractSaveHandler::load);
    }

    private static void onJoin(Player player){
        if(player.getCommandSenderWorld().isClientSide)
            return;

        TesseractTracker.sendReferences(player);
        TesseractChannelManager.sendChannels(player);
    }

    private static void tick(MinecraftServer server){
        if(System.currentTimeMillis() - lastSaveTime >= TesseractConfig.saveInterval.get() * 60000){
            Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
            TesseractTracker.saveReferences(saveDirectory);
            TesseractChannelManager.saveChannels(saveDirectory);
            lastSaveTime = System.currentTimeMillis();
        }
    }

    public static void save(Level level){
        if(level.isClientSide() || level.dimension() != Level.OVERWORLD)
            return;

        Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
        TesseractTracker.saveReferences(saveDirectory);
        TesseractChannelManager.saveChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }

    private static void load(MinecraftServer server, Level level){
        if(level.isClientSide() || level.dimension() != Level.OVERWORLD)
            return;

        Path saveDirectory = CommonUtils.getServer().getWorldPath(LevelResource.ROOT);
        TesseractTracker.loadReferences(saveDirectory);
        TesseractChannelManager.loadChannels(saveDirectory);
        lastSaveTime = System.currentTimeMillis();
    }
}
