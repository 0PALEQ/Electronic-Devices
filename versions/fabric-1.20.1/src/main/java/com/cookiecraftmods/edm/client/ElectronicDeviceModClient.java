package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.render.TvBlockEntityRenderer;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.cookiecraftmods.edm.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class ElectronicDeviceModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(MediaPlaybackManager::tick);
		BlockEntityRendererFactories.register(ModBlockEntities.TV, TvBlockEntityRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OPEN_MEDIA_SCREEN, (client, handler, buffer, responseSender) -> {
			Identifier dimension = buffer.readIdentifier();
			BlockPos pos = buffer.readBlockPos();
			long menuToken = buffer.readLong();
			int mediaCount = buffer.readVarInt();
			if (mediaCount < 0 || mediaCount > TvBlockEntity.MAX_SAVED_VIDEOS) {
				throw new IllegalStateException("Invalid TV playlist size: " + mediaCount);
			}
			List<String> mediaUrls = new ArrayList<>(mediaCount);
			for (int index = 0; index < mediaCount; index++) {
				mediaUrls.add(buffer.readString(ModNetworking.MAX_URL_PACKET_LENGTH));
			}
			int selectedMediaIndex = buffer.readVarInt();
			long playbackPosition = buffer.readLong();
			int maxUrlLength = buffer.readVarInt();
			int maxSavedVideos = buffer.readVarInt();
			int volume = buffer.readVarInt();
			boolean powered = buffer.readBoolean();
			boolean canEdit = buffer.readBoolean();
			boolean refreshOnly = buffer.readBoolean();
			client.execute(() -> {
				if (refreshOnly && !(client.currentScreen instanceof MediaScreen current && current.isFor(dimension, pos))) return;
				int page = client.currentScreen instanceof MediaScreen screen && screen.isFor(dimension, pos)
					? screen.getPage() : 0;
				client.setScreen(new MediaScreen(
					dimension, pos, menuToken, mediaUrls, selectedMediaIndex, playbackPosition,
					maxUrlLength, maxSavedVideos, volume, powered, canEdit, page
				));
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.MEDIA_ACTION_RESULT, (client, handler, buffer, responseSender) -> {
			long menuToken = buffer.readLong();
			boolean success = buffer.readBoolean();
			String message = buffer.readString(512);
			client.execute(() -> {
				if (client.currentScreen instanceof MediaScreen screen && screen.isSession(menuToken)) {
					screen.setStatus(message, success);
				}
			});
		});
	}
}
