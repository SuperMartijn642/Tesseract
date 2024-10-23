package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends AbstractButtonWidget {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("tesseract", "textures/gui/buttons.png");

    private final int textureX;
    private boolean active = true;

    public CycleButton(int x, int y, int textureX){
        super(x, y, 20, 20, null);
        this.textureX = textureX;
    }

    protected abstract int getCycleIndex();

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        ScreenUtils.drawTexture(TEXTURE, context.poseStack(), this.x, this.y, this.width, this.height, (this.textureX + this.getCycleIndex() * 20) / 120f, (this.active ? this.isFocused() ? 1 : 0 : 2) / 3f, 1 / 6f, 1 / 3f);
    }
}
