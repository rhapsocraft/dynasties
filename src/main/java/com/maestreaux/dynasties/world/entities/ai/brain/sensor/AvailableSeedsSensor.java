package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.ItemLocation;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class AvailableSeedsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var containers = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());
        var itemLocations = new ArrayList<ItemLocation>();

        if (containers != null) {
            for(var container: containers) {
                var itemHandler = container.getCapability(ITEM_HANDLER).resolve().orElse(null);

                if (itemHandler != null) {
                    var slots = itemHandler.getSlots();

                    for (int i = 0; i < slots; i++) {
                        var stackInSlot = itemHandler.getStackInSlot(i);

                        if (Dictionaries.VALID_SEEDS.contains(itemHandler.getStackInSlot(i).getItem())) {
                            itemLocations.add(new ItemLocation(stackInSlot, container, itemHandler, i));
                        }
                    }
                }
            }
        }

        if (itemLocations.isEmpty()) {
            BrainUtils.clearMemory(entity, ModMemoryTypes.AVAILABLE_SEEDS.get());
        } else {
            BrainUtils.setMemory(entity, ModMemoryTypes.AVAILABLE_SEEDS.get(), itemLocations);
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.AVAILABLE_SEEDS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.AVAILABLE_SEEDS.get(), ModMemoryTypes.HOME_CONTAINERS.get());
    }
}
