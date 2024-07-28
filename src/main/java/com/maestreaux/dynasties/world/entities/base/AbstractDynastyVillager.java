package com.maestreaux.dynasties.world.entities.base;

import com.maestreaux.dynasties.init.ModEntityTypes;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AbstractDynastyVillager extends AgeableMob implements InventoryCarrier {
    protected List<Plot> occupiedPlots = new ArrayList<>();
    protected Zone homeZone;


    protected AbstractDynastyVillager(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected AbstractDynastyVillager(Level pLevel, Zone homeZone) {
        this(ModEntityTypes.DYNASTY_VILLAGER.get(), pLevel);
        this.homeZone = homeZone;
    }

    public Zone getHomeZone() {
        return this.homeZone;
    }

    public void setHomeZone(Zone zone) {
        this.homeZone = zone;
    }

    public List<Plot> getOccupiedPlots() {
        return this.occupiedPlots;
    }

    public void occupyPlot(Plot plot) {
        this.occupiedPlots.add(plot);

        if (plot.getType() == Plot.PlotType.RESIDENTIAL) {
            this.brain.setMemory(ModMemoryTypes.HOME_PLOT.get(), plot);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();

        for (var plot: this.occupiedPlots) {
            plot.clearVillagerFromPlot(this);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public SimpleContainer getInventory() {
        return null;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(compoundTag.hasUUID("villagerdynasties:home_zone")) {
            this.homeZone = Zone.getZoneByUUID((ServerLevel) this.level(), compoundTag.getUUID("villagerdynasties:home_zone"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        if (this.homeZone != null) {
            compoundTag.putUUID("villagerdynasties:home_zone", this.homeZone.getUUID());
        }
    }
}
