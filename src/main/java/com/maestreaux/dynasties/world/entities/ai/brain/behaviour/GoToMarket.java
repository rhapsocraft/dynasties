package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.core.utils.MealUtils;
import com.maestreaux.dynasties.core.utils.TradeUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class GoToMarket<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Plot marketPlot;
    private BlockPos targetPos;

    private void doTrade(AbstractDynastyVillager entity, List<AbstractDynastyVillager> otherTraders) {
        var agent = entity.asMarketAgent();
        var money = agent.getMoney();

        var topOfferPair = TradeUtils.getDesiredOffers(entity, otherTraders).stream().filter(offer -> offer.getFirst().getPrice() <= money && offer.getSecond() > 0.0F).findFirst().orElse(null);

        if (topOfferPair != null && topOfferPair.getSecond() > 0.0F) {
            var topOffer = topOfferPair.getFirst();

            var itemSold = agent.buyOffer(topOffer, 1);

            if (itemSold != null) {
                agent.setMoney(money - topOffer.getPrice());
                entity.getInventory().addItem(itemSold);
            }
        }
    }

    protected void start(E entity) {
        if (this.targetPos != null) {
            if (AIUtils.isCloseEnoughToTarget(entity, this.targetPos, 2)) {
                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
                var otherTraders = entity.getHomeZone().getPlots().stream().flatMap(plot -> plot.getOccupiedSlots().stream().map(Plot.Slot::getOccupier)).toList();

                var canTrade = TradeUtils.getDesiredOffers(entity, otherTraders).stream().anyMatch(offer -> offer.getSecond() > 0.0F);

                if (canTrade) {
                    doTrade(entity, otherTraders);

                    entity.updateDebugData(TradeUtils.getDesiredOffers(entity, otherTraders).stream()
                            .map(offer -> offer.getFirst().getItemOffered().getItem().getName().getString() + ":" + offer.getSecond()).toList());
                }
            } else {
                BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, 0.6F, 1));
            }
        } else {
            if (this.marketPlot != null) {
                var startPos = this.marketPlot.getAbsoluteStartPos();
                var endPos = this.marketPlot.getAbsoluteEndPos();
                this.targetPos = new BlockPos((startPos.getX() + endPos.getX()) / 2, startPos.getY(), (startPos.getZ() + endPos.getZ()) / 2);
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var plots = BrainUtil.getMemory(entity, ModMemoryTypes.AVAILABLE_PLOTS.get());

        if (plots != null) {
            this.marketPlot = plots.stream().filter(plot -> plot.getType() == Plot.PlotType.MARKET).findFirst().orElse(null);
        }

        return this.marketPlot != null;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}

