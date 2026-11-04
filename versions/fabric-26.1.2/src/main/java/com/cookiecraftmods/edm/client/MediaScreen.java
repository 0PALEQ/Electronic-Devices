package com.cookiecraftmods.edm.client;

import com.cookiecraftmods.edm.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;
import java.util.Locale;

public final class MediaScreen extends Screen {
	private static final int ROW_HEIGHT = 22;

	private final Identifier tvDimension;
	private final BlockPos tvPos;
	private final long menuToken;
	private final List<String> mediaUrls;
	private final int selectedMediaIndex;
	private final long playbackPosition;
	private final int maxUrlLength;
	private final int maxSavedVideos;
	private int volume;
	private final boolean powered;
	private final boolean canEdit;
	private Component status = Component.empty();
	private int statusColor = 0xAAAAAA;
	private EditBox urlField;
	private int page;
	private int rowsPerPage;
	private int top;

	public MediaScreen(
		Identifier tvDimension, BlockPos tvPos, long menuToken, List<String> mediaUrls,
		int selectedMediaIndex, long playbackPosition, int maxUrlLength, int maxSavedVideos,
		int volume, boolean powered, boolean canEdit, int initialPage
	) {
		super(Component.translatable("screen.electronic_device_mod.tv_media"));
		this.tvDimension = tvDimension;
		this.tvPos = tvPos.immutable();
		this.menuToken = menuToken;
		this.mediaUrls = List.copyOf(mediaUrls);
		this.selectedMediaIndex = selectedMediaIndex;
		this.playbackPosition = playbackPosition;
		this.maxUrlLength = maxUrlLength;
		this.maxSavedVideos = maxSavedVideos;
		this.volume = Math.max(0, Math.min(volume, 100));
		this.powered = powered;
		this.canEdit = canEdit;
		this.page = Math.max(0, initialPage);
	}

	@Override
	protected void init() {
		rowsPerPage = Math.max(2, Math.min(6, (height - 166) / ROW_HEIGHT));
		int maxPage = maxPage();
		page = Math.min(page, maxPage);
		int contentHeight = 146 + rowsPerPage * ROW_HEIGHT;
		top = Math.max(8, (height - contentHeight) / 2);
		int center = width / 2;

		urlField = new EditBox(
			font, center - 150, top + 42, 236, 20,
			Component.translatable("screen.electronic_device_mod.tv_media.url")
		);
		urlField.setMaxLength(maxUrlLength);
		urlField.setSuggestion("https://... / file:///... / water://...");
		urlField.setEditable(canEdit);
		addRenderableWidget(urlField);

		Button addVideo = Button.builder(
			Component.translatable("screen.electronic_device_mod.tv_media.add"),
			button -> addVideo()
		).bounds(center + 90, top + 42, 60, 20).build();
		addVideo.active = canEdit && mediaUrls.size() < maxSavedVideos;
		addRenderableWidget(addVideo);

		int firstIndex = page * rowsPerPage;
		int rowY = top + 76;
		for (int row = 0; row < rowsPerPage; row++) {
			int index = firstIndex + row;
			if (index >= mediaUrls.size()) break;
			String prefix = index == selectedMediaIndex ? "> " : "";
			String label = prefix + (index + 1) + ". " + compactUrl(mediaUrls.get(index));
			Button select = Button.builder(
				Component.literal(font.plainSubstrByWidth(label, 250)),
				button -> selectVideo(index)
			).bounds(center - 150, rowY + row * ROW_HEIGHT, 272, 20).build();
			select.active = index != selectedMediaIndex;
			addRenderableWidget(select);

			Button remove = Button.builder(
				Component.translatable("screen.electronic_device_mod.tv_media.remove_short"),
				button -> removeVideo(index)
			).bounds(center + 126, rowY + row * ROW_HEIGHT, 24, 20).build();
			remove.active = canEdit;
			addRenderableWidget(remove);
		}

		int paginationY = top + 78 + rowsPerPage * ROW_HEIGHT;
		Button previous = Button.builder(
			Component.translatable("screen.electronic_device_mod.tv_media.previous"),
			button -> changePage(-1)
		).bounds(center - 150, paginationY, 72, 20).build();
		previous.active = page > 0;
		addRenderableWidget(previous);

		Button next = Button.builder(
			Component.translatable("screen.electronic_device_mod.tv_media.next"),
			button -> changePage(1)
		).bounds(center + 78, paginationY, 72, 20).build();
		next.active = page < maxPage;
		addRenderableWidget(next);

		int bottomY = paginationY + 24;
		addRenderableWidget(new VolumeSlider(center - 150, bottomY, 136, 20));

		Button power = Button.builder(
			Component.translatable(powered
				? "screen.electronic_device_mod.tv_media.power_off"
				: "screen.electronic_device_mod.tv_media.power_on"),
			button -> togglePower()
		).bounds(center - 10, bottomY, 78, 20).build();
		power.active = powered || !mediaUrls.isEmpty();
		addRenderableWidget(power);

		addRenderableWidget(Button.builder(
			Component.translatable("gui.done"), button -> onClose()
		).bounds(center + 72, bottomY, 78, 20).build());

		setInitialStatus();
	}

