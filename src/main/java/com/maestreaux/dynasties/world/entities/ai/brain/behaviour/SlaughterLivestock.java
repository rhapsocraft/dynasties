package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class SlaughterLivestock<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Animal targetEntity;

    // TEMPORARY CONSTRAINT
    private static int MAX_LIVESTOCK = 4;
    private final Predicate<LivingEntity> shouldSlaughter = (animal) ->
    {
        Animal _animal = (Animal) animal;
        return _animal.getAge() >= 0 && !_animal.isInLove() && _animal.isAlive();
    };

    private List<ItemStack> customLootDrop(Animal animal, E entity) {
        Optional<ResourceKey<LootTable>> optional = animal.getLootTable();
        ServerLevel serverLevel = (ServerLevel) animal.level();

        if (optional.isPresent()) {
            LootTable loottable = Objects.requireNonNull(serverLevel.getServer()).reloadableRegistries().getLootTable(optional.get());
            LootParams.Builder lootparams$builder = (new LootParams.Builder(serverLevel)).withParameter(LootContextParams.THIS_ENTITY, animal).withParameter(LootContextParams.ORIGIN, animal.position()).withParameter(LootContextParams.DAMAGE_SOURCE, animal.damageSources().mobAttack(entity)).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, entity).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, entity);

            LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
            return loottable.getRandomItems(lootparams, animal.getLootTableSeed());
        }

        return ObjectArrayList.of();
    }

    protected void start(E entity) {
        var animalsToSlaughter = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get()).stream().filter(shouldSlaughter).toList();

        if (this.targetEntity != null && shouldSlaughter.test(this.targetEntity)) {
            if (AIUtils.isCloseEnoughToTarget(entity, this.targetEntity.getOnPos(), 2)) {
                this.targetEntity.kill((ServerLevel) this.targetEntity.level());
                this.targetEntity = null;
                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
                BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);

                this.cooldownFor((e) -> 10);
            } else {
                BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetEntity, 0.6F, 2));
                BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(this.targetEntity, true));
            }
        } else if (!animalsToSlaughter.isEmpty()) {
            Animal animal = (Animal) animalsToSlaughter.getFirst();

            if (animal != null) {
                this.targetEntity = animal;
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var livestock = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get());

        if (livestock != null) {
            var adults = livestock.stream().filter(shouldSlaughter).toList();
            return !adults.isEmpty() && adults.size() > MAX_LIVESTOCK;
        } else {
            return false;
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_LIVESTOCK.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
