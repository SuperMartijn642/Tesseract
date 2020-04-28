package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketAddChannel {

    private EnumChannelType type;
    private String name;
    private boolean isPrivate;

    public PacketAddChannel(EnumChannelType type, String name, boolean isPrivate){
        this.type = type;
        this.name = name;
        this.isPrivate = isPrivate;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeString(this.type.name());
        buffer.writeString(this.name);
        buffer.writeBoolean(this.isPrivate);
    }

    public static PacketAddChannel decode(PacketBuffer buffer){
        return new PacketAddChannel(EnumChannelType.valueOf(buffer.readString(32767)), buffer.readString(32767), buffer.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        TesseractChannelManager.SERVER.addChannel(this.type, ctx.get().getSender().getUniqueID(), this.isPrivate, this.name);
    }
}
