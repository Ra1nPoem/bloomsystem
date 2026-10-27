package com.bloomsystem.client;

import com.bloomsystem.network.BloomTriggerPayload;

/**
 * 客户端技能演出状态（需求要求 CCA 存储；迁移时把字段移入 PlayerEntity 组件即可）。
 */
public final class BloomState {
    public static boolean active;
    public static String skillId = "prism_ritual";
    public static double centerX;
    public static double centerY;
    public static double centerZ;
    public static long startedAtMs;
    public static final BloomTimeline TIMELINE = BloomTimeline.prismRitual();

    private BloomState() {
    }

    public static void start(BloomTriggerPayload payload) {
        active = true;
        skillId = payload.skillId();
        centerX = payload.x();
        centerY = payload.y();
        centerZ = payload.z();
        startedAtMs = System.currentTimeMillis();
        TIMELINE.reset();
    }

    public static void tick() {
        if (!active) {
            return;
        }
        TIMELINE.advance();
        if (TIMELINE.finished()) {
            active = false;
        }
    }

    public static float alpha() {
        if (!active) {
            return 0.0F;
        }
        int tick = TIMELINE.tick();
        if (tick < 18) {
            return tick / 18.0F;
        }
        if (tick > 220) {
            return Math.max(0.0F, 1.0F - (tick - 220) / 40.0F);
        }
        return 1.0F;
    }
}
