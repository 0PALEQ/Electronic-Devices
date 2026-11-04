package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import com.cookiecraftmods.edm.client.render.TvBlockEntityRenderer;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public final class ElectronicDeviceModClient {
	private ElectronicDeviceModClient() {
	}

	public static void openMediaScreen(
		Identifier dimension, BlockPos pos, long menuToken, List<String> mediaUrls,
		int selectedMediaIndex, long playbackPosition, int maxUrlLength, int maxSavedVideos,
		int volume, boolean powered, boolean canEdit
	) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.execute(() -> {
			int page = client.currentScreen instanceof MediaScreen screen && screen.isFor(dimension, pos)
				? screen.getPage() : 0;
			client.setScreen(new MediaScreen(
				dimension, pos, menuToken, mediaUrls, selectedMediaIndex, playbackPosition,
				maxUrlLength, maxSavedVideos, volume, powered, canEdit, page
			));
		});
	}

	public static void handleMediaActionResult(long menuToken, boolean success, String message) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.execute(() -> {
			if (client.currentScreen instanceof MediaScreen screen && screen.isSession(menuToken)) {
				screen.setStatus(message, success);
			}
		});
	}

	@Mod.EventBusSubscriber(
		modid = ElectronicDeviceMod.MOD_ID,
		value = Dist.CLIENT,
		bus = Mod.EventBusSubscriber.Bus.MOD
	)
	public static final class ModEvents {
		private ModEvents() {
		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerBlockEntityRenderer(ModBlockEntities.TV.get(), TvBlockEntityRenderer::new);
		}
	}

	@Mod.EventBusSubscriber(modid = ElectronicDeviceMod.MOD_ID, value = Dist.CLIENT)
	public static final class ForgeEvents {
		private ForgeEvents() {
		}

		@SubscribeEvent
		public static void clientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				MediaPlaybackManager.tick(MinecraftClient.getInstance());
			}
		}
	}
}
