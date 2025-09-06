package com.maestreaux.dynasties.commands;

import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ResetValuationsCommand {
    public ResetValuationsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("resetvaluations").requires((sourceStack) -> sourceStack.hasPermission(2)).executes((command) -> resetValuations(command.getSource())));
    }

    private int resetValuations(CommandSourceStack sourceStack) {
        var player = sourceStack.getPlayer();

        if (player != null) {
            player.serverLevel().getEntities().getAll().forEach(entity -> {
                if (entity instanceof AbstractDynastyVillager villager) {
                    var marketAgent = villager.getSimEntity().asMarketAgent();
                    marketAgent.resetValuations();
                    marketAgent.setMoney(300);
                }
            });
        }

        sourceStack.sendSuccess(() -> Component.literal("Successfully reset all villager valuations"), true);
        return 1;
    }
}
