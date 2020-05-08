package com.supermartijn642.tesseract.screen;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketAddChannel;
import com.supermartijn642.tesseract.packets.PacketRemoveChannel;
import com.supermartijn642.tesseract.packets.PacketSetChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class TesseractScreen extends GuiScreen {

    private static final int CHANNEL_MAX_CHARACTERS = 19;

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Tesseract.MODID, "textures/gui/screen.png");
    private static final int BACKGROUND_WIDTH = 235, BACKGROUND_HEIGHT = 231;
    private static final ResourceLocation CHANNEL_BACKGROUND = new ResourceLocation(Tesseract.MODID, "textures/gui/background.png");
    private static final ResourceLocation TAB_ON = new ResourceLocation(Tesseract.MODID, "textures/gui/tab.png");
    private static final ResourceLocation TAB_OFF = new ResourceLocation(Tesseract.MODID, "textures/gui/tab_off.png");
    private static final ResourceLocation ITEM_ICON = new ResourceLocation(Tesseract.MODID, "textures/gui/item_tab_icon.png");
    private static final ResourceLocation ENERGY_ICON = new ResourceLocation(Tesseract.MODID, "textures/gui/energy_tab_icon.png");
    private static final ResourceLocation FLUID_ICON = new ResourceLocation(Tesseract.MODID, "textures/gui/fluid_tab_icon.png");
    private static final ResourceLocation SCROLL_BUTTONS = new ResourceLocation("minecraft", "textures/gui/server_selection.png");
    private static final ResourceLocation LOCK_ON = new ResourceLocation(Tesseract.MODID, "textures/gui/lock_on.png");
    private static final ResourceLocation LOCK_OFF = new ResourceLocation(Tesseract.MODID, "textures/gui/lock_off.png");

    private static EnumChannelType type = EnumChannelType.ITEMS;
    private BlockPos pos;
    private int left, top;

    private GuiButton setButton;
    private GuiButton removeButton;
    private GuiButton addButton;
    private GuiLockIconButton privateButton;
    private GuiTextField textField;
    private String lastText = "";

    private int selectedChannel = -1;
    private int scrollOffset = 0;

    public TesseractScreen(BlockPos pos){
        this.pos = pos;
    }

    @Override
    public void initGui(){
        this.left = (this.width - BACKGROUND_WIDTH) / 2;
        this.top = (this.height - BACKGROUND_HEIGHT) / 2;

        boolean enabled = this.setButton != null && this.setButton.enabled;
        this.setButton = this.addButton(new GuiButton(0, this.left + 140, this.top + 28 + 25, 80, 20, I18n.format("gui.tesseract.set")));
        this.setButton.enabled = enabled;
        enabled = this.removeButton != null && this.removeButton.enabled;
        this.removeButton = this.addButton(new GuiButton(1, this.left + 140, this.top + 28 + 50, 80, 20, I18n.format("gui.tesseract.remove")));
        this.removeButton.enabled = enabled;

        enabled = this.addButton != null && this.addButton.enabled;
        this.addButton = this.addButton(new GuiButton(2, this.left + 165, this.top + 28 + 173, 55, 20, I18n.format("gui.tesseract.add")));
        this.addButton.enabled = enabled;
        enabled = this.privateButton != null && this.privateButton.isLocked();
        this.privateButton = this.addButton(new GuiLockIconButton(3, this.left + 140, this.top + 28 + 173));
        this.privateButton.setLocked(enabled);
        enabled = this.textField != null && this.textField.isFocused();
        String text = this.textField == null ? "" : this.textField.getText();
        this.textField = new GuiTextField(4, this.fontRenderer, this.left + 15, this.top + 28 + 173, 120, 20);
        this.textField.setFocused(enabled);
        this.textField.setText(text);
        this.textField.setCanLoseFocus(true);
        this.textField.setMaxStringLength(CHANNEL_MAX_CHARACTERS);
    }

    @Override
    public void updateScreen(){
        this.getTileOrClose();
        this.textField.updateCursorCounter();
        if(!this.lastText.equals(this.textField.getText())){
            this.lastText = this.textField.getText();
            if(this.lastText.trim().isEmpty())
                this.addButton.enabled = false;
            else{
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannelsCreatedBy(type, Minecraft.getMinecraft().player.getUniqueID());
                boolean enabled = true;
                for(Channel channel : channels){
                    if(channel.name.equals(this.lastText.trim())){
                        enabled = false;
                        break;
                    }
                }
                this.addButton.enabled = enabled;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.left, this.top, 0);
        GlStateManager.color(1, 1, 1, 1);

        this.drawBackground();
        this.drawTabs();
        this.drawChannels();

        GlStateManager.popMatrix();

        for(GuiButton button : this.buttonList)
            button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        this.textField.drawTextBox();
    }

    private void drawBackground(){
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        this.drawTexturedModalRect(0, 0, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        String s = I18n.format("gui.tesseract." + type.name().toLowerCase(Locale.ENGLISH));
        this.fontRenderer.drawStringWithShadow(s, (BACKGROUND_WIDTH - this.fontRenderer.getStringWidth(s)) / 2f, 28 + 10, 0xffffffff);
    }

    private void drawTabs(){
        // items
        this.drawTab(EnumChannelType.ITEMS, 6, ITEM_ICON);

        // energy
        this.drawTab(EnumChannelType.ENERGY, 35, ENERGY_ICON);

        // fluid
        this.drawTab(EnumChannelType.FLUID, 64, FLUID_ICON);
    }

    private void drawTab(EnumChannelType type, int x, ResourceLocation icon){
        this.drawTexture(type == TesseractScreen.type ? TAB_ON : TAB_OFF, x, type == TesseractScreen.type ? 0 : 2, 28, type == TesseractScreen.type ? 31 : 26);

        double width = 16, height = 16;
        double iconX = x + (28 - width) / 2, iconY = (TesseractScreen.type == type ? 0 : 2) + (29 - height) / 2;

        this.drawTexture(icon, iconX, iconY, width, height);
    }

    private void drawChannels(){
        TesseractTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(CHANNEL_BACKGROUND);
        this.drawTexturedModalRect(15, 28 + 25, 0, 0, 120, 143);

        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        int channelHeight = 143 / 11;

        for(int i = 0; i < 11 && i + this.scrollOffset < channels.size(); i++){
            Channel channel = channels.get(i + this.scrollOffset);
            if(tile.getChannelId(type) == channel.id)
                this.drawGradientRect(15, 28 + 25 + i * channelHeight, 135, 28 + 25 + 13 + i * channelHeight, 0x69007050, 0x69007050);
            if(this.selectedChannel == channel.id){
                this.drawGradientRect(15, 28 + 25 + i * channelHeight, 135, 28 + 25 + i * channelHeight + 1, 0xffffffff, 0xffffffff);
                this.drawGradientRect(15, 28 + 25 + i * channelHeight + 12, 135, 28 + 25 + i * channelHeight + 13, 0xffffffff, 0xffffffff);
                this.drawGradientRect(15, 28 + 25 + i * channelHeight, 16, 28 + 25 + i * channelHeight + 13, 0xffffffff, 0xffffffff);
                this.drawGradientRect(134, 28 + 25 + i * channelHeight, 135, 28 + 25 + i * channelHeight + 13, 0xffffffff, 0xffffffff);
            }
            this.fontRenderer.drawString(channel.name, 15 + 3, 28 + 25 + 3 + i * channelHeight, 0xffffffff, false);
            if(channel.creator.equals(Minecraft.getMinecraft().player.getUniqueID())){
                int width = this.fontRenderer.getStringWidth(channel.name);
                this.drawTexture(channel.isPrivate ? LOCK_ON : LOCK_OFF, 15 + 6 + width, 28 + 25 + 2 + i * channelHeight, 9, 9);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    public TesseractTile getTileOrClose(){
        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;
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
        this.addButton.enabled = false;
        this.selectedChannel = -1;
        this.setButton.enabled = false;
        this.removeButton.enabled = false;
    }

    private void drawTexture(ResourceLocation texture, double x, double y, double width, double height){
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, this.zLevel).tex(0, 1).endVertex();
        bufferbuilder.pos(x + width, y + height, this.zLevel).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, this.zLevel).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, this.zLevel).tex(0, 0).endVertex();
        tessellator.draw();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        int screenX = mouseX - this.left, screenY = mouseY - this.top;
        if(mouseButton == 0){
            if(screenY >= 2 && screenY < 2 + 26){ // tabs
                if(screenX >= 6 && screenX < 6 + 28 && type != EnumChannelType.ITEMS)
                    this.setChannelType(EnumChannelType.ITEMS);
                else if(screenX >= 35 && screenX < 35 + 28 && type != EnumChannelType.ENERGY)
                    this.setChannelType(EnumChannelType.ENERGY);
                else if(screenX >= 64 && screenX < 64 + 28 && type != EnumChannelType.FLUID)
                    this.setChannelType(EnumChannelType.FLUID);
            }else if(screenX >= 15 && screenX < 135 && screenY >= 28 + 25 && screenY < 28 + 25 + 143){ // channels
                int index = (screenY - (28 + 25)) / (143 / 11) + this.scrollOffset;
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
                if(index < channels.size()){
                    TesseractTile tile = this.getTileOrClose();
                    if(tile != null){
                        this.selectedChannel = channels.get(index + this.scrollOffset).id;
                        this.setButton.enabled = tile.getChannelId(type) != this.selectedChannel;
                        this.removeButton.enabled = channels.get(index + this.scrollOffset).creator.equals(Minecraft.getMinecraft().player.getUniqueID());
                    }
                }else{
                    this.selectedChannel = -1;
                    this.setButton.enabled = false;
                    this.removeButton.enabled = false;
                }
            }
        }else if(mouseButton == 1){ // text field
            if(mouseX >= this.textField.x && mouseX < this.textField.x + this.textField.width
                && mouseY >= this.textField.y && mouseY < this.textField.y + this.textField.height)
                this.textField.setText("");
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        super.keyTyped(typedChar, keyCode);
        this.textField.textboxKeyTyped(typedChar, keyCode);
        if(!this.textField.isFocused() && (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)))
            this.mc.player.closeScreen();
    }

    private void scroll(int amount){
        if(TesseractChannelManager.CLIENT.getChannels(type).size() > 11){
            this.scrollOffset = Math.max(this.scrollOffset + amount, 0);
            this.scrollOffset = Math.min(this.scrollOffset, TesseractChannelManager.CLIENT.getChannels(type).size() - 11);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button){
        if(button == this.addButton){
            Tesseract.channel.sendToServer(new PacketAddChannel(type, this.lastText.trim(), this.privateButton.isLocked()));
            this.textField.setText("");
        }else if(button == this.privateButton)
            this.privateButton.setLocked(!this.privateButton.isLocked());
        else if(button == this.setButton){
            Tesseract.channel.sendToServer(new PacketSetChannel(type, this.selectedChannel, this.pos));
            this.selectedChannel = -1;
            this.setButton.enabled = false;
            this.removeButton.enabled = false;
        }else if(button == this.removeButton){
            Tesseract.channel.sendToServer(new PacketRemoveChannel(type, this.selectedChannel));
            this.selectedChannel = -1;
            this.setButton.enabled = false;
            this.removeButton.enabled = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - this.left;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.top;

        if(mouseX >= 15 && mouseX < 135 && mouseY >= 28 + 25 && mouseY < 28 + 25 + 143)
            this.scroll(- Mouse.getEventDWheel() / 120);
    }
}
