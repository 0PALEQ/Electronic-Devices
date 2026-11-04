
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.NoteBlockInstrument;
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

public class MonitorWithMousenKeyboardBlock extends Block {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public MonitorWithMousenKeyboardBlock() {
		super(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(1, 0, 8, 12, 0.5, 13), createCuboidShape(13.1, 0, 9, 14.9, 0.75, 12), createCuboidShape(4, 0, 3, 12, 0.25, 7), createCuboidShape(7.5, 0.25, 4.5, 8.5, 12.25, 5.5), createCuboidShape(7, 8, 5.75, 9, 10, 6.25), createCuboidShape(7.85, 8.75, 5.5, 8.15, 9.25, 5.75),
					createCuboidShape(1, 5, 6.75, 15, 13, 7), createCuboidShape(5, 7, 6.25, 11, 11, 6.75));
			case NORTH -> VoxelShapes.union(createCuboidShape(4, 0, 3, 15, 0.5, 8), createCuboidShape(1.1, 0, 4, 2.9, 0.75, 7), createCuboidShape(4, 0, 9, 12, 0.25, 13), createCuboidShape(7.5, 0.25, 10.5, 8.5, 12.25, 11.5), createCuboidShape(7, 8, 9.75, 9, 10, 10.25), createCuboidShape(7.85, 8.75, 10.25, 8.15, 9.25, 10.5),
					createCuboidShape(1, 5, 9, 15, 13, 9.25), createCuboidShape(5, 7, 9.25, 11, 11, 9.75));
			case EAST -> VoxelShapes.union(createCuboidShape(8, 0, 4, 13, 0.5, 15), createCuboidShape(9, 0, 1.1, 12, 0.75, 2.9), createCuboidShape(3, 0, 4, 7, 0.25, 12), createCuboidShape(4.5, 0.25, 7.5, 5.5, 12.25, 8.5), createCuboidShape(5.75, 8, 7, 6.25, 10, 9), createCuboidShape(5.5, 8.75, 7.85, 5.75, 9.25, 8.15),
					createCuboidShape(6.75, 5, 1, 7, 13, 15), createCuboidShape(6.25, 7, 5, 6.75, 11, 11));
			case WEST -> VoxelShapes.union(createCuboidShape(3, 0, 1, 8, 0.5, 12), createCuboidShape(4, 0, 13.1, 7, 0.75, 14.9), createCuboidShape(9, 0, 4, 13, 0.25, 12), createCuboidShape(10.5, 0.25, 7.5, 11.5, 12.25, 8.5), createCuboidShape(9.75, 8, 7, 10.25, 10, 9), createCuboidShape(10.25, 8.75, 7.85, 10.5, 9.25, 8.15),
					createCuboidShape(9, 5, 1, 9.25, 13, 15), createCuboidShape(9.25, 7, 5, 9.75, 11, 11));
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
