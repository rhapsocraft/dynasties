package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.stream.IntStream;

public class ReturnItems<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    private BaseContainerBlockEntity targetContainer = null;

    protected void start(E entity) {
        var containers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

        if (containers != null) {
            var entityInv = entity.getInventory();
            var entityTradeInv = entity.getTradeInventory();
            var itemHandlers = containers.stream().toList();
            var inventories = List.of(entityInv, entityTradeInv);

            if (!itemHandlers.isEmpty()) {
                ItemStack itemStackToReturn;
                var inventoryIter = itemHandlers.iterator();

                if (this.targetContainer != null) {
                    var currentInventory = InventoryUtils.getItemHandler(this.targetContainer);
                    int currentSlot = 0;
                    var targetPos = this.targetContainer.getBlockPos();

                    if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                        for (var inventory : inventories) {
                            var inventorySlotsIterator = IntStream.range(0, inventory.getContainerSize()).iterator();
                            while (inventorySlotsIterator.hasNext()) {
                                var currentItemSlot = inventorySlotsIterator.next();
                                itemStackToReturn = inventory.getItem(currentItemSlot);
                                var item = itemStackToReturn.getItem();

                                while (itemStackToReturn != ItemStack.EMPTY && currentSlot <= (currentInventory.getSlots() - 1)) {
                                    itemStackToReturn = currentInventory.insertItem(currentSlot, itemStackToReturn, false);
                                    inventory.setItem(currentItemSlot, itemStackToReturn);

                                    if (itemStackToReturn != ItemStack.EMPTY) {
                                        ++currentSlot;
                                    }
                                }

                                if (inventory == entityTradeInv) {
                                    var marketAgent = entity.asMarketAgent();
                                    marketAgent.removeOffer(item);
                                }

                                if (currentSlot > (currentInventory.getSlots() - 1)) {
                                    if (inventoryIter.hasNext()) {
                                        currentInventory = InventoryUtils.getItemHandler(inventoryIter.next());
                                    } else {
                                        break;
                                    }
                                }

                                currentSlot = 0;
                            }
                        }

                        BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
                        BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
                    } else {
                        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
                        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                    }
                } else {
                    this.targetContainer = inventoryIter.next();
                }
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !entity.getInventory().isEmpty() || !entity.getTradeInventory().isEmpty();
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_CONTAINERS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_FARMLANDS.get(), MemoryStatus.VALUE_ABSENT)
        });
    }
}
