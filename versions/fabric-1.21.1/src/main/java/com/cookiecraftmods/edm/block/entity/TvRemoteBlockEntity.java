package com.cookiecraftmods.edm.block.entity;

import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.media.BoundTv;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

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
		markDirty();
		if (world != null) world.updateListeners(pos, getCachedState(), getCachedState(), 3);
	}

	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		boundTv = BoundTv.read(nbt);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		if (boundTv != null) boundTv.write(nbt);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return createNbtWithIdentifyingData(registryLookup);
	}
}
