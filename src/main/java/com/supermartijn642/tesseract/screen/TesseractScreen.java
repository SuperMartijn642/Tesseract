package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.TileEntityBaseScreen;
import com.supermartijn642.tesseract.ClientProxy;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketScreenRemoveChannel;
import com.supermartijn642.tesseract.packets.PacketScreenSetChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Locale;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class TesseractScreen extends TileEntityBaseScreen<TesseractTile> {

    private static final int MAX_DISPLAYED_CHANNELS = 12;
    private static final int CHANNEL_CUTOFF_LENGTH = 100;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("tesseract", "textures/gui/new_gui.png");
    private static final int BACKGROUND_WIDTH = 249, BACKGROUND_HEIGHT = 211;
    private static final ResourceLocation CHANNEL_BACKGROUND = new ResourceLocation("tesseract", "textures/gui/background.png");
    private static final ResourceLocation TAB_ON = new ResourceLocation("tesseract", "textures/gui/tab_new.png");
    private static final ResourceLocation TAB_OFF = new ResourceLocation("tesseract", "textures/gui/tab_off_new.png");
    private static final ResourceLocation ITEM_ICON = new ResourceLocation("tesseract", "textures/gui/item_tab_icon.png");
    private static final ResourceLocation ENERGY_ICON = new ResourceLocation("tesseract", "textures/gui/energy_tab_icon.png");
    private static final ResourceLocation FLUID_ICON = new ResourceLocation("tesseract", "textures/gui/fluid_tab_icon.png");
    private static final ResourceLocation SCROLL_BUTTONS = new ResourceLocation("minecraft", "textures/gui/server_selection.png");
    private static final ResourceLocation LOCK_ON = new ResourceLocation("tesseract", "textures/gui/lock_on.png");
    private static final ResourceLocation LOCK_OFF = new ResourceLocation("tesseract", "textures/gui/lock_off.png");
    private static final ResourceLocation REDSTONE_TAB = new ResourceLocation("tesseract", "textures/gui/redstone_tab.png");
    private static final ResourceLocation SIDE_TAB = new ResourceLocation("tesseract", "textures/gui/side_tab_new.png");
    private static final ResourceLocation CHECKMARK = new ResourceLocation("tesseract", "textures/gui/checkmark_icon.png");

    private static EnumChannelType type = EnumChannelType.ITEMS;

    private TesseractButton setButton;
    private TesseractButton removeButton;

    private TransferButton transferButton;
    private RedstoneButton redstoneButton;

    private int selectedChannel = -1;
    private int scrollOffset = 0;

    @Override
    protected float sizeX(TesseractTile tile){
        return BACKGROUND_WIDTH;
    }

    @Override
    protected float sizeY(TesseractTile tile){
        return BACKGROUND_HEIGHT;
    }

    public TesseractScreen(BlockPos pos){
        super(new TranslationTextComponent("gui.tesseract.title"), pos);
    }

    @Override
    protected void addWidgets(TesseractTile tile){
        // set button
        this.setButton = this.addWidget(new TesseractButton(113, 185, 61, 18, new TranslationTextComponent("gui.tesseract.set"), () -> {
            TesseractTile tile2 = this.getObjectOrClose();
            if(tile2 != null){
                if(tile2.getChannelId(type) == this.selectedChannel){
                    Tesseract.CHANNEL.sendToServer(new PacketScreenSetChannel(type, -1, this.tilePos));
                    this.setButton.setText(new TranslationTextComponent("gui.tesseract.set"));
                }else{
                    Tesseract.CHANNEL.sendToServer(new PacketScreenSetChannel(type, this.selectedChannel, this.tilePos));
                    this.setButton.setText(new TranslationTextComponent("gui.tesseract.unset"));
                }
            }
        }));
        this.setButton.active = false;

        // remove button
        this.removeButton = this.addWidget(new TesseractButton(180, 185, 61, 18, new TranslationTextComponent("gui.tesseract.remove"), () -> {
            Tesseract.CHANNEL.sendToServer(new PacketScreenRemoveChannel(type, this.selectedChannel));
            this.selectedChannel = -1;
            this.setButton.active = false;
            this.setButton.setText(new TranslationTextComponent("gui.tesseract.set"));
            this.removeButton.active = false;
        }));
        this.removeButton.setRedBackground();
        this.removeButton.active = false;

        // add button
        this.addWidget(new TesseractButton(29, 190, 50, 10, new TranslationTextComponent("gui.tesseract.add"), () -> ClientUtils.displayScreen(new TesseractAddChannelScreen(this.tilePos, type))));

        // transfer button
        this.transferButton = this.addWidget(new TransferButton(-21, 156));
        this.transferButton.update(tile, type);
        // redstone button
        this.redstoneButton = this.addWidget(new RedstoneButton(-25, 59));
        this.redstoneButton.update(tile);

        // info button
        this.addWidget(new InfoButton(-25, 37, () -> ClientProxy.openInfoScreen(this.tilePos))).active = false; // TODO: make this active
    }

    @Override
    protected void tick(TesseractTile tile){
        this.transferButton.update(tile, type);
        this.redstoneButton.update(tile);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, TesseractTile tile){
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(matrixStack, 0, 0, this.sizeX(), this.sizeY());

        TextComponent s = new TranslationTextComponent("gui.tesseract." + type.name().toLowerCase(Locale.ROOT));
        ScreenUtils.drawCenteredString(matrixStack, this.font, s, 177, 14, 0xffffffff);

        this.drawTabs(matrixStack);
        this.drawChannels(matrixStack, mouseX, mouseY, tile);

        Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, this.selectedChannel);
        if(channel != null)
            this.drawSelectedChannelInfo(matrixStack, channel);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY, TesseractTile tile){
        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        for(int i = 0; i < MAX_DISPLAYED_CHANNELS && i + this.scrollOffset < channels.size(); i++){
            Channel channel = channels.get(i + this.scrollOffset);
            int x = tile.getChannelId(type) == channel.id ? 17 : 5, y = 31 + i * 13;
            if(mouseX >= x && mouseX < x + 9 && mouseY >= y + 2 && mouseY < y + 11){
                String creatorName = PlayerRenderer.getPlayerUsername(channel.creator);
                if(creatorName != null)
                    this.renderTooltip(matrixStack, new StringTextComponent(creatorName), mouseX, mouseY);
            }
        }

        if(mouseX >= 9 && mouseX < 31 && mouseY >= (type == EnumChannelType.ITEMS ? 2 : 4) && mouseY < 28)
            this.renderTooltip(matrixStack, EnumChannelType.ITEMS.getTranslation(), mouseX, mouseY);
        else if(mouseX >= 38 && mouseX < 60 && mouseY >= (type == EnumChannelType.ENERGY ? 2 : 4) && mouseY < 28)
            this.renderTooltip(matrixStack, EnumChannelType.ENERGY.getTranslation(), mouseX, mouseY);
        else if(mouseX >= 67 && mouseX < 89 && mouseY >= (type == EnumChannelType.FLUID ? 2 : 4) && mouseY < 28)
            this.renderTooltip(matrixStack, EnumChannelType.FLUID.getTranslation(), mouseX, mouseY);
    }

    private void drawTabs(MatrixStack matrixStack){
        // items
        this.drawTab(matrixStack, EnumChannelType.ITEMS, 6, ITEM_ICON);

        // energy
        this.drawTab(matrixStack, EnumChannelType.ENERGY, 35, ENERGY_ICON);

        // fluid
        this.drawTab(matrixStack, EnumChannelType.FLUID, 64, FLUID_ICON);

        // transfer
        ScreenUtils.bindTexture(SIDE_TAB);
        GlStateManager.enableAlphaTest();
        ScreenUtils.drawTexture(matrixStack, -27, 150, 30, 32);

        // info and redstone
        ScreenUtils.bindTexture(REDSTONE_TAB);
        ScreenUtils.drawTexture(matrixStack, -30, 32, 30, 52);
    }

    private void drawTab(MatrixStack matrixStack, EnumChannelType type, int x, ResourceLocation icon){
        ScreenUtils.bindTexture(type == TesseractScreen.type ? TAB_ON : TAB_OFF);
        GlStateManager.enableAlphaTest();
        ScreenUtils.drawTexture(matrixStack, x, type == TesseractScreen.type ? 0 : 2, 28, type == TesseractScreen.type ? 31 : 26);

        float width = 16, height = 16;
        float iconX = x + (28 - width) / 2f, iconY = (TesseractScreen.type == type ? 0 : 2) + (29 - height) / 2f;

//        ScreenUtils.bindTexture(icon);
//        ScreenUtils.drawTexture(matrixStack, iconX, iconY, width, height);
        this.itemRenderer.renderItemIntoGUI(new ItemStack(type.item.get()), (int)(this.left() + iconX), (int)(this.top() + iconY));
    }

    private void drawChannels(MatrixStack matrixStack, int mouseX, int mouseY, TesseractTile tile){
        ScreenUtils.bindTexture(CHANNEL_BACKGROUND);
        GlStateManager.enableAlphaTest();
        ScreenUtils.drawTexture(matrixStack, 3, 31, 102, 156, 0, 0, 102 / 256f, 157 / 256f);
        ScreenUtils.drawTexture(matrixStack, 26, 187, 56, 16, 0, 0, 56 / 256f, 16 / 256f);

        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        int channelHeight = 13;

        for(int i = 0; i < MAX_DISPLAYED_CHANNELS && i + this.scrollOffset < channels.size(); i++){
            int x = 3, y = 31 + i * channelHeight;
            Channel channel = channels.get(i + this.scrollOffset);

            // background
            if(tile.getChannelId(type) == channel.id)
                ScreenUtils.fillRect(matrixStack, x, y, 102, channelHeight, 0x69007050);
            if(this.selectedChannel == channel.id){
                ScreenUtils.fillRect(matrixStack, x, y, 102, 1, 0xffffffff);
                ScreenUtils.fillRect(matrixStack, x, y + 12, 102, 1, 0xffffffff);
                ScreenUtils.fillRect(matrixStack, x, y, 1, channelHeight, 0xffffffff);
                ScreenUtils.fillRect(matrixStack, x + 101, y, 1, channelHeight, 0xffffffff);
            }else if(mouseX >= x && mouseX < 105 && mouseY >= y && mouseY < y + channelHeight){
                ScreenUtils.fillRect(matrixStack, x, y, 102, 1, 0xff666666);
                ScreenUtils.fillRect(matrixStack, x, y + 12, 102, 1, 0xff666666);
                ScreenUtils.fillRect(matrixStack, x, y, 1, channelHeight, 0xff666666);
                ScreenUtils.fillRect(matrixStack, x + 101, y, 1, channelHeight, 0xff666666);
            }

            // channel name and icons
            x += 2;
            if(tile.getChannelId(type) == channel.id){
                ScreenUtils.bindTexture(CHECKMARK);
                ScreenUtils.drawTexture(matrixStack, x, y + 2, 9, 9);
                x += 12;
            }
            PlayerRenderer.renderPlayerHead(channel.creator, matrixStack, x, y + 2, 9, 9);
            x += 12;
            boolean isOwnedChannel = channel.creator.equals(ClientUtils.getPlayer().getUniqueID());
            // trim the channel name to fit
            int availableWidth = CHANNEL_CUTOFF_LENGTH - x - (isOwnedChannel ? 9 : 0);
            String name = channel.name;
            if(this.font.getStringWidth(name) > availableWidth)
                name = this.font.func_238420_b_().func_238361_b_(name, availableWidth - this.font.getStringWidth("..."), Style.EMPTY) + "...";
            ScreenUtils.drawString(matrixStack, name, x, y + 3, 0xffffffff);
            x += this.font.getStringWidth(name) + 3;
            if(isOwnedChannel){
                ScreenUtils.bindTexture(channel.isPrivate ? LOCK_ON : LOCK_OFF);
                GlStateManager.enableAlphaTest();
                ScreenUtils.drawTexture(matrixStack, x, y + 2, 9, 9);
            }
        }
    }

    private void drawSelectedChannelInfo(MatrixStack matrixStack, Channel channel){
        // channel name
        matrixStack.push();
        matrixStack.translate(177,35, 0);
        matrixStack.scale(1.2f, 1.2f, 1);
        ScreenUtils.drawCenteredString(matrixStack, channel.name, 0, 0, ScreenUtils.ACTIVE_TEXT_COLOR);
        matrixStack.pop();
        // creator
        ScreenUtils.drawString(matrixStack, new StringTextComponent("Creator:").setStyle(Style.EMPTY.setItalic(true)), 117, 55, 0xff666666);
        PlayerRenderer.renderPlayerHead(channel.creator, matrixStack, 117, 65, 9, 9);
        String creatorName = PlayerRenderer.getPlayerUsername(channel.creator);
        if(creatorName != null)
            ScreenUtils.drawString(matrixStack, creatorName, 129, 66, ScreenUtils.ACTIVE_TEXT_COLOR);
        // category
        ScreenUtils.drawString(matrixStack, new StringTextComponent("Category:").setStyle(Style.EMPTY.setItalic(true)), 117, 80, 0xff666666);
        GlStateManager.pushMatrix();
        GlStateManager.translated(this.left() + 115, this.top() + 88, 0);
        GlStateManager.scalef(0.8f, 0.8f, 1);
        this.itemRenderer.renderItemIntoGUI(new ItemStack(type.item.get()), 0, 0);
        GlStateManager.popMatrix();
        ScreenUtils.drawString(matrixStack, channel.type.getTranslation(), 129, 91, ScreenUtils.ACTIVE_TEXT_COLOR);
        // accessibility
        ScreenUtils.drawString(matrixStack, new StringTextComponent("Accessibility:").setStyle(Style.EMPTY.setItalic(true)), 117, 105, 0xff666666);
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(channel.isPrivate ? LOCK_ON : LOCK_OFF);
        ScreenUtils.drawTexture(matrixStack, 116, 114, 11, 11);
        ScreenUtils.drawString(matrixStack, new TranslationTextComponent("gui.tesseract.channel." + (channel.isPrivate ? "private" : "public")), 129, 116, ScreenUtils.ACTIVE_TEXT_COLOR);
    }

    private void setChannelType(EnumChannelType type){
        TesseractScreen.type = type;
        this.scrollOffset = 0;
        this.selectedChannel = -1;
        this.setButton.active = false;
        this.setButton.setText(new TranslationTextComponent("gui.tesseract.set"));
        this.removeButton.active = false;
    }

    @Override
    protected void onMousePress(int mouseX, int mouseY, int button){
        if(button == 0){
            if(mouseY >= 2 && mouseY < 2 + 26){ // tabs
                if(mouseX >= 6 && mouseX < 6 + 28 && type != EnumChannelType.ITEMS)
                    this.setChannelType(EnumChannelType.ITEMS);
                else if(mouseX >= 35 && mouseX < 35 + 28 && type != EnumChannelType.ENERGY)
                    this.setChannelType(EnumChannelType.ENERGY);
                else if(mouseX >= 64 && mouseX < 64 + 28 && type != EnumChannelType.FLUID)
                    this.setChannelType(EnumChannelType.FLUID);
            }else if(mouseX >= 3 && mouseX < 105 && mouseY >= 31 && mouseY < 187){ // channels
                int index = (mouseY - 31) / 13 + this.scrollOffset;
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
                if(index < channels.size()){
                    TesseractTile tile = this.getObjectOrClose();
                    if(tile != null){
                        this.selectedChannel = channels.get(index).id;
                        this.setButton.setText(new TranslationTextComponent("gui.tesseract." + (tile.getChannelId(type) == this.selectedChannel ? "unset" : "set")));
                        this.setButton.active = true;
                        this.removeButton.active = channels.get(index).creator.equals(Minecraft.getInstance().player.getUniqueID());
                    }
                }else{
                    this.selectedChannel = -1;
                    this.setButton.active = false;
                    this.setButton.setText(new TranslationTextComponent("gui.tesseract.set"));
                    this.removeButton.active = false;
                }
            }
        }
    }

    private void scroll(int amount){
        if(TesseractChannelManager.CLIENT.getChannels(type).size() > MAX_DISPLAYED_CHANNELS){
            this.scrollOffset = Math.max(this.scrollOffset + amount, 0);
            this.scrollOffset = Math.min(this.scrollOffset, TesseractChannelManager.CLIENT.getChannels(type).size() - MAX_DISPLAYED_CHANNELS);
        }else
            this.scrollOffset = 0;
    }

    @Override
    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
        if(mouseX >= 15 && mouseX < 135 && mouseY >= 28 + 25 && mouseY < 28 + 25 + 143)
            this.scroll(-(int)scroll);
    }
}
