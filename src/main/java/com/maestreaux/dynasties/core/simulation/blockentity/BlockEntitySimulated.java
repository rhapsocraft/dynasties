package com.maestreaux.dynasties.core.simulation.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntitySimulated<T extends BlockEntity> {
    private final BlockPos pos;
    private final ServerLevel level;

    public BlockEntitySimulated(T blockEntity) {
        this.pos = blockEntity.getBlockPos();
        this.level = (ServerLevel) blockEntity.getLevel();
    }

    public BlockEntity getBlockEntity() {
        return this.level.getBlockEntity(this.pos);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public ServerLevel level() {
        return this.level;
    }
}
