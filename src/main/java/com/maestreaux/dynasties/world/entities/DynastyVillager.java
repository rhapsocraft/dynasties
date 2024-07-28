package com.maestreaux.dynasties.world.entities;

import com.maestreaux.dynasties.init.ModBlocks;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.blocks.Tent;
import com.maestreaux.dynasties.world.entities.ai.brain.behaviour.ClaimPlot;
import com.maestreaux.dynasties.world.entities.ai.brain.behaviour.DoConstruction;
import com.maestreaux.dynasties.world.entities.ai.brain.behaviour.GoHome;
import com.maestreaux.dynasties.world.entities.ai.brain.sensor.AvailablePlotsSensor;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DynastyVillager extends AbstractDynastyVillager implements SmartBrainOwner<DynastyVillager>, VillagerDataHolder {
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA;
    public DynastyVillager(EntityType<DynastyVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public DynastyVillager(Level pLevel, Zone homeZone) {
        super(pLevel, homeZone);
    }

    @Override
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        // TODO: Remove later. Only necessary for visual purposes
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER_DATA);
    }

    @Override
    public void setVillagerData(VillagerData villagerData) {
        this.entityData.set(DATA_VILLAGER_DATA, villagerData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
    }

    private void setPosToBed(BlockPos blockPos, boolean isTent, Direction direction) {
        var offset = new Vec3(0.5D + (direction.getStepX() * 0.5D), isTent ?  0.2300D : 0.6875D, 0.5D + (direction.getStepZ() * 0.5D));

        this.setPos(blockPos.getX() + offset.x, blockPos.getY() + offset.y, blockPos.getZ() + offset.z);
    }

    @Override
    public void startSleeping(BlockPos pPos) {
        if (this.isPassenger()) {
            this.stopRiding();
        }

        BlockState blockstate = this.level().getBlockState(pPos);
        if (blockstate.isBed(this.level(), pPos, this)) {
            blockstate.setBedOccupied(this.level(), pPos, this, true);

            this.setPose(Pose.SLEEPING);
            this.setPosToBed(pPos, blockstate.is(ModBlocks.TENT.get()), blockstate.getValue(Tent.FACING));
            this.setSleepingPos(pPos);
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = true;
        }

    }

    @Override
    public List<? extends ExtendedSensor<? extends DynastyVillager>> getSensors() {
        return ObjectArrayList.of(
                new AvailablePlotsSensor<>(),
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>()
                //new AvailableTentsSensor<>()
        );
    }

    @Override
    public BrainActivityGroup<DynastyVillager> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new ClaimPlot<>(),
                new GoHome<>(),
                //new SleepInTent<>(),
                new DoConstruction<>()
        );
    }

    @Override
    public BrainActivityGroup<DynastyVillager> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new MoveToWalkTarget<>()
        );
    }

    static {
        DATA_VILLAGER_DATA = SynchedEntityData.defineId(DynastyVillager.class, EntityDataSerializers.VILLAGER_DATA);
    }
}
