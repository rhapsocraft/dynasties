package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Building;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class ModBuildings {
    public static final DeferredRegister<Building> BUILDINGS = DeferredRegister.create(new ResourceLocation("building"), DynastiesMod.MODID);
    public static Supplier<IForgeRegistry<Building>> BUILDINGS_REGISTRY;

    public static final RegistryObject<Building> BASIC_HOUSE = BUILDINGS.register("basic_house", () -> new Building("basic_house", "structures/basic_house.nbt"));

    public static void register(IEventBus bus) {
        BUILDINGS_REGISTRY = BUILDINGS.makeRegistry(() -> RegistryBuilder.of(new ResourceLocation("building")));
        BUILDINGS.register(bus);
    }
}
