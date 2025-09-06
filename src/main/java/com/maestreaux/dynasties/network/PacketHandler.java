package com.maestreaux.dynasties.network;

import com.maestreaux.dynasties.network.message.CAddPlot;
import com.maestreaux.dynasties.network.message.CAddZone;
import com.maestreaux.dynasties.network.message.CZonesList;
import com.maestreaux.dynasties.network.message.SBuyFromTrader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;


public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath("villagerdynasties", "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();


    public static void register() {
        INSTANCE.messageBuilder(CZonesList.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .codec(CZonesList.STREAM_CODEC)
                .consumerMainThread(CZonesList::handle)
                .add();

        INSTANCE.messageBuilder(CAddZone.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .codec(CAddZone.STREAM_CODEC)
                .consumerMainThread(CAddZone::handle)
                .add();

        INSTANCE.messageBuilder(CAddPlot.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .codec(CAddPlot.STREAM_CODEC)
                .consumerMainThread(CAddPlot::handle)
                .add();

        INSTANCE.messageBuilder(SBuyFromTrader.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .codec(SBuyFromTrader.STREAM_CODEC)
                .consumerMainThread(SBuyFromTrader::handle)
                .add();
    }

    public static <T> void sendToAll(T message) {
        INSTANCE.send(message, PacketDistributor.ALL.noArg());
    }

    public static <T> void sendToPlayer(T message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(msg, PacketDistributor.SERVER.noArg());
    }
}
