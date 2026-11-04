
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

public class BlackRouterBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public BlackRouterBlock() {
		super(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "black_router"))).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
			default -> Shapes.or(box(3, 0, 4, 13, 2, 10), box(10.75, 0.75, 3.5, 11.25, 1.25, 4), box(4.75, 0.75, 3.5, 5.25, 1.25, 4), box(11.65, 0, 2.9, 12.35, 7, 3.5), box(3.65, 0, 2.9, 4.35, 7, 3.5));
			case NORTH -> Shapes.or(box(3, 0, 6, 13, 2, 12), box(4.75, 0.75, 12, 5.25, 1.25, 12.5), box(10.75, 0.75, 12, 11.25, 1.25, 12.5), box(3.65, 0, 12.5, 4.35, 7, 13.1), box(11.65, 0, 12.5, 12.35, 7, 13.1));
			case EAST -> Shapes.or(box(4, 0, 3, 10, 2, 13), box(3.5, 0.75, 4.75, 4, 1.25, 5.25), box(3.5, 0.75, 10.75, 4, 1.25, 11.25), box(2.9, 0, 3.65, 3.5, 7, 4.35), box(2.9, 0, 11.65, 3.5, 7, 12.35));
			case WEST -> Shapes.or(box(6, 0, 3, 12, 2, 13), box(12, 0.75, 10.75, 12.5, 1.25, 11.25), box(12, 0.75, 4.75, 12.5, 1.25, 5.25), box(12.5, 0, 11.65, 13.1, 7, 12.35), box(12.5, 0, 3.65, 13.1, 7, 4.35));
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
