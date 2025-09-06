package com.maestreaux.dynasties.core.simulation;

import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CUpdateSimulatedEntity;
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
    public static Map<UUID, SimulatedEntity<?>> CLIENT_ENTITIES = new HashMap<>();
    private static Map<SimulatedEntity.SimulatedEntityType, Function<ServerLevel, SimulatedEntity<?>>> SIM_ENTITY_TYPES = Map.of(SimulatedEntity.SimulatedEntityType.BASE, SimulatedEntity::new, SimulatedEntity.SimulatedEntityType.VILLAGER, SimulatedVillagerEntity::new);

    public static SimulatedEntity<?> addEntity(ServerLevel level, SimulatedEntity<?> entity) {
        SimulationStateSavedData.addEntity(level, entity);
        return entity;
    }

    public static SimulatedEntity<?> getEntity(ServerLevel level, UUID uuid) {
        return SimulationStateSavedData.getEntity(level, uuid);
    }

    public static List<SimulatedEntity<?>> getEntities(ServerLevel level) {
        return SimulationStateSavedData.getEntities(level);
    }

    public static void setClientEntities(Map<UUID, SimulatedEntity<?>> clientEntities) {
        CLIENT_ENTITIES = clientEntities;
    }

    public static void removeEntity(ServerLevel level, UUID uuid) {
        SimulationStateSavedData.removeEntity(level, uuid);

        var savedData = SimulationStateSavedData.getInstance(level);
        savedData.save();
    }

    public static class SimulationStateSavedData extends SavedData {
        private final Map<UUID, SimulatedEntity<?>> simulatedEntities = new HashMap<>();

        public SimulationStateSavedData() {
        }

        public static SimulationStateSavedData create() {
            return new SimulationStateSavedData();
        }

        public static List<SimulatedEntity<?>> getEntities(ServerLevel level) {
            var instance = SimulationStateSavedData.getInstance(level);

            return instance.simulatedEntities.values().stream().toList();
        }

        public static void addEntity(ServerLevel level, SimulatedEntity<?> entity) {
            var instance = SimulationStateSavedData.getInstance(level);
            instance.simulatedEntities.put(entity.uuid, entity);
            instance.save();
        }

        public static void removeEntity(ServerLevel level, UUID uuid) {
            var instance = SimulationStateSavedData.getInstance(level);
            instance.simulatedEntities.remove(uuid);
            instance.save();
        }

        public static SimulatedEntity<?> getEntity(ServerLevel level, UUID uuid) {
            var instance = SimulationStateSavedData.getInstance(level);
            return instance.simulatedEntities.get(uuid);
        }

        public static SimulationStateSavedData load(ServerLevel level, CompoundTag compoundTag) {
            SimulationStateSavedData data = create();

            var simulatedEntitiesTag = (ListTag) compoundTag.get("sim_entities");

            if (simulatedEntitiesTag != null) {
                for (int i = 0; i < simulatedEntitiesTag.size(); i++) {
                    var simEntityTag = simulatedEntitiesTag.getCompound(i);
                    var type  = SimulatedEntity.SimulatedEntityType.valueOf(simEntityTag.getString("type"));
                    var newSimulatedEntity = SimulationState.SIM_ENTITY_TYPES.get(type).apply(level);
                    newSimulatedEntity.load(simEntityTag);

                    data.simulatedEntities.put(newSimulatedEntity.uuid, newSimulatedEntity);
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
