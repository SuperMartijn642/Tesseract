package com.supermartijn642.tesseract;

import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import com.supermartijn642.tesseract.recipe_conditions.TesseractRecipeCondition;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Objects;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod("tesseract")
public class Tesseract {

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @ObjectHolder(value = "tesseract:tesseract", registryName = "minecraft:block")
    public static BlockTesseract tesseract;
    @ObjectHolder(value = "tesseract:tesseract_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<TesseractTile> tesseract_tile;

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
        public static void onRegisterEvent(RegisterEvent e){
            if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS))
                onBlockRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
                onTileRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
                onItemRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
                onRecipeRegistry(Objects.requireNonNull(e.getForgeRegistry()));
        }

        public static void onBlockRegistry(IForgeRegistry<Block> registry){
            registry.register("tesseract", new BlockTesseract());
        }

        public static void onTileRegistry(IForgeRegistry<BlockEntityType<?>> registry){
            registry.register("tesseract_tile", BlockEntityType.Builder.of(TesseractTile::new, tesseract).build(null));
        }

        public static void onItemRegistry(IForgeRegistry<Item> registry){
            registry.register("tesseract", new BlockItem(tesseract, new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)));
        }

        public static void onRecipeRegistry(IForgeRegistry<RecipeSerializer<?>> registry){
            CraftingHelper.register(TesseractRecipeCondition.SERIALIZER);
        }
    }
}
