package com.maestreaux.dynasties.client.screen;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TradeScreen extends Screen {
    private static final Component TITLE = Component.translatable( "gui." + DynastiesMod.MODID + ".trade_screen");
    private static final ResourceLocation TEXTURE = new ResourceLocation(DynastiesMod.MODID, "textures/gui/trading_screen.png");

    private final AbstractDynastyVillager trader;
    private final int imageWidth, imageHeight;
    private int leftPos, topPos;

    public TradeScreen(AbstractDynastyVillager trader) {
        super(TITLE);

        this.trader = trader;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if (this.minecraft == null) return;

        var offers = this.trader.asMarketAgent().getActiveOffers().values();

        for (int i = 0; i < offers.size(); i++) {
            var offset = i * 20;


        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 8, 0x404040, false);

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
