package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.init.ModBuildings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Partition {
    public static StreamCodec<RegistryFriendlyByteBuf, Partition> STREAM_CODEC;

    private BlockPos origin = BlockPos.ZERO;
    private int length;
    private int width;
    private int weight;
    private PartitionType type;
    private int constructionCursor = 0;
    private Building construction;
    private Plot parentPlot;
    private List<Predicate<Partition>> placementPredicates;
    private Rotation rotation = Rotation.NONE;

    public Partition() {}

    public Partition(int width, int length, PartitionType type, int weight) {
        this.length = length;
        this.width = width;
        this.type = type;
    }

    public Partition(int width, int length, PartitionType type, Building building, int weight) {
        this(width, length, type, weight);
        this.setBuilding(building);
    }

    public Partition(int width, int length, PartitionType type, Building building, List<Predicate<Partition>> predicates) {
        this(width, length, type, building, 1);
        this.placementPredicates = predicates;
    }

    public Partition(BlockPos relativeOrigin, int width, int length) {
        this.length = length;
        this.width = width;
        this.setOrigin(relativeOrigin);
    }

    public Partition(BlockPos relativeOrigin, int width, int length, PartitionType type, Building building) {
        this(width, length, type, building, 0);
        this.setOrigin(relativeOrigin);
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return this.rotation;
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

    public BlockPos getCenter() {
        return new BlockPos(Mth.ceil((float) (this.origin.getX() + this.width)/2), this.origin.above().getY(), Mth.ceil((float) (this.origin.getZ() + this.length)/2));
    }

    public int getWeight() {
        return this.weight;
    }

    public BlockPos getAbsoluteCenter() {
        return this.parentPlot != null ? this.getCenter().offset(this.parentPlot.getAbsoluteStartPos()) : null;
    }

    public int getConstructionCursor() {
        return this.constructionCursor;
    }

    public void incrementConstructionCursor() {
        this.constructionCursor++;

        // Set dirty
        Zone parentZone = this.parentPlot.getParentZone();
        Level level = parentZone.level();

        if (level != null && !level.isClientSide()) {
            parentZone.save((ServerLevel) level);
        }
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

    public void setWidth(int newWidth) {
        this.width = newWidth;
    }

    public void setLength(int newLength) {
        this.length = newLength;
    }

    public int getLength() {
        return this.length;
    }

    public int getArea() {return this.length * this.width;}

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
        compoundTag.putString("villagerdynasties:partition_type", this.type.name());

        var buildingKey = ModBuildings.BUILDINGS_REGISTRY.get().getKey(this.construction);

        if (buildingKey != null) {
            compoundTag.putString("villagerdynasties:building", buildingKey.toString());
        }
    }

    public void load(CompoundTag compoundTag) {
        this.constructionCursor = compoundTag.getInt("villagerdynasties:construction_cursor");
        this.origin = NbtUtils.readBlockPos(compoundTag, "villagerdynasties:partition_origin").orElse(null);
        this.width = compoundTag.getInt("villagerdynasties:partition_width");
        this.length = compoundTag.getInt("villagerdynasties:partition_length");
        this.type = PartitionType.valueOf(compoundTag.getString("villagerdynasties:partition_type"));

        var buildingKey = ResourceLocation.parse(compoundTag.getString("villagerdynasties:building"));
        this.construction = ModBuildings.BUILDINGS_REGISTRY.get().getValue(buildingKey);
    }

    public enum PartitionType {
        HOME,
        GARDEN,
    }

    static {
        STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, Partition::getOrigin, ByteBufCodecs.INT, Partition::getWidth, ByteBufCodecs.INT, Partition::getLength, Partition::new);
    }
}
