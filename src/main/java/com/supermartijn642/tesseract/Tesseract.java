package com.supermartijn642.tesseract;

import com.supermartijn642.core.CommonUtils;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod(modid = "@mod_id@", name = "@mod_name@", version = "@mod_version@", dependencies = "required-after:forge@@forge_dependency@;required-after:supermartijn642corelib@@core_library_dependency@;required-after:supermartijn642configlib@@config_library_dependency@")
public class Tesseract {

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tesseract", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static TesseractBlock tesseract;
    @RegistryEntryAcceptor(namespace = "tesseract", identifier = "tiletesseract", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<TesseractBlockEntity> tesseract_tile;

    public static final Logger LOGGER = LogManager.getLogger("tesseract");

    public Tesseract(){
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
        if(CommonUtils.getEnvironmentSide().isClient())
            TesseractClient.register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("tesseract");
        handler.registerBlock("tesseract", TesseractBlock::new);
        handler.registerBlockEntityType("tiletesseract", () -> BaseBlockEntityType.create(TesseractBlockEntity::new, tesseract));
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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        if(CommonUtils.isModLoaded("theoneprobe"))
            TesseractTheOneProbePlugin.interModEnqueue();
    }
}
