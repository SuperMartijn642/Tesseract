package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
        this.type = EnumChannelType.byIndex(buf.readInt());
        this.id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.type.getIndex());
        buf.writeInt(this.id);
    }

    @Override
    public IMessage onMessage(PacketRemoveChannel message, MessageContext ctx){
        if(message.type != null && message.id >= 0)
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() ->
                TesseractChannelManager.SERVER.removeChannel(message.type, message.id)
            );
        return null;
    }
}
