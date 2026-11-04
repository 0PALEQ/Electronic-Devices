
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

public class PCBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

	public PCBlock() {
		super(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ElectronicDeviceMod.MOD_ID, "pc"))).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.METAL).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(5, 0.5, 1, 11, 14.5, 15), createCuboidShape(10, 0, 13, 10.5, 0.5, 14), createCuboidShape(5.5, 0, 13, 6, 0.5, 14), createCuboidShape(5.5, 0, 2, 6, 0.5, 3), createCuboidShape(10, 0, 2, 10.5, 0.5, 3));
			case NORTH -> VoxelShapes.union(createCuboidShape(5, 0.5, 1, 11, 14.5, 15), createCuboidShape(5.5, 0, 2, 6, 0.5, 3), createCuboidShape(10, 0, 2, 10.5, 0.5, 3), createCuboidShape(10, 0, 13, 10.5, 0.5, 14), createCuboidShape(5.5, 0, 13, 6, 0.5, 14));
			case EAST -> VoxelShapes.union(createCuboidShape(1, 0.5, 5, 15, 14.5, 11), createCuboidShape(13, 0, 5.5, 14, 0.5, 6), createCuboidShape(13, 0, 10, 14, 0.5, 10.5), createCuboidShape(2, 0, 10, 3, 0.5, 10.5), createCuboidShape(2, 0, 5.5, 3, 0.5, 6));
			case WEST -> VoxelShapes.union(createCuboidShape(1, 0.5, 5, 15, 14.5, 11), createCuboidShape(2, 0, 10, 3, 0.5, 10.5), createCuboidShape(2, 0, 5.5, 3, 0.5, 6), createCuboidShape(13, 0, 5.5, 14, 0.5, 6), createCuboidShape(13, 0, 10, 14, 0.5, 10.5));
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
