package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created 4/13/2021 by SuperMartijn642
 */
public class InfoButton extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tesseract", "textures/gui/info_button.png");

    public InfoButton(int x, int y, Runnable onPress){
        super(x, y, 20, 20, onPress);
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public void render(int x, int y, float partialTicks){
        ScreenUtils.bindTexture(TEXTURE);
        ScreenUtils.drawTexture(this.x, this.y, this.width, this.height, 0, (this.active ? this.hovered ? 1 : 0 : 2) / 3f, 1, 1 / 3f);
    }

    @Override
    public ITextComponent getHoverText(){
        return new TextComponentTranslation("gui.tesseract.info_button");
    }
}
