package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.media.MediaConfig;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

/**
 * Keeps WATERMeDIA optional at compile time and adapts media playback to its current v3 API.
 * The v2 fallback remains for older clients that also install the legacy YouTube plugin.
 */
public final class WaterMediaBridge {
	private static final String PLAYER_CLASS = "org.watermedia.api.player.videolan.VideoPlayer";
	private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
	
	private static boolean warned;
	private static volatile String lastDiagnostic = "";

	private WaterMediaBridge() {
	}

	public static boolean isInstalled() {
		return isModLoaded("watermedia");
	}

	public static boolean isRuntimeReady() {
		return isInstalled() && (!usesV3() || isModLoaded("watermedia_binaries"));
	}

	public static boolean usesV3() {
		return classExists("org.watermedia.api.player.MediaPlayer");
	}

	public static String backendSummary() {
		if (!isInstalled()) return "WATERMeDIA is not installed";
		if (usesV3() && !isModLoaded("watermedia_binaries")) return "WATERMeDIA 3 is missing WATERMeDIA Binaries";
		return usesV3() ? "WATERMeDIA 3" : "WATERMeDIA 2.1";
	}

	public static String lastDiagnostic() {
		return lastDiagnostic;
	}

	public static Playback open(URI media) {
		if (!isRuntimeReady()) return null;
		try {
			if (usesV3()) {
				if (isYouTube(media)) preferBuiltInYouTubeResolver();
				
				infoOnce("v3-backend", "Using WATERMeDIA v3 TV playback backend");
				return new V3Playback(media);
			}
			infoOnce("v2-backend", "Using WATERMeDIA v2.1 TV playback backend");
			Class<?> type = Class.forName(PLAYER_CLASS);
			Constructor<?> constructor = type.getConstructor(Executor.class);
			Object player = constructor.newInstance((Executor) MinecraftClient.getInstance());
			VideoPlayback playback = new VideoPlayback(type, player);
			playback.setRepeat(true);
			playback.start(media);
			return playback;
		} catch (ReflectiveOperationException | LinkageError exception) {
			if (!warned) {
				warned = true;
				LOGGER.error("WATERMeDIA is installed but neither its v2 nor v3 playback API could be initialized", exception);
			}
			failure("backend-initialization", exception);
			return null;
		}
	}

	private static boolean preferBuiltInYouTubeResolver() {
		try {
			Class<?> api = Class.forName("org.watermedia.api.platform.PlatformAPI");
			Class<?> platformType = Class.forName("org.watermedia.api.platform.Platform");
			Object value = api.getMethod("platforms").invoke(null);
			if (!(value instanceof List<?> platforms)) return true;

			Method unregister = api.getMethod("unregister", platformType);
			boolean removed = false;
			for (Object platform : platforms) {
				if (platform == null) continue;
				String className = platform.getClass().getName();
				if (className.startsWith("org.watermedia.api.platform.extension.")
					&& className.endsWith("YouTubePlatform")) {
					unregister.invoke(null, platform);
					removed = true;
				}
			}
			if (removed && REPORTED.add("warn:legacy-youtube-extension-disabled")) {
				LOGGER.warn(
					"Disabled the incompatible WATERMeDIA Platform Extension YouTube handler; "
						+ "WATERMeDIA 3's built-in YouTube resolver will be used instead"
				);
			}
			return true;
		} catch (ReflectiveOperationException | LinkageError exception) {
			failure("v3-platform-selection", exception);
			return false;
		}
	}

	private static boolean isYouTube(URI uri) {
		String host = uri.getHost();
		if (host == null) return false;
		host = host.toLowerCase(java.util.Locale.ROOT);
		return host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be");
	}
	private static String gameLanguage() {
		String code = MinecraftClient.getInstance().getLanguageManager().getLanguage();
		int separator = code.indexOf('_');
		return (separator < 0 ? code : code.substring(0, separator)).toLowerCase(java.util.Locale.ROOT);
	}

