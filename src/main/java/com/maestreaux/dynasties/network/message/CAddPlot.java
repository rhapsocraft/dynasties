package com.maestreaux.dynasties.network.message;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;


public record CAddPlot(Zone zone, Plot plot) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "add_plot_message");
    public static final Type<CAddZone> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CAddPlot> STREAM_CODEC = StreamCodec.composite(Zone.STREAM_CODEC, CAddPlot::zone, Plot.STREAM_CODEC, CAddPlot::plot, CAddPlot::new);

    public static void handle(CAddPlot message, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var targetZone = Zone.getZoneByUUID(message.zone.getUUID());
                targetZone.addPlot(message.plot);
            });
        });

        context.setPacketHandled(true);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
