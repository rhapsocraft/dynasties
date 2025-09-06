package com.maestreaux.dynasties.core.utils;

import com.maestreaux.dynasties.init.ModBuildings;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlotUtils {
    public static boolean overlaps(BlockPos startPos1, BlockPos endPos1, BlockPos startPos2, BlockPos endPos2) {
        var xOverlaps = startPos1.getX() > endPos2.getX() && endPos1.getX() < startPos2.getX();
        var zOverlaps = startPos1.getZ() > endPos2.getZ() && endPos1.getZ() < startPos2.getZ();

        return xOverlaps && zOverlaps;
    }

    public static boolean isValidSize(BlockPos startPos, BlockPos endPos) {
        var isValidWidth = Math.abs(endPos.getX() - startPos.getX()) >= 5;
        var isValidLength = Math.abs(endPos.getZ() - startPos.getZ()) >= 5;

        return isValidLength && isValidWidth;
    }

    public static boolean isValidPosition(BlockPos startPos, BlockPos endPos, Zone parentZone) {
        return parentZone.getPlots().stream().noneMatch(plot -> overlaps(startPos, endPos, plot.getStartPos(), plot.getEndPos()));
    }

    public static boolean isValidPlot(BlockPos startPos, BlockPos endPos, Zone parentZone) {
        var plot = new Plot(startPos.subtract(parentZone.getCenter()), endPos.subtract(parentZone.getCenter()));
        plot.setParentZone(parentZone);

        var isInsideZone = parentZone.getBoundingBox().contains(startPos.getX(), startPos.getY(), startPos.getZ()) && parentZone.getBoundingBox().contains(endPos.getX(), endPos.getY(), endPos.getZ());

        return isValidSize(plot.getStartPos(), plot.getEndPos()) && isValidPosition(plot.getStartPos(), plot.getEndPos(), plot.getParentZone()) && isInsideZone;
    }

    public static void debugSetPartitions(Plot plot) {
        var houseToBuild = Math.random() >= 0.5 ? ModBuildings.BASIC_HOUSE : ModBuildings.BASIC_HOUSE_2;

        var partitions = ObjectArrayList.of(
                new Partition(6, 6, Partition.PartitionType.HOME, houseToBuild.get(), 5),
                new Partition(2, 2, Partition.PartitionType.GARDEN, ModBuildings.SMALL_GARDEN.get(), 1),
                new Partition(2, 2, Partition.PartitionType.GARDEN, ModBuildings.SMALL_GARDEN.get(), 1),
                new Partition(2, 2, Partition.PartitionType.GARDEN, ModBuildings.SMALL_GARDEN.get(), 1),
                new Partition(3, 3, Partition.PartitionType.GARDEN, ModBuildings.MEDIUM_GARDEN.get(), 2),
                new Partition(3, 3, Partition.PartitionType.GARDEN, ModBuildings.MEDIUM_GARDEN.get(), 2),
                new Partition(3, 3, Partition.PartitionType.GARDEN, ModBuildings.MEDIUM_GARDEN.get(), 3),
                new Partition(3, 5, Partition.PartitionType.GARDEN, ModBuildings.LONG_GARDEN.get(), 4),
                new Partition(3, 5, Partition.PartitionType.GARDEN, ModBuildings.LONG_GARDEN.get(), 3),
                new Partition(3, 5, Partition.PartitionType.GARDEN, ModBuildings.LONG_GARDEN.get(), 3),
                new Partition(2, 5, Partition.PartitionType.GARDEN, ModBuildings.NARROW_GARDEN.get(), 2),
                new Partition(2, 5, Partition.PartitionType.GARDEN, ModBuildings.NARROW_GARDEN.get(), 2),
                new Partition(2, 5, Partition.PartitionType.GARDEN, ModBuildings.NARROW_GARDEN.get(), 2)
        );

        partitionPlot(plot, partitions);
    }

    private static boolean fitsWithin(Rectangle largerRectangle, Rectangle other) {
        return other.width <= largerRectangle.width && other.height <= largerRectangle.height;
    }

    private static List<Rectangle> splitRectangle(Rectangle largerRectangle, Rectangle placedRectangle) {
        List<Rectangle> newFreeRects = new ArrayList<>();
        boolean verticalSplit = largerRectangle.width - placedRectangle.width > largerRectangle.height - placedRectangle.height;

        if (!(placedRectangle.width == largerRectangle.width && placedRectangle.height == largerRectangle.height)) {
            if (verticalSplit) {
                if (placedRectangle.width < largerRectangle.width) {
                    newFreeRects.add(new Rectangle(largerRectangle.x + placedRectangle.width, largerRectangle.y, largerRectangle.width - placedRectangle.width, largerRectangle.height));
                }

                if (placedRectangle.height < largerRectangle.height) {
                    newFreeRects.add(new Rectangle(largerRectangle.x, largerRectangle.y + placedRectangle.height, placedRectangle.width, largerRectangle.height - placedRectangle.height));
                }
            } else {
                if (placedRectangle.height < largerRectangle.height) {
                    newFreeRects.add(new Rectangle(largerRectangle.x, largerRectangle.y + placedRectangle.height, largerRectangle.width, largerRectangle.height - placedRectangle.height));
                }

                if (placedRectangle.width < largerRectangle.width) {
                    newFreeRects.add(new Rectangle(largerRectangle.x + placedRectangle.width, largerRectangle.y, largerRectangle.width - placedRectangle.width, placedRectangle.height));
                }
            }
        }

        return newFreeRects;
    }

    public static Rectangle fitRectangle(Rectangle largerRectangle, Rectangle smallerRectangle) {

        if (fitsWithin(largerRectangle, smallerRectangle)) {
            return smallerRectangle;
        }

        var rotatedRectangle = new Rectangle((int) smallerRectangle.getHeight(), (int) smallerRectangle.getWidth());

        if (fitsWithin(largerRectangle, rotatedRectangle)) {
            return rotatedRectangle;
        }

        return null;
    }

    public static void partitionPlot(Plot plot, List<Partition> partitions) {
        var largeRect = new Rectangle(Math.abs(plot.getEndPos().getX() - plot.getStartPos().getX()) + 1, Math.abs(plot.getEndPos().getZ() - plot.getStartPos().getZ()) + 1);
        var partitionComparator = Comparator.comparing(Partition::getArea).thenComparing(Partition::getWeight).reversed();
        var _partitions = partitions.stream().sorted(partitionComparator).toList();

        var isPlotWide = Mth.abs(plot.getEndPos().getX() - plot.getStartPos().getX()) > Mth.abs(plot.getEndPos().getZ() - plot.getStartPos().getZ());

        List<Partition> packed = new ArrayList<>();
        List<Rectangle> freeRects = new ArrayList<>();
        freeRects.add(largeRect);

        for (Partition partition : _partitions) {
            // Rotate if plot is wide
            var newRect = isPlotWide ? new Rectangle(partition.getLength(), partition.getWidth()) : new Rectangle(partition.getWidth(), partition.getLength());

            if (isPlotWide) {
                partition.setRotation(Rotation.COUNTERCLOCKWISE_90);
            }

            for (int i = 0; i < freeRects.size(); i++) {
                Rectangle freeRect = freeRects.get(i);

                var correctedRectangle = fitRectangle(freeRect, newRect);

                if (correctedRectangle != null) {
                    var isRotated = correctedRectangle.height != newRect.height && correctedRectangle.width != newRect.width;
                    correctedRectangle.x = freeRect.x;
                    correctedRectangle.y = freeRect.y;

                    partition.setOrigin(new BlockPos(correctedRectangle.x, 0, correctedRectangle.y));
                    partition.setWidth(correctedRectangle.width);
                    partition.setLength(correctedRectangle.height);

                    if (isRotated) {
                        partition.setRotation(partition.getRotation().getRotated(Rotation.COUNTERCLOCKWISE_90));
                    }

                    packed.add(partition);

                    freeRects.remove(i);
                    freeRects.addAll(splitRectangle(freeRect, correctedRectangle));

                    break;
                }
            }
        }

        for (var part : packed) {
            plot.addPartition(part);
        }
    }
}