	private void setInitialStatus() {
		if (!WaterMediaBridge.isInstalled()) {
			status = Component.translatable("screen.electronic_device_mod.tv_media.watermedia_missing");
			statusColor = 0xFFAA55;
		} else if (!WaterMediaBridge.usesV3()) {
			status = Component.translatable("screen.electronic_device_mod.tv_media.watermedia_outdated");
			statusColor = 0xFFAA55;
		} else if (!WaterMediaBridge.isRuntimeReady()) {
			status = Component.translatable("screen.electronic_device_mod.tv_media.watermedia_binaries_missing");
			statusColor = 0xFFAA55;
		} else if (!canEdit) {
			status = Component.translatable("screen.electronic_device_mod.tv_media.no_permission");
			statusColor = 0xAAAAAA;
		}
	}

	private void addVideo() {
		String url = urlField.getValue().trim();
		if (url.isEmpty()) {
			setStatus(Component.translatable("screen.electronic_device_mod.tv_media.url_empty").getString(), false);
			return;
		}
		ClientPlayNetworking.send(new ModNetworking.SetYoutubeUrlPayload(menuToken, url));
		setStatus(Component.translatable("screen.electronic_device_mod.tv_media.saving").getString(), true);
	}

	private void selectVideo(int index) {
		ClientPlayNetworking.send(new ModNetworking.SelectMediaPayload(menuToken, index));
		setStatus(Component.translatable("screen.electronic_device_mod.tv_media.selecting").getString(), true);
	}

	private void removeVideo(int index) {
		ClientPlayNetworking.send(new ModNetworking.RemoveMediaPayload(menuToken, index));
		setStatus(Component.translatable("screen.electronic_device_mod.tv_media.removing").getString(), true);
	}

	private void togglePower() {
		ClientPlayNetworking.send(new ModNetworking.ToggleMediaPowerPayload(menuToken));
		setStatus(Component.translatable("screen.electronic_device_mod.tv_media.changing_power").getString(), true);
	}

	private void changePage(int offset) {
		page = Math.max(0, Math.min(maxPage(), page + offset));
		rebuildWidgets();
	}

	private int maxPage() {
		return mediaUrls.isEmpty() ? 0 : (mediaUrls.size() - 1) / rowsPerPage;
	}

	private void setVolume(int volume) {
		this.volume = Math.max(0, Math.min(volume, 100));
		ClientPlayNetworking.send(new ModNetworking.SetMediaVolumePayload(menuToken, this.volume));
	}

	public boolean isFor(Identifier dimension, BlockPos pos) {
		return tvDimension.equals(dimension) && tvPos.equals(pos);
	}