	private static boolean classExists(String name) {
		try {
			Class.forName(name, false, WaterMediaBridge.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError ignored) {
			return false;
		}
	}

	private static boolean isModLoaded(String id) {
		String[][] loaders = {
			{"net.fabricmc.loader.api.FabricLoader", "getInstance", "isModLoaded"},
			{"net.neoforged.fml.ModList", "get", "isLoaded"},
			{"net.minecraftforge.fml.ModList", "get", "isLoaded"}
		};
		for (String[] loader : loaders) {
			try {
				Class<?> type = Class.forName(loader[0], false, WaterMediaBridge.class.getClassLoader());
				Object instance = type.getMethod(loader[1]).invoke(null);
				Object result = type.getMethod(loader[2], String.class).invoke(instance, id);
				if (result instanceof Boolean loaded) return loaded;
			} catch (ReflectiveOperationException | LinkageError ignored) {
			}
		}
		return false;
	}

	private static void infoOnce(String key, String message) {
		if (REPORTED.add("info:" + key)) LOGGER.info(message);
	}

	private static void warning(String key, String message) {
		lastDiagnostic = message;
		if (REPORTED.add("warn:" + key)) LOGGER.warn(message);
	}

	private static void failure(String stage, Throwable throwable) {
		Throwable cause = throwable instanceof InvocationTargetException invocation && invocation.getCause() != null
			? invocation.getCause() : throwable;
		String detail = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
		lastDiagnostic = friendlyDiagnostic(stage, cause, detail);
		if (REPORTED.add("error:" + stage + ":" + cause.getClass().getName())) {
			LOGGER.error("TV media playback failure during {}: {}", stage, detail, cause);
		}
	}

	private static String friendlyDiagnostic(String stage, Throwable cause, String detail) {
		for (Throwable current = cause; current != null; current = current.getCause()) {
			String message = current.getMessage();
			if (current instanceof NoSuchMethodError && message != null && message.contains("DataSource")) {
				return "Obsolete WATERMeDIA Platform Extension detected. Remove it; WATERMeDIA 3 includes YouTube support.";
			}
			if (current instanceof NoClassDefFoundError && message != null && message.contains("DataSlave")) {
				return "This WATERMeDIA build is incompatible. Update or reinstall WATERMeDIA 3.0.0 (stable) or newer.";
			}
		}
		String diagnostic = stage + ": " + detail;
		return diagnostic.length() <= 300 ? diagnostic : diagnostic.substring(0, 297) + "...";
	}

	public interface Playback {
		int texture(long playbackTime);
		boolean isReady();
		default boolean canSeek() { return true; }
		boolean isPlaying();
		boolean isPaused();
		boolean isBroken();
		void seek(long milliseconds);
		long time();
		long duration();
		void setRepeat(boolean repeat);
		void setVolume(int volume);
		void play();
		void pause();
		void release();
	}

	private static final class V3Playback implements Playback {
		private final Object mrl;
		private final boolean youtube;
		private final Method mrlStatus;
		private final Method mrlException;
		private final Method createPlayer;
		private final Supplier<Object> gfx;
		private final Supplier<Object> sfx;
		private final Method texture;
		private final Method canPlay;
		private final Method canSeek;
		private final Method live;
		private final Method playing;
		private final Method paused;
		private final Method error;
		private final Method seek;
		private final Method time;
		private final Method duration;
		private final Method repeat;
		private final Method volume;
		private final Method resume;
		private final Method pause;
		private final Method release;
		private final Method start;
		private final Method ffmpegLoaded;
		private final Method ffmpegError;
		private Object player;
		private long lastCreateAttemptMillis;

		private V3Playback(URI uri) throws ReflectiveOperationException {
			youtube = isYouTube(uri);
			Class<?> api = Class.forName("org.watermedia.api.player.PlayerAPI");
			Class<?> mrlType = Class.forName("org.watermedia.api.player.MRL");
			Class<?> playerType = Class.forName("org.watermedia.api.player.MediaPlayer");
			mrl = api.getMethod("mrl", URI.class).invoke(null, uri);
			mrlStatus = mrlType.getMethod("status");
			mrlException = mrlType.getMethod("exception");
			createPlayer = api.getMethod("player", mrlType, Supplier.class, Supplier.class);

			Method glEngine = api.getMethod("glEngine", Thread.class, Executor.class);
			Method alEngine = api.getMethod("alEngine");
			ffmpegLoaded = Class.forName("org.watermedia.api.codecs.CodecsAPI").getMethod("ffmpegLoaded");
			ffmpegError = Class.forName("org.watermedia.api.codecs.CodecsAPI").getMethod("ffmpegError");
			Thread renderThread = Thread.currentThread();
			Executor renderExecutor = MinecraftClient.getInstance();
			gfx = () -> invokeStatic(glEngine, renderThread, renderExecutor);
			sfx = () -> invokeStatic(alEngine);

			texture = playerType.getMethod("texture");
			canPlay = playerType.getMethod("canPlay");
			canSeek = playerType.getMethod("canSeek");
			live = playerType.getMethod("live");
			playing = playerType.getMethod("playing");
			paused = playerType.getMethod("paused");
			error = playerType.getMethod("error");
			seek = playerType.getMethod("seek", long.class);
			time = playerType.getMethod("time");
			duration = playerType.getMethod("duration");
			repeat = playerType.getMethod("repeat", boolean.class);
			volume = playerType.getMethod("volume", int.class);
			resume = playerType.getMethod("resume");
			pause = playerType.getMethod("pause");
			release = playerType.getMethod("release");
			start = playerType.getMethod("start");
		}

		private boolean preparePlayer() {
			if (player != null) return true;
			try {
				if (!"LOADED".equals(String.valueOf(mrlStatus.invoke(mrl)))) return false;
				long now = System.currentTimeMillis();
				if (now - lastCreateAttemptMillis < 1000L) return false;
				lastCreateAttemptMillis = now;
				player = createPlayer.invoke(null, mrl, gfx, sfx);
				if (player == null) {
					Object loaded = ffmpegLoaded.invoke(null);
					Object backendError = ffmpegError.invoke(null);
					warning(
						"v3-player-null",
						"WATERMeDIA v3 could not create a TV player (FFmpeg loaded=" + loaded
							+ ", backend error=" + backendError + "). Check WATERMeDIA Binaries and the client log."
					);
					return false;
				}
				if (youtube && MediaConfig.get().preferGameLanguageForYouTube) {
					player.getClass().getMethod("audioLanguage", String.class).invoke(player, gameLanguage());
				}
				repeat.invoke(player, true);
				start.invoke(player);
				LOGGER.info("Created WATERMeDIA v3 TV player for {}", mrl);
				return true;
			} catch (ReflectiveOperationException | RuntimeException exception) {
				player = null;
				failure("v3-player-creation", exception);
				return false;
			}
		}

		@Override
		public int texture(long playbackTime) {
			Object result = invokePlayer(texture);
			if (!(result instanceof Number value)) return -1;
			int textureId = (int) value.longValue();
			return textureId > 0 ? textureId : -1;
		}

		@Override
		public boolean canSeek() {
			return Boolean.TRUE.equals(invokePlayer(canSeek)) && !Boolean.TRUE.equals(invokePlayer(live));
		}

		@Override
		public boolean isReady() {
			return Boolean.TRUE.equals(invokePlayer(canPlay));
		}

		@Override
		public boolean isPlaying() {
			return Boolean.TRUE.equals(invokePlayer(playing));
		}

		@Override
		public boolean isPaused() {
			return Boolean.TRUE.equals(invokePlayer(paused));
		}

		@Override
		public boolean isBroken() {
			try {
				String status = String.valueOf(mrlStatus.invoke(mrl));
				if ("ERROR".equals(status) || "BLOCKED".equals(status)) {
					Object problem = mrlException.invoke(mrl);
					if (problem instanceof Throwable throwable) {
						failure("v3-mrl-" + status.toLowerCase(), throwable);
					} else {
						warning("v3-mrl-" + status.toLowerCase(), "WATERMeDIA could not resolve this media source: " + status);
					}
					return true;
				}
			} catch (ReflectiveOperationException exception) {
				failure("v3-mrl-status", exception);
				return true;
			}
			return player != null && Boolean.TRUE.equals(invokePlayer(error));
		}

		@Override
		public void seek(long milliseconds) {
			invokePlayer(seek, milliseconds);
		}

		@Override
		public long time() {
			Object result = invokePlayer(time);
			return result instanceof Number value ? value.longValue() : 0L;
		}

		@Override
		public long duration() {
			Object result = invokePlayer(duration);
			return result instanceof Number value ? value.longValue() : 0L;
		}

		@Override
		public void setRepeat(boolean repeatMode) {
			invokePlayer(repeat, repeatMode);
		}

		@Override
		public void setVolume(int level) {
			invokePlayer(volume, Math.max(0, Math.min(100, level)));
		}

		@Override
		public void play() {
			invokePlayer(resume);
		}

		@Override
		public void pause() {
			invokePlayer(pause);
		}

		@Override
		public void release() {
			if (player != null) {
				invokePlayer(release);
				player = null;
			}
		}

		private Object invokePlayer(Method method, Object... arguments) {
			if (!preparePlayer()) return null;
			try {
				return method.invoke(player, arguments);
			} catch (ReflectiveOperationException | RuntimeException exception) {
				failure("v3-" + method.getName(), exception);
				return null;
			}
		}

		private static Object invokeStatic(Method method, Object... arguments) {
			try {
				return method.invoke(null, arguments);
			} catch (ReflectiveOperationException | RuntimeException exception) {
				failure("v3-engine-" + method.getName(), exception);
				return null;
			}
		}
	}

	private static final class VideoPlayback implements Playback {
		private final Object player;
		private final Method start;
		private final Method texture;
		private final Method isReady;
		private final Method isPlaying;
		private final Method isPaused;
		private final Method isBroken;
		private final Method seekTo;
		private final Method getTime;
		private final Method getDuration;
		private final Method setRepeatMode;
		private final Method setVolume;
		private final Method play;
		private final Method pause;
		private final Method release;

		private VideoPlayback(Class<?> type, Object player) throws NoSuchMethodException {
			this.player = player;
			start = type.getMethod("start", URI.class);
			texture = type.getMethod("texture");
			isReady = type.getMethod("isReady");
			isPlaying = type.getMethod("isPlaying");
			isPaused = type.getMethod("isPaused");
			isBroken = type.getMethod("isBroken");
			seekTo = type.getMethod("seekTo", long.class);
			getTime = type.getMethod("getTime");
			getDuration = type.getMethod("getDuration");
			setRepeatMode = type.getMethod("setRepeatMode", boolean.class);
			setVolume = type.getMethod("setVolume", int.class);
			play = type.getMethod("play");
			pause = type.getMethod("pause");
			release = type.getMethod("release");
		}

		public void start(URI uri) {
			invoke(start, uri);
		}

		@Override
		public int texture(long playbackTime) {
			Object result = invoke(texture);
			return result instanceof Integer value && value > 0 ? value : -1;
		}

		@Override
		public boolean isReady() {
			return Boolean.TRUE.equals(invoke(isReady));
		}

		@Override
		public boolean isPlaying() {
			return Boolean.TRUE.equals(invoke(isPlaying));
		}

		@Override
		public boolean isPaused() {
			return Boolean.TRUE.equals(invoke(isPaused));
		}

		@Override
		public boolean isBroken() {
			boolean broken = Boolean.TRUE.equals(invoke(isBroken));
			if (broken) warning("v2-player-broken", "WATERMeDIA v2 reported a broken TV video player.");
			return broken;
		}

		@Override
		public void seek(long milliseconds) {
			invoke(seekTo, milliseconds);
		}

		@Override
		public long time() {
			Object result = invoke(getTime);
			return result instanceof Long value ? value : 0L;
		}

		@Override
		public long duration() {
			Object result = invoke(getDuration);
			return result instanceof Long value ? value : 0L;
		}

		@Override
		public void setRepeat(boolean repeat) {
			invoke(setRepeatMode, repeat);
		}

		@Override
		public void setVolume(int volume) {
			invoke(setVolume, Math.max(0, Math.min(100, volume)));
		}

		@Override
		public void play() {
			invoke(play);
		}

		@Override
		public void pause() {
			invoke(pause);
		}

		@Override
		public void release() {
			invoke(release);
		}

		private Object invoke(Method method, Object... arguments) {
			try {
				return method.invoke(player, arguments);
			} catch (ReflectiveOperationException | RuntimeException exception) {
				failure("v2-" + method.getName(), exception);
				return null;
			}
		}
	}

}
