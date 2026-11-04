package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.render.WaterMediaVideoTexture;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

public final class MediaPlaybackManager {
	private static final Map<Key, Session> SESSIONS = new HashMap<>();
	private static final Map<Key, RetryState> RETRIES = new HashMap<>();
	private static final HashSet<String> LOGGED = new HashSet<>();
	private static long ticks;

	private MediaPlaybackManager() {
	}

	public static Identifier videoTexture(TvBlockEntity tv) {
		if (tv.getLevel() == null || !tv.hasMedia() || !isPowered(tv.getBlockState())) return null;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.level != tv.getLevel()) return null;
		Key key = new Key(client.level.dimension().identifier(), tv.getBlockPos().immutable());
		String source = sourceKey(tv);
		RetryState retry = RETRIES.get(key);
		if (retry != null && !retry.source.equals(source)) {
			RETRIES.remove(key);
			retry = null;
		}
		Session session = SESSIONS.get(key);
		if (session != null && !session.source.equals(source)) {
			session.close();
			SESSIONS.remove(key);
			session = null;
		}
		if (session == null) {
			if (retry != null && ticks < retry.afterTick) return null;
			WaterMediaBridge.Playback player;
			try {
				player = WaterMediaBridge.open(URI.create(tv.getMediaUrl()));
			} catch (IllegalArgumentException exception) {
				scheduleRetry(key, source);
				logOnce(source, "TV media URL is invalid on the client: " + tv.getMediaUrl());
				return null;
			}
			if (player == null) {
				scheduleRetry(key, source);
				logOnce(source, "Could not open TV media with " + WaterMediaBridge.backendSummary()
					+ ". " + WaterMediaBridge.lastDiagnostic());
				return null;
			}
			session = new Session(source, player, textureIdFor(key));
			SESSIONS.put(key, session);
			LOGGER.info("Opened TV media session {} at {} using {}", source, tv.getBlockPos(), WaterMediaBridge.backendSummary());
		}
		session.lastSeenTick = ticks;
		if (!synchronize(client, tv, session, key, false)) {
			session.close();
			SESSIONS.remove(key);
			scheduleRetry(key, source);
			logOnce(source, "TV media player entered a failed state. " + WaterMediaBridge.lastDiagnostic());
			return null;
		}
		int glId = session.player.texture(tv.getPlaybackPositionMs());
		if (glId < 0) return null;
		session.texture.update(glId);
		return session.textureId;
	}

	private static Identifier textureIdFor(Key key) {
		String dimension = (key.dimension.getNamespace() + "_" + key.dimension.getPath()).replace('/', '_');
		String path = "dynamic/tv/" + dimension + "/" + key.pos.getX() + "_" + key.pos.getY() + "_" + key.pos.getZ();
		return Identifier.fromNamespaceAndPath("electronic_device_mod", path);
	}

	public static void tick(Minecraft client) {
		ticks++;
		if (client.level == null) {
			closeAll();
			return;
		}
		Iterator<Map.Entry<Key, Session>> iterator = SESSIONS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Key, Session> entry = iterator.next();
			Key key = entry.getKey();
			Session session = entry.getValue();
			if (!key.dimension.equals(client.level.dimension().identifier())
				|| ticks - session.lastSeenTick > 100
				|| !(client.level.getBlockEntity(key.pos) instanceof TvBlockEntity tv)
				|| !tv.hasMedia()
				|| !session.source.equals(sourceKey(tv))) {
				session.close();
				iterator.remove();
				continue;
			}
			if (!synchronize(client, tv, session, key, ticks % 20 == 0)) {
				session.close();
				iterator.remove();
				scheduleRetry(key, session.source);
			}
		}
	}

	private static boolean synchronize(Minecraft client, TvBlockEntity tv, Session session, Key key, boolean allowSeek) {
		boolean powered = isPowered(tv.getBlockState());
		if (!powered) {
			session.player.setVolume(0);
			if (session.player.isPlaying()) session.player.pause();
			return true;
		}
		if (session.player.isPaused()) session.player.play();
		if (session.player.isBroken()) return false;
		if (!session.player.isReady()) return true;
		RETRIES.remove(key);

		long duration = session.player.duration();
		long expected = tv.getPlaybackPositionMs();
		if (duration > 0) expected %= duration;
		long drift = Math.abs(session.player.time() - expected);
		if (session.player.canSeek() && (!session.initiallySynchronized || (allowSeek && drift > 1250L))) {
			session.player.seek(expected);
			session.initiallySynchronized = true;
		}

		if (client.player == null) {
			session.player.setVolume(0);
			return true;
		}
		double distance = Math.sqrt(client.player.distanceToSqr(
			tv.getBlockPos().getX() + 0.5,
			tv.getBlockPos().getY() + 0.5,
			tv.getBlockPos().getZ() + 0.5
		));
		float attenuation = (float) Math.max(0d, 1d - distance / 32d);
		float tvVolume = tv.getVolume() / 100f;
		float master = client.options.getFinalSoundSourceVolume(SoundSource.MASTER);
		float records = client.options.getFinalSoundSourceVolume(SoundSource.RECORDS);
		session.player.setVolume(Math.round(100f * tvVolume * attenuation * master * records));
		return true;
	}

	private static boolean isPowered(BlockState state) {
		return state.hasProperty(TvDevice.DISPLAY_STATE) && state.getValue(TvDevice.DISPLAY_STATE) == 1;
	}

	public static void closeAll() {
		for (Session session : SESSIONS.values()) session.close();
		SESSIONS.clear();
		RETRIES.clear();
	}

	private static void scheduleRetry(Key key, String source) {
		RetryState previous = RETRIES.get(key);
		int attempts = previous != null && previous.source.equals(source) ? previous.attempts + 1 : 1;
		long delay = Math.min(6000L, 100L << Math.min(attempts - 1, 6));
		RETRIES.put(key, new RetryState(source, attempts, ticks + delay));
	}

	private static String sourceKey(TvBlockEntity tv) {
		return "media:" + tv.getMediaUrl();
	}

	private static void logOnce(String source, String message) {
		if (LOGGED.add(source + ":" + message)) LOGGER.warn(message);
	}

	private record Key(Identifier dimension, BlockPos pos) {
	}

	private record RetryState(String source, int attempts, long afterTick) {
	}

	private static final class Session {
		private final String source;
		private final WaterMediaBridge.Playback player;
		private final WaterMediaVideoTexture texture = new WaterMediaVideoTexture();
		private final Identifier textureId;
		private long lastSeenTick;
		private boolean initiallySynchronized;

		private Session(String source, WaterMediaBridge.Playback player, Identifier textureId) {
			this.source = source;
			this.player = player;
			this.textureId = textureId;
			this.lastSeenTick = ticks;
			Minecraft.getInstance().getTextureManager().register(textureId, texture);
		}

		private void close() {
			player.setVolume(0);
			player.release();
		}
	}
}
