package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketRemoveChannel implements IMessage, IMessageHandler<PacketRemoveChannel,IMessage> {

    private EnumChannelType type;
    private int id;

    public PacketRemoveChannel(EnumChannelType type, int id){
        this.type = type;
        this.id = id;
    }

    public PacketRemoveChannel(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.type = EnumChannelType.valueOf(new String(bytes, StandardCharsets.UTF_16));
        this.id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        byte[] bytes = this.type.name().getBytes(StandardCharsets.UTF_16);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeInt(this.id);
    }

    @Override
    public IMessage onMessage(PacketRemoveChannel message, MessageContext ctx){
        TesseractChannelManager.SERVER.removeChannel(message.type, message.id);
        Tesseract.channel.sendToAll(new PacketSendChannels(message.type));
        return null;
    }
}
