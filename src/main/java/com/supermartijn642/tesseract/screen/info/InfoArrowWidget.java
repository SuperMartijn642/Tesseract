package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class InfoArrowWidget extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation BUTTONS = new ResourceLocation("tesseract", "textures/gui/page_navigation.png");

    private final boolean left;
    private final Supplier<Integer> currentPage, numberOfPages;

    public InfoArrowWidget(int x, int y, int width, int height, boolean left, Supplier<Integer> currentPage, Supplier<Integer> numberOfPages, Consumer<Integer> setPage){
        super(x, y, width, height, () -> setPage.accept(left ? currentPage.get() - 1 : currentPage.get() + 1));
        this.left = left;
        this.currentPage = currentPage;
        this.numberOfPages = numberOfPages;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(BUTTONS);
        boolean active = this.left ? this.currentPage.get() > 0 : this.currentPage.get() < this.numberOfPages.get();
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.left ? 0 : 11 / 18f, (active ? this.hovered ? 1 : 0 : 3) / 4f, 7 / 18f, 1 / 4f);
    }

    @Override
    public ITextComponent getHoverText(){
        return new TranslationTextComponent("gui.tesseract.info." + (this.left ? "back" : "forward"));
    }
}
