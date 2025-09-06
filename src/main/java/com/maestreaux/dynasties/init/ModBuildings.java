package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Building;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class ModBuildings {
    public static final DeferredRegister<Building> BUILDINGS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "building"), DynastiesMod.MODID);
    public static Supplier<IForgeRegistry<Building>> BUILDINGS_REGISTRY;

    public static final RegistryObject<Building> BASIC_HOUSE = BUILDINGS.register("basic_house", () -> new Building("basic_house", "structures/basic_house.nbt"));
    public static final RegistryObject<Building> BASIC_HOUSE_2 = BUILDINGS.register("basic_house_2", () -> new Building("basic_house_2", "structures/basic_house2.nbt"));
    public static final RegistryObject<Building> SMALL_GARDEN = BUILDINGS.register("small_garden", () -> new Building("small_garden", "structures/small_garden.nbt"));
    public static final RegistryObject<Building> MEDIUM_GARDEN = BUILDINGS.register("medium_garden", () -> new Building("medium_garden", "structures/medium_garden.nbt"));
    public static final RegistryObject<Building> LONG_GARDEN = BUILDINGS.register("long_garden", () -> new Building("long_garden", "structures/long_garden.nbt"));
    public static final RegistryObject<Building> NARROW_GARDEN = BUILDINGS.register("narrow_garden", () -> new Building("narrow_garden", "structures/narrow_garden.nbt"));
    public static final RegistryObject<Building> MEDIUM_RANCH = BUILDINGS.register("medium_ranch", () -> new Building("medium_ranch", "structures/medium_ranch.nbt"));

    public static void register(IEventBus bus) {
        BUILDINGS_REGISTRY = BUILDINGS.makeRegistry(() -> RegistryBuilder.of(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "building")));
        BUILDINGS.register(bus);
    }
}
