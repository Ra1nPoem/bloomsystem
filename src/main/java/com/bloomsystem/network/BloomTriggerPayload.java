package com.bloomsystem.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 服务端 → 客户端的最小触发包：只带技能 ID、触发位置、时间戳。
 * 所有动画/粒子/渲染参数由客户端本地计算。
 */
public record BloomTriggerPayload(String skillId, double x, double y, double z, long timestamp) implements CustomPayload {
    public static final Id<BloomTriggerPayload> ID =
            new Id<>(Identifier.of("bloomsystem", "trigger"));

    public static final PacketCodec<RegistryByteBuf, BloomTriggerPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeString(payload.skillId, 64);
                buf.writeDouble(payload.x);
                buf.writeDouble(payload.y);
                buf.writeDouble(payload.z);
                buf.writeLong(payload.timestamp);
            },
            buf -> new BloomTriggerPayload(buf.readString(64), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
