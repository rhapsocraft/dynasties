package com.maestreaux.dynasties.world.entities;

import com.maestreaux.dynasties.client.ClientHooks;
import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.init.ModBlocks;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.blocks.Tent;
import com.maestreaux.dynasties.world.entities.ai.brain.behaviour.*;
import com.maestreaux.dynasties.world.entities.ai.brain.schedule.BasicSchedule;
import com.maestreaux.dynasties.world.entities.ai.brain.sensor.*;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.InteractWithDoor;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.schedule.SmartBrainSchedule;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.NearbyItemsSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DynastiesVillager extends AbstractDynastyVillager implements SmartBrainOwner<DynastiesVillager> {
    private static final EntityDataAccessor<Boolean> IS_FLEEING = SynchedEntityData.defineId(DynastiesVillager.class, EntityDataSerializers.BOOLEAN);
    private static final List<Item> DESIRED_ITEMS;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState fleeAnimationState = new AnimationState();
    public final AnimationState idleFaceAnimationState = new AnimationState();


    public DynastiesVillager(EntityType<DynastiesVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    public DynastiesVillager(Level pLevel, Zone homeZone) {
        super(pLevel, homeZone);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    @Override
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep(@NotNull ServerLevel level) {
        tickBrain(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    protected void defineSynchedData(SynchedEntityData.Builder synchedData) {
        super.defineSynchedData(synchedData);
        synchedData.define(IS_FLEEING, false);
    }

    @Override
    public void tick() {
        if(level().isClientSide()) {
            this.idleAnimationState.animateWhen(!this.walkAnimation.isMoving(), this.tickCount);
            this.fleeAnimationState.animateWhen(this.isFleeing(), this.tickCount);
            this.idleFaceAnimationState.animateWhen(true, this.tickCount);
        }

        super.tick();
    }

    public boolean isFleeing() {
        return this.entityData.get(IS_FLEEING);
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
        var offset = new Vec3(0.5D + (direction.getStepX() * 0.5D), isTent ?  0.6875D : 0.6875D, 0.5D + (direction.getStepZ() * 0.5D));

        this.setPos(blockPos.getX() + offset.x, blockPos.getY(), blockPos.getZ() + offset.z);
    }

    @Override
    public void startSleeping(BlockPos pPos) {
        if (this.isPassenger()) {
            this.stopRiding();
        }

        BlockState blockstate = this.level().getBlockState(pPos);


        if (blockstate.isBed(this.level(), pPos, this)) {
            blockstate.setBedOccupied(this.level(), pPos, this, true);
        }

        if (!blockstate.isAir()) {
            this.setPose(Pose.SLEEPING);
            this.setPosToBed(pPos, blockstate.is(ModBlocks.TENT.get()), blockstate.getValue(Tent.FACING));
            this.setSleepingPos(pPos);
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand interactionHand) {
        if(interactionHand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if(!this.level().isClientSide()) return InteractionResult.SUCCESS;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openMerchantScreen(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack pStack) {
        var item = pStack.getItem();

        return Dictionaries.VALID_SEEDS.contains(item) || DESIRED_ITEMS.contains(item);
    }

    @Override
    public List<? extends ExtendedSensor<? extends DynastiesVillager>> getSensors() {
        return ObjectArrayList.of(
                new AvailablePlotsSensor<>(),
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>(),
                new HomeContainersSensor<>(),
                new AvailableSeedsSensor<>(),
                new FarmlandsSensor<>(),
                new FullyGrownCropsSensor<>(),
                new NearbyItemsSensor<>(),
                new NearbyPlayersSensor<>(),
                new AvailableTentsSensor<>()
        );
    }

    public BrainActivityGroup<DynastiesVillager> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                //new GoHome<>(),
                new TargetOrRetaliate<>(),
                new FirstApplicableBehaviour<>(new PickUpItems<>(), new ReturnItems<>())
        );
    }

    @Override
    public BrainActivityGroup<DynastiesVillager> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new SetPlayerLookTarget<>(),
                new LookAtTarget<>(),
                new InteractWithDoor<>().holdDoorsOpenFor((entity, living, doorPos) -> false),
                new MoveToWalkTarget<>(),
                new ClaimPlot<>(),
                new DoConstruction<>()
        );
    }

    private BrainActivityGroup<DynastiesVillager> getWorkTasks() {
        return new BrainActivityGroup<DynastiesVillager>(Activity.WORK).priority(11).behaviours(
                new StockWares<>(),
                new FetchSeeds<>(),
                new FirstApplicableBehaviour<>(new PickUpItems<>(), new ReturnItems<>()),
                new FirstApplicableBehaviour<>(new HarvestCrops<>(), new PlantCrops<>())
        );
    }

    private BrainActivityGroup<DynastiesVillager> getRestTasks() {
        return new BrainActivityGroup<DynastiesVillager>(Activity.REST).priority(1).behaviours(
                new SleepInTent<>(),
                new GoHome<>()
        );
    }

    @Override
    public List<Activity> getActivityPriorities() {
        return ObjectArrayList.of(Activity.FIGHT);
    }

    @Override
    public Map<Activity, BrainActivityGroup<? extends DynastiesVillager>> getAdditionalTasks() {
        return Map.of(
                Activity.WORK, getWorkTasks(),
                Activity.REST, getRestTasks()
        );
    }

    @Override
    public SmartBrainSchedule getSchedule() {
        return new BasicSchedule().activityAt(1000, Activity.IDLE).activityAt(2500, Activity.WORK).activityAt(12000, Activity.REST);
    }

//    @Override
//    public BrainActivityGroup<DynastiesVillager> getFightTasks() {
//        return BrainActivityGroup.fightTasks(
//                new FleeTarget<>().speedModifier(0.5F).whenStarting((entity) -> this.entityData.set(IS_FLEEING, true)).whenStopping((entity) -> this.entityData.set(IS_FLEEING, false))
//        );
//    }

    static {
        DESIRED_ITEMS = List.of(Items.WHEAT, Items.BEETROOT);
    }
}
