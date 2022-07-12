package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.packets.PacketAddChannel;
import com.supermartijn642.tesseract.packets.PacketCompleteChannelsUpdate;
import com.supermartijn642.tesseract.packets.PacketRemoveChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

    public static TesseractChannelManager getInstance(Level world){
        return world.isClientSide ? CLIENT : SERVER;
    }

    private final HashMap<EnumChannelType,ChannelList> types = new HashMap<>();

    public Channel addChannel(EnumChannelType type, UUID creator, boolean isPrivate, String name){
        types.putIfAbsent(type, new ChannelList(type));
        Channel channel = types.get(type).add(creator, isPrivate, name);
        this.sendAddChannelPacket(channel);
        return channel;
    }

    public Channel addChannel(Channel channel){
        types.putIfAbsent(channel.type, new ChannelList(channel.type));
        types.get(channel.type).add(channel);
        this.sendAddChannelPacket(channel);
        return channel;
    }

    public void removeChannel(EnumChannelType type, int id){
        types.putIfAbsent(type, new ChannelList(type));
        types.get(type).remove(id);
        this.sendRemoveChannelPacket(type, id);
    }

    public void sortChannels(Player player, EnumChannelType type){
        if(player == null || player.level == null || !player.level.isClientSide)
            return;
        types.putIfAbsent(type, new ChannelList(type));
        types.get(type).sortForPlayer(player);
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

    public void clear(){
        for(EnumChannelType type : EnumChannelType.values()){
            types.putIfAbsent(type, new ChannelList(type));
            types.get(type).clear();
        }
    }

    public void sendCompleteUpdatePacket(Player player){
        if(this == SERVER)
            Tesseract.CHANNEL.sendToPlayer(player, new PacketCompleteChannelsUpdate(true));
    }

    public void sendAddChannelPacket(Channel channel){
        if(this == SERVER)
            Tesseract.CHANNEL.sendToAllPlayers(new PacketAddChannel(channel));
    }

    public void sendRemoveChannelPacket(EnumChannelType type, int id){
        if(this == SERVER)
            Tesseract.CHANNEL.sendToAllPlayers(new PacketRemoveChannel(type, id));
    }

    @SubscribeEvent
    public static void onSave(LevelEvent.Save e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        for(ChannelList list : SERVER.types.values()){
            File folder = new File(directory, list.type.name().toLowerCase(Locale.ENGLISH));
            if(!folder.exists())
                folder.mkdirs();
            list.write(folder);
        }
    }

    @SubscribeEvent
    public static void onLoad(LevelEvent.Load e){
        if(e.getLevel().isClientSide() || !(e.getLevel() instanceof Level) || ((Level)e.getLevel()).dimension() != Level.OVERWORLD)
            return;

        minecraftServer = ((ServerLevel)e.getLevel()).getServer();
        directory = new File(((ServerLevel)e.getLevel()).getServer().getWorldPath(LevelResource.ROOT).toFile(), "tesseract");
        for(EnumChannelType type : EnumChannelType.values()){
            ChannelList list = new ChannelList(type);
            SERVER.types.put(type, list);
            File folder = new File(directory, type.name().toLowerCase(Locale.ENGLISH));
            list.read(folder);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(!e.getEntity().getCommandSenderWorld().isClientSide)
            SERVER.sendCompleteUpdatePacket(e.getEntity());
    }
}
