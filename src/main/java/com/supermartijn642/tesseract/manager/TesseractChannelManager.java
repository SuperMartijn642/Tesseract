package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.packets.PacketSendChannels;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
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

    public static TesseractChannelManager getInstance(World world){
        return world.isRemote ? CLIENT : SERVER;
    }

    private final HashMap<EnumChannelType,ChannelList> types = new HashMap<>();

    public Channel addChannel(EnumChannelType type, UUID creator, boolean isPrivate, String name){
        types.putIfAbsent(type, new ChannelList(type));
        Channel channel;
        synchronized(types.get(type)){
            channel = types.get(type).add(creator, isPrivate, name);
        }
        this.update(type);
        return channel;
    }

    public Channel addChannel(Channel channel){
        types.putIfAbsent(channel.type, new ChannelList(channel.type));
        synchronized(types.get(channel.type)){
            types.get(channel.type).add(channel);
        }
        this.update(channel.type);
        return channel;
    }

    public void removeChannel(EnumChannelType type, int id){
        types.putIfAbsent(type, new ChannelList(type));
        synchronized(types.get(type)){
            types.get(type).remove(id);
        }
        this.update(type);
    }

    public void sortChannels(EntityPlayer player, EnumChannelType type){
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

    public void update(EnumChannelType type){
        if(this == SERVER)
            Tesseract.channel.sendToAll(new PacketSendChannels(type));
    }

    @SubscribeEvent
    public static void onSave(WorldEvent.Save e){
        World world = e.getWorld();

        if(world != null && !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD){
            File dir = new File(DimensionManager.getCurrentSaveRootDirectory(), "tesseract");

            for(ChannelList list : SERVER.types.values()){
                File folder = new File(dir, list.type.name().toLowerCase(Locale.ENGLISH));
                if(!folder.exists())
                    folder.mkdirs();
                list.write(folder);
            }
        }
    }

    @SubscribeEvent
    public static void onLoad(WorldEvent.Load e){
        World world = e.getWorld();
        if(world != null && !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD){
            File dir = new File(DimensionManager.getCurrentSaveRootDirectory(), "tesseract");
            for(EnumChannelType type : EnumChannelType.values()){
                ChannelList list = new ChannelList(type);
                SERVER.types.put(type, list);
                File folder = new File(dir, type.name().toLowerCase(Locale.ENGLISH));
                list.read(folder);
            }
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        for(EnumChannelType type : EnumChannelType.values())
            Tesseract.channel.sendTo(new PacketSendChannels(type), (EntityPlayerMP)e.player);
    }
}
