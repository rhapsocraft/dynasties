package com.maestreaux.dynasties.world.entities.base;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import com.maestreaux.dynasties.core.simulation.SimulationState;
import com.maestreaux.dynasties.init.ModEntityDataSerializers;
import com.maestreaux.dynasties.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class AbstractDynastyVillager extends AgeableMob implements InventoryCarrier {
    private static final EntityDataAccessor<Boolean> IS_FLEEING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<List<MarketAgent.TradeOffer>> TRADE_OFFERS = SynchedEntityData.defineId(AbstractDynastyVillager.class, ModEntityDataSerializers.TRADE_OFFERS.get());
    // TODO: TEMPORARY NOBILITY FLAG
    private static final EntityDataAccessor<Boolean> IS_NOBILITY = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);

    protected VillagerEntitySimulated simEntity;

    protected AbstractDynastyVillager(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }

    // This constructor should be called for the first time an entity is created
    protected AbstractDynastyVillager(ServerLevel level) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), level);

        this.initializeSimEntity(level);
    }

    // This constructor should be called for the first time an entity is created
    protected AbstractDynastyVillager(ServerLevel level, BlockPos pos) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), level);
        this.setPos(pos.getCenter());

        this.initializeSimEntity(level);
    }

    // This constructor should be called by the simulator when needing to spawn a simulated entity
    protected AbstractDynastyVillager(ServerLevel level, VillagerEntitySimulated simEntity) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), level);
        this.uuid = simEntity.getUUID();
        this.simEntity = simEntity;
    }

    protected void defineSynchedData(SynchedEntityData.Builder synchedData) {
        super.defineSynchedData(synchedData);
        synchedData.define(TRADE_OFFERS, new ArrayList<>());
        synchedData.define(IS_FLEEING, false);
        synchedData.define(IS_EATING, false);
        synchedData.define(IS_NOBILITY, false);
    }

    private void initializeSimEntity(ServerLevel level) {
        this.simEntity = (VillagerEntitySimulated) SimulationState.addEntity(level, new VillagerEntitySimulated(this));
    }

    public boolean isFleeing() {
        return this.entityData.get(IS_FLEEING);
    }

    public boolean isEating() {
        return this.entityData.get(IS_EATING);
    }

    public void setIsEating(boolean isEating) {
        this.entityData.set(IS_EATING, isEating);
    }

    public void setIsNobility(boolean isNobility) {
        this.entityData.set(IS_NOBILITY, isNobility);
    }

    public boolean isNobility() {
        return this.entityData.get(IS_NOBILITY);
    }

    public void setTradeOffers(List<MarketAgent.TradeOffer> newList) {
        this.entityData.set(TRADE_OFFERS, newList, true);
    }

    public VillagerEntitySimulated getSimEntity() {
        return this.simEntity;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        if (!this.level().isClientSide && this.simEntity != null) {
            for (var plot : this.simEntity.getOccupiedPlots()) {
                plot.clearVillagerFromPlot(this);
            }
        }

    }

    @Override
    public void tick() {
        super.tick();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return this.simEntity.getInventory();
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);

        var itemsRemoved = this.getInventory().removeAllItems();
        itemsRemoved.addAll(this.getInventory().removeAllItems());

        for (var item : itemsRemoved) {
            this.spawnAtLocation(level, item);
        }
    }

    @Override
    protected void pickUpItem(ServerLevel level, ItemEntity pItemEntity) {
        InventoryCarrier.pickUpItem(level, this, this, pItemEntity);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setCanPickUpLoot(true);
    }


    @Override
    public boolean save(CompoundTag tag) {
        // TODO: Passenger saving mechanics?

        if (this.simEntity != null) {
            this.simEntity.saveEntityData(this);
        }

        return false;
    }

    @Override
    public void remove(RemovalReason p_276115_) {
        if (p_276115_ == RemovalReason.KILLED || p_276115_ == RemovalReason.DISCARDED) {
            if (!this.level().isClientSide) {
                SimulationState.removeEntity((ServerLevel) this.level(), uuid);
            }
        } else if (!this.level().isClientSide) {
            this.simEntity.saveEntityData(this);
        }

        super.remove(p_276115_);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public void moveTo(Vec3 target) {
        super.moveTo(target);

        this.simEntity.setPos(new BlockPos((int) target.x, (int) target.y, (int) target.z));
    }

    public enum DynastiesVillagerPose {
        CROSSED_ARMS,
        ATTACKING,
        BOW_HOLD,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        BLOCK_HOLD,
        NEUTRAL;

        private DynastiesVillagerPose() {
        }
    }
}
