package com.maestreaux.dynasties.core.simulation;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.init.ModMealTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.items.Meal;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedVillagerEntity extends SimulatedEntity<AbstractDynastyVillager> {
    protected List<Plot> occupiedPlots = new ArrayList<>();
    protected List<Plot.Slot> occupiedSlots = new ArrayList<>();
    protected Zone homeZone;
    protected List<Plot.Job> jobs = new ArrayList<>();
    protected final MarketAgent agent = new MarketAgent(this);
    protected Map<Item, Integer> tradeSlot = new HashMap<>();

    protected int hunger = 2_000;
    protected int maxHunger = 15_000;
    protected SimulatedVillagerEntity.Stomach stomach = new SimulatedVillagerEntity.Stomach();

    protected final SimpleContainer inventory = new SimpleContainer(8);
    protected final SimpleContainer tradeInventory = new SimpleContainer(8);
    protected final DebugData debugData = new DebugData(List.of(), List.of(), 0);

    public SimulatedVillagerEntity(AbstractDynastyVillager villager) {
        super(villager);
        this.type = SimulatedEntityType.VILLAGER;
    }

    public SimulatedVillagerEntity(ServerLevel level) {
        super(level);
    }

    @Override
    public void tick() {
        super.tick();

        this.doHungerTick();
    }

    @Override
    protected AbstractDynastyVillager spawnEntity() {
        var newEntity = new DynastiesVillager(this.level, this);
        newEntity.setUUID(this.uuid);
        level.addFreshEntity(newEntity);

        newEntity.load(this.entitySavedData);

        return newEntity;
    }

    public int getMaxHunger() {
        return this.maxHunger;
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

    public void doHungerTick() {
        if (this.hunger > 0) {
            this.hunger = Math.max(this.hunger - Simulator.TICK_DIFFERENCE, 0);
        }
    }

    public DebugData getDebugData() {
        return this.debugData;
    }

    public MarketAgent asMarketAgent() {
        return this.agent;
    }

    public void updateDebugData(List<String> desiredItems) {
        // Run on schedule start?
//        var encodedValuations = this.asMarketAgent().getValuations().entrySet().stream().map(set -> set.getKey() + ":" + set.getValue()).toList();
//
//        this.entityData.set(DEBUG_DATA, new DynastyVillagerDebugData(desiredItems, encodedValuations, this.asMarketAgent().getMoney()));
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public Map<Item, MarketAgent.TradeOffer> getTradeOffers() {
        return this.agent.getActiveOffers();
    }

    public void updateTradeOffers() {
        var offersList = this.agent.getActiveOffers().values().stream()
                .filter(offer -> !offer.getItemOffered().isEmpty()).toList();

        var entity = getEntity();

        if (entity != null) {
            entity.setTradeOffers(offersList);
        }
    }

    public void eatFood(Item item) {
        this.stomach.add(item, this.level.getGameTime());
    }

    public SimulatedVillagerEntity.Stomach getStomach() {
        return this.stomach;
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
        var slot = plot.getAvailableSlot();
        if (slot != null) {
            this.occupySlot(slot);
        }
    }

    public void occupySlot(Plot.Slot slot) {
        var plot = slot.getParentPlot();
//        slot.setOccupier(this);

        this.occupiedPlots.add(plot);
        this.occupiedSlots.add(slot);
        this.jobs.add(slot.getJob());
//
//        if (plot.getType() == Plot.PlotType.RESIDENTIAL || plot.getType() == Plot.PlotType.RANCH || plot.getType() == Plot.PlotType.HALL) {
//            this.brain.setMemory(ModMemoryTypes.HOME_PLOT.get(), plot);
//        }
    }

    private void writeMarketAgentData(CompoundTag compoundTag) {
        var valuationsTag = new ListTag();

        for (var valuationEntrySet : this.agent.getValuations().entrySet()) {
            var valuationTag = new CompoundTag();

            valuationTag.putInt("villagerdynasties:valuation_item", Item.getId(valuationEntrySet.getKey()));
            valuationTag.putFloat("villagerdynasties:valuation_value", valuationEntrySet.getValue());

            valuationsTag.add(valuationTag);
        }

        compoundTag.put("villagerdynasties:valuations", valuationsTag);
    }

    private void readMarketAgentData(CompoundTag compoundTag) {
        var valuationsListTag = (ListTag) compoundTag.get("villagerdynasties:valuations");

        if (valuationsListTag != null) {
            var valuations = new HashMap<Item, Float>();

            for (int i = 0; i < valuationsListTag.size(); i++) {
                var item = Item.byId(valuationsListTag.getCompound(i).getInt("villagerdynasties:valuation_item"));
                var value = valuationsListTag.getCompound(i).getFloat("villagerdynasties:valuation_value");

                valuations.put(item, value);
            }

            this.agent.setValuations(valuations);
        }
    }

    @Override
    public void save(CompoundTag compoundTag) {
        super.save(compoundTag);

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

        var stomachListTag = new ListTag();
        for (var stomachContent: this.stomach.contents) {
            var stomachContentTag = new CompoundTag();
            var itemId = Item.getId(stomachContent.getFirst());

            stomachContentTag.putInt("villagerdynasties:stomach_item", itemId);
            stomachContentTag.putLong("villagerdynasties:stomach_eaten_at", stomachContent.getSecond());
            stomachListTag.add(stomachContentTag);
        }
        compoundTag.put("villagerdynasties:stomach_contents", stomachListTag);

        compoundTag.putInt("villagerdynasties:hunger", this.hunger);

        compoundTag.putInt("villagerdynasties:money", this.agent.getMoney());

        compoundTag.put("villagerdynasties:inventory", this.inventory.createTag(this.registryAccess()));
        compoundTag.put("villagerdynasties:trade_inventory", this.tradeInventory.createTag(this.registryAccess()));

        this.writeMarketAgentData(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        if (compoundTag.hasUUID("villagerdynasties:home_zone")) {
            setHomeZone(Zone.getZoneByUUID(this.level, compoundTag.getUUID("villagerdynasties:home_zone")));
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

        var stomachContentsTag = (ListTag) compoundTag.get("villagerdynasties:stomach_contents");
        if (stomachContentsTag != null) {
            for (int i = 0; i < stomachContentsTag.size(); i++) {
                var stomachContentItemId = stomachContentsTag.getCompound(i).getInt("villagerdynasties:stomach_item");
                var stomachContentItem = Item.byId(stomachContentItemId);

                var stomachContentEatenAt = stomachContentsTag.getCompound(i).getLong("villagerdynasties:stomach_eaten_at");

                this.stomach.contents.add(new Pair<>(stomachContentItem, stomachContentEatenAt));
            }
        }

        this.hunger = compoundTag.getInt("villagerdynasties:hunger");
        this.agent.setMoney(compoundTag.getInt("villagerdynasties:money"));

        if (compoundTag.contains("villagerdynasties:inventory", 9)) {
            this.inventory.fromTag(compoundTag.getList("villagerdynasties:inventory", 10), this.registryAccess());
        }
        if (compoundTag.contains("villagerdynasties:trade_inventory", 9)) {
            this.tradeInventory.fromTag(compoundTag.getList("villagerdynasties:trade_inventory", 10), this.registryAccess());
        }

        this.readMarketAgentData(compoundTag);
    }

    public record DebugData(List<String> desiredItems, List<String> valuations, float money) {
        public static final StreamCodec<RegistryFriendlyByteBuf, DebugData> STREAM_CODEC;

        static {
            STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugData::desiredItems,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DebugData::valuations,
                    ByteBufCodecs.FLOAT, DebugData::money,
                    DebugData::new);
        }
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
        public final long DIGESTION_DURATION = 72_000;
        private List<Pair<Item, Long>> contents;

        public Stomach() {
            this.contents = new ArrayList<>();
        }

        public void add(Item item, long gameTime) {
            this.contents.add(Pair.of(item, gameTime));
            this.update(gameTime);
        }

        public float getSurfeitFactor(MealType mealType, long gameTime) {
            this.update(gameTime);
            float surfeitDivisor = Math.max(this.contents.size(), 5F);
            return 1F - (this.contents.stream().filter(content -> {
                if (content.getFirst() instanceof Meal meal) {
                    return ModMealTypes.getMealType(meal).equals(mealType);
                } else {
                    return false;
                }
            }).count() / surfeitDivisor);
        }

        public StomachNutrients calculateNutrition(long gameTime) {
            this.update(gameTime);

            var result = new StomachNutrients(0, 0, 0, 0);

            if (!this.contents.isEmpty()) {
                this.contents.stream().filter((stomachItem) -> stomachItem.getFirst() instanceof Meal).forEach(stomachItem -> {
                    var mealItem = (Meal) stomachItem.getFirst();
                    var mealNutrients = ModMealTypes.getMealType(mealItem).getNutrients();

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
}
