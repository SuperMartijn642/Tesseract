package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenAddChannel {

    private EnumChannelType type;
    private String name;
    private boolean isPrivate;

    public PacketScreenAddChannel(EnumChannelType type, String name, boolean isPrivate){
        this.type = type;
        this.name = name;
        this.isPrivate = isPrivate;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeInt(this.type.getIndex());
        buffer.writeString(this.name);
        buffer.writeBoolean(this.isPrivate);
    }

    public static PacketScreenAddChannel decode(PacketBuffer buffer){
        return new PacketScreenAddChannel(EnumChannelType.byIndex(buffer.readInt()), buffer.readString(32767), buffer.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        if(this.type != null && !this.name.trim().isEmpty())
            ctx.get().enqueueWork(() ->
                TesseractChannelManager.SERVER.addChannel(this.type, ctx.get().getSender().getUniqueID(), this.isPrivate, this.name)
            );
    }
}
