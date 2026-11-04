
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class GamesBlock extends Block {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public GamesBlock() {
		super(AbstractBlock.Settings.create().instrument(Instrument.BASEDRUM).sounds(BlockSoundGroup.GRAVEL).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
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
			default -> VoxelShapes.union(createCuboidShape(6.5, 0.5, 6.5, 10.5, 1, 10.5), createCuboidShape(6.875, 0, 6.875, 10.875, 0.5, 10.875), createCuboidShape(6.625, 1, 6.875, 10.625, 1.5, 10.875), createCuboidShape(6.375, 1.5, 6.875, 10.375, 2, 10.875));
			case NORTH -> VoxelShapes.union(createCuboidShape(5.5, 0.5, 5.5, 9.5, 1, 9.5), createCuboidShape(5.125, 0, 5.125, 9.125, 0.5, 9.125), createCuboidShape(5.375, 1, 5.125, 9.375, 1.5, 9.125), createCuboidShape(5.625, 1.5, 5.125, 9.625, 2, 9.125));
			case EAST -> VoxelShapes.union(createCuboidShape(6.5, 0.5, 5.5, 10.5, 1, 9.5), createCuboidShape(6.875, 0, 5.125, 10.875, 0.5, 9.125), createCuboidShape(6.875, 1, 5.375, 10.875, 1.5, 9.375), createCuboidShape(6.875, 1.5, 5.625, 10.875, 2, 9.625));
			case WEST -> VoxelShapes.union(createCuboidShape(5.5, 0.5, 6.5, 9.5, 1, 10.5), createCuboidShape(5.125, 0, 6.875, 9.125, 0.5, 10.875), createCuboidShape(5.125, 1, 6.625, 9.125, 1.5, 10.625), createCuboidShape(5.125, 1.5, 6.375, 9.125, 2, 10.375));
		};
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING);
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
}
