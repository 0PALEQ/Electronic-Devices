package com.cookiecraftmods.edm.block.entity;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TvBlockEntity extends BlockEntity {
	public static final int MAX_SAVED_VIDEOS = 128;
	public static final int DEFAULT_VOLUME = 100;

	private UUID owner;
	private boolean serverMediaBlocked;
	private final List<String> mediaUrls = new ArrayList<>();
	private int selectedMediaIndex = -1;
	private int volume = DEFAULT_VOLUME;
	private long playbackBaseMs;
	private long playbackStartedTick;

	public TvBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TV, pos, state);
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		if (this.owner == null) {
			this.owner = owner;
			sync();
		}
	}

	public boolean isOwner(UUID player) {
		return owner == null || owner.equals(player);
	}

	public String getMediaUrl() {
		if (isSourceDisabled()) return "";
		return selectedMediaIndex >= 0 && selectedMediaIndex < mediaUrls.size()
			? mediaUrls.get(selectedMediaIndex)
			: "";
	}

	private boolean isSourceDisabled() {
		if (!(world instanceof net.minecraft.server.world.ServerWorld)) return serverMediaBlocked;
		if (selectedMediaIndex < 0 || selectedMediaIndex >= mediaUrls.size()) return false;
		MediaConfig config = MediaConfig.get();
		try {
			// Administrator-only media remains visible after an administrator starts it.
			config.mediaSources.check(mediaUrls.get(selectedMediaIndex), config.maxMediaUrlLength, true);
			return false;
		} catch (IllegalArgumentException exception) {
			return true;
		}
	}

	public List<String> getMediaUrls() {
		return List.copyOf(mediaUrls);
	}

	public int getSelectedMediaIndex() {
		return selectedMediaIndex;
	}

	public int getSavedVideoCount() {
		return mediaUrls.size();
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		int clamped = Math.max(0, Math.min(volume, 100));
		if (this.volume == clamped) return;
		this.volume = clamped;
		sync();
	}

	public int indexOfMedia(String url) {
		return mediaUrls.indexOf(url);
	}

	public boolean hasMedia() {
		return !getMediaUrl().isBlank();
	}

	public long getPlaybackPositionMs() {
		if (world == null) return playbackBaseMs;
		boolean powered = getCachedState().contains(TvDevice.DISPLAY_STATE) && getCachedState().get(TvDevice.DISPLAY_STATE) == 1;
		return powered ? playbackBaseMs + Math.max(0, world.getTime() - playbackStartedTick) * 50L : playbackBaseMs;
	}

	public void beforePowerChange(boolean poweringOn) {
		if (world == null) return;
		if (poweringOn) {
			playbackStartedTick = world.getTime();
		} else {
			playbackBaseMs = getPlaybackPositionMs();
		}
		sync();
	}

	public boolean addMediaUrl(String url, UUID player) {
		if (owner == null) owner = player;
		int index = mediaUrls.indexOf(url);
		boolean added = index < 0;
		if (added) {
			if (mediaUrls.size() >= MAX_SAVED_VIDEOS) {
				throw new IllegalStateException("The TV playlist is full.");
			}
			mediaUrls.add(url);
			index = mediaUrls.size() - 1;
		}
		selectedMediaIndex = index;
		resetPlayback();
		sync();
		return added;
	}

	public boolean selectMedia(int index) {
		if (index < 0 || index >= mediaUrls.size()) return false;
		if (selectedMediaIndex == index) return true;
		selectedMediaIndex = index;
		resetPlayback();
		sync();
		return true;
	}

	public boolean selectNextMedia() {
		if (mediaUrls.size() <= 1) return false;
		selectedMediaIndex = (selectedMediaIndex + 1) % mediaUrls.size();
		resetPlayback();
		sync();
		return true;
	}

	public boolean removeMedia(int index) {
		if (index < 0 || index >= mediaUrls.size()) return false;
		boolean removedSelected = index == selectedMediaIndex;
		mediaUrls.remove(index);
		if (mediaUrls.isEmpty()) {
			selectedMediaIndex = -1;
			playbackBaseMs = 0;
			playbackStartedTick = 0;
		} else if (removedSelected) {
			selectedMediaIndex = Math.min(index, mediaUrls.size() - 1);
			resetPlayback();
		} else if (index < selectedMediaIndex) {
			selectedMediaIndex--;
		}
		sync();
		return true;
	}

	public void clearMedia() {
		mediaUrls.clear();
		selectedMediaIndex = -1;
		playbackBaseMs = 0;
		playbackStartedTick = 0;
		sync();
	}

	private void resetPlayback() {
		playbackBaseMs = 0;
		playbackStartedTick = world == null ? 0 : world.getTime();
	}

	private void sync() {
		markDirty();
		if (world != null) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		serverMediaBlocked = view.getBoolean("ServerMediaBlocked", false);
		owner = view.read("Owner", Uuids.CODEC).orElse(null);
		mediaUrls.clear();
		view.getOptionalTypedListView("MediaUrls", Codec.STRING).ifPresent(savedUrls -> {
			for (String url : savedUrls) {
				if (mediaUrls.size() >= MAX_SAVED_VIDEOS) break;
				if (!url.isBlank() && !mediaUrls.contains(url)) mediaUrls.add(url);
			}
		});
		String legacyUrl = view.getString("MediaUrl", "");
		if (legacyUrl.isBlank() && "url".equals(view.getString("MediaType", ""))) {
			legacyUrl = view.getString("MediaName", "");
		}
		if (mediaUrls.isEmpty() && !legacyUrl.isBlank()) {
			mediaUrls.add(legacyUrl);
		}
		String finalLegacyUrl = legacyUrl;
		selectedMediaIndex = view.getOptionalInt("SelectedMediaIndex").orElseGet(() -> mediaUrls.indexOf(finalLegacyUrl));
		if (selectedMediaIndex < 0 || selectedMediaIndex >= mediaUrls.size()) {
			selectedMediaIndex = mediaUrls.isEmpty() ? -1 : 0;
		}
		volume = view.getOptionalInt("Volume").map(value -> Math.max(0, Math.min(value, 100))).orElse(DEFAULT_VOLUME);
		playbackBaseMs = view.getLong("PlaybackBaseMs", 0L);
		playbackStartedTick = view.getLong("PlaybackStartedTick", 0L);
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.putNullable("Owner", Uuids.CODEC, owner);
		WriteView.ListAppender<String> savedUrls = view.getListAppender("MediaUrls", Codec.STRING);
		for (String url : mediaUrls) savedUrls.add(url);
		view.putBoolean("ServerMediaBlocked", isSourceDisabled());
		view.putInt("SelectedMediaIndex", selectedMediaIndex);
		view.putInt("Volume", volume);
		// Retain the active URL for downgrade compatibility with the previous single-video format.
		view.putString("MediaUrl", getMediaUrl());
		view.putLong("PlaybackBaseMs", playbackBaseMs);
		view.putLong("PlaybackStartedTick", playbackStartedTick);
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
