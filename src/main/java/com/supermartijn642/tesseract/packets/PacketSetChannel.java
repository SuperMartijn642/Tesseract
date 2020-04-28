package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketSetChannel {

    private EnumChannelType type;
    private int id;
    private BlockPos pos;

    public PacketSetChannel(EnumChannelType type, int id, BlockPos pos){
        this.type = type;
        this.id = id;
        this.pos = pos;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeString(this.type.name());
        buffer.writeInt(this.id);
        buffer.writeBlockPos(this.pos);
    }

    public static PacketSetChannel decode(PacketBuffer buffer){
        return new PacketSetChannel(EnumChannelType.valueOf(buffer.readString(32767)), buffer.readInt(), buffer.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        PlayerEntity player = ctx.get().getSender();
        if(player == null)
            return;
        World world = player.world;
        if(world == null)
            return;
        TileEntity tile = world.getTileEntity(this.pos);
        if(tile instanceof TesseractTile)
            ((TesseractTile)tile).setChannel(this.type, this.id);
    }
}
