package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.blocks.Tent;
import com.maestreaux.dynasties.world.entities.blockentity.TentBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DynastiesMod.MODID);

    public static final RegistryObject<BlockEntityType<TentBlockEntity>> TENT_BE = BLOCK_ENTITY_TYPES.register("tent_be", () -> new BlockEntityType<>(TentBlockEntity::new, Set.of(ModBlocks.TENT.get())));
    public static final RegistryObject<BlockEntityType<CampfireBlockEntity>> CAMPFIRE_POT_BE = BLOCK_ENTITY_TYPES.register("campfire_pot_be", () -> new BlockEntityType<>(CampfireBlockEntity::new, Set.of(ModBlocks.CAMPFIRE_POT.get())));
}
