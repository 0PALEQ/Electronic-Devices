package com.cookiecraftmods.edm.client.render;

import com.mojang.blaze3d.opengl.GlTextureView;

/** Paired with {@link ExternalGlTexture}: never releases the wrapped texture on close. */
final class ExternalGlTextureView extends GlTextureView {
	ExternalGlTextureView(ExternalGlTexture texture) {
		super(texture, 0, 1);
	}

	@Override
	public void close() {
	}
}
