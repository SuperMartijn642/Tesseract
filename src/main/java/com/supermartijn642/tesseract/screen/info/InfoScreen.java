package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.tesseract.ClientProxy;
import com.supermartijn642.tesseract.screen.InfoButton;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 4/16/2021 by SuperMartijn642
 */
public class InfoScreen extends BaseScreen {

    private static final ResourceLocation TESSERACT_HOVER_TAB = new ResourceLocation("tesseract", "textures/gui/info/hovering_tab.png");

    public static final int WIDTH = 172, HEIGHT = 80;

    /**
     * Tesseract position to return to when this screen is closed
     */
    private final BlockPos pos;
    private static InfoTab tab = InfoTab.GUI;

    private InfoButton closeInfoButton;
    private InfoArrowWidget backButton, nextButton;
    private List<InfoPageButton> pageButtons = new LinkedList<>();

    public InfoScreen(BlockPos pos){
        super(TextComponents.translation("gui.tesseract.info.title").get());
        this.pos = pos;
    }

    @Override
    protected float sizeX(){
        return Math.max(WIDTH, tab.getCurrentPage().getWidth());
    }

    @Override
    protected float sizeY(){
        return HEIGHT + tab.getCurrentPage().getHeight();
    }

    @Override
    protected void addWidgets(){
        this.closeInfoButton = this.addWidget(new InfoButton(0, 0, () -> ClientProxy.openScreen(this.pos)));
        this.backButton = this.addWidget(new InfoArrowWidget(0, 0, 7, 7, true, () -> tab.currentPageIndex, tab::getNumberOfPages, this::setPage));
        this.nextButton = this.addWidget(new InfoArrowWidget(0, 0, 7, 7, false, () -> tab.currentPageIndex, tab::getNumberOfPages, this::setPage));
        this.updateNavigationWidgets();
    }

    private void updateNavigationWidgets(){
        int x = ((int)this.sizeX() - WIDTH) / 2;
        this.closeInfoButton.x = x + 5;
        this.closeInfoButton.y = 5;
    }

    private void setPage(int index){
        if(index < 0 || index >= tab.getNumberOfPages())
            return;

        tab.currentPageIndex = index;
        this.updateNavigationWidgets();
    }

    @Override
    protected void render(PoseStack matrixStack, int mouseX, int mouseY){
        // tabs
        int x = ((int)this.sizeX() - WIDTH) / 2;
        drawHoveringTab(matrixStack, x, 0, 30, 30);
        drawHoveringTab(matrixStack, x + 40, 0, 102, 30);
        this.renderInfoTab(matrixStack, x + 43, 3, 24, 24, mouseX, mouseY, InfoTab.GUI);
        this.renderInfoTab(matrixStack, x + 67, 3, 24, 24, mouseX, mouseY, InfoTab.ITEMS);
        this.renderInfoTab(matrixStack, x + 91, 3, 24, 24, mouseX, mouseY, InfoTab.FLUID);
        this.renderInfoTab(matrixStack, x + 115, 3, 24, 24, mouseX, mouseY, InfoTab.ENERGY);

        // page
        matrixStack.pushPose();
        Page page = tab.getCurrentPage();
        matrixStack.translate(((int)this.sizeX() - page.getWidth()) / 2f, 40, 0);
        page.render(matrixStack);
        matrixStack.popPose();

        // navigation
    }

    private void renderInfoTab(PoseStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, InfoTab tab){
        if(tab == InfoScreen.tab)
            ScreenUtils.fillRect(matrixStack, x, y, width, height, 0x69007050);
        else if(mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height){
            ScreenUtils.fillRect(matrixStack, x, y, width, 1, 0xffffffff);
            ScreenUtils.fillRect(matrixStack, x, y + height - 1, width, 1, 0xffffffff);
            ScreenUtils.fillRect(matrixStack, x, y, 1, height, 0xffffffff);
            ScreenUtils.fillRect(matrixStack, x + width - 1, y, 1, height, 0xffffffff);
        }

        ClientUtils.getItemRenderer().renderGuiItem(tab.getIconItem(), (int)this.left() + x + width / 2 - 8, (int)this.top() + y + height / 2 - 8);
    }

    @Override
    protected void renderTooltips(PoseStack matrixStack, int mouseX, int mouseY){

    }

    public static void drawHoveringTab(PoseStack matrixStack, int x, int y, int width, int height){
        ScreenUtils.bindTexture(TESSERACT_HOVER_TAB);
        ScreenUtils.drawTexture(matrixStack, x, y, width - 3, height - 3, 0, 0, (width - 3) / 200f, (height - 3) / 200f);
        ScreenUtils.drawTexture(matrixStack, x + width - 3, y, 3, height - 3, 197 / 200f, 0, 3 / 200f, (height - 3) / 200f);
        ScreenUtils.drawTexture(matrixStack, x, y + height - 3, width - 3, 3, 0, 197 / 200f, (width - 3) / 200f, 3 / 200f);
        ScreenUtils.drawTexture(matrixStack, x + width - 3, y + height - 3, 3, 3, 197 / 200f, 197 / 200f, 3 / 200f, 3 / 200f);
    }
}
