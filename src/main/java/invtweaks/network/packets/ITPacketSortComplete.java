package invtweaks.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.jetbrains.annotations.NotNull;

public class ITPacketSortComplete implements ITPacket {
    @Override
    public void readBytes(ByteBuf bytes) {

    }

    @Override
    public void writeBytes(ByteBuf bytes) {

    }

    @Override
    public void handle(INetHandler handler) {
        if(handler instanceof ServerPlayNetHandler) {
            @NotNull ServerPlayNetHandler serverHandler = (ServerPlayNetHandler) handler;
            ServerPlayerEntity player = serverHandler.player;

            player.sendContainerToPlayer(player.openContainer);
        }
    }
}
