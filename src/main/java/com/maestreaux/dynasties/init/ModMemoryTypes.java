package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Plot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class ModMemoryTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, DynastiesMod.MODID);
    public static final RegistryObject<MemoryModuleType<Plot>> HOME_PLOT = MEMORY_TYPES.register("home_plot", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Plot>> AVAILABLE_PLOT = MEMORY_TYPES.register("available_plot", () -> new MemoryModuleType<>(Optional.empty()));
}
