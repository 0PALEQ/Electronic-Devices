package com.cookiecraftmods.edm.client.render;

import com.cookiecraftmods.edm.block.MountedTVBlock;
import com.cookiecraftmods.edm.block.TV2Block;
import com.cookiecraftmods.edm.block.TV2StandingBlock;
import com.cookiecraftmods.edm.block.TVBlock;
import com.cookiecraftmods.edm.block.entity.TvBlockEntity;
import com.cookiecraftmods.edm.client.MediaPlaybackManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class TvBlockEntityRenderer implements BlockEntityRenderer<TvBlockEntity, TvBlockEntityRenderer.State> {
	public TvBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(
		TvBlockEntity tv, State state, float tickProgress, Vec3 cameraPos,
		ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(tv, state, tickProgress, cameraPos, crumblingOverlay);
		state.tvBlockState = tv.getBlockState();
		// Resolved here (off the immediate draw call) since this touches the WATERMeDIA
		// reflection bridge and per-listener distance/volume synchronization, not just vertices.
		state.videoTexture = MediaPlaybackManager.videoTexture(tv);
	}

	@Override
	public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
		if (state.videoTexture == null) return;

		ScreenArea area = area(state.tvBlockState);
		if (area == null) return;

		matrices.pushPose();
		matrices.translate(0.5, 0, 0.5);
		matrices.mulPose(Axis.YP.rotationDegrees(rotation(state.tvBlockState)));
		matrices.translate(-0.5, 0, -0.5);

		// Use Minecraft's complete emissive-entity path rather than a bare unlit quad.
		// Shader packs replace vanilla programs and expect these color, overlay, light,
		// and normal attributes; leaving them absent can make the sampled video yellow.
		RenderType layer = RenderTypes.entityTranslucentEmissive(state.videoTexture);
		queue.submitCustomGeometry(matrices, layer, (entry, vertexConsumer) -> {
			Matrix4f matrix = entry.pose();
			vertex(vertexConsumer, matrix, entry, area.minX, area.minY, area.z, 1, 1);
			vertex(vertexConsumer, matrix, entry, area.minX, area.maxY, area.z, 1, 0);
			vertex(vertexConsumer, matrix, entry, area.maxX, area.maxY, area.z, 0, 0);
			vertex(vertexConsumer, matrix, entry, area.maxX, area.minY, area.z, 0, 1);
		});

		matrices.popPose();
	}

	private static void vertex(
		VertexConsumer buffer, Matrix4f matrix, PoseStack.Pose entry,
		float x, float y, float z, float u, float v
	) {
		buffer.addVertex(matrix, x, y, z)
			.setColor(255, 255, 255, 255)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(0xF000F0)
			.setNormal(entry, 0, 0, -1);
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
		if (!state.hasProperty(TVBlock.FACING)) return 0;
		Direction facing = state.getValue(TVBlock.FACING);
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
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}

	private record ScreenArea(float minX, float maxX, float minY, float maxY, float z) {
	}

	public static final class State extends BlockEntityRenderState {
		@Nullable Identifier videoTexture;
		BlockState tvBlockState;
	}
}
