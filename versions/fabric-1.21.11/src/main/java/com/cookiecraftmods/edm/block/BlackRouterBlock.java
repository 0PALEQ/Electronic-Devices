
package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class BlackRouterBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

	public BlackRouterBlock() {
		super(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ElectronicDeviceMod.MOD_ID, "black_router"))).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public int getOpacity(BlockState state) {
		return 0;
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			default -> VoxelShapes.union(createCuboidShape(3, 0, 4, 13, 2, 10), createCuboidShape(10.75, 0.75, 3.5, 11.25, 1.25, 4), createCuboidShape(4.75, 0.75, 3.5, 5.25, 1.25, 4), createCuboidShape(11.65, 0, 2.9, 12.35, 7, 3.5), createCuboidShape(3.65, 0, 2.9, 4.35, 7, 3.5));
			case NORTH -> VoxelShapes.union(createCuboidShape(3, 0, 6, 13, 2, 12), createCuboidShape(4.75, 0.75, 12, 5.25, 1.25, 12.5), createCuboidShape(10.75, 0.75, 12, 11.25, 1.25, 12.5), createCuboidShape(3.65, 0, 12.5, 4.35, 7, 13.1), createCuboidShape(11.65, 0, 12.5, 12.35, 7, 13.1));
			case EAST -> VoxelShapes.union(createCuboidShape(4, 0, 3, 10, 2, 13), createCuboidShape(3.5, 0.75, 4.75, 4, 1.25, 5.25), createCuboidShape(3.5, 0.75, 10.75, 4, 1.25, 11.25), createCuboidShape(2.9, 0, 3.65, 3.5, 7, 4.35), createCuboidShape(2.9, 0, 11.65, 3.5, 7, 12.35));
			case WEST -> VoxelShapes.union(createCuboidShape(6, 0, 3, 12, 2, 13), createCuboidShape(12, 0.75, 10.75, 12.5, 1.25, 11.25), createCuboidShape(12, 0.75, 4.75, 12.5, 1.25, 5.25), createCuboidShape(12.5, 0, 11.65, 13.1, 7, 12.35), createCuboidShape(12.5, 0, 3.65, 13.1, 7, 4.35));
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
