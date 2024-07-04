package com.maestreaux.dynasties.network;

import com.maestreaux.dynasties.world.Zone;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ZonePacket {
    public static class CZonesPacket {
        private final List<Zone> zones;
        public CZonesPacket(List<Zone> zones) {
            this.zones = zones;
        }

        public CZonesPacket(FriendlyByteBuf buffer) {
            int size = buffer.readInt();

            this.zones = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                var tag = buffer.readNbt();
                var dimension = buffer.readResourceKey(Registries.DIMENSION);

                if (tag != null) {
                    var newZone = new Zone(tag.getUUID("zone-packet:uuid"), NbtUtils.readBlockPos(tag.getCompound("zone-packet:center")), dimension);
                    this.zones.add(newZone);
                }
            }
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeInt(zones.size());

            for (var zone: zones) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("zone-packet:uuid", zone.getUUID());
                tag.put("zone-packet:center", NbtUtils.writeBlockPos(zone.getCenter()));
                buffer.writeNbt(tag);
                buffer.writeResourceKey(zone.dimension());
            }
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            var ctx = context.get();

            ctx.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(context));
            });

            ctx.setPacketHandled(true);
        }

        public void handlePacket(Supplier<NetworkEvent.Context> context) {
            Zone.setZones(this.zones);
        }
    }
}
