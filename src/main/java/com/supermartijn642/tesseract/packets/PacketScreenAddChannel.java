package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SharedConstants;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenAddChannel implements BasePacket {

    private EnumChannelType type;
    private String name;
    private boolean isPrivate;

    public PacketScreenAddChannel(EnumChannelType type, String name, boolean isPrivate){
        this.type = type;
        this.name = name;
        this.isPrivate = isPrivate;
    }

    public PacketScreenAddChannel(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeVarInt(this.type.getIndex());
        buffer.writeUtf(this.name, Channel.CHANNEL_MAX_CHARACTERS + 1);
        buffer.writeBoolean(this.isPrivate);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.type = EnumChannelType.byIndex(buffer.readVarInt());
        this.name = buffer.readUtf(Channel.CHANNEL_MAX_CHARACTERS + 1).trim();
        this.isPrivate = buffer.readBoolean();
    }

    @Override
    public boolean verify(PacketContext context){
        return this.name.equals(SharedConstants.filterText(this.name))
            && this.name.length() >= Channel.CHANNEL_MIN_CHARACTERS
            && this.name.length() <= Channel.CHANNEL_MAX_CHARACTERS;
    }

    @Override
    public void handle(PacketContext context){
        TesseractChannelManager.SERVER.addChannel(this.type, context.getSendingPlayer().getUUID(), this.isPrivate, this.name);
    }
}
