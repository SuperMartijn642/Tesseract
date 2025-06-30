package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketCompleteChannelsUpdate implements BasePacket {

    private List<Channel> channels;

    public PacketCompleteChannelsUpdate(Player target){
        Set<Channel>[] channels = new Set[EnumChannelType.values().length];
        int size = 0;
        for(EnumChannelType type : EnumChannelType.values()){
            channels[type.ordinal()] = new HashSet<>(TesseractChannelManager.SERVER.getPublicChannels(type));
            channels[type.ordinal()].addAll(TesseractChannelManager.SERVER.getChannelsCreatedBy(type, target.getGameProfile().getId()));
            size += channels[type.ordinal()].size();
        }
        this.channels = new ArrayList<>(size);
        for(Set<Channel> set : channels)
            this.channels.addAll(set);
    }

    public PacketCompleteChannelsUpdate(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.channels.size());
        for(Channel channel : this.channels)
            channel.writeClientChannel(buffer);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        int channels = buffer.readInt();
        if(channels > 500)
            throw new IllegalStateException("Too many channels!");
        this.channels = new ArrayList<>(channels);
        for(int i = 0; i < channels; i++)
            this.channels.add(Channel.readClientChannel(buffer));
    }

    @Override
    public void handle(PacketContext buffer){
        TesseractChannelManager.CLIENT.clear();
        this.channels.forEach(TesseractChannelManager.CLIENT::addChannel);
        for(EnumChannelType type : EnumChannelType.values())
            TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), type);
    }
}
