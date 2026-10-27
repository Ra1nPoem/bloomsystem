package com.bloomsystem.client;

import com.bloomsystem.config.BloomConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.Random;

/**
 * HUD 层：三角渐变面板 + 逐字符彩虹发光文字 + 进度条 + 边缘星光。
 */
public final class BloomHud {
    private static final Random RANDOM = new Random(0xB1005L);

    private BloomHud() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!BloomConfig.enabled || !BloomConfig.hudEnabled || !BloomState.active) {
            return;
        }
        float alpha = BloomState.alpha();
        if (alpha <= 0.01F) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.textRenderer == null) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int panelW = Math.min(190, width / 2);
        int panelH = 62;
        int x = 6;
        int y = 6;
        long now = System.currentTimeMillis();

        // 三角渐变面板：逐行收窄
        for (int i = 0; i < panelH; i++) {
            float t = i / (float) panelH;
            int half = (int) (panelW * 0.5F * (0.1F + 0.9F * t));
            int cx = x + panelW / 2;
            int rgb = lerp(0x120826, 0x4A2A8A, t);
            int a = (int) (78 * alpha);
            context.fill(cx - half, y + i, cx + half, y + i + 1, (a << 24) | rgb);
        }
        // 底部发光边
        int bottomY = y + panelH - 1;
        context.fill(x + panelW / 5, bottomY, x + panelW - panelW / 5, bottomY + 1,
                ((int) (160 * alpha) << 24) | 0x9FEBFF);

        int centerX = x + panelW / 2;
        drawRainbowText(context, client, "BLOOM SYSTEM", centerX, y + 6, 0.9F, now, alpha);
        drawRainbowText(context, client, "棱镜法阵 · PRISM RITUAL", centerX, y + 18, 0.6F, now + 400, alpha);

        String phase = switch (BloomState.TIMELINE.phase()) {
            case "deploy" -> "阶段 1/5 · 法阵展开";
            case "charge" -> "阶段 2/5 · 充能";
            case "burst" -> "阶段 3/5 · 棱镜爆发";
            case "sustain" -> "阶段 4/5 · 维持";
            case "fade" -> "阶段 5/5 · 淡出";
            default -> "待机";
        };
        int textColor = ((int) (alpha * 230) << 24) | 0xE8D8FF;
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(phase), centerX, y + 28, textColor);

        int barW = (int) (panelW * 0.72F);
        int barX = centerX - barW / 2;
        int barY = y + 41;
        context.fill(barX - 1, barY - 1, barX + barW + 1, barY + 7, ((int) (alpha * 180) << 24) | 0x1A0B33);
        int filled = (int) (barW * BloomState.TIMELINE.progress());
        for (int i = 0; i < filled; i++) {
            float hue = ((now * 0.0004F) + i * 0.012F) % 1.0F;
            int rgb = Color.HSBtoRGB(hue, 0.8F, 1.0F) & 0x00FFFFFF;
            context.fill(barX + i, barY, barX + i + 1, barY + 6, ((int) (alpha * 235) << 24) | rgb);
        }

        // 边缘星光
        int seed = (int) (now / 90L);
        Random random = new Random(seed);
        for (int i = 0; i < 16; i++) {
            int sx = x + 6 + random.nextInt(panelW - 12);
            int sy = y + 6 + random.nextInt(panelH - 14);
            int a = (int) (alpha * (0.35F + 0.65F * random.nextFloat()) * 200);
            int size = random.nextBoolean() ? 1 : 2;
            context.fill(sx, sy, sx + size, sy + size, (a << 24) | 0xFFFFFF);
        }
    }

    private static void drawRainbowText(DrawContext context, MinecraftClient client, String text,
                                        int centerX, int y, float scale, long time, float alpha) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) centerX, (float) y, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        int total = client.textRenderer.getWidth(text);
        int penX = -total / 2;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int charWidth = client.textRenderer.getWidth(ch);
            float hue = ((time * 0.00035F) + i * 0.045F) % 1.0F;
            int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F) & 0x00FFFFFF;
            int glow = ((int) (alpha * 100) << 24) | rgb;
            int main = ((int) (alpha * 255) << 24) | rgb;
            context.drawText(client.textRenderer, Text.literal(ch), penX - 1, -1, glow, false);
            context.drawText(client.textRenderer, Text.literal(ch), penX + 1, 1, glow, false);
            context.drawText(client.textRenderer, Text.literal(ch), penX, 0, main, false);
            penX += charWidth;
        }
        matrices.pop();
    }

    private static int lerp(int from, int to, float t) {
        int r = (int) (((from >> 16) & 0xFF) * (1 - t) + ((to >> 16) & 0xFF) * t);
        int g = (int) (((from >> 8) & 0xFF) * (1 - t) + ((to >> 8) & 0xFF) * t);
        int b = (int) ((from & 0xFF) * (1 - t) + (to & 0xFF) * t);
        return (r << 16) | (g << 8) | b;
    }
}
