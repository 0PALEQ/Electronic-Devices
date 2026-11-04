package com.cookiecraftmods.edm.media;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.cookiecraftmods.edm.ElectronicDeviceMod.LOGGER;

public final class MediaConfig {
	public static final String FILE_NAME = "electronic_device_mod-media.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static MediaConfig instance;

	public MediaSourcePolicy mediaSources = new MediaSourcePolicy();
	public int maxMediaUrlLength = 2048;
	public int maxSavedVideosPerTv = 128;
	public int remoteRange = -1;
	public boolean allowCrossDimensionRemotes = true;
	public boolean preferGameLanguageForYouTube = false;

	private MediaConfig() {
	}

	public static MediaConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	public static MediaConfig reload() {
		instance = load();
		return instance;
	}

	private static MediaConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		MediaConfig config = null;
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				config = GSON.fromJson(reader, MediaConfig.class);
			} catch (Exception exception) {
				LOGGER.error("Could not read media config {}", path, exception);
			}
		}
		if (config == null) {
			config = new MediaConfig();
		}
		config.sanitize();
		config.save(path);
		return config;
	}

	private void sanitize() {
		if (mediaSources == null) mediaSources = new MediaSourcePolicy();
		mediaSources.sanitize();
		maxMediaUrlLength = Math.max(256, Math.min(maxMediaUrlLength, 8192));
		maxSavedVideosPerTv = Math.max(1, Math.min(maxSavedVideosPerTv, 128));
		remoteRange = Math.max(-1, remoteRange);
	}

	private void save(Path path) {
		try {
			Files.createDirectories(path.getParent());
			Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(this, writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException ignored) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			LOGGER.error("Could not write media config {}", path, exception);
		}
	}

}
