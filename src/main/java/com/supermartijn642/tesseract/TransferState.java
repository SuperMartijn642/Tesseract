package com.supermartijn642.tesseract;

import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created 7/4/2020 by SuperMartijn642
 */
public enum TransferState {

    SEND("gui.tesseract.transfer.send"), RECEIVE("gui.tesseract.transfer.receive"), BOTH("gui.tesseract.transfer.both");

    private final String translation;

    TransferState(String translation){
        this.translation = translation;
    }

    public TextComponentTranslation translate(){
        return new TextComponentTranslation(this.translation);
    }

    public boolean canSend(){
        return this == SEND || this == BOTH;
    }

    public boolean canReceive(){
        return this == RECEIVE || this == BOTH;
    }

}
