package com.maestreaux.dynasties.world.entities.base;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.init.ModEntityDataSerializers;
import com.maestreaux.dynasties.init.ModEntityTypes;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.items.Meal;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
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

import java.util.*;
import java.util.stream.Collectors;

public class AbstractDynastyVillager extends AgeableMob implements InventoryCarrier {
    private static final EntityDataAccessor<Boolean> IS_FLEEING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<DynastyVillagerDebugData> DEBUG_DATA = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializer.forValueType(DynastyVillagerDebugData.STREAM_CODEC));

    protected static final EntityDataAccessor<List<MarketAgent.TradeOffer>> TRADE_OFFERS = SynchedEntityData.defineId(AbstractDynastyVillager.class, ModEntityDataSerializers.TRADE_OFFERS.get());
    protected List<Plot> occupiedPlots = new ArrayList<>();
    protected List<Plot.Slot> occupiedSlots = new ArrayList<>();
    protected Zone homeZone;
    protected List<Plot.Job> jobs = new ArrayList<>();
    protected final MarketAgent agent = new MarketAgent(this);
    protected Map<Item, Integer> tradeSlot = new HashMap<>();

    protected int hunger = 2_000;
    protected int maxHunger = 15_000;
    protected Stomach stomach = new Stomach();

    private final SimpleContainer inventory = new SimpleContainer(8);
    private final SimpleContainer tradeInventory = new SimpleContainer(8);

    // TODO: TEMPORARY NOBILITY FLAG
    private static final EntityDataAccessor<Boolean> IS_NOBILITY = SynchedEntityData.defineId(AbstractDynastyVillager.class, EntityDataSerializers.BOOLEAN);

    protected AbstractDynastyVillager(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCanPickUpLoot(true);
    }

    protected AbstractDynastyVillager(Level pLevel, Zone homeZone) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), pLevel);
        setHomeZone(homeZone);
        this.setCanPickUpLoot(true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder synchedData) {
        super.defineSynchedData(synchedData);
        synchedData.define(TRADE_OFFERS, new ArrayList<>());
        synchedData.define(IS_FLEEING, false);
        synchedData.define(IS_EATING, false);
        synchedData.define(IS_NOBILITY, false);
        synchedData.define(DEBUG_DATA, new DynastyVillagerDebugData(new ArrayList<>(), this.asMarketAgent().getMoney()));
    }

    public boolean isFleeing() {
        return this.entityData.get(IS_FLEEING);
    }

    public boolean isEating() {
        return this.entityData.get(IS_EATING);
    }

    public DynastyVillagerDebugData getDebugData() {
        return this.entityData.get(DEBUG_DATA);
    }

    public void setIsEating(boolean isEating) {
        this.entityData.set(IS_EATING, isEating);
    }

    public void setIsNobility(boolean isNobility) {
        this.entityData.set(IS_NOBILITY, isNobility);
    }

    public void updateDebugData(List<String> desiredItems) {
        // Run on schedule start?
        this.entityData.set(DEBUG_DATA, new DynastyVillagerDebugData(desiredItems, this.asMarketAgent().getMoney()));
    }

    public boolean isNobility() {
        return this.entityData.get(IS_NOBILITY);
    }

    public void setTradeOffers(List<MarketAgent.TradeOffer> newList) {
        this.entityData.set(TRADE_OFFERS, newList, true);
    }

    public void updateTradeOffers() {
        var offersList = this.agent.getActiveOffers().values().stream()
                .filter(offer -> !offer.getItemOffered().isEmpty()).toList();

        this.setTradeOffers(offersList);
    }

    public void eatFood(Item item) {
        this.stomach.add(item, this.level().getGameTime());
    }

    public Stomach getStomach() {
        return this.stomach;
    }

    public List<MarketAgent.TradeOffer> getTradeOffers() {
        return this.entityData.get(TRADE_OFFERS);
    }

    public Zone getHomeZone() {
        return this.homeZone;
    }

    public void setHomeZone(Zone zone) {
        this.homeZone = zone;
        this.homeZone.addResident(this);
    }

    public List<Plot> getOccupiedPlots() {
        return this.occupiedPlots;
    }

    public void occupySlot(Plot plot) {
        // TODO: TEMPORARY NOBILITY FLAG
        var slot = this.entityData.get(IS_NOBILITY) ? plot.getAvailableSlot(Plot.Job.NOBLE) : plot.getAvailableSlot();
        if (slot != null) {
            this.occupySlot(slot);
        }
    }

    public void occupySlot(Plot.Slot slot) {
        var plot = slot.getParentPlot();
        slot.setOccupier(this);
        this.occupiedPlots.add(plot);
        this.occupiedSlots.add(slot);
        this.jobs.add(slot.getJob());

        if (plot.getType() == Plot.PlotType.RESIDENTIAL || plot.getType() == Plot.PlotType.RANCH || plot.getType() == Plot.PlotType.HALL) {
            this.brain.setMemory(ModMemoryTypes.HOME_PLOT.get(), plot);
        }
    }

    public List<Plot.Job> getJobs() {
        return this.jobs;
    }

    // TODO: TEMPORARY SINGLE JOB QUERY
    public Plot.Job getJob() {
        return !this.jobs.isEmpty() ? this.jobs.getFirst() : null;
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        for (var plot : this.occupiedPlots) {
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
        if (this.hunger > 0) {
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

        if (compoundTag.hasUUID("villagerdynasties:home_zone")) {
            setHomeZone(Zone.getZoneByUUID((ServerLevel) this.level(), compoundTag.getUUID("villagerdynasties:home_zone")));
        }

        var slotsListTag = (ListTag) compoundTag.get("villagerdynasties:villager_slots");
        if (slotsListTag != null) {
            if (this.homeZone != null) {
                var plots = this.homeZone.getPlots();

                for (int i = 0; i < slotsListTag.size(); i++) {
                    var newSlotUUID = slotsListTag.getCompound(i).getUUID("villagerdynasties:villager_slot_uuid");
                    plots.stream().map(plot -> plot.getSlot(newSlotUUID)).filter(Objects::nonNull).findFirst().ifPresent(this::occupySlot);
                }
            }
        }

        this.hunger = compoundTag.getInt("villagerdynasties:hunger");

        // TODO: TEMPORARY NOBILITY FLAG
        this.entityData.set(IS_NOBILITY, compoundTag.getBoolean("villagerdynasties:is_nobility"));

        this.setCanPickUpLoot(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.homeZone != null) {
            compoundTag.putUUID("villagerdynasties:home_zone", this.homeZone.getUUID());
        }

        var slotsListTag = new ListTag();
        for (var slot : this.occupiedSlots) {
            var slotTag = new CompoundTag();
            slotTag.putUUID("villagerdynasties:villager_slot_uuid", slot.getUUID());
            slotsListTag.add(slotTag);
        }
        compoundTag.put("villagerdynasties:villager_slots", slotsListTag);

        compoundTag.putBoolean("villagerdynasties:is_nobility", this.entityData.get(IS_NOBILITY));
        compoundTag.putInt("villagerdynasties:hunger", this.hunger);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    public static class StomachNutrients {
        public float meat;
        public float vegetable;
        public float sugar;
        public float carbohydrates;

        public StomachNutrients(float meat, float vegetable, float sugar, float carbohydrates) {
            this.meat = meat;
            this.vegetable = vegetable;
            this.sugar = sugar;
            this.carbohydrates = carbohydrates;
        }
    }

    public static class Stomach {
        public final long DIGESTION_DURATION = 24_000;
        private List<Pair<Item, Long>> contents;

        public Stomach() {
            this.contents = new ArrayList<>();
        }

        public void add(Item item, long gameTime) {
            this.contents.add(Pair.of(item, gameTime));
            this.update(gameTime);
        }

        public float getSurfeit(MealType mealType) {
            float surfeitDivisor = Math.max(this.contents.size(), 5F);
            return 1F - (this.contents.stream().filter(content -> ((Meal) content.getFirst()).getMealType().equals(mealType)).count() / surfeitDivisor);
        }

        public StomachNutrients calculateNutrition(long gameTime) {
            this.update(gameTime);

            var result = new StomachNutrients(0, 0, 0, 0);

            if (!this.contents.isEmpty()) {
                this.contents.stream().filter((stomachItem) -> stomachItem.getFirst() instanceof Meal).forEach(stomachItem -> {
                    var mealItem = (Meal) stomachItem.getFirst();
                    var mealNutrients = mealItem.getMealType().getNutrients();

                    result.meat += mealNutrients.meat();
                    result.carbohydrates += mealNutrients.carbohydrates();
                    result.sugar += mealNutrients.sugar();
                    result.vegetable += mealNutrients.vegetable();
                });

                result.meat /= this.contents.size();
                result.vegetable /= this.contents.size();
                result.sugar /= this.contents.size();
                result.carbohydrates /= this.contents.size();
            }

            return result;
        }

        private void update(long gameTime) {
            // Clear digested food
            this.contents = this.contents.stream().filter(stomachItem ->
                    (stomachItem.getSecond() + DIGESTION_DURATION) >= gameTime
            ).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public record DynastyVillagerDebugData(List<String> desiredItems, float money) {
        public static final StreamCodec<RegistryFriendlyByteBuf, DynastyVillagerDebugData> STREAM_CODEC;

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DynastyVillagerDebugData::desiredItems, ByteBufCodecs.FLOAT, DynastyVillagerDebugData::money, DynastyVillagerDebugData::new);
        }
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
