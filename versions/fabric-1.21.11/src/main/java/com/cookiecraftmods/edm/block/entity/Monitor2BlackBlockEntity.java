package com.cookiecraftmods.edm.block.entity;

import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.stream.IntStream;

import com.cookiecraftmods.edm.init.ModBlockEntities;

public class Monitor2BlackBlockEntity extends LootableContainerBlockEntity implements SidedInventory {
	private DefaultedList<ItemStack> stacks = DefaultedList.<ItemStack>ofSize(9, ItemStack.EMPTY);

	public Monitor2BlackBlockEntity(BlockPos position, BlockState state) {
		super(ModBlockEntities.MONITOR_2_BLACK, position, state);
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		this.stacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (!this.readLootTable(view)) {
			Inventories.readData(view, this.stacks);
		}
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		if (!this.writeLootTable(view)) {
			Inventories.writeData(view, this.stacks);
		}
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createNbtWithIdentifyingData(registryLookup);
	}

	@Override
	public int size() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	@Override
	public Text getContainerName() {
		return Text.literal("monitor_2_black");
	}

	@Override
	public int getMaxCountPerStack() {
		return 64;
	}

	@Override
	public ScreenHandler createScreenHandler(int id, PlayerInventory inventory) {
		return GenericContainerScreenHandler.createGeneric9x3(id, inventory);
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Monitor 2 Black");
	}

	@Override
	protected DefaultedList<ItemStack> getHeldStacks() {
		return this.stacks;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean isValid(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return IntStream.range(0, this.size()).toArray();
	}

	@Override
	public boolean canInsert(int index, ItemStack stack, @Nullable Direction direction) {
		return this.isValid(index, stack);
	}

	@Override
	public boolean canExtract(int index, ItemStack stack, Direction direction) {
		return true;
	}

}
