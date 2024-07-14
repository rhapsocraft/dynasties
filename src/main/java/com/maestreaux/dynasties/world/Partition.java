package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.init.ModBuildings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class Partition {
    private BlockPos origin = BlockPos.ZERO;
    private int length;
    private int width;
    private PartitionType type;
    private int constructionCursor = 0;
    private Building construction;
    private Plot parentPlot;

    public Partition() {}

    public Partition(int width, int length, PartitionType type) {
        this.length = length;
        this.width = width;
        this.type = type;
    }

    public Partition(int width, int length, PartitionType type, Building building) {
        this(width, length, type);
        this.setBuilding(building);
    }

    public Partition(BlockPos relativeOrigin, int width, int length) {
        this.length = length;
        this.width = width;
        setOrigin(relativeOrigin);
    }

    public Partition(BlockPos relativeOrigin, int width, int length, PartitionType type, Building building) {
        this(width, length, type, building);
        this.setOrigin(relativeOrigin);
    }

    public void setParentPlot(Plot parentPlot) {
        this.parentPlot = parentPlot;
    }

    public List<StructureTemplate.StructureBlockInfo> getBlocks() {
        return this.construction.getTemplate().palettes.get(0).blocks();
    }

    public boolean isConstructionFinished() {
        return this.constructionCursor >= getBlocks().size();
    }

    public int getConstructionCursor() {
        return this.constructionCursor;
    }

    public void incrementConstructionCursor() {
        this.constructionCursor++;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public BlockPos getAbsoluteOrigin() {
        return this.origin.offset(this.parentPlot.getAbsoluteStartPos());
    }

    public int getWidth() {
        return this.width;
    }

    public int getLength() {
        return this.length;
    }

    public void setOrigin(BlockPos newOrigin) {
        this.origin = newOrigin;
    }

    public PartitionType getPartitionType() {
        return this.type;
    }

    public void setBuilding(Building building) {
        this.construction = building;
    }

    public Building getBuilding() {
        return this.construction;
    }

    public void save(CompoundTag compoundTag) {
        compoundTag.put("villagerdynasties:partition_origin", NbtUtils.writeBlockPos(this.origin));
        compoundTag.putInt("villagerdynasties:partition_width", this.width);
        compoundTag.putInt("villagerdynasties:partition_length", this.length);

        compoundTag.putInt("villagerdynasties:construction_cursor", this.constructionCursor);

        var buildingKey = ModBuildings.BUILDINGS_REGISTRY.get().getKey(this.construction);

        if (buildingKey != null) {
            compoundTag.putString("villagerdynasties:building", buildingKey.toString());
        }
    }

    public void load(CompoundTag compoundTag) {
        this.constructionCursor = compoundTag.getInt("villagerdynasties:construction_cursor");
        this.origin = NbtUtils.readBlockPos(compoundTag.getCompound("villagerdynasties:partition_origin"));
        this.width = compoundTag.getInt("villagerdynasties:partition_width");
        this.length = compoundTag.getInt("villagerdynasties:partition_length");

        var buildingKey = new ResourceLocation(compoundTag.getString("villagerdynasties:building"));
        this.construction = ModBuildings.BUILDINGS_REGISTRY.get().getValue(buildingKey);
    }

    public enum PartitionType {
        HOME,
        GARDEN,
    }
}
