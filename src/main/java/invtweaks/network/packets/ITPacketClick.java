package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.jetbrains.annotations.NotNull;

public class ITPacketClick implements ITPacket {
    public int slot;
    public int data;
    public ClickType action;
    public int window;

    @SuppressWarnings("unused")
    public ITPacketClick() {
    }

    public ITPacketClick(int _slot, int _data, ClickType _action, int _window) {
        slot = _slot;
        data = _data;
        action = _action;
        window = _window;
    }

    @Override
    public void readBytes(@NotNull ByteBuf bytes) {
        slot = bytes.readInt();
        data = bytes.readInt();
        action = ClickType.values()[bytes.readInt()];
        window = bytes.readByte();
    }

    @Override
    public void writeBytes(@NotNull ByteBuf bytes) {
        bytes.writeInt(slot);
        bytes.writeInt(data);
        bytes.writeInt(action.ordinal());
        bytes.writeByte(window);
    }

    @Override
    public void handle(INetHandler handler) {
        if(handler instanceof ServerPlayNetHandler) {
            @NotNull ServerPlayNetHandler serverHandler = (ServerPlayNetHandler) handler;
            ServerPlayerEntity player = serverHandler.player;

            if(!player.isSpectator() && player.openContainer.windowId == window) {
                player.openContainer.slotClick(slot, data, action, player);
            }
            // TODO: Might want to set a flag to ignore all packets until next sortcomplete even if client window changes.
        }
    }
}
