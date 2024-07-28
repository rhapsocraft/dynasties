package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.blocks.Tent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DynastiesMod.MODID);

    public static final RegistryObject<Block> TENT = BLOCKS.register("tent", () -> new Tent(BlockBehaviour.Properties.of().noOcclusion().isSuffocating((p1, p2, p3) -> false)));
}
