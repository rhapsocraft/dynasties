package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Settlement {
    private final Zone primaryZone;
    protected final RandomSource random = RandomSource.create();
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    private final List<Zone> zones = new ArrayList<>();
    private final List<AbstractDynastyVillager> residents = new ArrayList<>();

    public Settlement(Zone primaryZone) {
        this.primaryZone = primaryZone;
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public void addResident(AbstractDynastyVillager villager) {
        this.residents.add(villager);
    }

    public Zone getPrimaryZone() {
        return this.primaryZone;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Plot getPlotByUUID(UUID plotUUID) {
        for (var zone: this.zones) {
            var plotMatch = zone.getPlotByUUID(plotUUID);

            if (plotMatch != null) {
                return plotMatch;
            }
        }

        return null;
    }

    public Plot getAvailablePlot() {
        for (var zone: this.zones) {
            var availablePlot = zone.getNextAvailablePlot();

            if (availablePlot != null) {
                return availablePlot;
            }
        }

        return null;
    }
}
