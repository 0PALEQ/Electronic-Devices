
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

public class ConsoleBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

	public ConsoleBlock() {
		super(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ElectronicDeviceMod.MOD_ID, "console"))).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.METAL).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(7, 0, 4, 14, 0.75, 12), createCuboidShape(7, 1, 4, 14, 1.75, 12), createCuboidShape(7, 0.75, 4, 14, 1, 11.75));
			case NORTH -> VoxelShapes.union(createCuboidShape(2, 0, 4, 9, 0.75, 12), createCuboidShape(2, 1, 4, 9, 1.75, 12), createCuboidShape(2, 0.75, 4.25, 9, 1, 12));
			case EAST -> VoxelShapes.union(createCuboidShape(4, 0, 2, 12, 0.75, 9), createCuboidShape(4, 1, 2, 12, 1.75, 9), createCuboidShape(4, 0.75, 2, 11.75, 1, 9));
			case WEST -> VoxelShapes.union(createCuboidShape(4, 0, 7, 12, 0.75, 14), createCuboidShape(4, 1, 7, 12, 1.75, 14), createCuboidShape(4.25, 0.75, 7, 12, 1, 14));
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
