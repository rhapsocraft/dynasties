package com.maestreaux.dynasties.client.screen;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.SBuyFromTrader;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TradeScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui." + DynastiesMod.MODID + ".trade_screen");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "textures/gui/trading_screen.png");

    private final AbstractDynastyVillager trader;
    private final int imageWidth, imageHeight;
    private int leftPos, topPos;
    private final List<Button> buttons = new ArrayList<>();

    public TradeScreen(AbstractDynastyVillager trader) {
        super(TITLE);

        this.trader = trader;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private void buyItem(int offerIndex, int quantity) {
        var offers = this.trader.getTradeOffers();

        if(offerIndex < offers.size()) {
            var offer = offers.get(offerIndex);

            if (offer != null && offer.getStock() > 0) {
                var packet = new SBuyFromTrader(this.trader.getUUID(), offer.getItemOffered(), quantity);
                PacketHandler.sendToServer(packet);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        if (this.minecraft == null) return;

        for (int i = 0; i < 5; i++) {
            var offset = i * 18;
            int finalI = i;

            var button1 = new Button.Builder(Component.literal("1"), (button) -> buyItem(finalI, 1)).bounds(this.leftPos + 120, this.topPos + 17 + offset, 16, 16).build();
            button1.visible = false;

            var button2 = new Button.Builder(Component.literal("8"), (button) -> buyItem(finalI, 8)).bounds(this.leftPos + 137, this.topPos + 17 + offset, 16, 16).build();
            button2.visible = false;

            var button3 = new Button.Builder(Component.literal("16"), (button) -> buyItem(finalI, 16)).bounds(this.leftPos + 154, this.topPos + 17 + offset, 16, 16).build();
            button3.visible = false;

            this.buttons.addAll(List.of(button1, button2, button3));
            addRenderableWidget(button1);
            addRenderableWidget(button2);
            addRenderableWidget(button3);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.blit(RenderType::guiTextured, TEXTURE, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, 512, 256);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 8, 0x404040, false);

        var offers = this.trader.getTradeOffers();


        int offersCount = offers.size();
        int visibleButtonCount = Math.min(offersCount, 5);

        this.buttons.forEach(button -> button.visible = false);

        for (int i = 0; i < visibleButtonCount; i++) {
            this.buttons.get(i * 3).visible = true;
            this.buttons.get(i * 3 + 1).visible = true;
            this.buttons.get(i * 3 + 2).visible = true;
        }

        for (int i = 0; i < offers.size(); i++) {
            if (i < 5) {
                var offset = i * 18;
                var offer = offers.get(i);
                var itemStackOffered = offer.getItemOffered();
                var itemOffered = itemStackOffered.getItem();

                pGuiGraphics.renderFakeItem(offer.getItemOffered(), this.leftPos + 8, this.topPos + 17 + offset);
                pGuiGraphics.drawString(this.font, itemOffered.getName(itemStackOffered), this.leftPos + 32, this.topPos + 22 + offset, 0x404040, false);
                pGuiGraphics.drawString(this.font, String.valueOf(offer.getPrice()), this.leftPos + 75, this.topPos + 22 + offset, 0x404040, false);
                pGuiGraphics.drawString(this.font, String.valueOf(offer.getStock()), this.leftPos + 100, this.topPos + 22 + offset, 0x404040, false);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
