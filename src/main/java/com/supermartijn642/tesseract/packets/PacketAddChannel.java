package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class PacketAddChannel {

    private Channel channel;

    public PacketAddChannel(Channel channel){
        this.channel = channel;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeCompoundTag(this.channel.writeClientChannel());
    }

    public static PacketAddChannel decode(PacketBuffer buffer){
        return new PacketAddChannel(Channel.readClientChannel(buffer.readCompoundTag()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> {
            TesseractChannelManager.CLIENT.addChannel(this.channel);
            TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), this.channel.type);
        });
    }
}
