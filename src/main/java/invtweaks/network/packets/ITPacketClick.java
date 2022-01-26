package invtweaks.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class ITPacketClick implements ITPacket {
    public int slot;
    public int data;
    public ClickType action;
    public int window;

    public ITPacketClick(int _slot, int _data, ClickType _action, int _window) {
        slot = _slot;
        data = _data;
        action = _action;
        window = _window;
    }

    public static ITPacketClick decode(@NotNull FriendlyByteBuf bytes) {
        return new ITPacketClick(bytes.readInt(), bytes.readInt(), ClickType.values()[bytes.readInt()], bytes.readByte());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeInt(data);
        buffer.writeInt(action.ordinal());
        buffer.writeByte(window);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            if(!player.isSpectator() && player.containerMenu.containerId == window) {
                //TODO: Changed to clicked from slotClick. Maybe wont work
                player.containerMenu.clicked(slot, data, action, player);
            }
            // TODO: Might want to set a flag to ignore all packets until next sortcomplete even if client window changes.
        }
    }
}
