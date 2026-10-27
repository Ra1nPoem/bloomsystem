package com.bloomsystem.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Cascade 风格时间线（阶段 + 缓动 + 回调）。后续可平移到 Cascade 的 Timeline API。
 */
public final class BloomTimeline {
    public enum Easing { LINEAR, SMOOTH, STEP }

    public record Stage(String id, int start, int end, Easing easing, Runnable onEnter) {
    }

    private final List<Stage> stages = new ArrayList<>();
    private int tick;
    private int duration = 260;
    private int lastStage = -1;

    public BloomTimeline add(String id, int start, int end, Easing easing, Runnable onEnter) {
        stages.add(new Stage(id, start, end, easing, onEnter));
        duration = Math.max(duration, end);
        return this;
    }

    public void reset() {
        tick = 0;
        lastStage = -1;
    }

    public void advance() {
        tick++;
        int index = currentIndex();
        if (index != lastStage) {
            lastStage = index;
            if (index >= 0 && stages.get(index).onEnter() != null) {
                stages.get(index).onEnter().run();
            }
        }
    }

    public int tick() {
        return tick;
    }

    public int duration() {
        return duration;
    }

    public boolean finished() {
        return tick >= duration;
    }

    public float progress() {
        return Math.min(1.0F, tick / (float) duration);
    }

    public String phase() {
        int index = currentIndex();
        return index < 0 ? "idle" : stages.get(index).id();
    }

    public float easedProgress() {
        int index = currentIndex();
        if (index < 0) {
            return 0.0F;
        }
        Stage stage = stages.get(index);
        float span = Math.max(1, stage.end() - stage.start());
        float t = Math.min(1.0F, Math.max(0.0F, (tick - stage.start()) / span));
        return switch (stage.easing()) {
            case SMOOTH -> t * t * (3.0F - 2.0F * t);
            case STEP -> t < 0.5F ? 0.0F : 1.0F;
            case LINEAR -> t;
        };
    }

    private int currentIndex() {
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            if (tick >= stage.start() && tick <= stage.end()) {
                return i;
            }
        }
        return -1;
    }

    public static BloomTimeline prismRitual() {
        BloomTimeline timeline = new BloomTimeline();
        timeline.add("deploy", 0, 18, Easing.SMOOTH, null);
        timeline.add("charge", 19, 70, Easing.SMOOTH, null);
        timeline.add("burst", 71, 112, Easing.LINEAR, null);
        timeline.add("sustain", 113, 220, Easing.SMOOTH, null);
        timeline.add("fade", 221, 260, Easing.SMOOTH, null);
        return timeline;
    }
}
