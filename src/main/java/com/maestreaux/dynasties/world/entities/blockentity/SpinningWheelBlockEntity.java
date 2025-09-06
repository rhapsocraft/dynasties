package com.maestreaux.dynasties.world.entities.blockentity;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.init.ModBlockEntityTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SpinningWheelBlockEntity extends WorkstationBlockEntity{
    public SpinningWheelBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public SpinningWheelBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.SPINNING_WHEEL_BE.get(), pPos, pBlockState);
    }

    @Override
    public void work(Production<?,?,?> production, AbstractDynastyVillager villager) {
        if (this.currentProduction == null) {
            this.currentProduction = production;
        }

        var requirement = this.currentProduction.getRequirement();

        if (!this.started()) {
            requirement.collectCost(villager);
            this.addProgress(1);
        } else {
            this.addProgress(10);
        }

        if (this.workProgress > this.maxWorkProgress) {
            this.complete(villager);
        }
    }
}
