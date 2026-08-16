package com.maestreaux.dynasties.core.simulation;

import com.maestreaux.dynasties.core.simulation.blockentity.BlockEntitySimulated;
import com.maestreaux.dynasties.core.simulation.cache.ZoneCache;
import com.maestreaux.dynasties.core.simulation.entity.EntitySimulated;
import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CRemoveSimulatedEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class SimulationState {
    public static Map<UUID, EntitySimulated<?>> CLIENT_ENTITIES = new HashMap<>();
    public static Map<UUID, ZoneCache> ZONE_CACHE = new HashMap<>();
    private static final Map<EntitySimulated.SimulatedEntityType, Function<ServerLevel, EntitySimulated<?>>> SIM_ENTITY_TYPES = Map.of(EntitySimulated.SimulatedEntityType.BASE, EntitySimulated::new, EntitySimulated.SimulatedEntityType.VILLAGER, VillagerEntitySimulated::new);

    public static EntitySimulated<?> addEntity(ServerLevel level, EntitySimulated<?> entity) {
        SimulationStateSavedData.addEntity(level, entity);
        return entity;
    }

    public static EntitySimulated<?> getEntity(ServerLevel level, UUID uuid) {
        return SimulationStateSavedData.getEntity(level, uuid);
    }

    public static List<EntitySimulated<?>> getEntities(ServerLevel level) {
        return SimulationStateSavedData.getEntities(level);
    }

    public static void setClientEntities(Map<UUID, EntitySimulated<?>> clientEntities) {
        CLIENT_ENTITIES = clientEntities;
    }

    public static void removeEntity(ServerLevel level, UUID uuid) {
        var removedEntity = SimulationStateSavedData.removeEntity(level, uuid);

        var savedData = SimulationStateSavedData.getInstance(level);
        savedData.save();

        if (removedEntity != null) {
            PacketHandler.sendToAll(new CRemoveSimulatedEntity(removedEntity.getUUID()));
        }
    }


    public static class SimulationStateSavedData extends SavedData {
        private final Map<UUID, EntitySimulated<?>> simulatedEntities = new HashMap<>();
        private final Map<UUID, BlockEntitySimulated<?>> simulatedBlockEntities = new HashMap<>();

        public static SimulationStateSavedData create() {
            return new SimulationStateSavedData();
        }

        public static List<EntitySimulated<?>> getEntities(ServerLevel level) {
            var instance = SimulationStateSavedData.getInstance(level);

            return instance.simulatedEntities.values().stream().toList();
        }

        public static void addEntity(ServerLevel level, EntitySimulated<?> entity) {
            var instance = SimulationStateSavedData.getInstance(level);
            instance.simulatedEntities.put(entity.getUUID(), entity);
            instance.save();
        }

        public static EntitySimulated<?> removeEntity(ServerLevel level, UUID uuid) {
            var instance = SimulationStateSavedData.getInstance(level);
            var removedEntity = instance.simulatedEntities.remove(uuid);
            instance.save();

            return removedEntity;
        }

        public static EntitySimulated<?> getEntity(ServerLevel level, UUID uuid) {
            var instance = SimulationStateSavedData.getInstance(level);
            return instance.simulatedEntities.get(uuid);
        }

        public static SimulationStateSavedData load(ServerLevel level, CompoundTag compoundTag) {
            SimulationStateSavedData data = create();

            var simulatedEntitiesTag = (ListTag) compoundTag.get("sim_entities");

            if (simulatedEntitiesTag != null) {
                for (int i = 0; i < simulatedEntitiesTag.size(); i++) {
                    var simEntityTag = simulatedEntitiesTag.getCompound(i);
                    var type  = EntitySimulated.SimulatedEntityType.valueOf(simEntityTag.getString("type"));
                    var newSimulatedEntity = SimulationState.SIM_ENTITY_TYPES.get(type).apply(level);
                    newSimulatedEntity.load(simEntityTag);

                    data.simulatedEntities.put(newSimulatedEntity.getUUID(), newSimulatedEntity);
                }
            }

            return data;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider var2) {
            var entitiesTag = new ListTag();

            for(var entity: this.simulatedEntities.values()) {
                var entityTag = new CompoundTag();
                entity.save(entityTag);

                entitiesTag.add(entityTag);
            }

            compoundTag.put("sim_entities", entitiesTag);

            return compoundTag;
        }

        public void save() {
            this.setDirty();
        }

        public static SimulationStateSavedData getInstance(ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(new Factory<>(SimulationStateSavedData::create, (compoundTag, provider) -> SimulationStateSavedData.load(serverLevel, compoundTag), DataFixTypes.LEVEL), "simulation_state");
        }
    }
}
