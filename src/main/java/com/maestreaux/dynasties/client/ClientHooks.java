package com.maestreaux.dynasties.client;

import com.maestreaux.dynasties.client.screen.TradeScreen;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openMerchantScreen(AbstractDynastyVillager villager) {
        Minecraft.getInstance().setScreen(new TradeScreen(villager));
    }
}
