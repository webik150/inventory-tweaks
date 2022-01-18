package invtweaks.network.packets;

import invtweaks.InvTweaksConst;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class ITPacketLogin implements ITPacket {
    public String protocolVersion = InvTweaksConst.PROTOCOL_VERSION;

    public ITPacketLogin(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public static ITPacketLogin decode(@NotNull FriendlyByteBuf buffer) {

        return new ITPacketLogin(buffer.readUtf());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(protocolVersion);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(protocolVersion == InvTweaksConst.PROTOCOL_VERSION) {
            InvTweaksMod.proxy.setServerHasInvTweaks(true);
        }
    }
}
