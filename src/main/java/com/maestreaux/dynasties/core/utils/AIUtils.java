package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.maestreaux.dynasties.core.utils.InventoryUtils.getPotentialNutrition;


public class AIUtils {
    public static boolean isCloseEnoughToTarget(Entity entity, Vec3i target, float distance) {
        return entity.blockPosition().distSqr(target) <= (distance * distance);
    }

    public static boolean isCloseEnoughToTarget(Entity entity, BlockPos target) {
        return isCloseEnoughToTarget(entity, target, 2F);
    }

    public static float calculatePotentialNutritionByCost(ServerLevel level, ItemStack item, MarketAgent seller) {
        var sellerItemValuation = seller.getValuations().get(item.getItem());
        var potentialItemNutrition = getPotentialNutrition(level, item);

        return potentialItemNutrition / sellerItemValuation;
    }

    public static int calculateRequiredHouseholdHunger(AbstractDynastyVillager villager) {
        var homePlot = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            return homePlot.getOccupiedSlots().stream().flatMapToInt(slot -> IntStream.of(slot.getOccupier().getSimEntity().getMaxHunger())).sum();
        } else {
            return villager.getSimEntity().getMaxHunger();
        }
    }

    public static List<Pair<ItemLocation, Integer>> getItemsToCook(AbstractDynastyVillager villager, List<ItemLocation> availableItems) {
        var itemLocationsIter = availableItems.stream().sorted(InventoryUtils.itemLocationPotentialNutritionSorter((ServerLevel) villager.level())).iterator();

        ServerLevel level = (ServerLevel) villager.level();
        int requiredNutrition = calculateRequiredHouseholdHunger(villager);
        int actualNutrition = 0;

        List<Pair<ItemLocation, Integer>> locations = ObjectArrayList.of();

        while (itemLocationsIter.hasNext()) {
            var itemLocation = itemLocationsIter.next();
            var itemCount = itemLocation.stack.getCount();
            var potentialNutrition = getPotentialNutrition(level, itemLocation.stack);
            var totalNutrition = itemCount * potentialNutrition;

            if (actualNutrition + totalNutrition > requiredNutrition) {
                var difference = requiredNutrition - actualNutrition;
                int amountToTake = Mth.floor((float) difference / potentialNutrition);
                locations.add(Pair.of(itemLocation, amountToTake));

                break;
            } else {
                locations.add(Pair.of(itemLocation, itemCount));
                actualNutrition += totalNutrition;
            }
        }

        return locations;
    }

    public static int getPotentialNutritionBeingProducedInPlot(Plot plot) {
        var level = (ServerLevel) plot.getParentZone().level();
        int nutritionBeingProduced = 0;

        var productionBlockEntitiesInPlot = BlockPos.betweenClosedStream(new AABB(
                        new Vec3(plot.getAbsoluteStartPos().getX(), plot.getAbsoluteStartPos().getY(), plot.getAbsoluteStartPos().getZ()),
                        new Vec3(plot.getAbsoluteEndPos().getX(), plot.getAbsoluteStartPos().getY() + 10, plot.getAbsoluteEndPos().getZ())
                )
        ).map(level::getBlockEntity).filter(
                blockEntity -> blockEntity instanceof CampfireBlockEntity
        ).toList();

        for (var blockEntity : productionBlockEntitiesInPlot) {
            if (blockEntity instanceof CampfireBlockEntity campfire) {
                nutritionBeingProduced += campfire.getItems().stream().flatMapToInt(item -> IntStream.of(getPotentialNutrition(level, item))).sum();
            }
        }

        return nutritionBeingProduced;
    }

    public static int scanItemsBeingProducedInPlot(Plot plot, ItemStack itemStack) {
        var level = (ServerLevel) plot.getParentZone().level();
        int itemsBeingProduced = 0;

        var productionBlockEntitiesInPlot = BlockPos.betweenClosedStream(new AABB(
                        new Vec3(plot.getAbsoluteStartPos().getX(), plot.getAbsoluteStartPos().getY(), plot.getAbsoluteStartPos().getZ()),
                        new Vec3(plot.getAbsoluteEndPos().getX(), plot.getAbsoluteStartPos().getY() + 10, plot.getAbsoluteEndPos().getZ())
                )
        ).map(level::getBlockEntity).filter(
                blockEntity -> blockEntity instanceof CampfireBlockEntity
        ).toList();

        for (var blockEntity : productionBlockEntitiesInPlot) {
            if (blockEntity instanceof CampfireBlockEntity campfire) {
                itemsBeingProduced += campfire.getItems().stream().filter(item -> item.getItem() == itemStack.getItem()).toList().size();
            }
        }

        return itemsBeingProduced;
    }

    public static Stream<BaseContainerBlockEntity> getPlotContainersStream(Plot plot, ServerLevel level) {
        return BlockPos.betweenClosedStream(plot.getAbsoluteStartPos(), plot.getAbsoluteEndPos().offset(0, 10, 0)).map((pos) -> {
            var blockEntity = level.getBlockEntity(pos);
            return blockEntity instanceof BaseContainerBlockEntity baseContainerEntity ? baseContainerEntity : null;
        }).filter(Objects::nonNull).filter(
                blockEntity -> blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ChestBlockEntity
        );
    }

    public static boolean shouldCook(AbstractDynastyVillager villager) {
        var containers = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_CONTAINERS.get());
        var level = (ServerLevel) villager.level();
        var totalActualNutrition = 0;
        var totalPotentialNutritionBeingProduced = 0;

        if (containers != null) {
            var foodLocations = InventoryUtils.getItemLocations(containers, Dictionaries.FOOD).values().stream().flatMap(List::stream)
                    .filter(itemLocation -> getPotentialNutrition(level, itemLocation.stack) == 0);

            List<ItemStack> items = new ArrayList<>(foodLocations.map(location -> location.stack).toList());
            var totalHouseholdHunger = calculateRequiredHouseholdHunger(villager);
            var homePlot = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_PLOT.get());

            if (homePlot != null) {
                var familyItems = homePlot.getSlots().stream().map(slot -> slot.getOccupier().getInventory().getItems())
                        .flatMap(List::stream).filter(itemStack -> getPotentialNutrition(level, itemStack) == 0).toList();

                items.addAll(familyItems);

                if (items.isEmpty()) {
                    return true;
                } else {
                    totalPotentialNutritionBeingProduced = getPotentialNutritionBeingProducedInPlot(homePlot);

                    for (var item : items) {
                        totalActualNutrition += InventoryUtils.getNutrition(item) * item.getCount();

                        if ((totalActualNutrition + totalPotentialNutritionBeingProduced) * 1000 >= totalHouseholdHunger) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
}
