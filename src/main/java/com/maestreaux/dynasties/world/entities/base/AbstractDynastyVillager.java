package com.maestreaux.dynasties.world.entities.base;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.init.ModEntityDataSerializers;
import com.maestreaux.dynasties.init.ModEntityTypes;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractDynastyVillager extends AgeableMob implements InventoryCarrier {
    private static final EntityDataAccessor<Boolean> IS_FLEEING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<List<MarketAgent.TradeOffer>> TRADE_OFFERS = SynchedEntityData.defineId(AbstractDynastyVillager.class, ModEntityDataSerializers.TRADE_OFFERS.get());
    protected List<Plot> occupiedPlots = new ArrayList<>();
    protected Zone homeZone;
    protected List<Plot.SlotJob> jobs = new ArrayList<>();
    protected final MarketAgent agent = new MarketAgent(this);
    protected Map<Item, Integer> tradeSlot = new HashMap<>();

    protected int hunger = 2_000;
    protected int maxHunger = 15_000;

    private final SimpleContainer inventory = new SimpleContainer(8);
    private final SimpleContainer tradeInventory = new SimpleContainer(8);

    protected AbstractDynastyVillager(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }

    protected AbstractDynastyVillager(Level pLevel, Zone homeZone) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), pLevel);
        this.homeZone = homeZone;
        this.setCanPickUpLoot(true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder synchedData) {
        super.defineSynchedData(synchedData);
        synchedData.define(TRADE_OFFERS, new ArrayList<>());
        synchedData.define(IS_FLEEING, false);
        synchedData.define(IS_EATING, false);
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

    public void setTradeOffers(List<MarketAgent.TradeOffer> newList) {
        this.entityData.set(TRADE_OFFERS, newList, true);
    }

    public void updateTradeOffers() {
        var offersList = this.agent.getActiveOffers().values().stream()
                .filter(offer -> !offer.getItemOffered().isEmpty()).toList();

        this.setTradeOffers(offersList);
    }

    public List<MarketAgent.TradeOffer> getTradeOffers() {
        return this.entityData.get(TRADE_OFFERS);
    }

    public Zone getHomeZone() {
        return this.homeZone;
    }

    public void setHomeZone(Zone zone) {
        this.homeZone = zone;
    }

    public List<Plot> getOccupiedPlots() {
        return this.occupiedPlots;
    }

    public void occupyPlot(Plot plot) {
        this.occupiedPlots.add(plot);
        var slot = plot.getAvailableSlot();
        slot.setOccupier(this);

        this.jobs.add(slot.getJob());

        if (plot.getType() == Plot.PlotType.RESIDENTIAL) {
            this.brain.setMemory(ModMemoryTypes.HOME_PLOT.get(), plot);
        }
    }

    public List<Plot.SlotJob> getJobs() {
        return this.jobs;
    }

    // TODO: TEMPORARY
    public Plot.SlotJob getJob() {
        return !this.jobs.isEmpty() ? this.jobs.getFirst() : null;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        for (var plot: this.occupiedPlots) {
            plot.clearVillagerFromPlot(this);
        }
    }

    public void setHunger(int newHungerValue) {
        if (newHungerValue > this.maxHunger) {
            this.hunger = this.maxHunger;
        } else if (newHungerValue < 0) {
            this.hunger = 0;
        } else {
            this.hunger = newHungerValue;
        }
    }

    public int getHunger() {
        return this.hunger;
    }

    public int getMaxHunger() {
        return this.maxHunger;
    }

    public void doHungerTick() {
        if(this.hunger > 0) {
             this.hunger -= 1;
        }
    }

    @Override
    public void tick() {
        this.doHungerTick();

        super.tick();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public SimpleContainer getTradeInventory() {
        return this.tradeInventory;
    }

    public Map<Item, Integer> getTradeSlot() {
        return this.tradeSlot;
    }

    public MarketAgent asMarketAgent() {
        return this.agent;
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);

        var itemsRemoved = this.inventory.removeAllItems();
        itemsRemoved.addAll(this.tradeInventory.removeAllItems());

        for (var item: itemsRemoved) {
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

        if(compoundTag.hasUUID("villagerdynasties:home_zone")) {
            this.homeZone = Zone.getZoneByUUID((ServerLevel) this.level(), compoundTag.getUUID("villagerdynasties:home_zone"));
        }

        this.hunger = compoundTag.getInt("villagerdynasties:hunger");

        this.setCanPickUpLoot(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.homeZone != null) {
            compoundTag.putUUID("villagerdynasties:home_zone", this.homeZone.getUUID());
        }

        compoundTag.putInt("villagerdynasties:hunger", this.hunger);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
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
