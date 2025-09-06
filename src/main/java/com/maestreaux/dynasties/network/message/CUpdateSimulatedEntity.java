package com.maestreaux.dynasties.network.message;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.simulation.SimulatedEntity;
import com.maestreaux.dynasties.core.simulation.SimulationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CUpdateSimulatedEntity(SimulatedEntity<?> entity) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "update_sim_entity");
    public static final Type<CUpdateSimulatedEntity> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CUpdateSimulatedEntity> STREAM_CODEC = StreamCodec.composite(SimulatedEntity.STREAM_CODEC, CUpdateSimulatedEntity::entity, CUpdateSimulatedEntity::new);

    public static void handle(CUpdateSimulatedEntity message, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SimulationState.CLIENT_ENTITIES.put(message.entity.getUUID(), message.entity);
            });
        });

        context.setPacketHandled(true);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
