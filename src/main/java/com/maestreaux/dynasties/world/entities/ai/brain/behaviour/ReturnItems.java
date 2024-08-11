package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import java.util.List;
import java.util.stream.IntStream;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class ReturnItems<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    private BlockEntity targetContainer = null;

    private IItemHandler getItemHandler(BlockEntity container) {
        return container.getCapability(ITEM_HANDLER).resolve().orElse(null);
    }

    protected void start(E entity) {
        var containers = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

        if (containers != null) {
            var entityInv = entity.getInventory();
            var itemHandlers = containers.stream().toList();
            var itemsToReturn = IntStream.range(0, entityInv.getContainerSize()).iterator();

            if (!itemHandlers.isEmpty()) {
                ItemStack itemToReturn;
                var inventoryIter = itemHandlers.iterator();

                if (this.targetContainer != null) {
                    var currentInventory = getItemHandler(this.targetContainer);
                    int currentSlot = 0;
                    var targetPos = this.targetContainer.getBlockPos();

                    if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                        while (itemsToReturn.hasNext()) {
                            var currentItemSlot = itemsToReturn.next();
                            itemToReturn = entityInv.getItem(currentItemSlot);

                            while (itemToReturn != ItemStack.EMPTY && currentSlot <= (currentInventory.getSlots() - 1)) {
                                itemToReturn = currentInventory.insertItem(currentSlot++, itemToReturn, false);
                                entityInv.setItem(currentItemSlot, itemToReturn);
                            }

                            if (currentSlot > (currentInventory.getSlots() - 1)) {
                                if (inventoryIter.hasNext()) {
                                    currentInventory = getItemHandler(inventoryIter.next());
                                } else {
                                    break;
                                }
                            }

                            currentSlot = 0;
                        }
                    } else {
                        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                    }
                } else {
                    this.targetContainer = inventoryIter.next();
                }
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !entity.getInventory().isEmpty();
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_CONTAINERS.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
