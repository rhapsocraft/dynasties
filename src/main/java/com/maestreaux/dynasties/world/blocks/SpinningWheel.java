package com.maestreaux.dynasties.world.blocks;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.blockentity.SpinningWheelBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpinningWheel extends BaseEntityBlock implements EntityBlock {
    public static final MapCodec<SpinningWheel> CODEC = simpleCodec(SpinningWheel::new);

    public SpinningWheel(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SpinningWheel(String name) {
        this(
                Properties.of()
                        .noOcclusion()
                        .lightLevel((state) -> 15)
                        .isSuffocating((p1, p2, p3) -> false)
                        .setId(ResourceKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SpinningWheelBlockEntity(blockPos, blockState);
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState();
    }
}
