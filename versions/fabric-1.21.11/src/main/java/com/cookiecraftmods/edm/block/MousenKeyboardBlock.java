
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

public class MousenKeyboardBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

	public MousenKeyboardBlock() {
		super(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ElectronicDeviceMod.MOD_ID, "mousen_keyboard"))).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().solidBlock((bs, br, bp) -> false));
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
			default -> VoxelShapes.union(createCuboidShape(1, 0, 6, 12, 0.5, 11), createCuboidShape(13.1, 0, 7, 14.9, 0.75, 10), createCuboidShape(13.9, 0.75, 7.5, 14.1, 0.95, 8.3));
			case NORTH -> VoxelShapes.union(createCuboidShape(4, 0, 5, 15, 0.5, 10), createCuboidShape(1.1, 0, 6, 2.9, 0.75, 9), createCuboidShape(1.9, 0.75, 7.7, 2.1, 0.95, 8.5));
			case EAST -> VoxelShapes.union(createCuboidShape(6, 0, 4, 11, 0.5, 15), createCuboidShape(7, 0, 1.1, 10, 0.75, 2.9), createCuboidShape(7.5, 0.75, 1.9, 8.3, 0.95, 2.1));
			case WEST -> VoxelShapes.union(createCuboidShape(5, 0, 1, 10, 0.5, 12), createCuboidShape(6, 0, 13.1, 9, 0.75, 14.9), createCuboidShape(7.7, 0.75, 13.9, 8.5, 0.95, 14.1));
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
