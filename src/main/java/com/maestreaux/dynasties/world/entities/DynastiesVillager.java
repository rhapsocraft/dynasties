package com.maestreaux.dynasties.world.entities;

import com.maestreaux.dynasties.client.ClientHooks;
import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.init.ModBlocks;
import com.maestreaux.dynasties.world.Plot;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.tslat.smartbrainlib.api.core.behaviour.AllApplicableBehaviours;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
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
    private static final List<Item> DESIRED_ITEMS;
    private final int tickOffset =  Mth.ceil(Math.random() * 20);

    // TODO: TEMPORARY NOBILITY FLAG
    public boolean isNobility = false;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState fleeAnimationState = new AnimationState();
    public final AnimationState idleFaceAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState fallAnimationState = new AnimationState();
    public final AnimationState bounceAnimationState = new AnimationState();
    public final AnimationState turnRightAnimationState = new AnimationState();
    public final AnimationState turnLeftAnimationState = new AnimationState();
    public final AnimationState swingAnimationState = new AnimationState();

    public DynastiesVillager(EntityType<DynastiesVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        ((GroundPathNavigation) this.getNavigation()).setCanWalkOverFences(true);
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

    public boolean isFalling() {
        return this.getDeltaMovement().y < -0.35F;
    }

    @Override
    public void tick() {
        this.updateSwingTime();

        if(level().isClientSide()) {
            this.idleAnimationState.animateWhen(!this.walkAnimation.isMoving(), this.tickCount);
            this.fleeAnimationState.animateWhen(this.isFleeing(), this.tickCount);
            this.eatAnimationState.animateWhen(this.isEating(), this.tickCount);
            this.idleFaceAnimationState.animateWhen(true, this.tickCount + this.tickOffset);

            this.swingAnimationState.animateWhen(this.swinging, this.tickCount);

            if (this.onGround() && this.fallAnimationState.isStarted()) {
                this.bounceAnimationState.start(this.tickCount);
            }

//            if (this.yBodyRotO < this.yBodyRot && !this.turnRightAnimationState.isStarted()) {
//                this.turnRightAnimationState.startIfStopped(this.tickCount);
//            } else if (this.yBodyRotO > this.yBodyRot && !this.turnLeftAnimationState.isStarted()) {
//                this.turnLeftAnimationState.startIfStopped(this.tickCount);
//            }

            this.fallAnimationState.animateWhen(isFalling(), this.tickCount);
        }

        super.tick();
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
                new AvailableTentsSensor<>(),
                new AvailableFoodSensor<>()
        );
    }

    public BrainActivityGroup<DynastiesVillager> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                //new GoHome<>(),
                new TargetOrRetaliate<>(),
                new FetchFood<>(),
                new FirstApplicableBehaviour<>(new EatFood<>(), new PickUpItems<>(), new ReturnItems<>())

        );
    }

    @Override
    public BrainActivityGroup<DynastiesVillager> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new SetPlayerLookTarget<>(),
                new LookAtTarget<>(),
                new InteractWithBarrier<>(),
                new MoveToWalkTarget<>(),
                new ClaimPlot<>(),
                new DoConstruction<>()
        );
    }

    private BrainActivityGroup<DynastiesVillager> getWorkTasks() {
        return new BrainActivityGroup<DynastiesVillager>(Activity.WORK).priority(11).behaviours(
                new FirstApplicableBehaviour<>(
                        new AllApplicableBehaviours<>(
                                new GoHome<>(),
                                new StockWares<>()
                        ).startCondition((villager) -> villager.getJob() == Plot.SlotJob.TRADER),
                        new AllApplicableBehaviours<>(
                                new FetchSeeds<>(),
                                new FirstApplicableBehaviour<>(new PickUpItems<>(), new ReturnItems<>()),
                                new FirstApplicableBehaviour<>(new HarvestCrops<>(), new PlantCrops<>())
                        ).startCondition((villager) -> villager.getJob() == Plot.SlotJob.WORKER)
                )
        );
    }

    private BrainActivityGroup<DynastiesVillager> getRestTasks() {
        return new BrainActivityGroup<DynastiesVillager>(Activity.REST).priority(1).behaviours(
                new SleepInTent<>(),
                //new GoHome<>(),
                new ReturnItems<>().startCondition((villager) -> villager.getJob() == Plot.SlotJob.WORKER),
                new FirstApplicableBehaviour<>(new ReturnItems<>(), new AdjustValuationsAndPrices<>()).startCondition((villager) -> villager.getJob() == Plot.SlotJob.TRADER)
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
