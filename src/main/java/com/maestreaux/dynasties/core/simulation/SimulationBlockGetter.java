package com.maestreaux.dynasties.core.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class SimulationBlockGetter implements BlockGetter {
    private ServerLevel level;

    public SimulationBlockGetter(ServerLevel level) {
        this.level = level;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return null;
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return null;
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }
}
