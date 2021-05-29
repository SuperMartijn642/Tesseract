package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketCompleteChannelsUpdate {

    private List<Channel> channels;

    public PacketCompleteChannelsUpdate(){
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

    private PacketCompleteChannelsUpdate(List<Channel> channels){
        this.channels = channels;
    }

    public void encode(PacketBuffer buffer){
        CompoundNBT compound = new CompoundNBT();

        Iterator<Channel> iterator = this.channels.iterator();
        for(int index = 0; iterator.hasNext(); index++)
            compound.put(Integer.toString(index), iterator.next().writeClientChannel());

        buffer.writeCompoundTag(compound);
    }

    public static PacketCompleteChannelsUpdate decode(PacketBuffer buffer){
        CompoundNBT compound = buffer.readCompoundTag();

        ArrayList<Channel> channels = new ArrayList<>();
        for(String key : compound.keySet())
            channels.add(Channel.readClientChannel(compound.getCompound(key)));

        return new PacketCompleteChannelsUpdate(channels);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> {
            TesseractChannelManager.CLIENT.clear();
            Set<EnumChannelType> types = new HashSet<>(3);
            this.channels.forEach(channel -> {
                TesseractChannelManager.CLIENT.addChannel(channel);
                types.add(channel.type);
            });
            types.forEach(type -> TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), type));
        });
    }
}
