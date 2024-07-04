package com.maestreaux.dynasties.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import com.maestreaux.dynasties.network.ZonePacket.*;
import org.lwjgl.system.windows.MSG;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("villagerdynasties", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.messageBuilder(CZonesPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CZonesPacket::encode)
                .decoder(CZonesPacket::new)
                .consumerMainThread(CZonesPacket::handle)
                .add();
    }

    public static void sendToAll(Object msg) {
        // TODO: Optimize networking?
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
