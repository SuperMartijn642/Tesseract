package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 4/16/2021 by SuperMartijn642
 */
public class InfoScreen extends BaseScreen {

    public static final int WIDTH = 172, HEIGHT = 256;

    /**
     * Tesseract position to return to when this screen is closed
     */
    private BlockPos pos;
    private static Category category = Category.CATEGORIES.get(0);
    private static Page page = category.getPage(0);

    private InfoArrowWidget backButton, nextButton;
    private List<InfoPageButton> pageButtons = new LinkedList<>();

    public InfoScreen(BlockPos pos){
        super(new TranslationTextComponent("gui.tesseract.info.title"));
        this.pos = pos;
    }

    @Override
    protected float sizeX(){
        return WIDTH;
    }

    @Override
    protected float sizeY(){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(){
        this.backButton = this.addWidget(new InfoArrowWidget(0,HEIGHT - 14,7,7,true, () -> this.setPage(page.getIndex() - 1)));
        this.nextButton = this.addWidget(new InfoArrowWidget(0,HEIGHT - 14,7,7,false, () -> this.setPage(page.getIndex() + 1)));
        this.updateNavigationWidgets();
    }

    private void updateNavigationWidgets(){

    }

    private void setPage(int index){
        if(index < 0 || index >= category.getPageCount())
            return;

        page = category.getPage(index);
        this.updateNavigationWidgets();
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        this.drawScreenBackground(matrixStack);
        this.drawPageFrame(matrixStack);
        matrixStack.push();
        matrixStack.translate(6, 6, 0);
        page.renderTop(matrixStack,mouseX, mouseY);
        matrixStack.pop();
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){

    }

    private void drawPageFrame(MatrixStack matrixStack){
        ScreenUtils.fillRect(matrixStack, 5,5, this.sizeX() - 11, 1, 85 / 255f, 85 / 255f, 58 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, 5,5, 1, page.getTopHeight() + 1, 198 / 255f, 198 / 255f, 198 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, 5,6 + page.getTopHeight(), 1, 1, 168 / 255f, 168 / 255f, 168 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, this.sizeX() - 6,5, 1, 1, 168 / 255f, 168 / 255f, 168 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, 6,6 + page.getTopHeight(), this.sizeX() - 11, 1, 255 / 255f, 255 / 255f, 255 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, this.sizeX() - 6,6, 1, page.getTopHeight() + 1, 255 / 255f, 255 / 255f, 255 / 255f, 1);
        ScreenUtils.fillRect(matrixStack, 6,6, this.sizeX() - 12, page.getTopHeight(), 122 / 255f, 122 / 255f, 122 / 255f, 1);
    }
}
