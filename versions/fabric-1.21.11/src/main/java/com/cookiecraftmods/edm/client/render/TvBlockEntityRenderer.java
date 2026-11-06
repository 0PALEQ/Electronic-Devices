package com.cookiecraftmods.edm.client.render;

import com.cookiecraftmods.edm.block.MountedTVBlock;
import com.cookiecraftmods.edm.block.TV2Block;
import com.cookiecraftmods.edm.block.TV2StandingBlock;
import com.cookiecraftmods.edm.block.TVBlock;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.MediaPlaybackManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class TvBlockEntityRenderer implements BlockEntityRenderer<TvBlockEntity, TvBlockEntityRenderer.State> {
	public TvBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void updateRenderState(
		TvBlockEntity tv, State state, float tickProgress, Vec3d cameraPos,
		ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
	) {
		BlockEntityRenderer.super.updateRenderState(tv, state, tickProgress, cameraPos, crumblingOverlay);
		// Resolved here (off the immediate draw call) since this touches the WATERMeDIA
		// reflection bridge and per-listener distance/volume synchronization, not just vertices.
		state.videoTexture = MediaPlaybackManager.videoTexture(tv);
	}

	@Override
	public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
		if (state.videoTexture == null) return;

		ScreenArea area = area(state.blockState);
		if (area == null) return;

		matrices.push();
		matrices.translate(0.5, 0, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation(state.blockState)));
		matrices.translate(-0.5, 0, -0.5);

		// Use Minecraft's complete opaque-entity path rather than a bare unlit quad.
		// Shader packs replace vanilla programs and expect these color, overlay, light,
		// and normal attributes; leaving them absent can make the sampled video yellow.
		RenderLayer layer = RenderLayers.entitySolid(state.videoTexture);
		queue.submitCustom(matrices, layer, (entry, vertexConsumer) -> {
			Matrix4f matrix = entry.getPositionMatrix();
			vertex(vertexConsumer, matrix, entry, area.minX, area.minY, area.z, 1, 1);
			vertex(vertexConsumer, matrix, entry, area.minX, area.maxY, area.z, 1, 0);
			vertex(vertexConsumer, matrix, entry, area.maxX, area.maxY, area.z, 0, 0);
			vertex(vertexConsumer, matrix, entry, area.maxX, area.minY, area.z, 0, 1);
		});

		matrices.pop();
	}

	private static void vertex(
		VertexConsumer buffer, Matrix4f matrix, MatrixStack.Entry entry,
		float x, float y, float z, float u, float v
	) {
		buffer.vertex(matrix, x, y, z)
			.color(255, 255, 255, 255)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
			.normal(entry, 0, 0, -1);
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
	public boolean rendersOutsideBoundingBox() {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 64;
	}

	private record ScreenArea(float minX, float maxX, float minY, float maxY, float z) {
	}

	public static final class State extends BlockEntityRenderState {
		@Nullable Identifier videoTexture;
	}
}
