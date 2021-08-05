package com.supermartijn642.tesseract;

import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import com.supermartijn642.tesseract.recipe_conditions.TesseractRecipeCondition;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod("tesseract")
public class Tesseract {

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @ObjectHolder("tesseract:tesseract")
    public static BlockTesseract tesseract;
    @ObjectHolder("tesseract:tesseract_tile")
    public static TileEntityType<TesseractTile> tesseract_tile;

    public Tesseract(){
        MinecraftForge.EVENT_BUS.register(TesseractTracker.class);
        MinecraftForge.EVENT_BUS.register(TesseractChannelManager.class);

        TesseractConfig.init();

        CHANNEL.registerMessage(PacketCompleteChannelsUpdate.class, PacketCompleteChannelsUpdate::new, true);
        CHANNEL.registerMessage(PacketScreenAddChannel.class, PacketScreenAddChannel::new, true);
        CHANNEL.registerMessage(PacketScreenRemoveChannel.class, PacketScreenRemoveChannel::new, true);
        CHANNEL.registerMessage(PacketScreenSetChannel.class, PacketScreenSetChannel::new, true);
        CHANNEL.registerMessage(PacketScreenCycleRedstoneState.class, PacketScreenCycleRedstoneState::new, true);
        CHANNEL.registerMessage(PacketScreenCycleTransferState.class, PacketScreenCycleTransferState::new, true);
        CHANNEL.registerMessage(PacketAddChannel.class, PacketAddChannel::new, true);
        CHANNEL.registerMessage(PacketRemoveChannel.class, PacketRemoveChannel::new, true);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new BlockTesseract());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.of(TesseractTile::new, tesseract).build(null).setRegistryName("tesseract_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(tesseract, new Item.Properties().tab(ItemGroup.TAB_SEARCH)).setRegistryName("tesseract"));
        }

        @SubscribeEvent
        public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> e){
            CraftingHelper.register(TesseractRecipeCondition.SERIALIZER);
        }
    }
}
