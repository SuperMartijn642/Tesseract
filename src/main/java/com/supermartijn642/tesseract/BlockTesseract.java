package com.supermartijn642.tesseract;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class BlockTesseract extends Block implements ITileEntityProvider {

    public BlockTesseract(){
        super(Material.ANVIL, MapColor.GREEN);
        this.setUnlocalizedName(Tesseract.MODID + ":tesseract");
        this.setRegistryName("tesseract");
        this.setCreativeTab(CreativeTabs.SEARCH);
        this.translucent = true;
        this.setSoundType(SoundType.METAL);
        this.setHardness(1.5f);
        this.setResistance(6);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            ClientProxy.openScreen(pos);

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
        if(tile instanceof TesseractTile)
            ((TesseractTile)tile).setPowered(worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up()));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state){
        return false;
    }
}
