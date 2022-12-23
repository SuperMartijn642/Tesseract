package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Created 7/4/2020 by SuperMartijn642
 */
public enum RedstoneState {

    DISABLED("gui.tesseract.redstone.disabled"), HIGH("gui.tesseract.redstone.high"), LOW("gui.tesseract.redstone.low");

    private final String translation;

    RedstoneState(String translation){
        this.translation = translation;
    }

    public ITextComponent translate(){
        return TextComponents.translation(this.translation).color(TextFormatting.GOLD).get();
    }
}
