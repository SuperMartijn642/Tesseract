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
public class PacketAddChannel implements IMessage, IMessageHandler<PacketAddChannel,IMessage> {

    private EnumChannelType type;
    private String name;
    private boolean isPrivate;

    public PacketAddChannel(EnumChannelType type, String name, boolean isPrivate){
        this.type = type;
        this.name = name;
        this.isPrivate = isPrivate;
    }

    public PacketAddChannel(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.type = EnumChannelType.valueOf(new String(bytes, StandardCharsets.UTF_16));
        bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.name = new String(bytes, StandardCharsets.UTF_16);
        this.isPrivate = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf){
        byte[] bytes = this.type.name().getBytes(StandardCharsets.UTF_16);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        bytes = this.name.getBytes(StandardCharsets.UTF_16);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeBoolean(this.isPrivate);
    }

    @Override
    public IMessage onMessage(PacketAddChannel message, MessageContext ctx){
        ctx.getServerHandler().player.getServerWorld().addScheduledTask(() ->
            TesseractChannelManager.SERVER.addChannel(message.type,ctx.getServerHandler().player.getUniqueID(),message.isPrivate,message.name)
        );
        return null;
    }
}
