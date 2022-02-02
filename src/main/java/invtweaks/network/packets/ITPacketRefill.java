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

    protected int targetSlot;
    protected int refillFromSlot;
    protected int playerId;

    /**
     * Creates a new Refill packet to send to server. Unlike sorting, this uses Inventory slot IDs.
     * That means 0-8 for hotbar, 9-35 for inventory, starting from TOP-LEFT.
     * @param targetSlot The slot we want to fill.
     * @param refillFromSlot The slot we want to take the item from.
     * @param playerId Player that triggered this refill.
     */
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

    /**
     * Handles received packet.
     * @param context
     */
    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            // Spectators don't really have an inventory so we don't handle those. If for any reason player IDs don't match (shouldn't happen), we also don't do anything.
            if(!player.isSpectator() && player.getId() == playerId) {
                var inv = player.getInventory();
                // Check that we have anything to refill from
                if(!inv.getItem(refillFromSlot).isEmpty()){
                    ItemStack replacedItem = ItemStack.EMPTY;
                    // Check whether we're filling an empty slot, or replacing a tool.
                    if(!inv.getItem(targetSlot).isEmpty()){
                        replacedItem = inv.getItem(targetSlot);
                    }

                    // Replace with a copy to avoid duplication (I guess? I've seen people do it lol)
                    inv.setItem(targetSlot, player.containerMenu.getSlot(refillFromSlot).getItem().copy());
                    inv.setItem(refillFromSlot, replacedItem);

                    inv.setChanged();
                }
            }
        }
    }
}
