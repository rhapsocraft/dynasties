package com.maestreaux.dynasties.core.simulation.entity;

import com.maestreaux.dynasties.core.simulation.SimulationState;
import com.maestreaux.dynasties.core.simulation.ai.IAgent;
import com.maestreaux.dynasties.core.simulation.ai.IAgentMemory;
import com.maestreaux.dynasties.core.simulation.ai.behavior.IAgentBehavior;
import com.maestreaux.dynasties.core.simulation.ai.sensor.BaseAgentSensor;
import com.maestreaux.dynasties.core.simulation.ai.sensor.IAgentSensor;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CUpdateSimulatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class EntitySimulated<T extends Entity> implements IAgent {
    public static StreamCodec<RegistryFriendlyByteBuf, EntitySimulated<?>> STREAM_CODEC;

    protected BlockPos pos;
    protected BlockPos lastKnownPos;
    protected UUID uuid;
    protected ServerLevel level;
    protected boolean tickingEntity = false;
    protected CompoundTag entitySavedData = new CompoundTag();
    protected SimulatedEntityType type;

    protected Map<Class<? extends BaseAgentSensor<?,?>>, IAgentSensor<?>> agentSensorsMap = new HashMap<>();
    protected Map<Class<? extends IAgentMemory<?>>, IAgentMemory<?>> agentMemories = new HashMap<>();

    // TODO: Potential bug if entity can potentially die, but is somehow unloaded before dying, causing it to continue being simulated when they would have died instead.
    // Possible solution: Force chunks to remain loaded after some time around an entity that is "in danger" (taking damage, on fire, falling)
    // It is planned that chunks will be loaded around entities that are in combat even if there are no players nearby.

    public EntitySimulated(ServerLevel level) {
        this.level = level;
    }

    // Client-Only
    public EntitySimulated(UUID uuid, BlockPos pos) {
        this.uuid = uuid;
        this.pos = pos;
    }

    // This constructor is called when entity is still loaded and is likely registered onto the Simulator
    // or if the entity is newly created
    public EntitySimulated(T entity) {
        this.uuid = entity.getUUID();
        this.level = (ServerLevel) entity.level();
        this.pos = entity.getOnPos();
        this.lastKnownPos = entity.getOnPos();
        this.type = SimulatedEntityType.BASE;

        this.entitySavedData = entity.serializeNBT(level.registryAccess());
    }

    public ServerLevel level() {
        return this.level;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setPos(BlockPos newPos) {
        this.pos = newPos;
    }

    public BlockPos getLastKnownPos() {
        return this.lastKnownPos;
    }

    @SuppressWarnings("unchecked")
    public T getEntity() {
        T entity = (T) this.level.getEntity(this.uuid);

        if (entity != null && !entity.isRemoved() && entity.isAlive()) {
            return entity;
        }

        return null;
    }

    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }

    public void tick() {
        var previousTickingState = this.tickingEntity;
        this.tickingEntity = this.level.isPositionEntityTicking(this.pos);

        if (!previousTickingState && this.tickingEntity) {
            // Simulated Entity is being ticked
            this.syncToEntity();
        } else if (previousTickingState && !this.tickingEntity) {
            // Simulated Entity is no longer being ticked
            this.syncFromEntity();
        }

        this.tickAI();
    }

    public boolean isEntityTicking() {
        return this.tickingEntity;
    }

    public void save(CompoundTag tag) {
        tag.putUUID("sim_uuid", this.uuid);
        tag.put("sim_pos", NbtUtils.writeBlockPos(this.pos));
        tag.put("last_known_pos", NbtUtils.writeBlockPos(this.lastKnownPos));
        tag.putString("type", this.type.toString());

        tag.put("entity_data", this.entitySavedData);
    }

    public void load(CompoundTag tag) {
        this.uuid = tag.getUUID("sim_uuid");
        this.pos = NbtUtils.readBlockPos(tag, "sim_pos").orElse(null);
        this.lastKnownPos = NbtUtils.readBlockPos(tag, "last_known_pos").orElse(null);
        this.type = SimulatedEntityType.valueOf(tag.getString("type"));
        this.entitySavedData = tag.getCompound("entity_data");

        PacketHandler.sendToAll(new CUpdateSimulatedEntity(this));
    }

    public void loadEntityData(T entity) {
        entity.deserializeNBT(this.registryAccess(), this.entitySavedData);
    }

    public void saveEntityData(T entity) {
        this.entitySavedData = entity.serializeNBT(this.registryAccess());
        this.syncFromEntity();

        var instance = SimulationState.SimulationStateSavedData.getInstance((ServerLevel) entity.level());
        instance.save();
    }

    public CompoundTag getEntityDataTag() {
        return this.entitySavedData;
    }

    protected T spawnEntity() {
        return null;
    }

    protected T spawnEntity(T newEntity) {
        newEntity.setUUID(this.uuid);
        level.addFreshEntity(newEntity);

        newEntity.load(this.entitySavedData);

        return newEntity;
    }

    public void syncToEntity() {
        var entity = this.getEntity();

        if (entity == null) {
            // Entity of this UUID does not exist anymore (unloaded) so we add one.
            this.spawnEntity();
        }
    }

    public void syncFromEntity() {
        var entity = this.getEntity();

        if (entity != null) {
            var lastKnownBlockPos = entity.getOnPos().above();

            this.pos = lastKnownBlockPos;
            this.lastKnownPos = lastKnownBlockPos;

            PacketHandler.sendToAll(new CUpdateSimulatedEntity(this));
        }
    }

    @Override
    public void tickAI() {
        var sensors = this.getSensors();

        // Tick sensors
        for (var sensor: sensors) {
            sensor.update(this.level);
        }

        var behaviors = this.getBehaviors();

        // Tick behaviors
        for (var behavior : behaviors) {
            if (behavior.canStart()) {
                behavior.start();
            }

            if (behavior.canTick()) {
                behavior.tick();
            }
        }
    }

    @Override
    public ServerLevel getLevel() {
        return null;
    }

    @Override
    public List<IAgentBehavior> getBehaviors() {
        return List.of();
    }

    @Override
    public List<IAgentSensor<?>> getSensors() {
        return this.agentSensorsMap.values().stream().toList();
    }

    @Override
    public <S extends BaseAgentSensor<?, ?>> S getSensor(Class<S> cls) {
        return cls.cast(this.agentSensorsMap.get(cls));
    }

    @Override
    public <M extends IAgentMemory<?>> M getMemory(Class<M> cls) {
        return cls.cast(this.agentMemories.get(cls));
    }

    // TODO: Use new registry
    public enum SimulatedEntityType {
        BASE,
        VILLAGER,
    }

    static {
        STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, EntitySimulated::getUUID, BlockPos.STREAM_CODEC, EntitySimulated::getPos, EntitySimulated::new);
    }
}
