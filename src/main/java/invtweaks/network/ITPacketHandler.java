package invtweaks.network;

import invtweaks.InvTweaksConst;
import invtweaks.forge.InvTweaksMod;
import invtweaks.network.packets.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

import static invtweaks.InvTweaksConst.PROTOCOL_VERSION;

/*
    Some of this code was stolen from https://github.com/sinkillerj/ProjectE/
     */
public final class ITPacketHandler {

    public static final SimpleChannel invtweaksChannel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(InvTweaksMod.MOD_ID, InvTweaksConst.INVTWEAKS_CHANNEL))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int index;

    public static void register() {
        //Client to server messages
        registerClientToServer(ITPacketClick.class, ITPacketClick::decode);
        registerClientToServer(ITPacketSortComplete.class, ITPacketSortComplete::decode);
        registerClientToServer(ITPacketAddStuff.class, ITPacketAddStuff::decode);

        //Server to client messages
        registerServerToClient(ITPacketLogin.class, ITPacketLogin::decode);

    }

    private static <MSG extends ITPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG extends ITPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <MSG extends ITPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
        invtweaksChannel.registerMessage(index++, type, ITPacket::encode, decoder, ITPacket::handle, Optional.of(networkDirection));
    }

    public static <MSG extends ITPacket> void sendTo(MSG msg, ServerPlayer player) {
        invtweaksChannel.sendTo((ITPacket) new ITPacketLogin(PROTOCOL_VERSION),
                player.connection.getConnection(),
                NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG extends ITPacket> void sendToServer(MSG msg) {
        invtweaksChannel.sendToServer(msg);
    }


}
