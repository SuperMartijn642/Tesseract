package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import net.minecraft.util.ResourceLocation;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends AbstractButtonWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tesseract", "textures/gui/buttons.png");

    private int textureX;

    public CycleButton(int x, int y, int textureX){
        super(x, y, 20, 20, null);
        this.textureX = textureX;
    }

    protected abstract int getCycleIndex();

    @Override
    public void render(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_){
        ScreenUtils.bindTexture(TEXTURE);
        ScreenUtils.drawTexture(this.x, this.y, this.width, this.height, (this.textureX + this.getCycleIndex() * 20) / 120f, (this.active ? this.hovered ? 1 : 0 : 2) / 3f, 1 / 6f, 1 / 3f);
    }
}
