package invtweaks.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ITPacketSortComplete implements ITPacket {

    public static ITPacketSortComplete decode(FriendlyByteBuf buffer) {
        return new ITPacketSortComplete();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {

    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            //TODO: Wtf do here
            //player.sendContainerToPlayer(player.openContainer);
        }
    }
}
