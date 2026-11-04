
package com.cookiecraftmods.edm.block;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MountedTVBlock extends Block implements EntityBlock, TvDevice {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public MountedTVBlock() {
		super(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ElectronicDeviceMod.MOD_ID, "mounted_tv"))).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(1f, 10f).noOcclusion().emissiveRendering((bs, br, bp) -> true)
				.isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DISPLAY_STATE, 0));
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
			default -> Shapes.or(box(-8, 1, 1.25, 24, 19, 2.25), box(-6, 3, 0.75, 22, 14, 1.25), box(2, 4, 0, 14, 13, 0.5), box(12.5, 11.5, 0.5, 13.75, 12.75, 0.75), box(2.5, 11.5, 0.5, 3.75, 12.75, 0.75), box(2.5, 4.5, 0.5, 3.75, 5.75, 0.75),
					box(12.5, 4.5, 0.5, 13.75, 5.75, 0.75), box(19.5, 0.75, 1.5, 20.5, 1, 2), box(19.90937, 0.80313, 1.52187, 20.09062, 0.95313, 2.02187), box(-7.75, 1.25, 2.25, 23.75, 18.75, 2.27));
			case NORTH -> Shapes.or(box(-8, 1, 13.75, 24, 19, 14.75), box(-6, 3, 14.75, 22, 14, 15.25), box(2, 4, 15.5, 14, 13, 16), box(2.25, 11.5, 15.25, 3.5, 12.75, 15.5), box(12.25, 11.5, 15.25, 13.5, 12.75, 15.5),
					box(12.25, 4.5, 15.25, 13.5, 5.75, 15.5), box(2.25, 4.5, 15.25, 3.5, 5.75, 15.5), box(-4.5, 0.75, 14, -3.5, 1, 14.5), box(-4.09062, 0.80313, 13.97813, -3.90937, 0.95313, 14.47813), box(-7.75, 1.25, 13.73, 23.75, 18.75, 13.75));
			case EAST -> Shapes.or(box(1.25, 1, -8, 2.25, 19, 24), box(0.75, 3, -6, 1.25, 14, 22), box(0, 4, 2, 0.5, 13, 14), box(0.5, 11.5, 2.25, 0.75, 12.75, 3.5), box(0.5, 11.5, 12.25, 0.75, 12.75, 13.5), box(0.5, 4.5, 12.25, 0.75, 5.75, 13.5),
					box(0.5, 4.5, 2.25, 0.75, 5.75, 3.5), box(1.5, 0.75, -4.5, 2, 1, -3.5), box(1.52187, 0.80313, -4.09062, 2.02187, 0.95313, -3.90937), box(2.25, 1.25, -7.75, 2.27, 18.75, 23.75));
			case WEST -> Shapes.or(box(13.75, 1, -8, 14.75, 19, 24), box(14.75, 3, -6, 15.25, 14, 22), box(15.5, 4, 2, 16, 13, 14), box(15.25, 11.5, 12.5, 15.5, 12.75, 13.75), box(15.25, 11.5, 2.5, 15.5, 12.75, 3.75),
					box(15.25, 4.5, 2.5, 15.5, 5.75, 3.75), box(15.25, 4.5, 12.5, 15.5, 5.75, 13.75), box(14, 0.75, 19.5, 14.5, 1, 20.5), box(13.97813, 0.80313, 19.90937, 14.47813, 0.95313, 20.09062), box(13.73, 1.25, -7.75, 13.75, 18.75, 23.75));
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, DISPLAY_STATE);
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

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return TvDevice.onUse(state, world, pos, player, hit);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.setPlacedBy(world, pos, state, placer, itemStack);
		TvDevice.onPlaced(world, pos, placer, itemStack);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TvBlockEntity(pos, state);
	}
}
