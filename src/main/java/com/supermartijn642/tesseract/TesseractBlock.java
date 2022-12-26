package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlock extends BaseBlock implements EntityHoldingBlock {

    public TesseractBlock(){
        super(false, BlockProperties.create(Material.ANVIL, MapColor.GREEN).sound(SoundType.METAL).destroyTime(1.5f).explosionResistance(6).noOcclusion());
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(level.isRemote)
            TesseractClient.openScreen(pos);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return new TesseractBlockEntity();
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
    public boolean canEntitySpawn(IBlockState state, Entity entity){
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World level, BlockPos pos, Block blockIn, BlockPos fromPos){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof TesseractBlockEntity){
            ((TesseractBlockEntity)entity).setPowered(level.isBlockPowered(pos) || level.isBlockPowered(pos.up()));
            ((TesseractBlockEntity)entity).onNeighborChanged(fromPos);
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
    public void breakBlock(World level, BlockPos pos, IBlockState state){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof TesseractBlockEntity)
            ((TesseractBlockEntity)entity).onReplaced();
        super.breakBlock(level, pos, state);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("tesseract.tesseract.info").color(TextFormatting.AQUA).get());
    }
}
