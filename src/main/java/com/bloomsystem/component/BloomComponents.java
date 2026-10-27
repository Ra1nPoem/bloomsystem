package com.bloomsystem.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

/**
 * Registers {@link BloomPlayerComponent} on all players via CCA.
 *
 * <p>fabric.mod.json: {@code "cardinal-components": ["com.bloomsystem.component.BloomComponents"]}</p>
 */
public final class BloomComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, BloomPlayerComponent.KEY, BloomPlayerComponentImpl::new);
    }

    /** Concrete CCA component implementation. */
    public static final class BloomPlayerComponentImpl implements BloomPlayerComponent {
        private final PlayerEntity player;
        private int phase;
        private float hueOffset;

        public BloomPlayerComponentImpl(PlayerEntity player) {
            this.player = player;
        }

        @Override
        public int phase() {
            return phase;
        }

        @Override
        public void setPhase(int phase) {
            this.phase = phase;
            BloomPlayerComponent.KEY.sync(this.player);
        }

        @Override
        public float hueOffset() {
            return hueOffset;
        }

        @Override
        public void setHueOffset(float hueOffset) {
            this.hueOffset = hueOffset;
            BloomPlayerComponent.KEY.sync(this.player);
        }

        @Override
        public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            this.phase = nbt.getInt("phase");
            this.hueOffset = nbt.getFloat("hue");
        }

        @Override
        public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putInt("phase", this.phase);
            nbt.putFloat("hue", this.hueOffset);
        }
    }
}
