package com.bloomsystem.client;

import com.bloomsystem.config.BloomConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * YACL configuration screen for BloomSystem.
 *
 * <p>Opened with the configured keybind (default B) or {@code /bloomconfig}.</p>
 */
public final class BloomConfigScreen {
    private BloomConfigScreen() {
    }

    public static Screen create(Screen parent) {
        Option<Boolean> enabled = Option.<Boolean>createBuilder()
                .name(Text.literal("BloomSystem 总开关"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Text.literal("关闭后不渲染世界法阵/HUD 特效")))
                .binding(BloomConfig.enabled, () -> BloomConfig.enabled, value -> BloomConfig.enabled = value)
                .controller(BooleanControllerBuilder::create)
                .build();

        Option<Float> bloom = Option.<Float>createBuilder()
                .name(Text.literal("Bloom 强度"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Text.literal("Veil bloom 输出强度")))
                .binding(BloomConfig.bloomStrength, () -> BloomConfig.bloomStrength, value -> BloomConfig.bloomStrength = value)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0.0F, 2.0F).step(0.05F))
                .build();

        Option<Float> dispersion = Option.<Float>createBuilder()
                .name(Text.literal("色散强度"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Text.literal("后处理 RGB 色差/色散强度")))
                .binding(BloomConfig.dispersionStrength, () -> BloomConfig.dispersionStrength, value -> BloomConfig.dispersionStrength = value)
                .controller(option -> FloatSliderControllerBuilder.create(option).range(0.0F, 2.0F).step(0.05F))
                .build();

        Option<Boolean> hud = Option.<Boolean>createBuilder()
                .name(Text.literal("HUD 发光文字"))
                .binding(true, () -> BloomConfig.hudEnabled, value -> BloomConfig.hudEnabled = value)
                .controller(BooleanControllerBuilder::create)
                .build();

        ConfigCategory category = ConfigCategory.createBuilder()
                .name(Text.literal("渲染"))
                .option(enabled)
                .option(bloom)
                .option(dispersion)
                .option(hud)
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("BloomSystem 配置"))
                .category(category)
                .save(BloomConfig::save)
                .build()
                .generateScreen(parent);
    }
}
