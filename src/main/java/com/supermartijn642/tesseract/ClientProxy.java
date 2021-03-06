package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import com.supermartijn642.tesseract.screen.info.InfoScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
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
        ClientRegistry.bindTileEntityRenderer(Tesseract.tesseract_tile, TesseractTileRenderer::new);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        e.getModelLoader().getUnbakedModel(new ResourceLocation("tesseract","block/pipe"));
        e.getModelLoader().getUnbakedModel(new ResourceLocation("tesseract","block/pipe_extract"));
        e.getModelLoader().getUnbakedModel(new ResourceLocation("tesseract","block/pipe_extract_and_insert"));
        e.getModelLoader().getUnbakedModel(new ResourceLocation("tesseract","block/pipe_insert"));
        e.getModelManager().getModel(new ResourceLocation("tesseract","block/pipe_insert"));
    }

    public static void openScreen(BlockPos pos){
        ClientUtils.getMinecraft().displayGuiScreen(new TesseractScreen(pos));
    }

    public static void openInfoScreen(BlockPos pos){
        ClientUtils.getMinecraft().displayGuiScreen(new InfoScreen(pos));
    }
}
