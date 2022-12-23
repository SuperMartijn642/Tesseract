package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlock extends BaseBlock implements EntityHoldingBlock {

    public TesseractBlock(){
        super(false, BlockProperties.create(Material.HEAVY_METAL, MaterialColor.COLOR_GREEN).sound(SoundType.METAL).destroyTime(1.5f).explosionResistance(6).noOcclusion());
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        if(level.isClientSide)
            TesseractClient.openScreen(pos);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return new TesseractBlockEntity();
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader level, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, EntityType<?> entityType){
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof TesseractBlockEntity){
            ((TesseractBlockEntity)entity).setPowered(level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above()));
            ((TesseractBlockEntity)entity).onNeighborChanged(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof TesseractBlockEntity)
            ((TesseractBlockEntity)entity).onReplaced();
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("tesseract.tesseract.info").color(TextFormatting.AQUA).get());
    }
}
