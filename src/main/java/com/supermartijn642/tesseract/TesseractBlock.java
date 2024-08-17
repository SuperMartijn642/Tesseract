package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlock extends BaseBlock implements EntityHoldingBlock {

    public TesseractBlock(){
        super(false, BlockProperties.create().mapColor(MapColor.COLOR_GREEN).sound(SoundType.METAL).destroyTime(1.5f).explosionResistance(6).noOcclusion());
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(level.isClientSide)
            TesseractClient.openScreen(pos);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return new TesseractBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof TesseractBlockEntity){
            ((TesseractBlockEntity)entity).setPowered(level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above()));
            ((TesseractBlockEntity)entity).onNeighborChanged(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof TesseractBlockEntity)
            ((TesseractBlockEntity)entity).onReplaced();
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("tesseract.tesseract.info").color(ChatFormatting.AQUA).get());
    }
}
