
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import org.jetbrains.annotations.Nullable;

public class TVBlock extends Block implements BlockEntityProvider, TvDevice {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public TVBlock() {
		super(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(DISPLAY_STATE, 0));
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			default -> VoxelShapes.union(createCuboidShape(-8, 1, 7, 24, 19, 8), createCuboidShape(1, 0, 3, 2, 1, 12), createCuboidShape(14, 0, 3, 15, 1, 12), createCuboidShape(14, 1, 6, 15, 7, 7), createCuboidShape(1, 1, 6, 2, 7, 7));
			case NORTH -> VoxelShapes.union(createCuboidShape(-8, 1, 8, 24, 19, 9), createCuboidShape(14, 0, 4, 15, 1, 13), createCuboidShape(1, 0, 4, 2, 1, 13), createCuboidShape(1, 1, 9, 2, 7, 10), createCuboidShape(14, 1, 9, 15, 7, 10));
			case EAST -> VoxelShapes.union(createCuboidShape(7, 1, -8, 8, 19, 24), createCuboidShape(3, 0, 14, 12, 1, 15), createCuboidShape(3, 0, 1, 12, 1, 2), createCuboidShape(6, 1, 1, 7, 7, 2), createCuboidShape(6, 1, 14, 7, 7, 15));
			case WEST -> VoxelShapes.union(createCuboidShape(8, 1, -8, 9, 19, 24), createCuboidShape(4, 0, 1, 13, 1, 2), createCuboidShape(4, 0, 14, 13, 1, 15), createCuboidShape(9, 1, 14, 10, 7, 15), createCuboidShape(9, 1, 1, 10, 7, 2));
		};
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING, DISPLAY_STATE);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return super.getPlacementState(context).with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	public BlockState rotate(BlockState state, BlockRotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(FACING)));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		return TvDevice.onUse(state, world, pos, player, hit);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		TvDevice.onPlaced(world, pos, placer, itemStack);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TvBlockEntity(pos, state);
	}
}
