package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModBlocks;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.blocks.Tent;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Iterator;
import java.util.List;

public class AvailableTentsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;


    protected void doTick(ServerLevel level, E entity) {
        List<Pair<BlockPos, BlockState>> blocks = new ObjectArrayList();

        var zone = entity.getHomeZone();

        if (zone != null) {
            var zoneBbox = zone.getBoundingBox();
            var minBox = new BlockPos((int) zoneBbox.minX, (int) zoneBbox.minY, (int) zoneBbox.minZ);
            var maxBox = new BlockPos((int) zoneBbox.maxX, (int) zoneBbox.maxY, (int) zoneBbox.maxZ);

            Iterator var4 = BlockPos.betweenClosed(minBox, maxBox).iterator();

            while(var4.hasNext()) {
                BlockPos pos = (BlockPos)var4.next();
                BlockState state = level.getBlockState(pos);

                if (state.is(ModBlocks.TENT.get())) {
                    blocks.add(Pair.of(pos.immutable(), state));
                }
            }
        } else {
            SquareRadius radius = new SquareRadius(6.0F, 6.0F);

            for(BlockPos pos : BlockPos.betweenClosed(entity.blockPosition().subtract(radius.toVec3i()), entity.blockPosition().offset(radius.toVec3i()))) {
                BlockState state = level.getBlockState(pos);
                if (state.is(ModBlocks.TENT.get()) && !state.getValue(BedBlock.OCCUPIED)) {
                    blocks.add(Pair.of(pos.immutable(), state));
                }
            }
        }

        if (blocks.isEmpty()) {
            BrainUtil.clearMemory(entity, ModMemoryTypes.AVAILABLE_TENT.get());
        } else {
            BrainUtil.setMemory(entity, ModMemoryTypes.AVAILABLE_TENT.get(), blocks.get(0).getFirst());
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.AVAILABLE_TENTS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(new MemoryModuleType[]{ModMemoryTypes.AVAILABLE_TENT.get()});
    }
}
