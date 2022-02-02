package invtweaks.network.packets;

import invtweaks.container.ContainerUtils;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Handles refilling of hotbar.
 */
public class ITPacketRefill implements ITPacket {
    public int targetSlot;
    public int refillFromSlot;
    public int playerId;

    public ITPacketRefill(int targetSlot, int refillFromSlot, int playerId) {
        this.targetSlot = targetSlot;
        this.refillFromSlot = refillFromSlot;
        this.playerId = playerId;
    }

    public static ITPacketRefill decode(@NotNull FriendlyByteBuf bytes) {
        return new ITPacketRefill(bytes.readInt(), bytes.readInt(), bytes.readInt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(targetSlot);
        buffer.writeInt(refillFromSlot);
        buffer.writeInt(playerId);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            if(!player.isSpectator() && player.getId() == playerId) {
                var inv = player.getInventory();
                if(!inv.getItem(refillFromSlot).isEmpty()){
                    ItemStack replacedItem = ItemStack.EMPTY;
                    if(!inv.getItem(targetSlot).isEmpty()){
                        replacedItem = inv.getItem(targetSlot);
                    }

                    inv.setItem(targetSlot, player.containerMenu.getSlot(refillFromSlot).getItem().copy());
                    inv.setItem(refillFromSlot, replacedItem);

                    inv.setChanged();
                }
            }
        }
    }
}
