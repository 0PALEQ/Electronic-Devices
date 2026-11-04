
package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GamesBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public GamesBlock() {
		super(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "games"))).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.GRAVEL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	@Override
	public int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			default -> Shapes.or(box(6.5, 0.5, 6.5, 10.5, 1, 10.5), box(6.875, 0, 6.875, 10.875, 0.5, 10.875), box(6.625, 1, 6.875, 10.625, 1.5, 10.875), box(6.375, 1.5, 6.875, 10.375, 2, 10.875));
			case NORTH -> Shapes.or(box(5.5, 0.5, 5.5, 9.5, 1, 9.5), box(5.125, 0, 5.125, 9.125, 0.5, 9.125), box(5.375, 1, 5.125, 9.375, 1.5, 9.125), box(5.625, 1.5, 5.125, 9.625, 2, 9.125));
			case EAST -> Shapes.or(box(6.5, 0.5, 5.5, 10.5, 1, 9.5), box(6.875, 0, 5.125, 10.875, 0.5, 9.125), box(6.875, 1, 5.375, 10.875, 1.5, 9.375), box(6.875, 1.5, 5.625, 10.875, 2, 9.625));
			case WEST -> Shapes.or(box(5.5, 0.5, 6.5, 9.5, 1, 10.5), box(5.125, 0, 6.875, 9.125, 0.5, 10.875), box(5.125, 1, 6.625, 9.125, 1.5, 10.625), box(5.125, 1.5, 6.375, 9.125, 2, 10.375));
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
}
