package com.cookiecraftmods.edm.client.render;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.texture.GlTexture;

/**
 * Wraps a GL texture id WATERMeDIA already owns and re-uploads frame data into, so it can be
 * sampled through Minecraft's GpuTexture-based render pipeline. Never frees the wrapped id:
 * WATERMeDIA controls its lifecycle, not Minecraft's TextureManager, so {@link #close()} is a
 * no-op even though the base class would otherwise delete the GL texture.
 */
final class ExternalGlTexture extends GlTexture {
	ExternalGlTexture(String label, int glId) {
		super(GpuTexture.USAGE_TEXTURE_BINDING, label, TextureFormat.RGBA8, 1, 1, 1, 1, glId);
	}

	@Override
	public void close() {
	}
}
