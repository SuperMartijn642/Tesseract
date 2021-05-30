package com.supermartijn642.tesseract.screen.info;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Supplier;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class InfoPageButton extends Widget implements IHoverTextWidget {

    private static final ResourceLocation BUTTONS = new ResourceLocation("tesseract", "textures/gui/page_navigation.png");

    private final Supplier<Page> page;

    public InfoPageButton(int x, int y, int width, int height, Supplier<Page> page){
        super(x, y, width, height);
        this.page = page;
    }

    @Override
    public ITextComponent getHoverText(){
        return this.page.get().getTitle();
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return new TextComponentTranslation("gui.tesseract.info.narrate_page", this.page.get().getTitle());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(BUTTONS);
        ScreenUtils.drawTexture(this.x, this.y, this.width, this.height, 7 / 18f, (this.active ? this.hovered ? 1 : 0 : 2) / 3f, 4 / 18f, 1 / 3f);
    }
}
