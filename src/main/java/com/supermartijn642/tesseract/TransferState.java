package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Created 7/4/2020 by SuperMartijn642
 */
public enum TransferState {

    SEND("gui.tesseract.transfer.send"), RECEIVE("gui.tesseract.transfer.receive"), BOTH("gui.tesseract.transfer.both");

    private final String translation;

    TransferState(String translation){
        this.translation = translation;
    }

    public ITextComponent translate(){
        return TextComponents.translation(this.translation).color(TextFormatting.GOLD).get();
    }

    public boolean canSend(){
        return this == SEND || this == BOTH;
    }

    public boolean canReceive(){
        return this == RECEIVE || this == BOTH;
    }
}
