package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class TaxableItems {
    public List<ItemLocation> itemLocations = new ArrayList<>();
    public int count = 0;
}

public class CollectTaxes<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    //TEMPORARY TAX FREQUENCY
    public static int TAX_FREQUENCY_TICKS = 12_000;
    private Plot targetPlot;
    private Map<Item, TaxableItems> itemTally = new HashMap<>();

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    protected void start(E entity) {
        var targetLocation = targetPlot.getAbsoluteStartPos().offset(targetPlot.getAbsoluteEndPos().multiply(1/2));

        if (AIUtils.isCloseEnoughToTarget(entity, targetLocation,4)) {
            var plotContainers = AIUtils.getPlotContainersStream(targetPlot, (ServerLevel)entity.level());

            for (var container : plotContainers.toList()) {
                var handler = InventoryUtils.getItemHandler(container);
                var slots = IntStream.range(0, handler.getSlots()).toArray();

                for (var slot : slots) {
                    var stackInSlot = handler.getStackInSlot(slot);

                    if (stackInSlot != ItemStack.EMPTY) {
                        var stackItem = stackInSlot.getItem();
                        var taxableItem = this.itemTally.computeIfAbsent(stackItem, key -> new TaxableItems());
                        taxableItem.itemLocations.add(new ItemLocation(stackInSlot, container, handler, slot));
                        taxableItem.count += stackInSlot.getCount();
                    }
                }
            }

            float TAX_RATE = 0.2F;
            var itemsToCollect = this.itemTally.values().stream().toList();

            for (var taxableItem : itemsToCollect) {
                var neededItemCount = Mth.floor(taxableItem.count * TAX_RATE);

                for (var location: taxableItem.itemLocations) {
                    var amountToGet = Math.min(location.stack.getCount(), neededItemCount);

                    var extractedStack = location.itemHandler.extractItem(location.slot, neededItemCount,false);
                    var entityInv = entity.getInventory();

                    ItemStack itemsToInsert;
                    while ((itemsToInsert = entityInv.addItem(extractedStack)) != ItemStack.EMPTY) {
                        if (!entityInv.canAddItem(itemsToInsert)) {
                            break;
                        }
                    }

                    neededItemCount -= amountToGet;
                    if (neededItemCount == 0) {
                        break;
                    }
                }
            }

            this.targetPlot.setLastTaxed(entity.level().getGameTime());
            this.targetPlot = null;
            BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        } else {
            BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetLocation));
            BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetLocation, 0.6F, 2));
        }


    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var currentGameTime = entity.level().getGameTime();
        entity.getHomeZone().getPlots().stream().filter(plot ->
                currentGameTime - plot.getLastTaxed() >= TAX_FREQUENCY_TICKS
                        && !plot.getOccupiedSlots().isEmpty()
                        && !entity.getOccupiedPlots().contains(plot)
        ).findFirst().ifPresent(taxablePlot -> this.targetPlot = taxablePlot);

        if (this.targetPlot != null) {
            return true;
        } else {
            this.itemTally.clear();
            return false;
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{});
    }
}
