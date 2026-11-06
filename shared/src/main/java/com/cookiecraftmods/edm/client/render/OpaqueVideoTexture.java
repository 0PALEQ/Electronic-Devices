package com.cookiecraftmods.edm.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

/** Video frames are opaque, even when a decoder supplies unused alpha bytes. */
public final class OpaqueVideoTexture {
    private OpaqueVideoTexture() { }

    /** Called on the render thread with a WATERMeDIA-owned 2D texture. */
    public static int prepare(int textureId) {
        if (textureId <= 0) return -1;
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            // Swizzle only alpha: preserve the decoder's RGB format and color channels.
            // Apply for each frame in case the decoder reuses or reconfigures its GL id.
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_ONE);
        } finally {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previous);
        }
        return textureId;
    }
}
