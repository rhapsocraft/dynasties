package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.blocks.CampfirePot;
import com.maestreaux.dynasties.world.blocks.Tent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DynastiesMod.MODID);

    public static final RegistryObject<Block> TENT = register("tent", Tent::new);
    public static final RegistryObject<Block> CAMPFIRE_POT = register("campfire_pot", CampfirePot::new);

    public static RegistryObject<Block> register(String name, Function<String, Block> blockFn) {
        return BLOCKS.register(name, () -> blockFn.apply(name));
    }
}
