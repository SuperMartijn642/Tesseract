package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.ClientProxy;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketAddChannel implements IMessage, IMessageHandler<PacketAddChannel,IMessage> {

    private Channel channel;

    public PacketAddChannel(Channel channel){
        this.channel = channel;
    }

    public PacketAddChannel(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.channel = Channel.readClientChannel(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, this.channel.writeClientChannel());
    }

    @Override
    public IMessage onMessage(PacketAddChannel message, MessageContext ctx){
        ClientProxy.queTask(() -> {
            TesseractChannelManager.CLIENT.addChannel(message.channel);
            TesseractChannelManager.CLIENT.sortChannels(ClientProxy.getPlayer(), message.channel.type);
        });
        return null;
    }
}