	public boolean isSession(long token) {
		return menuToken == token;
	}

	public int getPage() {
		return page;
	}

	public void setStatus(String message, boolean success) {
		status = Component.literal(message);
		statusColor = success ? 0x55FF55 : 0xFF5555;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);
		int center = width / 2;
		context.centeredText(font, title, center, top, 0xFFFFFF);

		String currentUrl = selectedMediaIndex >= 0 && selectedMediaIndex < mediaUrls.size()
			? compactUrl(mediaUrls.get(selectedMediaIndex))
			: "";
		Component current = currentUrl.isBlank()
			? Component.translatable("screen.electronic_device_mod.tv_media.none")
			: Component.translatable("screen.electronic_device_mod.tv_media.current_url", currentUrl);
		Component displayedCurrent = Component.literal(font.plainSubstrByWidth(
			current.getString(), Math.max(40, Math.min(width - 32, 500))
		));
		context.centeredText(font, displayedCurrent, center, top + 15, powered ? 0xDDFFDD : 0xAAAAAA);
		if (!currentUrl.isBlank()) {
			context.centeredText(
				font,
				Component.translatable("screen.electronic_device_mod.tv_media.position", formatDuration(playbackPosition)),
				center, top + 27, 0xAAAAAA
			);
		}

		context.centeredText(
			font,
			Component.translatable("screen.electronic_device_mod.tv_media.playlist", mediaUrls.size(), maxSavedVideos),
			center, top + 66, 0xDDDDDD
		);
		if (mediaUrls.isEmpty()) {
			context.centeredText(
				font, Component.translatable("screen.electronic_device_mod.tv_media.empty_list"),
				center, top + 82, 0x888888
			);
		}

		int paginationY = top + 78 + rowsPerPage * ROW_HEIGHT;
		context.centeredText(
			font,
			Component.translatable("screen.electronic_device_mod.tv_media.page", page + 1, maxPage() + 1),
			center, paginationY + 6, 0xAAAAAA
		);

		String diagnostic = WaterMediaBridge.lastDiagnostic();
		boolean showingDiagnostic = status.getString().isBlank() && !diagnostic.isBlank();
		Component shownStatus = showingDiagnostic ? Component.literal(diagnostic) : status;
		drawWrappedStatus(
			context, shownStatus, center, paginationY + 50,
			showingDiagnostic ? 0xFF5555 : statusColor
		);
	}

	private void drawWrappedStatus(GuiGraphicsExtractor context, Component message, int center, int y, int color) {
		int maxWidth = Math.max(40, Math.min(width - 32, 600));
		List<FormattedCharSequence> lines = font.split(message, maxWidth);
		int maximumLines = Math.max(1, Math.min(4, (height - y - 2) / 10));
		for (int index = 0; index < Math.min(lines.size(), maximumLines); index++) {
			context.centeredText(font, lines.get(index), center, y + index * 10, color);
		}
	}

	private static String compactUrl(String url) {
		String compact = url
			.replaceFirst("^https://(www\\.)?", "")
			.replaceFirst("^https://", "");
		return compact.length() > 72 ? compact.substring(0, 69) + "..." : compact;
	}

	private static String formatDuration(long milliseconds) {
		long seconds = Math.max(0, milliseconds / 1000);
		return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private final class VolumeSlider extends AbstractSliderButton {
		private VolumeSlider(int x, int y, int width, int height) {
			super(x, y, width, height, Component.empty(), volume / 100.0);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable(
				"screen.electronic_device_mod.tv_media.volume",
				Math.max(0, Math.min((int) Math.round(value * 100.0), 100))
			));
		}

		@Override
		protected void applyValue() {
			int nextVolume = Math.max(0, Math.min((int) Math.round(value * 100.0), 100));
			if (nextVolume != MediaScreen.this.volume) MediaScreen.this.setVolume(nextVolume);
		}
	}
}
