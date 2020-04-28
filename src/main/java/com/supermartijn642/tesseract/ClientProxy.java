package com.supermartijn642.tesseract;

import com.supermartijn642.tesseract.screen.TesseractScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntityRenderer(Tesseract.tesseract_tile,TesseractTileRenderer::new);
    }

    public static void openScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new TesseractScreen(pos));
    }

    public static PlayerEntity getPlayer(){
        return Minecraft.getInstance().player;
    }
}
