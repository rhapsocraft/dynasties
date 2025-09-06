package com.maestreaux.dynasties.world.entities.blockentity;

import com.maestreaux.dynasties.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CampfirePotBlockEntity extends BlockEntity {
    public CampfirePotBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public CampfirePotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.CAMPFIRE_POT_BE.get(), pPos, pBlockState);
    }
}
