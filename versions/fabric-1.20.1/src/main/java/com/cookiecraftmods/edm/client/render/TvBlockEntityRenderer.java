package com.cookiecraftmods.edm.client.render;

import com.cookiecraftmods.edm.block.MountedTVBlock;
import com.cookiecraftmods.edm.block.TV2Block;
import com.cookiecraftmods.edm.block.TV2StandingBlock;
import com.cookiecraftmods.edm.block.TVBlock;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.MediaPlaybackManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class TvBlockEntityRenderer implements BlockEntityRenderer<TvBlockEntity> {
	public TvBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(
		TvBlockEntity tv, float tickDelta, MatrixStack matrices,
		net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
		int light, int overlay
	) {
		int texture = MediaPlaybackManager.texture(tv);
		if (texture < 0) return;

		ScreenArea area = area(tv.getCachedState());
		if (area == null) return;

		matrices.push();
		matrices.translate(0.5, 0, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation(tv.getCachedState())));
		matrices.translate(-0.5, 0, -0.5);
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		Matrix3f normalMatrix = matrices.peek().getNormalMatrix();
		float[] previousShaderColor = RenderSystem.getShaderColor().clone();

		// The display is an opaque surface; decoder alpha must not blend with the room.
		RenderSystem.disableBlend();
		RenderSystem.disableCull();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		// Use Minecraft's complete opaque-entity path rather than POSITION_TEXTURE.
		// Shader packs replace vanilla programs and expect these color, overlay, light,
		// and normal attributes; leaving them absent can make the sampled video yellow.
		RenderSystem.setShader(GameRenderer::getRenderTypeEntitySolidProgram);
		RenderSystem.setShaderTexture(0, texture);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
		vertex(buffer, matrix, normalMatrix, area.minX, area.minY, area.z, 1, 1);
		vertex(buffer, matrix, normalMatrix, area.minX, area.maxY, area.z, 1, 0);
		vertex(buffer, matrix, normalMatrix, area.maxX, area.maxY, area.z, 0, 0);
		vertex(buffer, matrix, normalMatrix, area.maxX, area.minY, area.z, 0, 1);
		tessellator.draw();
		RenderSystem.setShaderColor(
			previousShaderColor[0], previousShaderColor[1], previousShaderColor[2], previousShaderColor[3]
		);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		matrices.pop();
	}

	private static void vertex(
		BufferBuilder buffer, Matrix4f matrix, Matrix3f normalMatrix,
		float x, float y, float z, float u, float v
	) {
		buffer.vertex(matrix, x, y, z)
			.color(255, 255, 255, 255)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(normalMatrix, 0, 0, -1)
			.next();
	}

	private static ScreenArea area(BlockState state) {
		if (state.getBlock() instanceof TVBlock) {
			return new ScreenArea(-7.75f / 16f, 23.75f / 16f, 1.25f / 16f, 18.75f / 16f, 7.975f / 16f);
		}
		if (state.getBlock() instanceof MountedTVBlock) {
			return new ScreenArea(-7.75f / 16f, 23.75f / 16f, 1.25f / 16f, 18.75f / 16f, 13.725f / 16f);
		}
		if (state.getBlock() instanceof TV2Block) {
			return new ScreenArea(-15.75f / 16f, 31.75f / 16f, 0.25f / 16f, 26.75f / 16f, 13.995f / 16f);
		}
		if (state.getBlock() instanceof TV2StandingBlock) {
			return new ScreenArea(-15.75f / 16f, 31.75f / 16f, 1.25f / 16f, 27.75f / 16f, 11.995f / 16f);
		}
		return null;
	}

	private static float rotation(BlockState state) {
		if (!state.contains(TVBlock.FACING)) return 0;
		Direction facing = state.get(TVBlock.FACING);
		return switch (facing) {
			// Blockstate JSON Y rotations use the opposite sign from MatrixStack's
			// positive-Y convention. Matching that sign keeps side-facing screens on
			// the model instead of reflecting them across the block center.
			case EAST -> -90;
			case SOUTH -> 180;
			case WEST -> 90;
			default -> 0;
		};
	}

	@Override
	public boolean rendersOutsideBoundingBox(TvBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 64;
	}

	private record ScreenArea(float minX, float maxX, float minY, float maxY, float z) {
	}
}
