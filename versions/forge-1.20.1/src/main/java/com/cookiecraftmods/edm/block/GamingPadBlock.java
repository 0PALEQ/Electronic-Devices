
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

public class GamingPadBlock extends Block {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public GamingPadBlock() {
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
			default -> VoxelShapes.union(createCuboidShape(6.5, 0.14063, 6.5, 9.5, 1.14063, 7.5), createCuboidShape(5.75, 0, 6, 7, 1.25, 8.5), createCuboidShape(9, 0, 6, 10.25, 1.25, 8.5));
			case NORTH -> VoxelShapes.union(createCuboidShape(6.5, 0.14063, 8.5, 9.5, 1.14063, 9.5), createCuboidShape(9, 0, 7.5, 10.25, 1.25, 10), createCuboidShape(5.75, 0, 7.5, 7, 1.25, 10));
			case EAST -> VoxelShapes.union(createCuboidShape(6.5, 0.14063, 6.5, 7.5, 1.14063, 9.5), createCuboidShape(6, 0, 9, 8.5, 1.25, 10.25), createCuboidShape(6, 0, 5.75, 8.5, 1.25, 7));
			case WEST -> VoxelShapes.union(createCuboidShape(8.5, 0.14063, 6.5, 9.5, 1.14063, 9.5), createCuboidShape(7.5, 0, 5.75, 10, 1.25, 7), createCuboidShape(7.5, 0, 9, 10, 1.25, 10.25));
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
