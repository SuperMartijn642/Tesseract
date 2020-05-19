package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.ClientProxy;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketSendChannels {

    private EnumChannelType type;
    private List<Channel> channels = new ArrayList<>();

    public PacketSendChannels(EnumChannelType type){
        this.type = type;
        this.channels.addAll(TesseractChannelManager.SERVER.getChannels(type));
    }

    public PacketSendChannels(EnumChannelType type, List<Channel> channels){
        this.type = type;
        this.channels = channels;
    }

    public void encode(PacketBuffer buffer){
        CompoundNBT compound = new CompoundNBT();
        compound.putString("type", this.type.name());
        CompoundNBT channels = new CompoundNBT();
        this.channels.forEach(channel -> channels.put(Integer.toString(channel.id), channel.write()));
        compound.put("channels", channels);
        buffer.writeCompoundTag(compound);
    }

    public static PacketSendChannels decode(PacketBuffer buffer){
        CompoundNBT compound = buffer.readCompoundTag();

        EnumChannelType type = EnumChannelType.valueOf(compound.getString("type"));

        CompoundNBT channelTag = compound.getCompound("channels");
        ArrayList<Channel> channels = new ArrayList<>();

        for(String key : channelTag.keySet()){
            int id;
            try{
                id = Integer.parseInt(key);
            }catch(Exception e){
                e.printStackTrace();
                continue;
            }
            channels.add(new Channel(id, type, channelTag.getCompound(key)));
        }

        return new PacketSendChannels(type, channels);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> {
            TesseractChannelManager.CLIENT.clear(this.type);
            this.channels.forEach(TesseractChannelManager.CLIENT::addChannel);
            TesseractChannelManager.CLIENT.sortChannels(ClientProxy.getPlayer(), this.type);
        });
    }
}
