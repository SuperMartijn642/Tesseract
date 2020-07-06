package com.supermartijn642.tesseract;

import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 7/4/2020 by SuperMartijn642
 */
public enum RedstoneState {

    DISABLED("gui.tesseract.redstone.disabled"), HIGH("gui.tesseract.redstone.high"), LOW("gui.tesseract.redstone.low");

    private final String translation;

    RedstoneState(String translation){
        this.translation = translation;
    }

    public TranslationTextComponent translate(){
        return new TranslationTextComponent(this.translation);
    }

}
