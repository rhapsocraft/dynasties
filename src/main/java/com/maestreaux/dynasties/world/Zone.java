package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.ZonePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Zone {
    private BlockPos center;
    private AABB boundingBox;
    private final List<Plot> plots = new ArrayList<>();
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    private final Level level;
    private final ResourceKey<Level> dimension;
    public static List<Zone> ZONES = new ArrayList<>();

    public Zone(UUID uuid, BlockPos center, ResourceKey<Level> dimension) {
        this.level = null;
        this.dimension = dimension;
        this.uuid = uuid;
        this.setCenter(center);
    }

    public Zone(Level level) {
        this.level = level;
        this.dimension = level.dimension();
    }

    public Zone(Level level, BlockPos center) {
        this(level);
        this.setCenter(center);
    }

    public static Zone getContainerZone(ServerLevel level, Vec3i pos) {
        var zoneMatch = ZoneSavedData.getZones(level).stream().filter((zone) -> zone.getBoundingBox().contains(pos.getX(), pos.getY(), pos.getZ())).findFirst();
        return zoneMatch.orElse(null);
    }

    public static void add(ServerLevel level, Zone newZone) {
        ZoneSavedData.addZone(level, newZone);
        PacketHandler.sendToAll(new ZonePacket.CAddZonePacket(newZone));
    }

    public static void add(Zone newZone) {
        ZONES.add(newZone);
    }

    public static List<Zone> getZones(ServerLevel level) {
        return ZoneSavedData.getZones(level);
    }

    public static void setZones(List<Zone> newZones) {
        ZONES = newZones;
    }

    public static List<Zone> getZones() {
        return ZONES;
    }
    public void save(ServerLevel level) {
        var data = ZoneSavedData.getInstance(level);
        data.save();
    }

    public void setCenter(BlockPos newCenter) {
            this.center = newCenter;
            var startPos = this.center.subtract(new Vec3i(16, 4,  16));
            var endPos = this.center.subtract(new Vec3i(-16, -12, -16));
            this.boundingBox = new AABB(startPos, endPos);
    }

    public Level level() {
        return this.level;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public static Zone getZoneByUUID(UUID uuid) {
        var firstMatch = ZONES.stream().filter(zone -> uuid.equals(zone.getUUID())).findFirst();

        return firstMatch.orElse(null);
    }

    public static Zone getZoneByUUID(ServerLevel level, UUID uuid) {
        return ZoneSavedData.getZones(level).stream().filter(zone -> uuid.equals(zone.getUUID())).findFirst().orElse(null);
    }

    public Plot addPlot(BlockPos startPos, BlockPos endPos, Plot.PlotType type, int numSlots) {
        var newPlot = new Plot(startPos, endPos, type);
        newPlot.setParentZone(this);

        for(int i = 0; i < numSlots; i++) {
            newPlot.addEmptySlot();
        }

        this.plots.add(newPlot);

        if (this.level != null && !this.level.isClientSide()) {
            this.save((ServerLevel) this.level);
        }

        return newPlot;
    }

    public Plot addPlot(BlockPos startPos, BlockPos endPos, Plot.PlotType type) {
        return this.addPlot(startPos, endPos, type, 0);
    }

    public Plot getPlotByUUID(UUID plotUUID) {
        return this.plots.stream().filter((plot) -> plot.getUUID().equals(plotUUID)).findFirst().orElse(null);
    }

    public List<Plot> getAvailablePlots() {
        return this.plots.stream().filter(plot -> !plot.isPlotFull()).toList();
    }

    public Plot getNextAvailablePlot() {
        return this.plots.stream().filter(plot -> plot.getAvailableSlot() != null).findFirst().orElse(null);
    }

    public List<Plot> getPlots() {
        return this.plots;
    }

    public void clearPlots() {
        this.plots.clear();
    }

    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    public CompoundTag nbtWritePlot(CompoundTag compoundTag, Plot plot) {
        compoundTag.put("villagerdynasties:plot_start", NbtUtils.writeBlockPos(plot.getStartPos()));
        compoundTag.put("villagerdynasties:plot_end", NbtUtils.writeBlockPos(plot.getEndPos()));
        compoundTag.putString("villagerdynasties:plot_type", plot.getType().name());

        plot.save(compoundTag);

        return compoundTag;
    }

    public Plot nbtReadPlot(CompoundTag plotTag) {
        var plotStartPos = NbtUtils.readBlockPos(plotTag.getCompound("villagerdynasties:plot_start"));
        var plotEndPos = NbtUtils.readBlockPos(plotTag.getCompound("villagerdynasties:plot_end"));
        var plotType = Plot.PlotType.valueOf(plotTag.getString("villagerdynasties:plot_type"));

        return new Plot(plotStartPos, plotEndPos, plotType);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put("villagerdynasties:zone_center", NbtUtils.writeBlockPos(this.center));

        var plotList = new ListTag();

        for(var plot: this.plots) {
            var plotTag = new CompoundTag();
            plotList.add(nbtWritePlot(plotTag, plot));
        }

        compoundTag.putUUID("villagerdynasties:zone_uuid", this.uuid);
        compoundTag.put("villagerdynasties:zone_plots", plotList);

        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.setCenter(NbtUtils.readBlockPos(compoundTag.getCompound("villagerdynasties:zone_center")));
        var listTag = (ListTag) compoundTag.get("villagerdynasties:zone_plots");

        if (listTag != null) {
            for (int i = 0; i < listTag.size(); i++) {
                var currentPlot = listTag.getCompound(i);
                var newPlot = nbtReadPlot(currentPlot);
                newPlot.setParentZone(this);
                newPlot.load(currentPlot);

                this.plots.add(newPlot);
            }
        }

        if (compoundTag.hasUUID("villagerdynasties:zone_uuid")) {
            this.uuid = compoundTag.getUUID("villagerdynasties:zone_uuid");
        }
    }

    public static class ZoneSavedData extends SavedData {
        private final List<Zone> zones = new ArrayList<>();

        public ZoneSavedData() {
        }

        public static ZoneSavedData create() {
            return new ZoneSavedData();
        }

        public static void addZone(ServerLevel level, Zone zone) {
            var instance = getInstance(level);
            instance.zones.add(zone);
            instance.save();
        }

        public static List<Zone> getZones(ServerLevel level) {
            var instance = getInstance(level);
            return instance.zones;
        }

        public static ZoneSavedData load(ServerLevel level, CompoundTag compoundTag) {
            ZoneSavedData data = create();

            var zonesTag = (ListTag) compoundTag.get("villagerdynasties:zones");

            if (zonesTag != null) {
                for (int i = 0; i < zonesTag.size(); i++) {
                    var newZone = new Zone(level);
                    newZone.load(zonesTag.getCompound(i));
                    data.zones.add(newZone);
                }
            }

            return data;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
            var zoneList = new ListTag();

            for(var zone: this.zones) {
                var zoneTag = new CompoundTag();
                zone.save(zoneTag);
                zoneList.add(zoneTag);
            }

            compoundTag.put("villagerdynasties:zones", zoneList);

            return compoundTag;
        }

        public void save() {
            this.setDirty();
        }

        public static ZoneSavedData getInstance(ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(((compoundTag) -> ZoneSavedData.load(serverLevel, compoundTag)), ZoneSavedData::create, "zone");
        }

    }
}
