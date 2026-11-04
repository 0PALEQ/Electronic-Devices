package com.cookiecraftmods.edm.item;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.media.BoundTv;
import com.cookiecraftmods.edm.media.RemoteControl;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TvRemoteBlockItem extends BlockItem {
	public TvRemoteBlockItem(Block block, Properties settings) {
		super(block, settings);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		if (player != null && player.isShiftKeyDown()
			&& world.getBlockState(context.getClickedPos()).getBlock() instanceof TvDevice) {
			if (!world.isClientSide()) {
				BoundTv binding = new BoundTv(world.dimension(), context.getClickedPos().immutable());
				CustomData.update(DataComponents.CUSTOM_DATA, stack, binding::write);
				player.sendOverlayMessage(Component.translatable(
					"message.electronic_device_mod.remote_bound",
					context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ()
				));
			}
			return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}
		BoundTv binding = boundTv(stack);
		if (player != null && player.isShiftKeyDown() && binding != null) {
			if (!world.isClientSide()) RemoteControl.openMenu(player, binding);
			return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
		}
		return super.useOn(context);
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);
		if (!world.isClientSide()) {
			BoundTv binding = boundTv(stack);
			if (user.isShiftKeyDown()) RemoteControl.openMenu(user, binding);
			else RemoteControl.advance(user, binding);
		}
		return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public void appendHoverText(
		ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type
	) {
		BoundTv binding = boundTv(stack);
		if (binding == null) {
			textConsumer.accept(Component.translatable("tooltip.electronic_device_mod.remote_unbound"));
		} else {
			textConsumer.accept(Component.translatable(
				"tooltip.electronic_device_mod.remote_bound",
				binding.pos().getX(), binding.pos().getY(), binding.pos().getZ(),
				binding.dimension().identifier().toString()
			));
			textConsumer.accept(Component.translatable("tooltip.electronic_device_mod.remote_next"));
			textConsumer.accept(Component.translatable("tooltip.electronic_device_mod.remote_menu"));
		}
	}

	private static BoundTv boundTv(ItemStack stack) {
		return BoundTv.read(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
	}
}
