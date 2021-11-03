package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.*;

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
        CompoundNBT compound = new CompoundNBT();

        Iterator<Channel> iterator = this.channels.iterator();
        for(int index = 0; iterator.hasNext(); index++)
            compound.put(Integer.toString(index), iterator.next().writeClientChannel());

        buffer.writeNbt(compound);
    }

    @Override
    public void read(PacketBuffer buffer){
        CompoundNBT compound = buffer.readNbt();

        this.channels = new ArrayList<>();
        for(String key : compound.getAllKeys())
            this.channels.add(Channel.readClientChannel(compound.getCompound(key)));
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
