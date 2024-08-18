package com.supermartijn642.tesseract.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlock;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
@WailaPlugin("tesseract")
public class TesseractWailaPlugin implements IBlockComponentProvider, IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration){
        registration.registerBlockComponent(this, TesseractBlock.class);
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig pluginConfig){
                // Prevent Jade from showing the energy and fluid capability data
                tooltip.remove(ResourceLocation.withDefaultNamespace("fe"));
                tooltip.remove(ResourceLocation.withDefaultNamespace("fluid"));
            }

            @Override
            public ResourceLocation getUid(){
                return ResourceLocation.fromNamespaceAndPath("tesseract", "remove_default");
            }

            @Override
            public int getDefaultPriority(){
                return TooltipPosition.TAIL;
            }
        }, TesseractBlock.class);
    }

    @Override
    public ResourceLocation getUid(){
        return ResourceLocation.fromNamespaceAndPath("tesseract", "tesseract");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config){
        BlockEntity entity = accessor.getBlockEntity();
        if(entity instanceof TesseractBlockEntity){
            TesseractBlockEntity tesseract = (TesseractBlockEntity)entity;

            tooltip.add(TextComponents.translation("tesseract.tesseract.highlight.channels").get());
            tooltip.add(formatChannelInfo(EnumChannelType.ITEMS, tesseract.getChannelId(EnumChannelType.ITEMS)));
            tooltip.add(formatChannelInfo(EnumChannelType.ENERGY, tesseract.getChannelId(EnumChannelType.ENERGY)));
            tooltip.add(formatChannelInfo(EnumChannelType.FLUID, tesseract.getChannelId(EnumChannelType.FLUID)));
            if(tesseract.isBlockedByRedstone())
                tooltip.add(TextComponents.translation("tesseract.tesseract.highlight.redstone_blocked").color(ChatFormatting.RED).get());
        }
    }

    private static Component formatChannelInfo(EnumChannelType type, int channelId){
        Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, channelId);
        Component channelType = TextComponents.fromTextComponent(type.getTranslation()).color(ChatFormatting.GOLD).get();
        Component separator = TextComponents.translation("tesseract.tesseract.highlight.channel_info.separator").get();
        Component channelName = channel == null ? TextComponents.translation("tesseract.tesseract.highlight.channel_info.inactive").color(ChatFormatting.DARK_GRAY).italic().get() : TextComponents.string(channel.name).get();
        return TextComponents.translation("tesseract.tesseract.highlight.channel_info", channelType, separator, channelName).get();
    }
}
