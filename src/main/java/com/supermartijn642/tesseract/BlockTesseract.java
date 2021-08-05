package com.supermartijn642.tesseract;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class BlockTesseract extends BaseBlock implements EntityBlock {

    public BlockTesseract(){
        super("tesseract", false, Block.Properties.of(Material.HEAVY_METAL, MaterialColor.COLOR_GREEN).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).strength(1.5F, 6.0F));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit){
        if(worldIn.isClientSide)
            ClientProxy.openScreen(pos);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new TesseractTile(pos, state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos){
        return Shapes.empty();
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, @Nullable EntityType<?> entityType){
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof TesseractTile){
            ((TesseractTile)tile).setPowered(worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(pos.above()));
            ((TesseractTile)tile).onNeighborChanged(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof TesseractTile)
            ((TesseractTile)tile).onReplaced();
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(new TranslatableComponent("tesseract.tesseract.info").withStyle(ChatFormatting.AQUA));
    }
}
