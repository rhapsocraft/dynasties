package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DynastiesMod.MODID);

    public static final RegistryObject<EntityType<DynastiesVillager>> DYNASTY_VILLAGER = ENTITY_TYPES.register("dynasty_villager", () -> EntityType.Builder.<DynastiesVillager>of(DynastiesVillager::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "dynasty_villager"))));
}
