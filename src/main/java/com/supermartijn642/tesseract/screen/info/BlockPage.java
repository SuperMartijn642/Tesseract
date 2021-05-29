package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class BlockPage extends Page {

    public BlockPage(int index, ITextComponent title, ITextComponent text){
        super(index, title, text);
    }

    @Override
    public int getTopHeight(){
        return 101;
    }

    @Override
    public void renderTop(MatrixStack matrixStack, int mouseX, int mouseY){

    }
}
