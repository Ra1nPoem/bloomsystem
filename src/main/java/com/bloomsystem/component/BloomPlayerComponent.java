package com.bloomsystem.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

/**
 * CCA player state for BloomSystem spell phases / hue offset.
 * Server writes the skill phase; client reads it for HUD/weapon effects.
 */
public interface BloomPlayerComponent extends Component {
    ComponentKey<BloomPlayerComponent> KEY =
            ComponentRegistry.getOrCreate(Identifier.of("bloomsystem", "player"), BloomPlayerComponent.class);

    int phase();

    void setPhase(int phase);

    float hueOffset();

    void setHueOffset(float hueOffset);

    @Override
    default void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        // implementation lives in the concrete class
    }

    @Override
    default void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        // implementation lives in the concrete class
    }
}
