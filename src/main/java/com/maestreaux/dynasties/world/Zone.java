package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.core.simulation.cache.ZoneCache;
import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CAddZone;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Zone {
    public static StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC;

    private BlockPos center;
    private AABB boundingBox;
    private List<Plot> plots = new ArrayList<>();
    private final Set<VillagerEntitySimulated> residents = new HashSet<>();
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    private final Level level;
    private final ResourceKey<Level> dimension;
    public ZoneCache cache;

    public static List<Zone> ZONES = new ArrayList<>();

    public Zone(UUID uuid, BlockPos center, ResourceKey<Level> dimension) {
        this.level = null;
        this.dimension = dimension;
        this.uuid = uuid;
        this.setCenter(center);
    }

    public Zone(UUID uuid, BlockPos center, ResourceKey<Level> dimension, List<Plot> plots) {
        this(uuid, center, dimension);
        plots.forEach(this::addPlot);
    }

    public Zone(Level level) {
        this.level = level;
        this.dimension = level.dimension();
    }

    public Zone(Level level, BlockPos center) {
        this(level);
        this.setCenter(center);

        if (!level.isClientSide) {
            this.cache = new ZoneCache((ServerLevel) level, this);
            this.cache.indexBlocks(this.boundingBox);
            this.cache.cacheSections(this.boundingBox);
        }
    }

    public static Zone getContainerZone(ServerLevel level, Vec3i pos) {
        var zoneMatch = ZoneSavedData.getZones(level).stream().filter((zone) -> zone.getBoundingBox().contains(pos.getX(), pos.getY(), pos.getZ())).findFirst();
        return zoneMatch.orElse(null);
    }

    public static void add(ServerLevel level, Zone newZone) {
        ZoneSavedData.addZone(level, newZone);
        PacketHandler.sendToAll(new CAddZone(newZone));
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
            this.boundingBox = new AABB(Vec3.atLowerCornerOf(startPos), Vec3.atLowerCornerOf(endPos));
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

    public void addResident(VillagerEntitySimulated villager) {
        this.residents.add(villager);
    }

    public Set<VillagerEntitySimulated> getResidents() {
        return this.residents;
    }

    public Plot addPlot(BlockPos startPos, BlockPos endPos, Plot.PlotType type) {
        var newPlot = new Plot(startPos, endPos, type);
        newPlot.setParentZone(this);

        this.plots.add(newPlot);

        if (this.level != null && !this.level.isClientSide()) {
            this.save((ServerLevel) this.level);
        }

        return newPlot;
    }

    public void addPlot(Plot plot) {
        // Client-side method. No need to encode other params at the moment
        plot.setParentZone(this);

        // Replace plot if existing
        var existingPlot = this.plots.stream().filter(zonePlot -> zonePlot.uuid.equals(plot.uuid)).findFirst().orElse(null);

        if (existingPlot != null) {
            this.plots.replaceAll((zonePlot) -> zonePlot.uuid.equals(plot.uuid) ? plot : zonePlot);
        } else {
            this.plots.add(plot);
        }

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
        var plotStartPos = NbtUtils.readBlockPos(plotTag, "villagerdynasties:plot_start").orElse(null);
        var plotEndPos = NbtUtils.readBlockPos(plotTag, "villagerdynasties:plot_end").orElse(null);
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
        this.setCenter(NbtUtils.readBlockPos(compoundTag, "villagerdynasties:zone_center").orElse(null));
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
        public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider var2) {
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
            return serverLevel.getDataStorage().computeIfAbsent(new Factory<>(ZoneSavedData::create, (compoundTag, provider) -> ZoneSavedData.load(serverLevel, compoundTag), DataFixTypes.LEVEL), "zone");
        }

    }

    static {
        STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Zone::getUUID, BlockPos.STREAM_CODEC, Zone::getCenter, ResourceKey.streamCodec(Registries.DIMENSION), Zone::dimension, Plot.STREAM_CODEC.apply(ByteBufCodecs.list()), Zone::getPlots, Zone::new);
    }
}
