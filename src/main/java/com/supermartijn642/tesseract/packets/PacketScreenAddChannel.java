package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;

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
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.type.getIndex());
        buffer.writeUtf(this.name);
        buffer.writeBoolean(this.isPrivate);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.type = EnumChannelType.byIndex(buffer.readInt());
        this.name = buffer.readUtf(32767);
        this.isPrivate = buffer.readBoolean();
    }

    @Override
    public boolean verify(PacketContext context){
        return this.type != null && !this.name.trim().isEmpty() && this.name.trim().equals(SharedConstants.filterText(this.name.trim()));
    }

    @Override
    public void handle(PacketContext context){
        TesseractChannelManager.SERVER.addChannel(this.type, context.getSendingPlayer().getUUID(), this.isPrivate, this.name);
    }
}
