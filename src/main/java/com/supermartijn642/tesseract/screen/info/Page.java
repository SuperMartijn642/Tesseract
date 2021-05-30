package com.supermartijn642.tesseract.screen.info;

import net.minecraft.util.text.ITextComponent;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public abstract class Page {

    private final int index;
    private final ITextComponent title, text;

    public Page(int index, ITextComponent title, ITextComponent text){
        this.index = index;
        this.title = title;
        this.text = text;
    }

    public int getIndex(){
        return this.index;
    }

    public ITextComponent getTitle(){
        return this.title;
    }

    public ITextComponent getText(){
        return this.text;
    }

    public abstract int getTopHeight();

    public abstract void renderTop(int mouseX, int mouseY);

}
