package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Plot {
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    private final BlockPos startPos;
    private final BlockPos endPos;
    private Zone parentZone;
    private final List<Slot> slots = new ArrayList<>();
    private final List<Partition> partitions = new ArrayList<>();

    public Plot(BlockPos startPos, BlockPos endPos) {
        var corner1 = new BlockPos(startPos.getX(), startPos.getY(), endPos.getZ());
        var corner2 = new BlockPos(endPos.getX(), startPos.getY(), startPos.getZ());
        List<BlockPos> list = ObjectArrayList.of(startPos, endPos, corner1, corner2);
        list.sort(Plot::mostSouthernEast);

        this.endPos = ((BlockPos) list.toArray()[0]);
        this.startPos = ((BlockPos) list.toArray()[list.size() - 1]);
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

    public BlockPos getStartPos() {
        return this.startPos;
    }

    public BlockPos getAbsoluteStartPos() {
        return this.startPos.offset(this.parentZone.getCenter());
    }

    public BlockPos getEndPos() {
        return this.endPos;
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

    public UUID getUUID() {
        return this.uuid;
    }

    public List<Slot> getSlots() {
        return this.slots;
    }

    public void refreshAllSlots() {
        for(var slot: slots) {
            slot.refreshSlot();
        }
    }

    // TODO: Temporary
    public Partition getPartitionToBuildOn() {
        return this.partitions.stream().filter(part -> part.getPartitionType() == Partition.PartitionType.HOME).findFirst().orElse(null);
    }

    public void addPartition(Partition newPartition) {
        newPartition.setParentPlot(this);
        this.partitions.add(newPartition);
    }

    public List<Partition> getPartitions() {
        return this.partitions;
    }

    public Slot getAvailableSlot() {
        return this.slots.stream().filter(slot -> slot.occupiedBy == null).findFirst().orElse(null);
    }

    public void save(CompoundTag tag) {
        var slotsListTag = new ListTag();
        var partitionsListTag = new ListTag();

        for(var slot: this.slots) {
            var slotTag = new CompoundTag();
            slot.save(slotTag);

            slotsListTag.add(slotTag);
        }

        for(var partition: this.partitions) {
            var partitionTag = new CompoundTag();
            partition.save(partitionTag);

            partitionsListTag.add(partitionTag);
        }

        tag.put("villagerdynasties:slots", slotsListTag);
        tag.put("villagerdynasties:partitions", partitionsListTag);
        tag.putUUID("villagerdynasties:plot_uuid", this.uuid);
    }

    public void load(CompoundTag tag) {
        var slotsListTag = (ListTag) tag.get("villagerdynasties:slots");
        var partitionsListTag = (ListTag) tag.get("villagerdynasties:partitions");

        if (slotsListTag != null) {
            for(int i = 0; i < slotsListTag.size(); i++) {
                var newSlot = new Slot(this);
                newSlot.load(slotsListTag.getCompound(i));

                this.slots.add(newSlot);
            }
        }

        if (partitionsListTag != null) {
            for(int i = 0; i < partitionsListTag.size(); i++) {
                var newPartition = new Partition();
                newPartition.load(partitionsListTag.getCompound(i));

                this.partitions.add(newPartition);
            }
        }


        if (tag.hasUUID("villagerdynasties:plot_uuid")) {
            this.uuid = tag.getUUID("villagerdynasties:plot_uuid");
        }
    }

    public static class PlotRecipe {
        private final String name;

        public PlotRecipe(String name) {
            this.name = name;
        }
    }

    public static class Slot {
        private final Plot parentPlot;
        private AbstractDynastyVillager occupiedBy = null;

        public Slot(Plot parent) {
            this.parentPlot = parent;
        }

        public void refreshSlot() {
            if (this.occupiedBy != null && !this.occupiedBy.isAlive()) {
                this.occupiedBy = null;
            }
        }

        public void setOccupier(AbstractDynastyVillager villager) {
            this.occupiedBy = villager;
            villager.setHomePlot(this.parentPlot);
        }

        public void save(CompoundTag tag) {
            if (this.occupiedBy != null) {
                tag.putUUID("villagerdynasties:slot_occupier",this.occupiedBy.getUUID());
            }
        }

        public void load(CompoundTag tag) {
            if (tag.hasUUID("villagerdynasties:slot_occupier")) {
                var level = (ServerLevel) parentPlot.getParentZone().level();
                this.occupiedBy = (AbstractDynastyVillager) level.getEntity(tag.getUUID("villagerdynasties:slot_occupier"));

                if (occupiedBy !=null) {
                    this.occupiedBy.setHomePlot(this.parentPlot);
                }
            }
        }
    }
}
