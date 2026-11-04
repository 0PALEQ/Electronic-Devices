
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

public class Laptop1Block extends Block {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public Laptop1Block() {
		super(AbstractBlock.Settings.create().instrument(Instrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(1, 0, 4.25, 15, 0.5, 14.25), createCuboidShape(1, 0.25, 1.75, 15, 10.25, 2.25), createCuboidShape(12.5, 0.275, 3.9, 14, 0.775, 4.4), createCuboidShape(2, 0.275, 3.9, 3.5, 0.775, 4.4));
			case NORTH -> VoxelShapes.union(createCuboidShape(1, 0, 1.75, 15, 0.5, 11.75), createCuboidShape(1, 0.25, 13.75, 15, 10.25, 14.25), createCuboidShape(2, 0.275, 11.6, 3.5, 0.775, 12.1), createCuboidShape(12.5, 0.275, 11.6, 14, 0.775, 12.1));
			case EAST -> VoxelShapes.union(createCuboidShape(4.25, 0, 1, 14.25, 0.5, 15), createCuboidShape(1.75, 0.25, 1, 2.25, 10.25, 15), createCuboidShape(3.9, 0.275, 2, 4.4, 0.775, 3.5), createCuboidShape(3.9, 0.275, 12.5, 4.4, 0.775, 14));
			case WEST -> VoxelShapes.union(createCuboidShape(1.75, 0, 1, 11.75, 0.5, 15), createCuboidShape(13.75, 0.25, 1, 14.25, 10.25, 15), createCuboidShape(11.6, 0.275, 12.5, 12.1, 0.775, 14), createCuboidShape(11.6, 0.275, 2, 12.1, 0.775, 3.5));
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
