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
import com.supermartijn642.tesseract.manager.TesseractSaveHandler;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class Tesseract implements ModInitializer {

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tesseract", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static TesseractBlock tesseract;
    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tesseract_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<TesseractBlockEntity> tesseract_tile;

    public static final Logger LOGGER = LoggerFactory.getLogger("tesseract");

    @Override
    public void onInitialize(){
        TesseractTracker.registerListeners();
        TesseractSaveHandler.registerListeners();

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
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("tesseract");
        handler.registerBlock("tesseract", TesseractBlock::new);
        handler.registerBlockEntityType("tesseract_tile", () -> BaseBlockEntityType.create(TesseractBlockEntity::new, tesseract));
        handler.registerItem("tesseract", () -> new BaseBlockItem(tesseract, ItemProperties.create().group(CreativeItemGroup.getDecoration())));
        // Api providers
        handler.registerBlockEntityTypeCallback(helper -> TesseractBlockApiProviders.register());
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
