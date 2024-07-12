package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.core.BlockPos;
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

    public Plot(BlockPos startPos, BlockPos endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public void addEmptySlot() {
        slots.add(new Slot(this));
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public BlockPos getAbsoluteStartPos() {
        return this.startPos.offset(this.parentZone.getCenter());
    }

    public BlockPos getEndPos() {
        return endPos;
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

    public Slot getAvailableSlot() {
        return this.slots.stream().filter(slot -> slot.occupiedBy == null).findFirst().orElse(null);
    }

    public void save(CompoundTag tag) {
        var slotsListTag = new ListTag();

        for(var slot: this.slots) {
            var slotTag = new CompoundTag();
            slot.save(slotTag);

            slotsListTag.add(slotTag);
        }

        tag.put("villagerdynasties:slots", slotsListTag);
        tag.putUUID("villagerdynasties:plot_uuid", this.uuid);
    }

    public void load(CompoundTag tag) {
        var slotsListTag = (ListTag) tag.get("villagerdynasties:slots");

        if (slotsListTag != null) {
            for(int i = 0; i < slotsListTag.size(); i++) {
                var newSlot = new Slot(this);
                newSlot.load(slotsListTag.getCompound(i));

                this.slots.add(newSlot);
            }
        }

        if (tag.hasUUID("villagerdynasties:plot_uuid")) {
            this.uuid = tag.getUUID("villagerdynasties:plot_uuid");
        }
    }

    public static class Partition {
        private BlockPos origin;
        private int length;
        private int width;
        private String type;

        public Partition(BlockPos relativeOrigin, int width, int length, String type) {
            this.origin = relativeOrigin;
            this.length = length;
            this.width = width;
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
