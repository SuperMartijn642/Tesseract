package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.File;
import java.util.*;

/**
 * Created 4/22/2020 by SuperMartijn642
 */
public class ChannelList {

    private int channelId = 0;

    public final EnumChannelType type;
    private final ArrayList<Channel> channels = new ArrayList<>();
    private final ArrayList<Channel> publicChannels = new ArrayList<>();
    private final HashMap<Integer,Channel> channelsById = new HashMap<>();
    private final HashMap<UUID,List<Channel>> channelsByCreator = new HashMap<>();
    private final List<Integer> removedIds = new ArrayList<>();

    public ChannelList(EnumChannelType type){
        this.type = type;
    }

    public Channel add(UUID creator, boolean isPrivate, String name){
        Channel channel = new Channel(this.channelId++, this.type, creator, isPrivate, name);
        this.add(channel);
        return channel;
    }

    void add(Channel channel){
        this.channels.add(channel);
        if(!channel.isPrivate)
            this.publicChannels.add(channel);
        this.channelsById.put(channel.id, channel);
        this.channelsByCreator.putIfAbsent(channel.creator, new ArrayList<>());
        this.channelsByCreator.get(channel.creator).add(channel);
    }

    public void remove(int id){
        this.remove(this.getById(id));
    }

    public void remove(Channel channel){
        if(channel == null)
            return;

        this.channels.remove(channel);
        this.publicChannels.remove(channel);
        this.channelsById.remove(channel.id);
        this.channelsByCreator.get(channel.creator).remove(channel);
        channel.delete();
        this.removedIds.add(channel.id);
    }

    public List<Channel> sortForPlayer(PlayerEntity player){
        final UUID uuid = player.getUUID();

        this.channels.removeIf(channel -> channel.isPrivate && !channel.creator.equals(uuid));

        this.channels.sort((a, b) -> {
            boolean aUuid = a.creator.equals(uuid);
            boolean bUuid = b.creator.equals(uuid);
            if(aUuid ^ bUuid)
                return aUuid ? -1 : 1;
            return a.name.compareTo(b.name);
        });
        return this.channels;
    }

    public Channel getById(int id){
        return this.channelsById.get(id);
    }

    public List<Channel> getChannels(){
        return Collections.unmodifiableList(this.channels);
    }

    public List<Channel> getChannelsCreatedBy(UUID creator){
        return this.channelsByCreator.getOrDefault(creator, Collections.emptyList());
    }

    public void write(File folder){
        for(Channel channel : this.channels){
            File file = new File(folder, "channel" + channel.id + ".nbt");
            try{
                CompressedStreamTools.write(channel.write(), file);
            }catch(Exception exception){exception.printStackTrace();}
        }
        for(int id : this.removedIds){
            File file = new File(folder, "channel" + id + ".nbt");
            try{
                file.delete();
            }catch(Exception exception){exception.printStackTrace();}
        }
        this.removedIds.clear();
    }

    public void read(File folder){
        this.channelId = 0;

        if(!folder.exists())
            return;

        File[] files = folder.listFiles();
        if(files == null || files.length == 0)
            return;

        for(File file : files){
            String name = file.getName();
            if(name.startsWith("channel") && name.endsWith(".nbt")){
                try{
                    int id = Integer.parseInt(name.substring("channel".length(), name.length() - ".nbt".length()));
                    CompoundNBT compound = CompressedStreamTools.read(file);
                    if(compound != null){
                        Channel channel = new Channel(id, this.type, compound);
                        if(channel.id >= this.channelId)
                            this.channelId = channel.id + 1;
                        this.add(channel);
                    }
                }catch(Exception exception){exception.printStackTrace();}
            }
        }
    }

    public void clear(){
        this.channels.clear();
        this.publicChannels.clear();
        this.channelsById.clear();
        this.channelsByCreator.clear();
    }
}
