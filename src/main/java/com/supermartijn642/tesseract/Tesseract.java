package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.tesseract.generators.*;
import com.supermartijn642.tesseract.integration.TesseractTheOneProbePlugin;
import com.supermartijn642.tesseract.manager.TesseractSaveHandler;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import com.supermartijn642.tesseract.recipe_conditions.TesseractRecipeCondition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod("tesseract")
public class Tesseract {

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tesseract", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static TesseractBlock tesseract;
    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tesseract_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<TesseractBlockEntity> tesseract_tile;

    public static final Logger LOGGER = LogManager.getLogger("tesseract");

    public Tesseract(){
        TesseractTracker.registerListeners();
        TesseractSaveHandler.registerListeners();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TesseractTheOneProbePlugin::interModEnqueue);

        TesseractConfig.init();

        CHANNEL.registerMessage(PacketCompleteChannelsUpdate.class, PacketCompleteChannelsUpdate::new, true);
        CHANNEL.registerMessage(PacketScreenAddChannel.class, PacketScreenAddChannel::new, true);
        CHANNEL.registerMessage(PacketScreenRemoveChannel.class, PacketScreenRemoveChannel::new, true);
        CHANNEL.registerMessage(PacketScreenSetChannel.class, PacketScreenSetChannel::new, true);
        CHANNEL.registerMessage(PacketScreenCycleRedstoneState.class, PacketScreenCycleRedstoneState::new, true);
        CHANNEL.registerMessage(PacketScreenCycleTransferState.class, PacketScreenCycleTransferState::new, true);
        CHANNEL.registerMessage(PacketAddChannel.class, PacketAddChannel::new, true);
        CHANNEL.registerMessage(PacketRemoveChannel.class, PacketRemoveChannel::new, true);
        CHANNEL.registerMessage(PacketAddTesseractReferences.class, PacketAddTesseractReferences::new, true);
        CHANNEL.registerMessage(PacketRemoveTesseractReferences.class, PacketRemoveTesseractReferences::new, true);

        register();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> TesseractClient::register);
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("tesseract");
        handler.registerBlock("tesseract", TesseractBlock::new);
        handler.registerBlockEntityType("tesseract_tile", () -> BaseBlockEntityType.create(TesseractBlockEntity::new, tesseract));
        handler.registerItem("tesseract", () -> new BaseBlockItem(tesseract, ItemProperties.create().group(CreativeItemGroup.getDecoration())));
        handler.registerResourceConditionSerializer("thermal_recipe", TesseractRecipeCondition.SERIALIZER);
    }

    private static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get("tesseract");
        handler.addGenerator(TesseractBlockStateGenerator::new);
        handler.addGenerator(TesseractLanguageGenerator::new);
        handler.addGenerator(TesseractLootTableGenerator::new);
        handler.addGenerator(TesseractRecipeGenerator::new);
        handler.addGenerator(TesseractTagGenerator::new);
    }
}
