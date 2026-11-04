package com.cookiecraftmods.edm.block.entity;

import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.media.BoundTv;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TvRemoteBlockEntity extends BlockEntity {
	private BoundTv boundTv;

	public TvRemoteBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TV_REMOTE, pos, state);
	}

	public BoundTv getBoundTv() {
		return boundTv;
	}

	public void setBoundTv(BoundTv boundTv) {
		this.boundTv = boundTv;
		setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		boundTv = BoundTv.read(view);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		if (boundTv != null) boundTv.write(view);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return saveWithFullMetadata(registryLookup);
	}
}
