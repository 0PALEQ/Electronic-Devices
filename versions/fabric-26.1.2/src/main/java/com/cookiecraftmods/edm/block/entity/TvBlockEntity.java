package com.cookiecraftmods.edm.block.entity;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TvBlockEntity extends BlockEntity {
    private static final java.util.Set<TvBlockEntity> INSTANCES = java.util.Collections.newSetFromMap(
        new java.util.WeakHashMap<TvBlockEntity, Boolean>()
    );

    /** Immediately publish revoked approval to clients watching already-loaded TVs. */
    public static void refreshMediaPolicy(Object server) {
        synchronized (INSTANCES) {
            for (TvBlockEntity tv : INSTANCES) {
                if (tv.level instanceof net.minecraft.server.level.ServerLevel serverWorld
                    && serverWorld.getServer() == server) tv.sync();
            }
        }
    }

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
		synchronized (INSTANCES) { INSTANCES.add(this); }
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
		if (!(level instanceof net.minecraft.server.level.ServerLevel)) return serverMediaBlocked;
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
		if (level == null) return playbackBaseMs;
		boolean powered = getBlockState().hasProperty(TvDevice.DISPLAY_STATE) && getBlockState().getValue(TvDevice.DISPLAY_STATE) == 1;
		return powered ? playbackBaseMs + Math.max(0, level.getGameTime() - playbackStartedTick) * 50L : playbackBaseMs;
	}

	public void beforePowerChange(boolean poweringOn) {
		if (level == null) return;
		if (poweringOn) {
			playbackStartedTick = level.getGameTime();
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
		playbackStartedTick = level == null ? 0 : level.getGameTime();
	}

	private void sync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);
		serverMediaBlocked = view.getBooleanOr("ServerMediaBlocked", false);
		owner = view.read("Owner", UUIDUtil.AUTHLIB_CODEC).orElse(null);
		mediaUrls.clear();
		view.list("MediaUrls", Codec.STRING).ifPresent(savedUrls -> {
			for (String url : savedUrls) {
				if (mediaUrls.size() >= MAX_SAVED_VIDEOS) break;
				if (!url.isBlank() && !mediaUrls.contains(url)) mediaUrls.add(url);
			}
		});
		String legacyUrl = view.getStringOr("MediaUrl", "");
		if (legacyUrl.isBlank() && "url".equals(view.getStringOr("MediaType", ""))) {
			legacyUrl = view.getStringOr("MediaName", "");
		}
		if (mediaUrls.isEmpty() && !legacyUrl.isBlank()) {
			mediaUrls.add(legacyUrl);
		}
		String finalLegacyUrl = legacyUrl;
		selectedMediaIndex = view.getInt("SelectedMediaIndex").orElseGet(() -> mediaUrls.indexOf(finalLegacyUrl));
		if (selectedMediaIndex < 0 || selectedMediaIndex >= mediaUrls.size()) {
			selectedMediaIndex = mediaUrls.isEmpty() ? -1 : 0;
		}
		volume = view.getInt("Volume").map(value -> Math.max(0, Math.min(value, 100))).orElse(DEFAULT_VOLUME);
		playbackBaseMs = view.getLongOr("PlaybackBaseMs", 0L);
		playbackStartedTick = view.getLongOr("PlaybackStartedTick", 0L);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);
		view.storeNullable("Owner", UUIDUtil.AUTHLIB_CODEC, owner);
		ValueOutput.TypedOutputList<String> savedUrls = view.list("MediaUrls", Codec.STRING);
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
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return saveWithFullMetadata(registryLookup);
	}
}
