package com.supermartijn642.tesseract;

import com.supermartijn642.core.ToolType;
import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class BlockTesseract extends BaseBlock implements ITileEntityProvider {

    public BlockTesseract(){
        super("tesseract", false, Properties.create(Material.ANVIL, MapColor.GREEN).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).hardnessAndResistance(1.5F, 6.0F));
        this.setCreativeTab(CreativeTabs.SEARCH);
        this.translucent = true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            ClientProxy.openScreen(pos);

        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta){
        return new TesseractTile();
    }

    @Override
    public boolean isFullBlock(IBlockState state){
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state){
        return false;
    }

    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn){
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TesseractTile){
            ((TesseractTile)tile).setPowered(worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up()));
            ((TesseractTile)tile).onNeighborChanged(fromPos);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state){
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TesseractTile)
            ((TesseractTile)tile).onReplaced();
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced){
        tooltip.add(new TextComponentTranslation("tesseract.tesseract.info").setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());
    }
}
