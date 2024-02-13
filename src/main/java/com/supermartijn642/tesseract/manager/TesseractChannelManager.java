package com.supermartijn642.tesseract.manager;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.packets.PacketAddChannel;
import com.supermartijn642.tesseract.packets.PacketCompleteChannelsUpdate;
import com.supermartijn642.tesseract.packets.PacketRemoveChannel;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class TesseractChannelManager {

    public static final TesseractChannelManager SERVER = new TesseractChannelManager();
    public static final TesseractChannelManager CLIENT = new TesseractChannelManager();

    public static TesseractChannelManager getInstance(boolean isClientSide){
        return isClientSide ? CLIENT : SERVER;
    }

    public static TesseractChannelManager getInstance(Level level){
        return getInstance(level.isClientSide);
    }

    private final HashMap<EnumChannelType,ChannelList> types = new HashMap<>();

    public Channel addChannel(EnumChannelType type, UUID creator, boolean isPrivate, String name){
        this.types.putIfAbsent(type, new ChannelList(type));
        Channel channel = this.types.get(type).add(creator, isPrivate, name);
        this.sendAddChannelPacket(channel);
        return channel;
    }

    public Channel addChannel(Channel channel){
        this.types.putIfAbsent(channel.type, new ChannelList(channel.type));
        this.types.get(channel.type).add(channel);
        this.sendAddChannelPacket(channel);
        return channel;
    }

    public void removeChannel(EnumChannelType type, int id, Player remover){
        Channel channel = this.getChannelById(type, id);
        if(channel != null && (this == CLIENT || channel.creator.equals(remover.getUUID()) || remover.hasPermissions(2))){
            this.types.putIfAbsent(type, new ChannelList(type));
            this.types.get(type).remove(id);
            this.sendRemoveChannelPacket(channel);
        }
    }

    public void sortChannels(Player player, EnumChannelType type){
        if(this != CLIENT)
            return;
        this.types.putIfAbsent(type, new ChannelList(type));
        this.types.get(type).sortForPlayer(player);
    }

    public List<Channel> getChannels(EnumChannelType type){
        this.types.putIfAbsent(type, new ChannelList(type));
        return this.types.get(type).getChannels();
    }

    public List<Channel> getPublicChannels(EnumChannelType type){
        this.types.putIfAbsent(type, new ChannelList(type));
        return this.types.get(type).getPublicChannels();
    }

    public List<Channel> getChannelsCreatedBy(EnumChannelType type, UUID creator){
        this.types.putIfAbsent(type, new ChannelList(type));
        return this.types.get(type).getChannelsCreatedBy(creator);
    }

    public Channel getChannelById(EnumChannelType type, int id){
        this.types.putIfAbsent(type, new ChannelList(type));
        return this.types.get(type).getById(id);
    }

    public void clear(){
        for(EnumChannelType type : EnumChannelType.values()){
            this.types.putIfAbsent(type, new ChannelList(type));
            this.types.get(type).clear();
        }
    }

    public void sendCompleteUpdatePacket(Player player){
        if(this == SERVER)
            Tesseract.CHANNEL.sendToPlayer(player, new PacketCompleteChannelsUpdate(player));
    }

    public void sendAddChannelPacket(Channel channel){
        if(this != SERVER)
            return;
        if(channel.isPrivate){
            PlayerLookup.all(CommonUtils.getServer()).stream()
                .filter(player -> player.getGameProfile().getId().equals(channel.creator))
                .findAny()
                .ifPresent(player -> Tesseract.CHANNEL.sendToPlayer(player, new PacketAddChannel(channel)));
        }else
            Tesseract.CHANNEL.sendToAllPlayers(new PacketAddChannel(channel));
    }

    public void sendRemoveChannelPacket(Channel channel){
        if(this != SERVER)
            return;
        if(channel.isPrivate){
            PlayerLookup.all(CommonUtils.getServer()).stream()
                .filter(player -> player.getGameProfile().getId().equals(channel.creator))
                .findAny()
                .ifPresent(player -> Tesseract.CHANNEL.sendToPlayer(player, new PacketRemoveChannel(channel)));
        }else
            Tesseract.CHANNEL.sendToAllPlayers(new PacketRemoveChannel(channel));
    }

    public static void saveChannels(Path saveDirectory){
        for(ChannelList list : SERVER.types.values()){
            Path folder = saveDirectory.resolve("tesseract").resolve(list.type.name().toLowerCase(Locale.ENGLISH));
            try{
                Files.createDirectories(folder);
            }catch(IOException e){
                Tesseract.LOGGER.error("Failed to create channel save directory for '" + folder + "'!", e);
                continue;
            }
            list.write(folder);
        }
    }

    public static void loadChannels(Path saveDirectory){
        for(EnumChannelType type : EnumChannelType.values()){
            ChannelList list = new ChannelList(type);
            SERVER.types.put(type, list);
            Path folder = saveDirectory.resolve("tesseract").resolve(type.name().toLowerCase(Locale.ENGLISH));
            list.read(folder);
        }
    }

    public static void sendChannels(Player player){
        SERVER.sendCompleteUpdatePacket(player);
    }
}
