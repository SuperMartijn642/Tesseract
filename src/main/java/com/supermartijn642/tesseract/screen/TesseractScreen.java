package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketAddChannel;
import com.supermartijn642.tesseract.packets.PacketRemoveChannel;
import com.supermartijn642.tesseract.packets.PacketSetChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.LockIconButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class TesseractScreen extends Screen {

    private static final int CHANNEL_MAX_CHARACTERS = 19;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("tesseract", "textures/gui/screen.png");
    private static final int BACKGROUND_WIDTH = 235, BACKGROUND_HEIGHT = 231;
    private static final ResourceLocation CHANNEL_BACKGROUND = new ResourceLocation("tesseract", "textures/gui/background.png");
    private static final ResourceLocation TAB_ON = new ResourceLocation("tesseract", "textures/gui/tab.png");
    private static final ResourceLocation TAB_OFF = new ResourceLocation("tesseract", "textures/gui/tab_off.png");
    private static final ResourceLocation ITEM_ICON = new ResourceLocation("tesseract", "textures/gui/item_tab_icon.png");
    private static final ResourceLocation ENERGY_ICON = new ResourceLocation("tesseract", "textures/gui/energy_tab_icon.png");
    private static final ResourceLocation FLUID_ICON = new ResourceLocation("tesseract", "textures/gui/fluid_tab_icon.png");
    private static final ResourceLocation SCROLL_BUTTONS = new ResourceLocation("minecraft", "textures/gui/server_selection.png");
    private static final ResourceLocation LOCK_ON = new ResourceLocation("tesseract", "textures/gui/lock_on.png");
    private static final ResourceLocation LOCK_OFF = new ResourceLocation("tesseract", "textures/gui/lock_off.png");
    private static final ResourceLocation REDSTONE_TAB = new ResourceLocation("tesseract", "textures/gui/redstone_tab.png");
    private static final ResourceLocation SIDE_TAB = new ResourceLocation("tesseract", "textures/gui/side_tab.png");

    private static EnumChannelType type = EnumChannelType.ITEMS;
    private BlockPos pos;
    private int left, top;

    private Button setButton;
    private Button removeButton;
    private Button addButton;
    private LockIconButton privateButton;
    private TextFieldWidget textField;
    private String lastText = "";

    private TransferButton transferButton;
    private RedstoneButton redstoneButton;

    private int selectedChannel = -1;
    private int scrollOffset = 0;

    public TesseractScreen(BlockPos pos){
        super(new TranslationTextComponent("gui.tesseract.title"));
        this.pos = pos;
    }

    @Override
    public void init(){
        TesseractTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        this.left = (this.width - BACKGROUND_WIDTH) / 2;
        this.top = (this.height - BACKGROUND_HEIGHT) / 2;

        // set button
        boolean enabled = this.setButton != null && this.setButton.active;
        this.setButton = this.addButton(new Button(this.left + 140, this.top + 28 + 25, 80, 20, new TranslationTextComponent("gui.tesseract.set"), button -> {
            Tesseract.CHANNEL.sendToServer(new PacketSetChannel(type, this.selectedChannel, this.pos));
            this.selectedChannel = -1;
            this.setButton.active = false;
            this.removeButton.active = false;
        }));
        this.setButton.active = enabled;

        // remove button
        enabled = this.removeButton != null && this.removeButton.active;
        this.removeButton = this.addButton(new Button(this.left + 140, this.top + 28 + 50, 80, 20, new TranslationTextComponent("gui.tesseract.remove"), buttons -> {
            Tesseract.CHANNEL.sendToServer(new PacketRemoveChannel(type, this.selectedChannel));
            this.selectedChannel = -1;
            this.setButton.active = false;
            this.removeButton.active = false;
        }));
        this.removeButton.active = enabled;

        // add button
        enabled = this.addButton != null && this.addButton.active;
        this.addButton = this.addButton(new Button(this.left + 165, this.top + 28 + 173, 55, 20, new TranslationTextComponent("gui.tesseract.add"), button -> {
            Tesseract.CHANNEL.sendToServer(new PacketAddChannel(type, this.lastText.trim(), this.privateButton.isLocked()));
            this.textField.setText("");
        }));
        this.addButton.active = enabled;

        // private button
        enabled = this.privateButton != null && this.privateButton.isLocked();
        this.privateButton = this.addButton(new LockIconButton(this.left + 140, this.top + 28 + 173, button ->
            this.privateButton.setLocked(!this.privateButton.isLocked())
        ));
        this.privateButton.setLocked(enabled);

        // text field
        enabled = this.textField != null && this.textField.isFocused();
        String text = this.textField == null ? "" : this.textField.getText();
        this.children.add(this.textField = new TextFieldWidget(this.font, this.left + 15, this.top + 28 + 173, 120, 20, new StringTextComponent("")));
        this.textField.setFocused2(enabled);
        this.textField.setText(text);
        this.textField.setCanLoseFocus(true);
        this.textField.setMaxStringLength(CHANNEL_MAX_CHARACTERS);

        this.transferButton = this.addButton(new TransferButton(this.left + 236, this.top + 47));
        this.transferButton.update(tile, type);
        this.redstoneButton = this.addButton(new RedstoneButton(this.left + 240, this.top + 198));
        this.redstoneButton.update(tile);
    }

    @Override
    public void tick(){
        TesseractTile tile = this.getTileOrClose();
        if(tile == null)
            return;
        this.textField.tick();
        if(!this.lastText.equals(this.textField.getText())){
            this.lastText = this.textField.getText();
            if(this.lastText.trim().isEmpty())
                this.addButton.active = false;
            else{
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannelsCreatedBy(type, Minecraft.getInstance().player.getUniqueID());
                boolean enabled = true;
                for(Channel channel : channels){
                    if(channel.name.equals(this.lastText.trim())){
                        enabled = false;
                        break;
                    }
                }
                this.addButton.active = enabled;
            }
        }
        this.transferButton.update(tile, type);
        this.redstoneButton.update(tile);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);

        RenderSystem.pushMatrix();
        RenderSystem.translated(this.left, this.top, 0);
        RenderSystem.color4f(1, 1, 1, 1);

        this.drawBackground(matrixStack);
        this.drawTabs();
        this.drawChannels(matrixStack);

        RenderSystem.popMatrix();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.textField.render(matrixStack, mouseX, mouseY, partialTicks);

        if(this.privateButton.isHovered())
            this.renderTooltip(matrixStack, new TranslationTextComponent("gui.tesseract.channel." + (this.privateButton.isLocked() ? "private" : "public")), mouseX, mouseY);
        if(this.transferButton.isHovered())
            this.renderTooltip(matrixStack, this.transferButton.state.translate(), mouseX, mouseY);
        if(this.redstoneButton.isHovered())
            this.renderTooltip(matrixStack, this.redstoneButton.state.translate(), mouseX, mouseY);
    }

    private void drawBackground(MatrixStack matrixStack){
        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(0, 0, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        TextComponent s = new TranslationTextComponent("gui.tesseract." + type.name().toLowerCase(Locale.ENGLISH));
        this.font.func_243246_a(matrixStack, s, (BACKGROUND_WIDTH - this.font.func_238414_a_(s)) / 2f, 28 + 10, 0xffffffff);
    }

    private void drawTabs(){
        // items
        this.drawTab(EnumChannelType.ITEMS, 6, ITEM_ICON);

        // energy
        this.drawTab(EnumChannelType.ENERGY, 35, ENERGY_ICON);

        // fluid
        this.drawTab(EnumChannelType.FLUID, 64, FLUID_ICON);

        // transfer
        this.drawTexture(SIDE_TAB, 232, 41, 30, 32);

        // redstone
        this.drawTexture(REDSTONE_TAB, 235, 192, 30, 32);
    }

    private void drawTab(EnumChannelType type, int x, ResourceLocation icon){
        this.drawTexture(type == TesseractScreen.type ? TAB_ON : TAB_OFF, x, type == TesseractScreen.type ? 0 : 2, 28, type == TesseractScreen.type ? 31 : 26);

        double width = 16, height = 16;
        double iconX = x + (28 - width) / 2, iconY = (TesseractScreen.type == type ? 0 : 2) + (29 - height) / 2;

//        this.drawTexture(icon, iconX, iconY, width, height);
        this.itemRenderer.renderItemIntoGUI(new ItemStack(type.item.get()), (int)iconX, (int)iconY);
    }

    private void drawChannels(MatrixStack matrixStack){
        TesseractTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        Minecraft.getInstance().getTextureManager().bindTexture(CHANNEL_BACKGROUND);
        this.drawTexturedModalRect(15, 28 + 25, 0, 0, 120, 143);

        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        int channelHeight = 143 / 11;

        for(int i = 0; i < 11 && i + this.scrollOffset < channels.size(); i++){
            Channel channel = channels.get(i + this.scrollOffset);
            if(tile.getChannelId(type) == channel.id)
                this.drawColoredRect(15, 28 + 25 + i * channelHeight, 120, channelHeight, 0x69007050);
            if(this.selectedChannel == channel.id){
                this.drawColoredRect(15, 28 + 25 + i * channelHeight, 120, 1, 0xffffffff);
                this.drawColoredRect(15, 28 + 25 + i * channelHeight + 12, 120, 1, 0xffffffff);
                this.drawColoredRect(15, 28 + 25 + i * channelHeight, 1, channelHeight, 0xffffffff);
                this.drawColoredRect(134, 28 + 25 + i * channelHeight, 1, channelHeight, 0xffffffff);
            }
            this.font.func_243248_b(matrixStack, new StringTextComponent(channel.name), 15 + 3, 28 + 25 + 3 + i * channelHeight, 0xffffffff);
            if(channel.creator.equals(Minecraft.getInstance().player.getUniqueID())){
                int width = this.font.getStringWidth(channel.name);
                this.drawTexture(channel.isPrivate ? LOCK_ON : LOCK_OFF, 15 + 6 + width, 28 + 25 + 2 + i * channelHeight, 9, 9);
            }
        }
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    public TesseractTile getTileOrClose(){
        World world = Minecraft.getInstance().world;
        PlayerEntity player = Minecraft.getInstance().player;
        if(world == null || player == null)
            return null;
        TileEntity tile = world.getTileEntity(this.pos);
        if(tile instanceof TesseractTile)
            return (TesseractTile)tile;
        player.closeScreen();
        return null;
    }

    private void setChannelType(EnumChannelType type){
        TesseractScreen.type = type;
        this.scrollOffset = 0;
        this.lastText = "";
        this.textField.setText("");
        this.addButton.active = false;
        this.selectedChannel = -1;
        this.setButton.active = false;
        this.removeButton.active = false;
    }

    private void drawTexture(ResourceLocation texture, double x, double y, double width, double height){
        GlStateManager.enableAlphaTest();

        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    private void drawColoredRect(int x, int y, int width, int height, int color){
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
        GlStateManager.shadeModel(7425);

        int red = (color & 0x00ff0000) >> 16, green = (color & 0x0000ff00) >> 8, blue = (color & 0x000000ff), alpha = 256 + ((color & 0xff000000) >> 24);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y + height, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x + width, y, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height){
        GlStateManager.enableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0).tex(textureX * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex((textureX + width) * 0.00390625F, (textureY + height) * 0.00390625F).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex((textureX + width) * 0.00390625F, textureY * 0.00390625F).endVertex();
        bufferbuilder.pos(x, y, 0).tex(textureX * 0.00390625F, textureY * 0.00390625F).endVertex();
        tessellator.draw();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        double screenX = mouseX - this.left, screenY = mouseY - this.top;
        if(mouseButton == 0){
            if(screenY >= 2 && screenY < 2 + 26){ // tabs
                if(screenX >= 6 && screenX < 6 + 28 && type != EnumChannelType.ITEMS)
                    this.setChannelType(EnumChannelType.ITEMS);
                else if(screenX >= 35 && screenX < 35 + 28 && type != EnumChannelType.ENERGY)
                    this.setChannelType(EnumChannelType.ENERGY);
                else if(screenX >= 64 && screenX < 64 + 28 && type != EnumChannelType.FLUID)
                    this.setChannelType(EnumChannelType.FLUID);
            }else if(screenX >= 15 && screenX < 135 && screenY >= 28 + 25 && screenY < 28 + 25 + 143){ // channels
                int index = ((int)screenY - (28 + 25)) / (143 / 11) + this.scrollOffset;
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
                if(index < channels.size()){
                    TesseractTile tile = this.getTileOrClose();
                    if(tile != null){
                        this.selectedChannel = channels.get(index).id;
                        this.setButton.active = tile.getChannelId(type) != this.selectedChannel;
                        this.removeButton.active = channels.get(index).creator.equals(Minecraft.getInstance().player.getUniqueID());
                    }
                }else{
                    this.selectedChannel = -1;
                    this.setButton.active = false;
                    this.removeButton.active = false;
                }
            }
        }else if(mouseButton == 1){ // clear text field
            if(this.textField.isHovered())
                this.textField.setText("");
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
        if(super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        InputMappings.Input mouseKey = InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_);
        if(!this.textField.isFocused() && (p_keyPressed_1_ == 256 || Minecraft.getInstance().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))){
            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return false;
    }

    private void scroll(int amount){
        if(TesseractChannelManager.CLIENT.getChannels(type).size() > 11){
            this.scrollOffset = Math.max(this.scrollOffset + amount, 0);
            this.scrollOffset = Math.min(this.scrollOffset, TesseractChannelManager.CLIENT.getChannels(type).size() - 11);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll){
        if(super.mouseScrolled(mouseX, mouseY, scroll))
            return true;

        if(mouseX >= this.left + 15 && mouseX < this.left + 135 && mouseY >= this.top + 28 + 25 && mouseY < this.top + 28 + 25 + 143){
            this.scroll(-(int)scroll);
            return true;
        }

        return false;
    }
}
