package com.supermartijn642.tesseract.packets;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketSendChannels implements IMessage, IMessageHandler<PacketSendChannels,IMessage> {

    private EnumChannelType type;
    private List<Channel> channels = new ArrayList<>();

    public PacketSendChannels(EnumChannelType type){
        this.type = type;
        this.channels.addAll(TesseractChannelManager.SERVER.getChannels(type));
    }

    public PacketSendChannels(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        NBTTagCompound compound;
        try{
            compound = ByteBufUtils.readTag(buf);
        }catch(Exception e){e.printStackTrace();return;}
        if(compound == null || !compound.hasKey("type") || !compound.hasKey("channels"))
            return;
        this.type = EnumChannelType.valueOf(compound.getString("type"));
        NBTTagCompound channels = compound.getCompoundTag("channels");
        for(String key : channels.getKeySet()){
            int id;
            try{
                id = Integer.parseInt(key);
            }catch(Exception e){e.printStackTrace();continue;}
            this.channels.add(new Channel(id,this.type,channels.getCompoundTag(key)));
        }
    }

    @Override
    public void toBytes(ByteBuf buf){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("type", this.type.name());
        NBTTagCompound channels = new NBTTagCompound();
        this.channels.forEach(channel -> channels.setTag(Integer.toString(channel.id), channel.write()));
        compound.setTag("channels", channels);
        ByteBufUtils.writeTag(buf,compound);
    }

    @Override
    public IMessage onMessage(PacketSendChannels message, MessageContext ctx){
        TesseractChannelManager.CLIENT.clear(message.type);
        message.channels.forEach(TesseractChannelManager.CLIENT::addChannel);
        TesseractChannelManager.CLIENT.sortChannels(ClientProxy.getPlayer(), message.type);
        return null;
    }
}
