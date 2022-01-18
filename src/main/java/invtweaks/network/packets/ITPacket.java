package invtweaks.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface ITPacket {
    /*
    Some of this code was stolen from https://github.com/sinkillerj/ProjectE/blob/mc1.18.x/src/main/java/moze_intel/projecte/network/packets/IPEPacket.java
     */
    void encode(FriendlyByteBuf buffer);

    void handle(NetworkEvent.Context context);

    static <PACKET extends ITPacket> void handle(final PACKET message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> message.handle(context));
        context.setPacketHandled(true);
    }
}
