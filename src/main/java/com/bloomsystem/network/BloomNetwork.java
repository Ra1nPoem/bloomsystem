package com.bloomsystem.network;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;

/**
 * 服务端触发入口：/bloom trigger [目标] [技能 ID]。
 * 服务端只发 {@link BloomTriggerPayload}，不保存任何动画状态。
 */
public final class BloomNetwork {
    public static final String DEFAULT_SKILL = "prism_ritual";
    public static final Identifier CHROMATIC_PIPELINE = Identifier.of("bloomsystem", "chromatic");

    private BloomNetwork() {
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(BloomTriggerPayload.ID, BloomTriggerPayload.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("bloom")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("trigger")
                                .executes(context -> trigger(context, context.getSource().getPlayerOrThrow(),
                                        DEFAULT_SKILL))
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(context -> trigger(context, EntityArgumentType.getPlayers(context, "targets"),
                                                DEFAULT_SKILL))
                                        .then(CommandManager.argument("skill", StringArgumentType.word())
                                                .executes(context -> trigger(context,
                                                        EntityArgumentType.getPlayers(context, "targets"),
                                                        StringArgumentType.getString(context, "skill"))))))));
    }

    private static int trigger(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, String skill) {
        return trigger(context, java.util.List.of(player), skill);
    }

    private static int trigger(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets,
                               String skill) {
        long now = System.currentTimeMillis();
        int sent = 0;
        for (ServerPlayerEntity player : targets) {
            BloomTriggerPayload payload = new BloomTriggerPayload(skill,
                    player.getX(), player.getY(), player.getZ(), now);
            ServerPlayNetworking.send(player, payload);
            sent++;
        }
        final int count = sent;
        context.getSource().sendFeedback(() -> Text.literal(
                "[BloomSystem] triggered skill=" + skill + " players=" + count), false);
        return count;
    }
}
