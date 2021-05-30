package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import com.supermartijn642.tesseract.screen.info.InfoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Tesseract.tesseract), 0, new ModelResourceLocation(Tesseract.tesseract.getRegistryName(), "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TesseractTile.class, new TesseractTileRenderer());
    }

    public static void openScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new TesseractScreen(pos));
    }

    public static void queTask(Runnable task){
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    public static void openInfoScreen(BlockPos pos){
        ClientUtils.getMinecraft().displayGuiScreen(new InfoScreen(pos));
    }
}
