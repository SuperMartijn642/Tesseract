package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.gui.widget.BlockEntityBaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketScreenSetChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class TesseractScreen extends BlockEntityBaseWidget<TesseractBlockEntity> {

    private static final int MAX_DISPLAYED_CHANNELS = 12;
    private static final int CHANNEL_CUTOFF_LENGTH = 100;

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("tesseract", "gui/new_gui");
    private static final int BACKGROUND_WIDTH = 249, BACKGROUND_HEIGHT = 211;
    public static final Identifier CHANNEL_BACKGROUND = Identifier.fromNamespaceAndPath("tesseract", "gui/background");
    public static final Identifier TAB_ON = Identifier.fromNamespaceAndPath("tesseract", "gui/tab_new");
    public static final Identifier TAB_OFF = Identifier.fromNamespaceAndPath("tesseract", "gui/tab_off_new");
    public static final Identifier ITEM_ICON = Identifier.fromNamespaceAndPath("tesseract", "gui/item_tab_icon");
    public static final Identifier ENERGY_ICON = Identifier.fromNamespaceAndPath("tesseract", "gui/energy_tab_icon");
    public static final Identifier FLUID_ICON = Identifier.fromNamespaceAndPath("tesseract", "gui/fluid_tab_icon");
    public static final Identifier LOCK_ON = Identifier.fromNamespaceAndPath("tesseract", "gui/lock_on");
    public static final Identifier LOCK_OFF = Identifier.fromNamespaceAndPath("tesseract", "gui/lock_off");
    public static final Identifier REDSTONE_TAB = Identifier.fromNamespaceAndPath("tesseract", "gui/redstone_tab");
    public static final Identifier SIDE_TAB = Identifier.fromNamespaceAndPath("tesseract", "gui/side_tab_new");
    public static final Identifier CHECKMARK = Identifier.fromNamespaceAndPath("tesseract", "gui/checkmark_icon");

    private static EnumChannelType type = EnumChannelType.ITEMS;

    private TesseractButton setButton;
    private TesseractButton removeButton;

    private TransferButton transferButton;
    private RedstoneButton redstoneButton;

    private int selectedChannel = -1;
    private int scrollOffset = 0;

    public TesseractScreen(Level level, BlockPos pos){
        super(0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, level, pos);
    }

    @Override
    protected void addWidgets(TesseractBlockEntity entity){
        // set button
        this.setButton = this.addWidget(new TesseractButton(113, 185, 61, 18, TextComponents.translation("gui.tesseract.set").get(), () -> {
            if(this.object != null){
                if(this.object.getChannelId(type) == this.selectedChannel){
                    Tesseract.CHANNEL.sendToServer(new PacketScreenSetChannel(type, -1, this.blockEntityPos));
                    this.setButton.setText(TextComponents.translation("gui.tesseract.set").get());
                }else{
                    Tesseract.CHANNEL.sendToServer(new PacketScreenSetChannel(type, this.selectedChannel, this.blockEntityPos));
                    this.setButton.setText(TextComponents.translation("gui.tesseract.unset").get());
                }
            }
        }));
        this.setButton.active = false;

        // remove button
        this.removeButton = this.addWidget(new TesseractButton(180, 185, 61, 18, TextComponents.translation("gui.tesseract.remove").get(),
            () -> {
                Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, this.selectedChannel);
                if(channel != null && (channel.creator.equals(ClientUtils.getPlayer().getUUID()) || (ClientUtils.getPlayer().permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) && Minecraft.getInstance().hasShiftDown()))
                    ClientUtils.displayScreen(WidgetScreen.of(new TesseractRemoveChannelScreen(this.blockEntityLevel, this.blockEntityPos, type, this.selectedChannel)));
            }
        ));
        this.removeButton.setRedBackground();
        this.removeButton.active = false;

        // add button
        this.addWidget(new TesseractButton(29, 190, 50, 10, TextComponents.translation("gui.tesseract.add").get(), () -> ClientUtils.displayScreen(WidgetScreen.of(new TesseractAddChannelScreen(this.blockEntityLevel, this.blockEntityPos, type)))));

        // transfer button
        this.transferButton = this.addWidget(new TransferButton(-21, 156));
        this.transferButton.update(entity, type);
        // redstone button
        this.redstoneButton = this.addWidget(new RedstoneButton(-25, 37));
        this.redstoneButton.update(entity);

        super.addWidgets(entity);
    }

    @Override
    protected void update(TesseractBlockEntity entity){
        this.transferButton.update(entity, type);
        this.redstoneButton.update(entity);

        super.update(entity);
    }

    @Override
    protected void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, TesseractBlockEntity object){
        this.setFocused(true);
        super.renderBackground(context, graphics, mouseX, mouseY, object);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, TesseractBlockEntity entity){
        graphics.submitSprite(BACKGROUND, 0, 0, this.width(), this.height());

        Component s = TextComponents.translation("gui.tesseract." + type.name().toLowerCase(Locale.ROOT)).get();
        graphics.submitText(s, 177, 14, p -> p.color(0xffffffff).centerHorizontally());

        this.drawTabs(graphics);
        this.drawChannels(graphics, mouseX, mouseY, entity);

        Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, this.selectedChannel);
        if(channel != null)
            this.drawSelectedChannelInfo(graphics, channel);

        super.render(context, graphics, mouseX, mouseY, entity);
    }

    @Override
    protected void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, TesseractBlockEntity entity){
        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        for(int i = 0; i < MAX_DISPLAYED_CHANNELS && i + this.scrollOffset < channels.size(); i++){
            Channel channel = channels.get(i + this.scrollOffset);
            int x = entity.getChannelId(type) == channel.id ? 17 : 5, y = 31 + i * 13;
            if(mouseX >= x && mouseX < x + 9 && mouseY >= y + 2 && mouseY < y + 11){
                String creatorName = PlayerRenderer.getPlayerUsername(channel.creator);
                if(creatorName != null)
                    graphics.submitTooltip(c -> c.literal(creatorName), mouseX, mouseY);
            }
        }

        if(mouseX >= 9 && mouseX < 31 && mouseY >= (type == EnumChannelType.ITEMS ? 2 : 4) && mouseY < 28)
            graphics.submitTooltip(c -> c.text(EnumChannelType.ITEMS.getTranslation()), mouseX, mouseY);
        else if(mouseX >= 38 && mouseX < 60 && mouseY >= (type == EnumChannelType.ENERGY ? 2 : 4) && mouseY < 28)
            graphics.submitTooltip(c -> c.text(EnumChannelType.ENERGY.getTranslation()), mouseX, mouseY);
        else if(mouseX >= 67 && mouseX < 89 && mouseY >= (type == EnumChannelType.FLUID ? 2 : 4) && mouseY < 28)
            graphics.submitTooltip(c -> c.text(EnumChannelType.FLUID.getTranslation()), mouseX, mouseY);

        super.renderTooltips(context, graphics, mouseX, mouseY, entity);
    }

    private void drawTabs(GuiGraphicsHelper graphics){
        // items
        this.drawTab(graphics, EnumChannelType.ITEMS, 6, ITEM_ICON);

        // energy
        this.drawTab(graphics, EnumChannelType.ENERGY, 35, ENERGY_ICON);

        // fluid
        this.drawTab(graphics, EnumChannelType.FLUID, 64, FLUID_ICON);

        // transfer
        graphics.submitSprite(SIDE_TAB, -27, 150, 30, 32);

        // info and redstone
        graphics.submitSprite(REDSTONE_TAB, -30, 32, 30, 30);
    }

    private void drawTab(GuiGraphicsHelper graphics, EnumChannelType type, int x, Identifier icon){
        Identifier texture = type == TesseractScreen.type ? TAB_ON : TAB_OFF;
        graphics.submitSprite(texture, x, type == TesseractScreen.type ? 0 : 2, 28, type == TesseractScreen.type ? 31 : 26);

        float width = 16, height = 16;
        float iconX = x + (28 - width) / 2f, iconY = (TesseractScreen.type == type ? 0 : 2) + (29 - height) / 2f;

        graphics.submitItem(new ItemStack(type.item.get()), (int)iconX, (int)iconY);
    }

    private void drawChannels(GuiGraphicsHelper graphics, int mouseX, int mouseY, TesseractBlockEntity entity){
        graphics.submitSprite(CHANNEL_BACKGROUND, 3, 31, 102, 156, p -> p.uv(0, 0, 102 / 256f, 157 / 256f));
        graphics.submitSprite(CHANNEL_BACKGROUND, 26, 187, 56, 16, p -> p.uv(0, 0, 56 / 256f, 16 / 256f));

        List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
        int channelHeight = 13;

        for(int i = 0; i < MAX_DISPLAYED_CHANNELS && i + this.scrollOffset < channels.size(); i++){
            int x = 3, y = 31 + i * channelHeight;
            Channel channel = channels.get(i + this.scrollOffset);

            // background
            if(entity.getChannelId(type) == channel.id)
                graphics.submitRectangle(x, y, 102, channelHeight, p -> p.color(0x69007050));
            if(this.selectedChannel == channel.id){
                graphics.submitRectangle(x, y, 102, 1, p -> p.color(0xffffffff));
                graphics.submitRectangle(x, y + 12, 102, 1, p -> p.color(0xffffffff));
                graphics.submitRectangle(x, y, 1, channelHeight, p -> p.color(0xffffffff));
                graphics.submitRectangle(x + 101, y, 1, channelHeight, p -> p.color(0xffffffff));
            }else if(mouseX >= x && mouseX < 105 && mouseY >= y && mouseY < y + channelHeight){
                graphics.submitRectangle(x, y, 102, 1, p -> p.color(0xff666666));
                graphics.submitRectangle(x, y + 12, 102, 1, p -> p.color(0xff666666));
                graphics.submitRectangle(x, y, 1, channelHeight, p -> p.color(0xff666666));
                graphics.submitRectangle(x + 101, y, 1, channelHeight, p -> p.color(0xff666666));
            }

            // channel name and icons
            x += 2;
            if(entity.getChannelId(type) == channel.id){
                graphics.submitSprite(CHECKMARK, x, y + 2, 9, 9);
                x += 12;
            }
            PlayerRenderer.renderPlayerHead(channel.creator, graphics, x, y + 2, 9, 9);
            x += 12;
            boolean isOwnedChannel = channel.creator.equals(ClientUtils.getPlayer().getUUID());
            // trim the channel name to fit
            int availableWidth = CHANNEL_CUTOFF_LENGTH - x - (isOwnedChannel ? 9 : 0);
            String name = channel.name;
            if(ClientUtils.getFontRenderer().width(name) > availableWidth)
                name = ClientUtils.getFontRenderer().getSplitter().plainHeadByWidth(name, availableWidth - ClientUtils.getFontRenderer().width("..."), Style.EMPTY) + "...";
            graphics.submitText(name, x, y + 3, p -> p.color(0xffffffff));
            x += ClientUtils.getFontRenderer().width(name) + 3;
            if(isOwnedChannel)
                graphics.submitSprite(channel.isPrivate ? LOCK_ON : LOCK_OFF, x, y + 2, 9, 9);
        }
    }

    private void drawSelectedChannelInfo(GuiGraphicsHelper graphics, Channel channel){
        // channel name
        graphics.poseStack().pushMatrix();
        graphics.poseStack().translate(177, 35);
        graphics.poseStack().scale(1.2f, 1.2f);
        graphics.submitText(channel.name, 0, 0, p -> p.activeColor().centerHorizontally());
        graphics.poseStack().popMatrix();
        // creator
        graphics.submitText(TextComponents.string("Creator:").italic().get(), 117, 55, p -> p.color(0xff666666));
        PlayerRenderer.renderPlayerHead(channel.creator, graphics, 117, 65, 9, 9);
        String creatorName = PlayerRenderer.getPlayerUsername(channel.creator);
        if(creatorName != null)
            //noinspection Convert2MethodRef
            graphics.submitText(creatorName, 129, 66, p -> p.activeColor());
        // category
        graphics.submitText(TextComponents.string("Category:").italic().get(), 117, 80, p -> p.color(0xff666666));
        graphics.poseStack().pushMatrix();
        graphics.poseStack().translate(115, 88);
        graphics.poseStack().scale(0.8f, 0.8f);
        graphics.submitItem(new ItemStack(type.item.get()), 0, 0);
        graphics.poseStack().popMatrix();
        //noinspection Convert2MethodRef
        graphics.submitText(channel.type.getTranslation(), 129, 91, p -> p.activeColor());
        // accessibility
        graphics.submitText(TextComponents.string("Accessibility:").italic().get(), 117, 105, p -> p.color(0xff666666));
        graphics.submitSprite(channel.isPrivate ? LOCK_ON : LOCK_OFF, 116, 114, 11, 11);
        //noinspection Convert2MethodRef
        graphics.submitText(TextComponents.translation("gui.tesseract.channel." + (channel.isPrivate ? "private" : "public")).get(), 129, 116, p -> p.activeColor());
    }

    private void setChannelType(EnumChannelType type){
        TesseractScreen.type = type;
        this.scrollOffset = 0;
        this.selectedChannel = -1;
        this.setButton.active = false;
        this.setButton.setText(TextComponents.translation("gui.tesseract.set").get());
        this.removeButton.active = false;
    }

    @Override
    protected boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled, TesseractBlockEntity entity){
        if(!hasBeenHandled && info.button() == 0){
            if(mouseY >= 2 && mouseY < 2 + 26){ // tabs
                if(mouseX >= 6 && mouseX < 6 + 28 && type != EnumChannelType.ITEMS){
                    this.setChannelType(EnumChannelType.ITEMS);
                    hasBeenHandled = true;
                }else if(mouseX >= 35 && mouseX < 35 + 28 && type != EnumChannelType.ENERGY){
                    this.setChannelType(EnumChannelType.ENERGY);
                    hasBeenHandled = true;
                }else if(mouseX >= 64 && mouseX < 64 + 28 && type != EnumChannelType.FLUID){
                    this.setChannelType(EnumChannelType.FLUID);
                    hasBeenHandled = true;
                }
            }else if(mouseX >= 3 && mouseX < 105 && mouseY >= 31 && mouseY < 187){ // channels
                int index = (mouseY - 31) / 13 + this.scrollOffset;
                List<Channel> channels = TesseractChannelManager.CLIENT.getChannels(TesseractScreen.type);
                if(index < channels.size()){
                    this.selectedChannel = channels.get(index).id;
                    this.setButton.setText(TextComponents.translation("gui.tesseract." + (entity.getChannelId(type) == this.selectedChannel ? "unset" : "set")).get());
                    this.setButton.active = true;
                    this.removeButton.active = channels.get(index).creator.equals(Minecraft.getInstance().player.getUUID());
                }else{
                    this.selectedChannel = -1;
                    this.setButton.active = false;
                    this.setButton.setText(TextComponents.translation("gui.tesseract.set").get());
                    this.removeButton.active = false;
                }
                hasBeenHandled = true;
            }
        }

        return super.mousePressed(mouseX, mouseY, info, isDoubleClick, hasBeenHandled, entity);
    }

    @Override
    protected boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled, TesseractBlockEntity entity){
        if(!hasBeenHandled){
            if(TesseractChannelManager.CLIENT.getChannels(type).size() > MAX_DISPLAYED_CHANNELS){
                this.scrollOffset = Math.max(this.scrollOffset - (int)scrollAmount, 0);
                this.scrollOffset = Math.min(this.scrollOffset, TesseractChannelManager.CLIENT.getChannels(type).size() - MAX_DISPLAYED_CHANNELS);
            }else
                this.scrollOffset = 0;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled, entity);
    }

    @Override
    protected Component getNarrationMessage(TesseractBlockEntity entity){
        return TextComponents.translation("gui.tesseract.title").get();
    }
}
