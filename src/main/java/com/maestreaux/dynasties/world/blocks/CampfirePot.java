package com.maestreaux.dynasties.world.blocks;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CampfirePot extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<CampfirePot> CODEC = simpleCodec(CampfirePot::new);

    public CampfirePot(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public CampfirePot(String name) {
        this(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .lightLevel((state) -> 15)
                        .isSuffocating((p1, p2, p3) -> false)
                        .setId(ResourceKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CampfirePotBlockEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    protected @NotNull VoxelShape getVisualShape(BlockState p_312193_, BlockGetter p_310654_, BlockPos p_310658_, CollisionContext p_311129_) {
        return Shapes.empty();
    }

    protected float getShadeBrightness(BlockState p_312407_, BlockGetter p_310193_, BlockPos p_311965_) {
        return 1.0F;
    }

    protected boolean propagatesSkylightDown(BlockState p_312717_) {
        return true;
    }
}
