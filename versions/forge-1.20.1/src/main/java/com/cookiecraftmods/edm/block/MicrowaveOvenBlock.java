
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

public class MicrowaveOvenBlock extends Block {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public MicrowaveOvenBlock() {
		super(AbstractBlock.Settings.create().instrument(Instrument.BASEDRUM).sounds(BlockSoundGroup.METAL).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(11, 2, 13, 12, 3, 15), createCuboidShape(11, 9, 13, 12, 10, 15), createCuboidShape(11, 3, 14, 12, 9, 15), createCuboidShape(0, 1, 1, 16, 11, 13));
			case NORTH -> VoxelShapes.union(createCuboidShape(4, 2, 1, 5, 3, 3), createCuboidShape(4, 9, 1, 5, 10, 3), createCuboidShape(4, 3, 1, 5, 9, 2), createCuboidShape(0, 1, 3, 16, 11, 15));
			case EAST -> VoxelShapes.union(createCuboidShape(13, 2, 4, 15, 3, 5), createCuboidShape(13, 9, 4, 15, 10, 5), createCuboidShape(14, 3, 4, 15, 9, 5), createCuboidShape(1, 1, 0, 13, 11, 16));
			case WEST -> VoxelShapes.union(createCuboidShape(1, 2, 11, 3, 3, 12), createCuboidShape(1, 9, 11, 3, 10, 12), createCuboidShape(1, 3, 11, 2, 9, 12), createCuboidShape(3, 1, 0, 15, 11, 16));
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
