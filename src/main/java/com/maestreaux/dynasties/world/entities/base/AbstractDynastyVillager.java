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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractDynastyVillager extends AgeableMob implements InventoryCarrier {
    protected static final EntityDataAccessor<List<MarketAgent.TradeOffer>> TRADE_OFFERS = SynchedEntityData.defineId(AbstractDynastyVillager.class, ModEntityDataSerializers.TRADE_OFFERS.get());
    protected List<Plot> occupiedPlots = new ArrayList<>();
    protected Zone homeZone;
    protected final MarketAgent agent = new MarketAgent(this);
    protected Map<Item, Integer> tradeSlot = new HashMap<>();
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

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRADE_OFFERS, new ArrayList<>());
    }

    public void setTradeOffers(List<MarketAgent.TradeOffer> newList) {
        this.entityData.set(TRADE_OFFERS, newList);
    }

    public void updateTradeOffers() {
        var offersList = this.agent.getActiveOffers().values().stream().toList();
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

        if (plot.getType() == Plot.PlotType.RESIDENTIAL) {
            this.brain.setMemory(ModMemoryTypes.HOME_PLOT.get(), plot);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        for (var plot: this.occupiedPlots) {
            plot.clearVillagerFromPlot(this);
        }
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
    protected void dropEquipment() {
        super.dropEquipment();

        var itemsRemoved = this.inventory.removeAllItems();
        itemsRemoved.addAll(this.tradeInventory.removeAllItems());

        for (var item: itemsRemoved) {
            this.spawnAtLocation(item);
        }
    }

    protected void pickUpItem(ItemEntity pItemEntity) {
        InventoryCarrier.pickUpItem(this, this, pItemEntity);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.hasUUID("villagerdynasties:home_zone")) {
            this.homeZone = Zone.getZoneByUUID((ServerLevel) this.level(), compoundTag.getUUID("villagerdynasties:home_zone"));
        }

        this.setCanPickUpLoot(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.homeZone != null) {
            compoundTag.putUUID("villagerdynasties:home_zone", this.homeZone.getUUID());
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }
}
