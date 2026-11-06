package com.cookiecraftmods.edm.block.entity;

import com.cookiecraftmods.edm.block.TvDevice;
import com.cookiecraftmods.edm.media.MediaConfig;
import com.cookiecraftmods.edm.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TvBlockEntity extends BlockEntity {
    private static final java.util.Set<TvBlockEntity> INSTANCES = java.util.Collections.newSetFromMap(
        new java.util.WeakHashMap<TvBlockEntity, Boolean>()
    );

    /** Immediately publish revoked approval to clients watching already-loaded TVs. */
    public static void refreshMediaPolicy(Object server) {
        synchronized (INSTANCES) {
            for (TvBlockEntity tv : INSTANCES) {
                if (tv.world instanceof net.minecraft.server.world.ServerWorld serverWorld
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
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		serverMediaBlocked = nbt.getBoolean("ServerMediaBlocked");
		owner = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
		mediaUrls.clear();
		NbtList savedUrls = nbt.getList("MediaUrls", NbtElement.STRING_TYPE);
		for (int index = 0; index < savedUrls.size() && mediaUrls.size() < MAX_SAVED_VIDEOS; index++) {
			String url = savedUrls.getString(index);
			if (!url.isBlank() && !mediaUrls.contains(url)) mediaUrls.add(url);
		}
		String legacyUrl = nbt.getString("MediaUrl");
		if (legacyUrl.isBlank() && "url".equals(nbt.getString("MediaType"))) {
			legacyUrl = nbt.getString("MediaName");
		}
		if (mediaUrls.isEmpty() && !legacyUrl.isBlank()) {
			mediaUrls.add(legacyUrl);
		}
		selectedMediaIndex = nbt.contains("SelectedMediaIndex", NbtElement.INT_TYPE)
			? nbt.getInt("SelectedMediaIndex")
			: mediaUrls.indexOf(legacyUrl);
		if (selectedMediaIndex < 0 || selectedMediaIndex >= mediaUrls.size()) {
			selectedMediaIndex = mediaUrls.isEmpty() ? -1 : 0;
		}
		volume = nbt.contains("Volume", NbtElement.INT_TYPE)
			? Math.max(0, Math.min(nbt.getInt("Volume"), 100))
			: DEFAULT_VOLUME;
		playbackBaseMs = nbt.getLong("PlaybackBaseMs");
		playbackStartedTick = nbt.getLong("PlaybackStartedTick");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		if (owner != null) nbt.putUuid("Owner", owner);
		NbtList savedUrls = new NbtList();
		for (String url : mediaUrls) savedUrls.add(NbtString.of(url));
		nbt.put("MediaUrls", savedUrls);
		nbt.putBoolean("ServerMediaBlocked", isSourceDisabled());
		nbt.putInt("SelectedMediaIndex", selectedMediaIndex);
		nbt.putInt("Volume", volume);
		// Retain the active URL for downgrade compatibility with the previous single-video format.
		nbt.putString("MediaUrl", getMediaUrl());
		nbt.putLong("PlaybackBaseMs", playbackBaseMs);
		nbt.putLong("PlaybackStartedTick", playbackStartedTick);
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
