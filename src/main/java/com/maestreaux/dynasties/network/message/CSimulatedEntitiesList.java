package com.maestreaux.dynasties.network.message;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.simulation.SimulatedEntity;
import com.maestreaux.dynasties.core.simulation.SimulationState;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public record CSimulatedEntitiesList(List<SimulatedEntity<?>> entities) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "sim_entities_message");
    public static final CustomPacketPayload.Type<CSimulatedEntitiesList> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CSimulatedEntitiesList> STREAM_CODEC = StreamCodec.composite(SimulatedEntity.STREAM_CODEC.apply(ByteBufCodecs.list()), CSimulatedEntitiesList::entities, CSimulatedEntitiesList::new);

    public static void handle(CSimulatedEntitiesList message, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var newMap = new HashMap<UUID, SimulatedEntity<?>>();

                for (var entity : message.entities()) {
                    newMap.putIfAbsent(entity.getUUID(), entity);
                }

                SimulationState.setClientEntities(newMap);
            });
        });

        context.setPacketHandled(true);
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
