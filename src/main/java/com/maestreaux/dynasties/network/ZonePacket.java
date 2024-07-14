package com.maestreaux.dynasties.network;

import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ZonePacket {
    public static List<Partition> decodePartitionsFromBuffer(FriendlyByteBuf buffer) {
        var partitionsCount = buffer.readInt();
        var partitions = new ArrayList<Partition>();

        for (int j = 0; j < partitionsCount; j++) {
            var partOrigin = buffer.readBlockPos();
            var partWidth = buffer.readInt();
            var partLength = buffer.readInt();

            partitions.add(new Partition(partOrigin, partWidth, partLength));
        }

        return partitions;
    }

    public static void encodePartitionsToBuffer(FriendlyByteBuf buffer, List<Partition> partitions) {
        buffer.writeInt(partitions.size());

        for(var partition: partitions) {
            buffer.writeBlockPos(partition.getOrigin());
            buffer.writeInt(partition.getWidth());
            buffer.writeInt(partition.getLength());
        }
    }

    public static void encodeZoneToBuffer(FriendlyByteBuf buffer, Zone zone) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("zone-packet:uuid", zone.getUUID());
        tag.put("zone-packet:center", NbtUtils.writeBlockPos(zone.getCenter()));

        buffer.writeNbt(tag);
        buffer.writeResourceKey(zone.dimension());

        var plots = zone.getPlots();
        buffer.writeInt(plots.size());

        for(var plot: plots) {
            buffer.writeBlockPos(plot.getStartPos());
            buffer.writeBlockPos(plot.getEndPos());

            var partitions = plot.getPartitions();
            encodePartitionsToBuffer(buffer, partitions);
        }
    }

    public static Zone decodeZoneFromBuffer(FriendlyByteBuf buffer) {
        var tag = buffer.readNbt();
        var dimension = buffer.readResourceKey(Registries.DIMENSION);

        if (tag != null) {
            var newZone = new Zone(tag.getUUID("zone-packet:uuid"), NbtUtils.readBlockPos(tag.getCompound("zone-packet:center")), dimension);

            var plotsCount = buffer.readInt();

            for (int i = 0; i < plotsCount; i++) {
                var startPos = buffer.readBlockPos();
                var endPos = buffer.readBlockPos();

                var newPlot = newZone.addPlot(startPos, endPos);
                var partitions = decodePartitionsFromBuffer(buffer);

                for (var partition: partitions) {
                    newPlot.addPartition(partition);
                }
            }

            return newZone;
        }

        return null;
    }

    public static class CAddPlotPacket {
        private final BlockPos plotStartPos;
        private final BlockPos plotEndPos;
        private final UUID zoneUUID;
        private final List<Partition> partitions;

        public CAddPlotPacket(UUID zoneUUID, BlockPos plotStartPos, BlockPos plotEndPos) {
            this.zoneUUID = zoneUUID;
            this.plotStartPos = plotStartPos;
            this.plotEndPos = plotEndPos;
            this.partitions = new ArrayList<>();
        }

        public CAddPlotPacket(UUID zoneUUID, BlockPos plotStartPos, BlockPos plotEndPos, List<Partition> partitions) {
            this(zoneUUID, plotStartPos, plotEndPos);
            this.partitions.addAll(partitions);
        }

        public CAddPlotPacket(FriendlyByteBuf buffer) {
            this.zoneUUID = buffer.readUUID();
            this.plotStartPos = buffer.readBlockPos();
            this.plotEndPos = buffer.readBlockPos();

            this.partitions = new ArrayList<>();
            this.partitions.addAll(decodePartitionsFromBuffer(buffer));
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(this.zoneUUID);
            buffer.writeBlockPos(this.plotStartPos);
            buffer.writeBlockPos(this.plotEndPos);

            encodePartitionsToBuffer(buffer, this.partitions);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            var ctx = context.get();

            ctx.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(context));
            });

            ctx.setPacketHandled(true);
        }

        public void handlePacket(Supplier<NetworkEvent.Context> context) {
            var zone = Zone.getZoneByUUID(this.zoneUUID);
            var plot = zone.addPlot(this.plotStartPos, this.plotEndPos);

            for (var partition: this.partitions) {
                plot.addPartition(partition);
            }
        }
    }

    public static class CAddZonePacket {
        private final Zone zone;

        public CAddZonePacket(Zone zone) {
            this.zone = zone;
        }

        public CAddZonePacket(FriendlyByteBuf buffer) {
            this.zone = decodeZoneFromBuffer(buffer);
        }

        public void encode(FriendlyByteBuf buffer) {
            encodeZoneToBuffer(buffer, this.zone);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            var ctx = context.get();

            ctx.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(context));
            });

            ctx.setPacketHandled(true);
        }

        public void handlePacket(Supplier<NetworkEvent.Context> context) {
            Zone.add(this.zone);
        }

    }

    public static class CZonesPacket {
        private final List<Zone> zones;
        public CZonesPacket(List<Zone> zones) {
            this.zones = zones;
        }

        public CZonesPacket(FriendlyByteBuf buffer) {
            int size = buffer.readInt();

            this.zones = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                var zone = decodeZoneFromBuffer(buffer);
                this.zones.add(zone);
            }
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeInt(zones.size());

            for (var zone: zones) {
                encodeZoneToBuffer(buffer, zone);
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
