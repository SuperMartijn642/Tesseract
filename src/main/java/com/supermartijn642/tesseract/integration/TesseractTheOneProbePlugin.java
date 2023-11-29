package com.supermartijn642.tesseract.integration;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.function.Function;

/**
 * Created 7/13/2021 by SuperMartijn642
 */
public class TesseractTheOneProbePlugin {

    public static void interModEnqueue(){
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "com.supermartijn642.tesseract.integration.TesseractTheOneProbePlugin$ProbeInfoProvider");
    }

    public static class ProbeInfoProvider implements IProbeInfoProvider, IProbeConfigProvider, Function<ITheOneProbe,Void> {

        @Override
        public Void apply(ITheOneProbe theOneProbe){
            theOneProbe.registerProvider(this);
            theOneProbe.registerProbeConfigProvider(this);
            return null;
        }

        @Override
        public String getID(){
            return "tesseract";
        }

        @Override
        public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer player, World level, IBlockState state, IProbeHitData probeHitData){
            TileEntity entity = level.getTileEntity(probeHitData.getPos());
            if(entity instanceof TesseractBlockEntity){
                TesseractBlockEntity tesseract = (TesseractBlockEntity)entity;

                probeInfo.text(TextComponents.translation("tesseract.tesseract.highlight.channels").format());
                probeInfo.text(formatChannelInfo(EnumChannelType.ITEMS, tesseract.getChannelId(EnumChannelType.ITEMS)));
                probeInfo.text(formatChannelInfo(EnumChannelType.ENERGY, tesseract.getChannelId(EnumChannelType.ENERGY)));
                probeInfo.text(formatChannelInfo(EnumChannelType.FLUID, tesseract.getChannelId(EnumChannelType.FLUID)));
                if(tesseract.isBlockedByRedstone())
                    probeInfo.text(TextStyleClass.ERROR + TextComponents.translation("tesseract.tesseract.highlight.redstone_blocked").format());
            }
        }

        private static String formatChannelInfo(EnumChannelType type, int channelId){
            Channel channel = TesseractChannelManager.CLIENT.getChannelById(type, channelId);
            String channelType = TextStyleClass.WARNING + TextComponents.fromTextComponent(type.getTranslation()).format();
            String separator = TextStyleClass.INFO + TextComponents.translation("tesseract.tesseract.highlight.channel_info.separator").format();
            String channelName = channel == null ? TextStyleClass.LABEL + TextComponents.translation("tesseract.tesseract.highlight.channel_info.inactive").format() : TextStyleClass.INFO + channel.name;
            return TextComponents.translation("tesseract.tesseract.highlight.channel_info", channelType, separator, channelName).format();
        }

        @Override
        public void getProbeConfig(IProbeConfig probeConfig, EntityPlayer player, World level, Entity entity, IProbeHitEntityData probeHitEntityData){
        }

        @Override
        public void getProbeConfig(IProbeConfig probeConfig, EntityPlayer player, World level, IBlockState state, IProbeHitData probeHitData){
            // Prevent The One Probe from showing the energy and fluid capability data
            if(state.getBlock() == Tesseract.tesseract){
                probeConfig.setRFMode(0);
                probeConfig.setTankMode(0);
            }
        }
    }
}
