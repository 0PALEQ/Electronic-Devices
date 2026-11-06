package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.client.render.TvBlockEntityRenderer;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class ElectronicDeviceModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(MediaPlaybackManager::tick);
		BlockEntityRenderers.register(ModBlockEntities.TV, TvBlockEntityRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenMediaScreenPayload.ID, (payload, context) ->
			context.client().execute(() -> {
				if (payload.refreshOnly() && !(context.client().screen instanceof MediaScreen current && current.isFor(payload.dimension(), payload.pos()))) return;
			int page = context.client().screen instanceof MediaScreen screen
					&& screen.isFor(payload.dimension(), payload.pos())
					? screen.getPage() : 0;
				context.client().setScreen(new MediaScreen(
					payload.dimension(), payload.pos(), payload.token(), payload.mediaUrls(), payload.selectedMediaIndex(),
					payload.playbackPositionMs(), payload.maxUrlLength(), payload.maxSavedVideos(), payload.volume(),
					payload.powered(), payload.canEdit(), page
				));
			}));

		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.MediaActionResultPayload.ID, (payload, context) ->
			context.client().execute(() -> {
				if (context.client().screen instanceof MediaScreen screen && screen.isSession(payload.token())) {
					screen.setStatus(payload.message(), payload.success());
				}
			}));
	}
}
