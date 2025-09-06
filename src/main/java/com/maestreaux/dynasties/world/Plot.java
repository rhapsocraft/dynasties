package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class Plot {
    public static StreamCodec<RegistryFriendlyByteBuf, Plot> STREAM_CODEC;
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    private final BlockPos startPos;
    private final BlockPos endPos;
    private Zone parentZone;
    private final List<Slot> slots = new ArrayList<>();
    private final List<Partition> partitions = new ArrayList<>();
    private PlotType type;

    public Map<JobType, JobTicket> jobMap = Map.of(JobType.COOK_FOOD, new JobTicket("cook_food"));
    // TODO: Per household instead of per plot
    private long lastTaxed = -1;

    private boolean isEnabled = false;

    public Plot(BlockPos startPos, BlockPos endPos, PlotType type) {
        var corner1 = new BlockPos(startPos.getX(), startPos.getY(), endPos.getZ());
        var corner2 = new BlockPos(endPos.getX(), startPos.getY(), startPos.getZ());
        List<BlockPos> list = ObjectArrayList.of(startPos, endPos, corner1, corner2);
        list.sort(Plot::mostSouthernEast);

        this.endPos = ((BlockPos) list.toArray()[0]);
        this.startPos = ((BlockPos) list.toArray()[list.size() - 1]);

        this.type = type;
    }

    public Plot(BlockPos startPos, BlockPos endPos) {
        this(startPos, endPos, null);
    }

    public Plot(UUID uuid, BlockPos startPos, BlockPos endPos, String type, List<Partition> partitions) {
        // CLIENT-SIDE
        this(startPos, endPos);
        partitions.forEach(this::addPartition);

        this.type = PlotType.valueOf(type);
        this.uuid = uuid;
    }

    private static int mostSouthernEast(Vec3i pos1, Vec3i pos2) {
        if (pos1.getX() + pos1.getZ() > pos2.getX() + pos2.getZ()) {
            return -1;
        } else {
            return 1;
        }
    }

    public void addEmptySlot() {
        slots.add(new Slot(this));
    }

    public void addSlot(Job job) {
        slots.add(new Slot(this, job));
    }

    public void clearSlots() {
        this.slots.clear();
    }

    public BlockPos getStartPos() {
        return this.startPos;
    }

    public BlockPos getAbsoluteStartPos() {
        return this.startPos.offset(this.parentZone.getCenter());
    }

    public BlockPos getEndPos() {
        return this.endPos;
    }

    public PlotType getType() {
        return this.type;
    }

    public String getTypeName() {
        return this.type.name();
    }

    public BlockPos getAbsoluteEndPos() {
        return this.endPos.offset(this.parentZone.getCenter());
    }

    public void setParentZone(Zone zone) {
        this.parentZone = zone;
    }

    public Zone getParentZone() {
        return this.parentZone;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void enable() {
        this.setEnabled(true);
    }

    public void disable() {
        this.setEnabled(false);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Slot getSlot(UUID uuid) {
        return this.slots.stream().filter(slot -> slot.uuid.equals(uuid)).findFirst().orElse(null);
    }

    public List<Slot> getSlots() {
        return this.slots;
    }

    public List<Slot> getOccupiedSlots() {
        return this.slots.stream().filter(slot -> slot.occupiedBy != null).toList();
    }

    public void clearVillagerFromPlot(AbstractDynastyVillager villager) {
        var occupiedSlots = this.slots.stream().filter(slot -> slot.occupiedBy == villager).toList();

        for (var occupiedSlot : occupiedSlots) {
            occupiedSlot.refreshSlot();
        }
    }

    public void setType(PlotType newType) {
        this.type = newType;
    }

    public Slot getSlotByVillager(AbstractDynastyVillager villager) {
        return this.slots.stream().filter(slot -> slot.occupiedBy == villager).findFirst().orElse(null);
    }

    // TODO: Temporary
    public Partition getPartitionToBuildOn() {
        return this.partitions.stream().filter(part -> !part.isConstructionFinished()).findFirst().orElse(null);
    }

    public Partition getHomePartition() {
        return this.partitions.stream().filter(part -> part.getPartitionType() == Partition.PartitionType.HOME).findFirst().orElse(null);
    }

    public List<Partition> getPartitionsByType(Partition.PartitionType type) {
        return this.partitions.stream().filter(part -> part.getPartitionType() == type).toList();
    }

    public void addPartition(Partition newPartition) {
        newPartition.setParentPlot(this);
        this.partitions.add(newPartition);
    }

    public void clearPartitions() {
        this.partitions.clear();
    }

    public List<Partition> getPartitions() {
        return this.partitions;
    }

    public Slot getAvailableSlot() {
        return this.slots.stream().filter(slot -> slot.occupiedBy == null && slot.job != Job.NOBLE).findFirst().orElse(null);
    }

    public Slot getAvailableSlot(Job desiredJob) {
        return this.slots.stream().filter(slot -> slot.occupiedBy == null && slot.job == desiredJob).findFirst().orElse(null);
    }

    public boolean isPlotFull() {
        return this.slots.stream().noneMatch(slot -> slot.occupiedBy == null);
    }

    public void save(CompoundTag tag) {
        var slotsListTag = new ListTag();
        var partitionsListTag = new ListTag();
        var jobsListTag = new ListTag();

        for (var slot : this.slots) {
            var slotTag = new CompoundTag();
            slot.save(slotTag);

            slotsListTag.add(slotTag);
        }

        for (var job: this.jobMap.values()) {
            var jobFulfilledTimeTag = new CompoundTag();

            jobFulfilledTimeTag.putLong("villagerdynasties:job_time_fulfilled", job.fulfilledDayTime);
        }

        for (var partition : this.partitions) {
            var partitionTag = new CompoundTag();
            partition.save(partitionTag);

            partitionsListTag.add(partitionTag);
        }

        tag.put("villagerdynasties:jobs", jobsListTag);
        tag.put("villagerdynasties:slots", slotsListTag);
        tag.put("villagerdynasties:partitions", partitionsListTag);
        tag.putUUID("villagerdynasties:plot_uuid", this.uuid);
        tag.putString("villagerdynasties:plot_type", this.type.toString());
        tag.putBoolean("villagerdynasties:plot_enabled", this.isEnabled);
        tag.putLong("villagerdynasties:last_taxed", this.lastTaxed);
    }

    public void load(CompoundTag tag) {
        var slotsListTag = (ListTag) tag.get("villagerdynasties:slots");
        var partitionsListTag = (ListTag) tag.get("villagerdynasties:partitions");
        var jobsListTag = (ListTag) tag.get("villagerdynasties:jobs");

        if (slotsListTag != null) {
            for (int i = 0; i < slotsListTag.size(); i++) {
                var newSlot = new Slot(this);
                newSlot.load(slotsListTag.getCompound(i));

                this.slots.add(newSlot);
            }
        }

        if (partitionsListTag != null) {
            for (int i = 0; i < partitionsListTag.size(); i++) {
                var newPartition = new Partition();
                newPartition.load(partitionsListTag.getCompound(i));

                this.addPartition(newPartition);
            }
        }

        if (jobsListTag != null) {
            var jobArr = this.jobMap.values().toArray(new JobTicket[0]);

            for (int i = 0; i < jobsListTag.size(); i++) {
                var jobTag = jobsListTag.getCompound(i);
                jobArr[i].fulfilledDayTime = tag.getLong("villagerdynasties:job_time_fulfilled");
            }
        }

        if (tag.hasUUID("villagerdynasties:plot_uuid")) {
            this.uuid = tag.getUUID("villagerdynasties:plot_uuid");
        }

        this.type = PlotType.valueOf(tag.getString("villagerdynasties:plot_type"));
        this.isEnabled = tag.getBoolean("villagerdynasties:plot_enabled");
        this.lastTaxed = tag.getLong("villagerdynasties:last_taxed");
    }

    public long getLastTaxed() {
        return lastTaxed;
    }

    public void setLastTaxed(long lastTaxed) {
        this.lastTaxed = lastTaxed;
        this.getParentZone();
    }

    public static class PlotRecipe {
        private final String name;

        public PlotRecipe(String name) {
            this.name = name;
        }
    }

    public enum Job {
        TRADER,
        WORKER,
        RANCHER,
        NOBLE
    }

    public enum JobType {
        COOK_FOOD
    }

    public static class JobTicket {
        private String id;
        private AbstractDynastyVillager claimedBy;
        private long fulfilledDayTime = -1;

        public JobTicket(String id) {
            this.id = id;
        }

        public boolean isFulfilledForToday(long gameTime) {
            return gameTime % 24_000 < this.fulfilledDayTime;
        }

        public void fulfill(long gameTime) {
            this.claimedBy = null;
            this.fulfilledDayTime = gameTime % 24_000;
        }

        public boolean isClaimed() {
            return this.claimedBy != null;
        }

        public AbstractDynastyVillager getClaimant() {
            return this.claimedBy;
        }

        public void claim(AbstractDynastyVillager claimant) {
            this.claimedBy = claimant;
        }
    }

    public static class Slot {
        protected final RandomSource random = RandomSource.create();
        protected  UUID uuid;
        protected final Plot parentPlot;
        protected AbstractDynastyVillager occupiedBy = null;
        protected Job job;

        public Slot(Plot parent) {
            this(parent, null);
        }

        public Slot(Plot parent, Job job) {
            this.parentPlot = parent;
            this.uuid = Mth.createInsecureUUID(this.random);
            this.job = job;
        }

        public void refreshSlot() {
            if (this.occupiedBy != null && !this.occupiedBy.isAlive()) {
                this.occupiedBy = null;
            }
        }

        public AbstractDynastyVillager getOccupier() {
            return this.occupiedBy;
        }

        public void setOccupier(AbstractDynastyVillager villager) {
            this.occupiedBy = villager;
        }

        public Job getJob() {
            return this.job;
        }

        public Plot getParentPlot() { return this.parentPlot; }

        public UUID getUUID() {return this.uuid; }

        public boolean isOccupiedBy(AbstractDynastyVillager villager) {
            if (this.occupiedBy != null) {
                return this.occupiedBy == villager;
            }

            return false;
        }

        public void save(CompoundTag tag) {
            tag.putUUID("villagerdynasties:slot_uuid", this.uuid);

            if (this.job != null) {
                tag.putString("villagerdynasties:slot_job", this.job.name());
            }
        }

        public void load(CompoundTag tag) {
            if (tag.hasUUID("villagerdynasties:slot_uuid")) {
                this.uuid = tag.getUUID("villagerdynasties:slot_uuid");
            }

            var jobTag = tag.getString("villagerdynasties:slot_job");
            this.job = Job.valueOf(jobTag);
        }
    }

    public enum PlotType implements StringRepresentable {
        RESERVED,
        RESIDENTIAL,
        RANCH,
        BURGAGE,
        MARKET,
        HALL,
        ;

        public static final Codec<PlotType> CODEC = StringRepresentable.fromEnum(PlotType::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.toString();
        }
    }

    static {
        STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Plot::getUUID, BlockPos.STREAM_CODEC, Plot::getStartPos, BlockPos.STREAM_CODEC, Plot::getEndPos, ByteBufCodecs.STRING_UTF8, Plot::getTypeName, Partition.STREAM_CODEC.apply(ByteBufCodecs.list()), Plot::getPartitions, Plot::new);
    }
}
