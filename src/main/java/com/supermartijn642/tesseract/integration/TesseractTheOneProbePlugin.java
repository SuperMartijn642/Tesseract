package com.supermartijn642.tesseract.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Function;

/**
 * Created 7/13/2021 by SuperMartijn642
 */
public class TesseractTheOneProbePlugin {

    public static void interModEnqueue(InterModEnqueueEvent e){
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new ProbeInfoProvider());
    }

    public static class ProbeInfoProvider implements IProbeInfoProvider, IProbeConfigProvider, Function<ITheOneProbe,Void> {

        @Override
        public Void apply(ITheOneProbe theOneProbe){
            theOneProbe.registerProvider(this);
            theOneProbe.registerProbeConfigProvider(this);
            return null;
        }

        @Override
        public ResourceLocation getID(){
            return new ResourceLocation("tesseract", "tesseract");
        }

        @Override
        public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level level, BlockState state, IProbeHitData probeHitData){
            BlockEntity entity = level.getBlockEntity(probeHitData.getPos());
            if(entity instanceof TesseractBlockEntity){
                TesseractBlockEntity tesseract = (TesseractBlockEntity)entity;

                probeInfo.text(TextComponents.translation("tesseract.tesseract.highlight.channels").get());
                probeInfo.text(formatChannelInfo(EnumChannelType.ITEMS, tesseract.getChannelId(EnumChannelType.ITEMS)));
                probeInfo.text(formatChannelInfo(EnumChannelType.ENERGY, tesseract.getChannelId(EnumChannelType.ENERGY)));
                probeInfo.text(formatChannelInfo(EnumChannelType.FLUID, tesseract.getChannelId(EnumChannelType.FLUID)));
                if(tesseract.isBlockedByRedstone())
                    probeInfo.text(TextComponents.string(TextStyleClass.ERROR.toString()).translation("tesseract.tesseract.highlight.redstone_blocked").get());
            }
        }

        private static Component formatChannelInfo(EnumChannelType type, int channelId){
            Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, channelId);
            Component channelType = TextComponents.string(TextStyleClass.HIGHLIGHTED.toString()).get().append(type.getTranslation());
            Component separator = TextComponents.string(TextStyleClass.INFO.toString()).translation("tesseract.tesseract.highlight.channel_info.separator").get();
            Component channelName = channel == null ? TextComponents.string(TextStyleClass.LABEL.toString()).translation("tesseract.tesseract.highlight.channel_info.inactive").get() : TextComponents.string(TextStyleClass.INFO.toString()).string(channel.name).get();
            return TextComponents.translation("tesseract.tesseract.highlight.channel_info", channelType, separator, channelName).get();
        }

        @Override
        public void getProbeConfig(IProbeConfig probeConfig, Player player, Level level, Entity entity, IProbeHitEntityData probeHitEntityData){
        }

        @Override
        public void getProbeConfig(IProbeConfig probeConfig, Player player, Level level, BlockState state, IProbeHitData probeHitData){
            // Prevent The One Probe from showing the energy and fluid capability data
            if(state.getBlock() == Tesseract.tesseract){
                probeConfig.setRFMode(0);
                probeConfig.setTankMode(0);
            }
        }
    }
}
