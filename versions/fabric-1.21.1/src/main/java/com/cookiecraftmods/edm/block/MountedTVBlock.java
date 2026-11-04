
package com.cookiecraftmods.edm.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import org.jetbrains.annotations.Nullable;

public class MountedTVBlock extends Block implements BlockEntityProvider, TvDevice {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public MountedTVBlock() {
		super(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).strength(1f, 10f).nonOpaque().postProcess((bs, br, bp) -> true).emissiveLighting((bs, br, bp) -> true)
				.solidBlock((bs, br, bp) -> false));
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(DISPLAY_STATE, 0));
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
			default -> VoxelShapes.union(createCuboidShape(-8, 1, 1.25, 24, 19, 2.25), createCuboidShape(-6, 3, 0.75, 22, 14, 1.25), createCuboidShape(2, 4, 0, 14, 13, 0.5), createCuboidShape(12.5, 11.5, 0.5, 13.75, 12.75, 0.75), createCuboidShape(2.5, 11.5, 0.5, 3.75, 12.75, 0.75), createCuboidShape(2.5, 4.5, 0.5, 3.75, 5.75, 0.75),
					createCuboidShape(12.5, 4.5, 0.5, 13.75, 5.75, 0.75), createCuboidShape(19.5, 0.75, 1.5, 20.5, 1, 2), createCuboidShape(19.90937, 0.80313, 1.52187, 20.09062, 0.95313, 2.02187), createCuboidShape(-7.75, 1.25, 2.25, 23.75, 18.75, 2.27));
			case NORTH -> VoxelShapes.union(createCuboidShape(-8, 1, 13.75, 24, 19, 14.75), createCuboidShape(-6, 3, 14.75, 22, 14, 15.25), createCuboidShape(2, 4, 15.5, 14, 13, 16), createCuboidShape(2.25, 11.5, 15.25, 3.5, 12.75, 15.5), createCuboidShape(12.25, 11.5, 15.25, 13.5, 12.75, 15.5),
					createCuboidShape(12.25, 4.5, 15.25, 13.5, 5.75, 15.5), createCuboidShape(2.25, 4.5, 15.25, 3.5, 5.75, 15.5), createCuboidShape(-4.5, 0.75, 14, -3.5, 1, 14.5), createCuboidShape(-4.09062, 0.80313, 13.97813, -3.90937, 0.95313, 14.47813), createCuboidShape(-7.75, 1.25, 13.73, 23.75, 18.75, 13.75));
			case EAST -> VoxelShapes.union(createCuboidShape(1.25, 1, -8, 2.25, 19, 24), createCuboidShape(0.75, 3, -6, 1.25, 14, 22), createCuboidShape(0, 4, 2, 0.5, 13, 14), createCuboidShape(0.5, 11.5, 2.25, 0.75, 12.75, 3.5), createCuboidShape(0.5, 11.5, 12.25, 0.75, 12.75, 13.5), createCuboidShape(0.5, 4.5, 12.25, 0.75, 5.75, 13.5),
					createCuboidShape(0.5, 4.5, 2.25, 0.75, 5.75, 3.5), createCuboidShape(1.5, 0.75, -4.5, 2, 1, -3.5), createCuboidShape(1.52187, 0.80313, -4.09062, 2.02187, 0.95313, -3.90937), createCuboidShape(2.25, 1.25, -7.75, 2.27, 18.75, 23.75));
			case WEST -> VoxelShapes.union(createCuboidShape(13.75, 1, -8, 14.75, 19, 24), createCuboidShape(14.75, 3, -6, 15.25, 14, 22), createCuboidShape(15.5, 4, 2, 16, 13, 14), createCuboidShape(15.25, 11.5, 12.5, 15.5, 12.75, 13.75), createCuboidShape(15.25, 11.5, 2.5, 15.5, 12.75, 3.75),
					createCuboidShape(15.25, 4.5, 2.5, 15.5, 5.75, 3.75), createCuboidShape(15.25, 4.5, 12.5, 15.5, 5.75, 13.75), createCuboidShape(14, 0.75, 19.5, 14.5, 1, 20.5), createCuboidShape(13.97813, 0.80313, 19.90937, 14.47813, 0.95313, 20.09062), createCuboidShape(13.73, 1.25, -7.75, 13.75, 18.75, 23.75));
		};
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING, DISPLAY_STATE);
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

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		return TvDevice.onUse(state, world, pos, player, hit);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		TvDevice.onPlaced(world, pos, placer, itemStack);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TvBlockEntity(pos, state);
	}
}
