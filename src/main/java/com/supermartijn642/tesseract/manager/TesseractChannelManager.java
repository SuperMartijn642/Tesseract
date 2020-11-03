package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.packets.PacketSendChannels;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class TesseractChannelManager {

    public static MinecraftServer minecraftServer;

    private static File directory;

    public static final TesseractChannelManager SERVER = new TesseractChannelManager();
    public static final TesseractChannelManager CLIENT = new TesseractChannelManager();

    private final HashMap<EnumChannelType,ChannelList> types = new HashMap<>();

    public Channel addChannel(EnumChannelType type, UUID creator, boolean isPrivate, String name){
        types.putIfAbsent(type, new ChannelList(type));
        Channel channel;
        synchronized(types.get(type)){
            channel = types.get(type).add(creator, isPrivate, name);
        }
        Tesseract.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSendChannels(type));
        return channel;
    }

    public Channel addChannel(Channel channel){
        types.putIfAbsent(channel.type, new ChannelList(channel.type));
        synchronized(types.get(channel.type)){
            types.get(channel.type).add(channel);
        }
        return channel;
    }

    public void removeChannel(EnumChannelType type, int id){
        types.putIfAbsent(type, new ChannelList(type));
        synchronized(types.get(type)){
            types.get(type).remove(id);
        }
        Tesseract.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSendChannels(type));
    }

    public void sortChannels(PlayerEntity player, EnumChannelType type){
        if(player == null || player.world == null || !player.world.isRemote)
            return;
        types.putIfAbsent(type, new ChannelList(type));
        synchronized(types.get(type)){
            types.get(type).sortForPlayer(player);
        }
    }

    public List<Channel> getChannels(EnumChannelType type){
        types.putIfAbsent(type, new ChannelList(type));
        return types.get(type).getChannels();
    }

    public List<Channel> getChannelsCreatedBy(EnumChannelType type, UUID creator){
        types.putIfAbsent(type, new ChannelList(type));
        return types.get(type).getChannelsCreatedBy(creator);
    }

    public Channel getChannelById(EnumChannelType type, int id){
        types.putIfAbsent(type, new ChannelList(type));
        return types.get(type).getById(id);
    }

    public void clear(EnumChannelType type){
        types.putIfAbsent(type, new ChannelList(type));
        types.get(type).clear();
    }

    @SubscribeEvent
    public static void onSave(WorldEvent.Save e){
        if(e.getWorld().isRemote() || !(e.getWorld() instanceof World) || ((World)e.getWorld()).getDimensionKey() != World.OVERWORLD)
            return;
        for(ChannelList list : SERVER.types.values()){
            File folder = new File(directory, list.type.name().toLowerCase(Locale.ENGLISH));
            if(!folder.exists())
                folder.mkdirs();
            list.write(folder);
        }
    }

    @SubscribeEvent
    public static void onLoad(WorldEvent.Load e){
        if(e.getWorld().isRemote() || !(e.getWorld() instanceof World) || ((World)e.getWorld()).getDimensionKey() != World.OVERWORLD)
            return;
        minecraftServer = ((ServerWorld)e.getWorld()).getServer();
        directory = new File(((ServerWorld)e.getWorld()).getServer().func_240776_a_(FolderName.DOT).toFile(), "tesseract");
        for(EnumChannelType type : EnumChannelType.values()){
            ChannelList list = new ChannelList(type);
            SERVER.types.put(type, list);
            File folder = new File(directory, type.name().toLowerCase(Locale.ENGLISH));
            list.read(folder);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        for(EnumChannelType type : EnumChannelType.values())
            Tesseract.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)e.getPlayer()), new PacketSendChannels(type));
    }
}
