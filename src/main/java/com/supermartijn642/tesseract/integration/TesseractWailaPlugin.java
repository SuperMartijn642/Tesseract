package com.supermartijn642.tesseract.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlock;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import mcp.mobius.waila.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("tesseract")
public class TesseractWailaPlugin implements IWailaDataProvider, IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar){
        registrar.registerBodyProvider(this, TesseractBlock.class);
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        TileEntity entity = accessor.getTileEntity();
        if(entity instanceof TesseractBlockEntity){
            TesseractBlockEntity tesseract = (TesseractBlockEntity)entity;

            tooltip.add(TextComponents.translation("tesseract.tesseract.highlight.channels").format());
            tooltip.add(formatChannelInfo(EnumChannelType.ITEMS, tesseract.getChannelId(EnumChannelType.ITEMS)));
            tooltip.add(formatChannelInfo(EnumChannelType.ENERGY, tesseract.getChannelId(EnumChannelType.ENERGY)));
            tooltip.add(formatChannelInfo(EnumChannelType.FLUID, tesseract.getChannelId(EnumChannelType.FLUID)));
            if(tesseract.isBlockedByRedstone())
                tooltip.add(TextComponents.translation("tesseract.tesseract.highlight.redstone_blocked").color(TextFormatting.RED).format());
        }
        return tooltip;
    }

    private static String formatChannelInfo(EnumChannelType type, int channelId){
        Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, channelId);
        ITextComponent channelType = TextComponents.fromTextComponent(type.getTranslation()).color(TextFormatting.GOLD).get();
        ITextComponent separator = TextComponents.translation("tesseract.tesseract.highlight.channel_info.separator").get();
        ITextComponent channelName = channel == null ? TextComponents.translation("tesseract.tesseract.highlight.channel_info.inactive").color(TextFormatting.DARK_GRAY).italic().get() : TextComponents.string(channel.name).get();
        return TextComponents.translation("tesseract.tesseract.highlight.channel_info", channelType, separator, channelName).format();
    }
}
