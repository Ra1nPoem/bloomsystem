package com.bloomsystem.client;

import com.bloomsystem.config.BloomConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * World-space render layer: multi-layer wireframe magic circle + radial light bands + rotating wedge blades.
 *
 * <p>Primary entry point is {@link #renderVeil}, which is driven by Veil's
 * {@code VeilRenderLevelStageEvent} (AFTER_ENTITIES) and uses Veil's MatrixStack/BufferSource.
 * A Fabric {@link WorldRenderContext} fallback is retained for environments without Veil.</p>
 */
public final class BloomWorldRenderer {
    private BloomWorldRenderer() {
    }

    /** Veil world stage entry point. */
    public static void renderVeil(foundry.veil.api.client.render.MatrixStack matrixStack,
                                  VertexConsumerProvider consumers, Camera camera) {
        if (matrixStack == null || consumers == null) {
            return;
        }
        MatrixStack vanilla = matrixStack.toPoseStack();
        drawScene(vanilla.peek().getPositionMatrix(), consumers, camera.getPos());
    }

    /** Fabric world render fallback. */
    public static void render(WorldRenderContext context) {
        if (context.matrixStack() == null || context.consumers() == null) {
            return;
        }
        drawScene(context.matrixStack().peek().getPositionMatrix(), context.consumers(),
                context.camera().getPos());
    }

    private static void drawScene(Matrix4f matrix, VertexConsumerProvider consumers, Vec3d camera) {
        if (!BloomConfig.enabled || !BloomState.active) {
            return;
        }
        float alpha = BloomState.alpha();
        if (alpha <= 0.001F) {
            return;
        }
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLightning());
        double time = (System.currentTimeMillis() - BloomState.startedAtMs) / 50.0;
        double cx = BloomState.centerX - camera.x;
        double cy = BloomState.centerY - camera.y + 0.04;
        double cz = BloomState.centerZ - camera.z;

        // Rings first (base body).
        drawRing(buffer, matrix, cx, cy, cz, 5.2, 120, time * 0.012, alpha, 0.9F);
        drawRing(buffer, matrix, cx, cy, cz, 4.1, 96, -time * 0.02, alpha, 0.75F);
        drawRing(buffer, matrix, cx, cy, cz, 2.6, 64, time * 0.03, alpha, 0.6F);

        // Radial light bands.
        int rays = 36;
        for (int i = 0; i < rays; i++) {
            double angle = i * Math.PI * 2.0 / rays + time * 0.008;
            float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.12 + i * 0.8);
            float inner = (float) (1.0 + 0.25 * pulse);
            float outer = (float) (6.4 + 1.7 * pulse);
            float halfWidth = 0.035F;
            float hue = (float) ((time * 0.0015 + i * 0.028) % 1.0);
            int rgb = Color.HSBtoRGB(hue, 0.75F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float a = alpha * (0.25F + 0.5F * pulse);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double px = -dz * halfWidth;
            double pz = dx * halfWidth;
            addQuad(buffer, matrix,
                    cx + dx * inner + px, cy + 0.06, cz + dz * inner + pz,
                    cx + dx * inner - px, cy + 0.06, cz + dz * inner - pz,
                    cx + dx * outer - px, cy + 0.06, cz + dz * outer - pz,
                    cx + dx * outer + px, cy + 0.06, cz + dz * outer + pz,
                    r, g, b, a);
        }

        // Rotating wedge blades.
        for (int i = 0; i < 16; i++) {
            double base = i * Math.PI * 2.0 / 16 + time * 0.018;
            double half = 0.12;
            float hue = (float) ((i / 16.0 + time * 0.002) % 1.0);
            int rgb = Color.HSBtoRGB(hue, 0.7F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float a = alpha * 0.07F;
            float inner = 0.9F;
            float outer = 4.0F;
            addQuad(buffer, matrix,
                    cx + Math.cos(base) * inner, cy + 0.07, cz + Math.sin(base) * inner,
                    cx + Math.cos(base) * inner, cy + 0.07, cz + Math.sin(base) * inner,
                    cx + Math.cos(base + half) * outer, cy + 0.07, cz + Math.sin(base + half) * outer,
                    cx + Math.cos(base - half) * outer, cy + 0.07, cz + Math.sin(base - half) * outer,
                    r, g, b, a);
        }

        // Draw rings last so they stay visible over the additive blade quads.
        drawRing(buffer, matrix, cx, cy + 0.09, cz, 5.2, 160, time * 0.012, alpha, 1.1F);
        drawRing(buffer, matrix, cx, cy + 0.09, cz, 4.1, 128, -time * 0.02, alpha, 0.95F);
        drawRing(buffer, matrix, cx, cy + 0.09, cz, 2.6, 96, time * 0.03, alpha, 0.8F);

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(RenderLayer.getLightning());
        }

        RenderLayer bloomType = VeilRenderType.get(Identifier.of("bloomsystem", "circle_bloom"));
        if (bloomType != null) {
            VertexConsumer bloom = consumers.getBuffer(bloomType);
            drawRing(bloom, matrix, cx, cy + 0.09, cz, 5.2, 96, time * 0.012, 1.0F, 1.0F);
            drawRing(bloom, matrix, cx, cy + 0.09, cz, 4.1, 72, -time * 0.02, 1.0F, 0.9F);
            drawRing(bloom, matrix, cx, cy + 0.09, cz, 2.6, 48, time * 0.03, 1.0F, 0.8F);
            if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
                immediate.draw(bloomType);
            }
        }

    }

    private static void drawRing(VertexConsumer buffer, Matrix4f matrix, double cx, double cy, double cz,
                                 double radius, int segments, double rotation, float alpha, float brightness) {
        for (int i = 0; i < segments; i++) {
            double a1 = i * Math.PI * 2.0 / segments + rotation;
            double a2 = (i + 1) * Math.PI * 2.0 / segments + rotation;
            float hue = (float) ((rotation * 0.1 + i / (double) segments) % 1.0);
            int rgb = Color.HSBtoRGB(hue, 0.65F, 1.0F);
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float width = 0.055F;
            double dx1 = Math.cos(a1);
            double dz1 = Math.sin(a1);
            double dx2 = Math.cos(a2);
            double dz2 = Math.sin(a2);
            addQuad(buffer, matrix,
                    cx + dx1 * (radius - width), cy, cz + dz1 * (radius - width),
                    cx + dx1 * (radius + width), cy, cz + dz1 * (radius + width),
                    cx + dx2 * (radius + width), cy, cz + dz2 * (radius + width),
                    cx + dx2 * (radius - width), cy, cz + dz2 * (radius - width),
                    r, g, b, alpha * 0.75F * brightness);
        }
    }

    private static void addQuad(VertexConsumer buffer, Matrix4f matrix,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                double x3, double y3, double z3,
                                double x4, double y4, double z4,
                                float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a);
        buffer.vertex(matrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a);
        buffer.vertex(matrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a);
    }
}
