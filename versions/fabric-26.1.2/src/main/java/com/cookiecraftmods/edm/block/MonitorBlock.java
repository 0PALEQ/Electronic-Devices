
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

public class MonitorBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public MonitorBlock() {
		super(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "monitor"))).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
			default -> Shapes.or(box(4, 0, 3, 12, 0.25, 7), box(7.5, 0.25, 4.5, 8.5, 12.25, 5.5), box(7, 8, 5.75, 9, 10, 6.25), box(7.85, 8.75, 5.5, 8.15, 9.25, 5.75), box(1, 5, 6.75, 15, 13, 7), box(5, 7, 6.25, 11, 11, 6.75));
			case NORTH -> Shapes.or(box(4, 0, 9, 12, 0.25, 13), box(7.5, 0.25, 10.5, 8.5, 12.25, 11.5), box(7, 8, 9.75, 9, 10, 10.25), box(7.85, 8.75, 10.25, 8.15, 9.25, 10.5), box(1, 5, 9, 15, 13, 9.25), box(5, 7, 9.25, 11, 11, 9.75));
			case EAST -> Shapes.or(box(3, 0, 4, 7, 0.25, 12), box(4.5, 0.25, 7.5, 5.5, 12.25, 8.5), box(5.75, 8, 7, 6.25, 10, 9), box(5.5, 8.75, 7.85, 5.75, 9.25, 8.15), box(6.75, 5, 1, 7, 13, 15), box(6.25, 7, 5, 6.75, 11, 11));
			case WEST -> Shapes.or(box(9, 0, 4, 13, 0.25, 12), box(10.5, 0.25, 7.5, 11.5, 12.25, 8.5), box(9.75, 8, 7, 10.25, 10, 9), box(10.25, 8.75, 7.85, 10.5, 9.25, 8.15), box(9, 5, 1, 9.25, 13, 15), box(9.25, 7, 5, 9.75, 11, 11));
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
