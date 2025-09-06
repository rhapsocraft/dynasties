package com.maestreaux.dynasties.world.entities.blockentity;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WorkstationBlockEntity extends BlockEntity {
    protected int workProgress = 0;
    protected int maxWorkProgress = 100;
    protected Production<?,?,?> currentProduction;

    public WorkstationBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void setProduction(Production<?,?,?> production, AbstractDynastyVillager villager) {
        this.currentProduction = production;
    }

    public void resetProgress() {
        this.workProgress = 0;
    }

    public int getProgress() {
        return this.workProgress;
    }

    public boolean started() {
        return this.workProgress > 0;
    }

    public void complete(AbstractDynastyVillager villager) {
        this.workProgress = 0;

        this.currentProduction.produceProduct(villager);
    }

    public boolean completed() {
        return this.workProgress >= this.maxWorkProgress;
    }

    public void addProgress(int progressToAdd) {
        this.workProgress += progressToAdd;
    }

    public Production<?, ?, ?> getProduction() {
        return this.currentProduction;
    }

    public void work(Production<?,?,?> production, AbstractDynastyVillager villager) {

    }
}
