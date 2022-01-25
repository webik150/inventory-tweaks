package invtweaks.network.packets;

import invtweaks.forge.InvTweaksMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ITPacketRefill implements ITPacket {
    public int slot;
    public String id;
    public int amount;


    public ITPacketRefill(int slot, String id, int amount) {
        this.slot = slot;
        this.id = id;
        this.amount = amount;
    }

    public static ITPacketRefill decode(@NotNull FriendlyByteBuf bytes) {
        return new ITPacketRefill(bytes.readInt(), bytes.readUtf(), bytes.readInt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeUtf(id);
        buffer.writeInt(amount);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayer player = context.getSender();
            InvTweaksMod.log.info("Adding items to player {}",player.getDisplayName());
            if(!player.isSpectator()) {
                //TODO: Changed to clicked from slotClick. Maybe wont work
                player.inventoryMenu.slots.get(slot).set(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)), amount));
            }
            // TODO: Might want to set a flag to ignore all packets until next sortcomplete even if client window changes.
        }
    }
}
