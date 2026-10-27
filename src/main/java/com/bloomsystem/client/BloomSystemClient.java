package com.bloomsystem.client;

import com.bloomsystem.config.BloomConfig;
import com.bloomsystem.network.BloomNetwork;
import com.bloomsystem.network.BloomTriggerPayload;
import foundry.veil.fabric.event.FabricVeilRendererAvailableEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.fabric.event.FabricVeilRenderLevelStageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BloomSystem client entrypoint.
 *
 * <p>Registers the server trigger receiver, world render layer, HUD layer and Veil post pipeline.
 * A small auto-demo is included for the vertical slice; in production only /bloom trigger is used.</p>
 */
public final class BloomSystemClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bloomsystem");
    private static int joinTicks;
    private static boolean demoStarted;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BloomTriggerPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    LOGGER.info("[BloomSystem] trigger skill={} pos=({},{},{})",
                            payload.skillId(), payload.x(), payload.y(), payload.z());
                    BloomState.start(payload);
                }));

        BloomConfig.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommandManager.literal("bloomconfig").executes(context -> {
                    context.getSource().getClient().setScreen(BloomConfigScreen.create(context.getSource().getClient().currentScreen));
                    return 1;
                })));
        KeyBinding configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bloomsystem.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.bloomsystem"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.world.setTimeOfDay(18000L);
            }
            BloomState.tick();
            if (client.player == null || client.world == null) {
                joinTicks = 0;
                demoStarted = false;
                return;
            }
            if (!demoStarted && ++joinTicks > 40) {
                demoStarted = true;
                startDemo(client);
            }
            if (demoStarted && !BloomState.active && BloomState.TIMELINE.finished()) {
                startDemo(client);
            }
        });

        FabricVeilRenderLevelStageEvent.EVENT.register((stage, levelRenderer, bufferSource, matrixStack,
                frustumMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum) -> {
            if (stage == VeilRenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                BloomWorldRenderer.renderVeil(matrixStack, bufferSource, camera);
            }
        });
        HudRenderCallback.EVENT.register(BloomHud::render);

        FabricVeilRendererAvailableEvent.EVENT.register(renderer -> {
            try {
                renderer.getPostProcessingManager().add(BloomNetwork.CHROMATIC_PIPELINE);
                LOGGER.info("[BloomSystem] Veil chromatic post pipeline registered id={}",
                        BloomNetwork.CHROMATIC_PIPELINE);
            } catch (Throwable throwable) {
                LOGGER.warn("[BloomSystem] failed to register Veil post pipeline", throwable);
            }
        });

        LOGGER.info("[BloomSystem] client initialized veilPost={} hud={} worldFx={}",
                BloomConfig.dispersionStrength, BloomConfig.hudEnabled, BloomConfig.enabled);
    }

    private static void startDemo(net.minecraft.client.MinecraftClient client) {
        client.world.setTimeOfDay(18000L);
        client.player.setYaw(0.0F);
        client.player.setPitch(30.0F);
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ() + 5.2;
        BloomState.start(new BloomTriggerPayload("prism_ritual", x, y, z, System.currentTimeMillis()));
        LOGGER.info("[BloomSystem] demo ritual triggered at ({},{},{})", x, y, z);
    }
}
