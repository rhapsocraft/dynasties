package com.maestreaux.dynasties.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
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
    //public static Zone TEST_ZONE = new Zone(new BlockPos(0, 0, 0));
    private BlockPos zoneCenter;
    private AABB zoneAABB;
    private final List<Plot> plots = new ArrayList<>();
    private final Level level;
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);

    public Zone(ServerLevel level) {
        this.level = level;
    }

    public Zone(ServerLevel level, BlockPos center) {
        this(level);
        this.setCenter(center);
    }

    public static Zone getContainerZone(ServerLevel level, Vec3i pos) {
        var zoneMatch = ZoneSavedData.getZones(level).stream().filter((zone) -> zone.getZoneAABB().contains(pos.getX(), pos.getY(), pos.getZ())).findFirst();
        return zoneMatch.orElse(null);
    }

    public static void addZone(ServerLevel level, Zone newZone) {
        ZoneSavedData.addZone(level, newZone);
    }

    public static void getZones(ServerLevel level) {
        ZoneSavedData.getZones(level);
    }

    public void setCenter(BlockPos newCenter) {
        if (!this.level.isClientSide()) {
            this.zoneCenter = newCenter;
            var startPos = this.zoneCenter.subtract(new Vec3i(16, 4,  16));
            var endPos = this.zoneCenter.subtract(new Vec3i(-16, -12, -16));
            this.zoneAABB = new AABB(startPos, endPos);
        }
    }

    public BlockPos getCenter() {
        return this.zoneCenter;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void addPlot(BlockPos startPos, BlockPos endPos) {
        if (!this.level.isClientSide()) {
            this.plots.add(new Plot(startPos.subtract(this.zoneCenter), endPos.subtract(this.zoneCenter)));
        }
    }

    public List<Plot> getPlots() {
        return this.plots;
    }

    public void clearPlots() {
        this.plots.clear();
    }

    public AABB getZoneAABB() {
        return this.zoneAABB;
    }

    public CompoundTag nbtWritePlot(CompoundTag compoundTag, Plot plot) {
        compoundTag.put("villagerdynasties:plot_start", NbtUtils.writeBlockPos(plot.startPos));
        compoundTag.put("villagerdynasties:plot_end", NbtUtils.writeBlockPos(plot.endPos));

        return compoundTag;
    }

    public Plot nbtReadPlot(CompoundTag plotTag) {
        var plotStartPos = NbtUtils.readBlockPos(plotTag.getCompound("villagerdynasties:plot_start"));
        var plotEndPos = NbtUtils.readBlockPos(plotTag.getCompound("villagerdynasties:plot_end"));

        return new Plot(plotStartPos, plotEndPos);
    }

    public void save(CompoundTag compoundTag) {
        compoundTag.put("villagerdynasties:zone_center", NbtUtils.writeBlockPos(this.zoneCenter));

        var plotList = new ListTag();

        for(var plot: this.plots) {
            var plotTag = new CompoundTag();
            plotList.add(nbtWritePlot(plotTag, plot));
        }

        compoundTag.putUUID("villagerdynasties:zone_uuid", this.uuid);
        compoundTag.put("villagerdynasties:zone_plots", plotList);
    }

    public void load(CompoundTag compoundTag) {
        this.setCenter(NbtUtils.readBlockPos(compoundTag.getCompound("villagerdynasties:zone_center")));
        var listTag = (ListTag) compoundTag.get("villagerdynasties:zone_plots");

        if (listTag != null) {
            for (int i = 0; i < listTag.size(); i++) {
                var currentPlot = listTag.getCompound(i);
                this.plots.add(nbtReadPlot(currentPlot));
            }
        }

        if (compoundTag.hasUUID("villagerdynasties:zone_uuid")) {
            this.uuid = compoundTag.getUUID("villagerdynasties:zone_uuid");
        }
    }

    public static class Plot {
        private final BlockPos startPos;
        private final BlockPos endPos;

        public Plot(BlockPos startPos, BlockPos endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }

        public Vec3i getStartPos() {
            return startPos;
        }

        public Vec3i getEndPos() {
            return endPos;
        }
    }

    public static class ZoneSavedData extends SavedData {
        private ServerLevel level;
        private final List<Zone> zones = new ArrayList<>();
        public static ZoneSavedData create() {
            return new ZoneSavedData();
        }

        public static void addZone(ServerLevel level, Zone zone) {
            var instance = getInstance(level);
            instance.zones.add(zone);
            instance.setDirty();
        }

        public static List<Zone> getZones(ServerLevel level) {
            var instance = getInstance(level);
            return instance.zones;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public void setLevel(ServerLevel level) {
            this.level = level;
        }

        public static ZoneSavedData load(CompoundTag compoundTag) {
            ZoneSavedData data = create();

            var zonesTag = (ListTag) compoundTag.get("villagerdynasties:zones");

            if (zonesTag != null && data.level != null) {
                for (int i = 0; i < zonesTag.size(); i++) {
                    var newZone = new Zone(data.level);
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
            return serverLevel.getDataStorage().computeIfAbsent(ZoneSavedData::load, () -> {
                var zoneSavedData = ZoneSavedData.create();
                zoneSavedData.setLevel(serverLevel);

                return zoneSavedData;
            }, "zone");
        }
    }
}
