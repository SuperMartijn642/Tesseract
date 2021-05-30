package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.ClientProxy;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.*;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketCompleteChannelsUpdate implements IMessage, IMessageHandler<PacketCompleteChannelsUpdate,IMessage> {

    private List<Channel> channels;

    public PacketCompleteChannelsUpdate(boolean server){
        if(server){
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
        }else
            this.channels = new ArrayList<>();
    }

    public PacketCompleteChannelsUpdate(){
        this.channels = new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf){
        NBTTagCompound compound = ByteBufUtils.readTag(buf);

        for(String key : compound.getKeySet())
            this.channels.add(Channel.readClientChannel(compound.getCompoundTag(key)));
    }

    @Override
    public void toBytes(ByteBuf buf){
        NBTTagCompound compound = new NBTTagCompound();

        Iterator<Channel> iterator = this.channels.iterator();
        for(int index = 0; iterator.hasNext(); index++)
            compound.setTag(Integer.toString(index), iterator.next().writeClientChannel());

        ByteBufUtils.writeTag(buf, compound);
    }

    @Override
    public IMessage onMessage(PacketCompleteChannelsUpdate message, MessageContext ctx){
        ClientProxy.queTask(() -> {
            TesseractChannelManager.CLIENT.clear();
            Set<EnumChannelType> types = new HashSet<>(3);
            message.channels.forEach(channel -> {
                TesseractChannelManager.CLIENT.addChannel(channel);
                types.add(channel.type);
            });
            types.forEach(type -> TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), type));
        });
        return null;
    }
}
