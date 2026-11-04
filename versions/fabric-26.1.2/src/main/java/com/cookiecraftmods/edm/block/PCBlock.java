
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

public class PCBlock extends Block {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public PCBlock() {
		super(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "pc"))).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.METAL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
			default -> Shapes.or(box(5, 0.5, 1, 11, 14.5, 15), box(10, 0, 13, 10.5, 0.5, 14), box(5.5, 0, 13, 6, 0.5, 14), box(5.5, 0, 2, 6, 0.5, 3), box(10, 0, 2, 10.5, 0.5, 3));
			case NORTH -> Shapes.or(box(5, 0.5, 1, 11, 14.5, 15), box(5.5, 0, 2, 6, 0.5, 3), box(10, 0, 2, 10.5, 0.5, 3), box(10, 0, 13, 10.5, 0.5, 14), box(5.5, 0, 13, 6, 0.5, 14));
			case EAST -> Shapes.or(box(1, 0.5, 5, 15, 14.5, 11), box(13, 0, 5.5, 14, 0.5, 6), box(13, 0, 10, 14, 0.5, 10.5), box(2, 0, 10, 3, 0.5, 10.5), box(2, 0, 5.5, 3, 0.5, 6));
			case WEST -> Shapes.or(box(1, 0.5, 5, 15, 14.5, 11), box(2, 0, 10, 3, 0.5, 10.5), box(2, 0, 5.5, 3, 0.5, 6), box(13, 0, 5.5, 14, 0.5, 6), box(13, 0, 10, 14, 0.5, 10.5));
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
