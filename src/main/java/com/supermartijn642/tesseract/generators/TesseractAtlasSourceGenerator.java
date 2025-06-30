package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.screen.*;

import java.util.Arrays;

/**
 * Created 20/01/2023 by SuperMartijn642
 */
public class TesseractAtlasSourceGenerator extends AtlasSourceGenerator {

    public TesseractAtlasSourceGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.blockAtlas()
            .texturesFromModel("block/on")
            .texturesFromModel("item/tesseract");

        this.guiAtlas()
            .texture(TesseractButton.BUTTON_BACKGROUND)
            .texture(TesseractButton.RED_BUTTON_BACKGROUND)
            .texture(TesseractScreen.BACKGROUND)
            .texture(TesseractScreen.CHANNEL_BACKGROUND)
            .texture(TesseractScreen.TAB_ON)
            .texture(TesseractScreen.TAB_OFF)
            .texture(TesseractScreen.ITEM_ICON)
            .texture(TesseractScreen.ENERGY_ICON)
            .texture(TesseractScreen.FLUID_ICON)
            .texture(TesseractScreen.LOCK_ON)
            .texture(TesseractScreen.LOCK_OFF)
            .texture(TesseractScreen.REDSTONE_TAB)
            .texture(TesseractScreen.SIDE_TAB)
            .texture(TesseractScreen.CHECKMARK)
            .texture(CycleButton.TEXTURE)
            .texture(TesseractAddChannelScreen.BACKGROUND)
            .texture(TesseractRemoveChannelScreen.BACKGROUND);
        Arrays.stream(LockButton.Icon.values())
            .map(LockButton.Icon::location)
            .forEach(this.guiAtlas()::texture);
    }
}
