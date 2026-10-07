package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketCompleteChannelsUpdate implements BasePacket {

    private List<Channel> channels;

    public PacketCompleteChannelsUpdate(boolean server){
        if(!server)
            throw new IllegalStateException();

        List<Channel>[] lists = new List[EnumChannelType.values().length];
        int index = 0;
        int size = 0;
        for(EnumChannelType type : EnumChannelType.values()){
            lists[index] = TesseractChannelManager.SERVER.getChannels(type);
            size += lists[index].size();
            index++;
        }
        this.channels = new ArrayList<>(size);
        for(List<Channel> list : lists)
            this.channels.addAll(list);
    }

    public PacketCompleteChannelsUpdate(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeInt(this.channels.size());
        for(Channel channel : this.channels)
            channel.writeClientChannel(buffer);
    }

    @Override
    public void read(PacketBuffer buffer){
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
        Set<EnumChannelType> types = new HashSet<>(3);
        this.channels.forEach(channel -> {
            TesseractChannelManager.CLIENT.addChannel(channel);
            types.add(channel.type);
        });
        types.forEach(type -> TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), type));
    }
}
