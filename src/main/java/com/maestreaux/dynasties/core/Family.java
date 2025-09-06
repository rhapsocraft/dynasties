package com.maestreaux.dynasties.core;

import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;

import java.util.List;
import java.util.UUID;

public class Family {
    private UUID uuid;
    private List<AbstractDynastyVillager> members;
    private Plot homePlot;
    private AbstractDynastyVillager familyHead;

    public Family(AbstractDynastyVillager ...members) {
        this.uuid = UUID.randomUUID();
        this.members = List.of(members);
        this.familyHead = this.members.getFirst();
    }

    public void setHomePlot(Plot plot) {
        this.homePlot = plot;
    }

    public Plot getHomePlot() {
        return this.homePlot;
    }

    public AbstractDynastyVillager getFamilyHead() {
        return this.familyHead;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
