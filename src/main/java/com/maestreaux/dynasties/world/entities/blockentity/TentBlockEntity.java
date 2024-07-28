package com.maestreaux.dynasties.world.entities.blockentity;

import com.maestreaux.dynasties.init.ModBlockEntityTypes;
import com.maestreaux.dynasties.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TentBlockEntity extends BlockEntity {
    public TentBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public TentBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.TENT_BE.get(), pPos, pBlockState);
    }
}
