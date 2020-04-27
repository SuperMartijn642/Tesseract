package com.supermartijn642.tesseract;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        e.getRegistry().register(new BlockTesseract());
        GameRegistry.registerTileEntity(TesseractTile.class, new ResourceLocation(Tesseract.MODID, "tiletesseract"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e){
        e.getRegistry().register(new ItemBlock(Tesseract.tesseract).setRegistryName(Tesseract.tesseract.getRegistryName()));
    }

}
