package invtweaks.network.packets;

import invtweaks.container.ContainerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class ITPacketMoveStack implements ITPacket {


    private final int containerId;
    private final int sourceSlotId;
    private final int targetSlotIdMax;
    private final int targetSlotIdMin;
    private final boolean reverse;

    public ITPacketMoveStack(int containerId, int sourceSlotId, int targetSlotIdMax, int targetSlotIdMin, boolean reverse) {
        this.containerId = containerId;
        this.sourceSlotId = sourceSlotId;
        this.targetSlotIdMin = targetSlotIdMin;
        this.targetSlotIdMax = targetSlotIdMax;
        this.reverse = reverse;
    }

    public static ITPacketMoveStack decode(@NotNull FriendlyByteBuf bytes) {
        return new ITPacketMoveStack(bytes.readInt(), bytes.readInt(), bytes.readInt(), bytes.readInt(), bytes.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(containerId);
        buffer.writeInt(sourceSlotId);
        buffer.writeInt(targetSlotIdMin);
        buffer.writeInt(targetSlotIdMax);
        buffer.writeBoolean(reverse);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            if(!player.isSpectator() && player.containerMenu.containerId == containerId) {
                ContainerUtils.moveItemStackTo(player.containerMenu, player.containerMenu.getSlot(sourceSlotId).getItem(), targetSlotIdMin, targetSlotIdMax, reverse);
            }
        }
    }
}
