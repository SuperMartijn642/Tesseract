package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public abstract class Page {

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void render(MatrixStack matrixStack);

}
