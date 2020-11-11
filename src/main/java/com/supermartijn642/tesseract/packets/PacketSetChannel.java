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
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
        buffer.writeBlockPos(this.pos);
    }

    public static PacketSetChannel decode(PacketBuffer buffer){
        return new PacketSetChannel(EnumChannelType.byIndex(buffer.readInt()), buffer.readInt(), buffer.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        if(this.type == null || this.id < 0)
            return;
        PlayerEntity player = ctx.get().getSender();
        if(player == null)
            return;
        World world = player.world;
        if(world == null)
            return;
        ctx.get().enqueueWork(() -> {
            TileEntity tile = world.getTileEntity(this.pos);
            if(tile instanceof TesseractTile)
                ((TesseractTile)tile).setChannel(this.type, this.id);
        });
    }
}
