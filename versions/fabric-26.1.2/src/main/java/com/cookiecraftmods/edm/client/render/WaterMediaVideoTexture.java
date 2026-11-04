package com.cookiecraftmods.edm.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.texture.AbstractTexture;

/**
 * Adapts a WATERMeDIA-owned raw GL texture id into Minecraft's TextureManager so the TV
 * renderer can sample it through a normal Identifier-backed RenderLayer. WATERMeDIA re-uploads
 * frame data into the same texture id in place, so this only needs to swap wrappers when the id
 * itself changes (a new player or source).
 */
public final class WaterMediaVideoTexture extends AbstractTexture {
	private int wrappedGlId = -1;

	public WaterMediaVideoTexture() {
		this.sampler = RenderSystem.getSamplerCache()
			.getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
	}

	public void update(int glId) {
		if (glId == this.wrappedGlId) return;
		this.wrappedGlId = glId;
		ExternalGlTexture texture = new ExternalGlTexture("watermedia_tv", glId);
		this.texture = texture;
		this.textureView = new ExternalGlTextureView(texture);
	}

	@Override
	public void close() {
		this.texture = null;
		this.textureView = null;
	}
}
