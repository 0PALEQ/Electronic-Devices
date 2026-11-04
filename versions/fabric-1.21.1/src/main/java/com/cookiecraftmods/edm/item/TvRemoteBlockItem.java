package com.cookiecraftmods.edm.item;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.media.BoundTv;
import com.cookiecraftmods.edm.media.RemoteControl;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class TvRemoteBlockItem extends BlockItem {
	public TvRemoteBlockItem(Block block, Settings settings) {
		super(block, settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		ItemStack stack = context.getStack();
		PlayerEntity player = context.getPlayer();
		if (player != null && player.isSneaking()
			&& world.getBlockState(context.getBlockPos()).getBlock() instanceof TvDevice) {
			if (!world.isClient) {
				BoundTv binding = new BoundTv(world.getRegistryKey(), context.getBlockPos().toImmutable());
				NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, binding::write);
				player.sendMessage(Text.translatable(
					"message.electronic_device_mod.remote_bound",
					context.getBlockPos().getX(), context.getBlockPos().getY(), context.getBlockPos().getZ()
				), true);
			}
			return ActionResult.success(world.isClient);
		}
		BoundTv binding = boundTv(stack);
		if (player != null && player.isSneaking() && binding != null) {
			if (!world.isClient) RemoteControl.openMenu(player, binding);
			return ActionResult.success(world.isClient);
		}
		return super.useOnBlock(context);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			BoundTv binding = boundTv(stack);
			if (user.isSneaking()) RemoteControl.openMenu(user, binding);
			else RemoteControl.advance(user, binding);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		BoundTv binding = boundTv(stack);
		if (binding == null) {
			tooltip.add(Text.translatable("tooltip.electronic_device_mod.remote_unbound"));
		} else {
			tooltip.add(Text.translatable(
				"tooltip.electronic_device_mod.remote_bound",
				binding.pos().getX(), binding.pos().getY(), binding.pos().getZ(),
				binding.dimension().getValue().toString()
			));
			tooltip.add(Text.translatable("tooltip.electronic_device_mod.remote_next"));
			tooltip.add(Text.translatable("tooltip.electronic_device_mod.remote_menu"));
		}
	}

	private static BoundTv boundTv(ItemStack stack) {
		return BoundTv.read(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt());
	}
}
