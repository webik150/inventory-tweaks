package invtweaks.network;

import invtweaks.network.packets.ITPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

public class ITPacketHandlerServer extends SimpleChannelInboundHandler<ITPacket> {
    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull ITPacket msg) throws Exception {
        @NotNull final NetHandlerPlayServer handler = (NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        handler.player.server.addScheduledTask(() -> msg.handle(handler));
    }
}
