package com.supermartijn642.tesseract;

import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod("tesseract")
public class Tesseract {

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("tesseract", "main"), () -> "1", "1"::equals, "1"::equals);

    @ObjectHolder("tesseract:tesseract")
    public static BlockTesseract tesseract;
    @ObjectHolder("tesseract:tesseract_tile")
    public static TileEntityType<TesseractTile> tesseract_tile;

    public Tesseract(){
        MinecraftForge.EVENT_BUS.register(TesseractTracker.class);
        MinecraftForge.EVENT_BUS.register(TesseractChannelManager.class);

        CHANNEL.registerMessage(0, PacketCompleteChannelsUpdate.class, PacketCompleteChannelsUpdate::encode, PacketCompleteChannelsUpdate::decode, PacketCompleteChannelsUpdate::handle);
        CHANNEL.registerMessage(1, PacketScreenAddChannel.class, PacketScreenAddChannel::encode, PacketScreenAddChannel::decode, PacketScreenAddChannel::handle);
        CHANNEL.registerMessage(2, PacketScreenRemoveChannel.class, PacketScreenRemoveChannel::encode, PacketScreenRemoveChannel::decode, PacketScreenRemoveChannel::handle);
        CHANNEL.registerMessage(3, PacketScreenSetChannel.class, PacketScreenSetChannel::encode, PacketScreenSetChannel::decode, PacketScreenSetChannel::handle);
        CHANNEL.registerMessage(4, PacketScreenCycleRedstoneState.class, PacketScreenCycleRedstoneState::encode, PacketScreenCycleRedstoneState::decode, PacketScreenCycleRedstoneState::handle);
        CHANNEL.registerMessage(5, PacketScreenCycleTransferState.class, PacketScreenCycleTransferState::encode, PacketScreenCycleTransferState::decode, PacketScreenCycleTransferState::handle);
        CHANNEL.registerMessage(6, PacketAddChannel.class, PacketAddChannel::encode, PacketAddChannel::decode, PacketAddChannel::handle);
        CHANNEL.registerMessage(7, PacketRemoveChannel.class, PacketRemoveChannel::encode, PacketRemoveChannel::decode, PacketRemoveChannel::handle);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new BlockTesseract());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(TesseractTile::new, tesseract).build(null).setRegistryName("tesseract_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(tesseract, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName("tesseract"));
        }
    }
}
