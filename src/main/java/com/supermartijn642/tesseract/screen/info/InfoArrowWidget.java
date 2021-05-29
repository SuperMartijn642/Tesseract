package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class InfoArrowWidget extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation BUTTONS = new ResourceLocation("tesseract", "textures/gui/page_navigation.png");

    private final boolean left;

    public InfoArrowWidget(int x, int y, int width, int height, boolean left, Runnable onPress){
        super(x, y, width, height, onPress);
        this.left = left;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(BUTTONS);
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.left ? 0 : 11 / 18f, (this.active ? this.hovered ? 1 : 0 : 2) / 3f, 7 / 18f, 1 / 3f);
    }

    @Override
    public ITextComponent getHoverText(){
        return new TranslationTextComponent("gui.tesseract.info." + (this.left ? "back" : "forward"));
    }
}
