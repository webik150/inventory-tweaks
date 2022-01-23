package invtweaks.forge;

import invtweaks.api.IItemTreeListener;
import invtweaks.api.InvTweaksAPI;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.integration.ItemListSorter;
import invtweaks.network.ITPacketHandler;
import invtweaks.network.packets.ITPacket;
import invtweaks.network.packets.ITPacketLogin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.jetbrains.annotations.NotNull;

import static invtweaks.InvTweaksConst.PROTOCOL_VERSION;

public class CommonProxy implements InvTweaksAPI {
    protected static MinecraftServer server;
    protected ITPacketHandler packetHandler;

    public void commonSetup(final FMLCommonSetupEvent e) {
        ITPacketHandler.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void postInit(final InterModProcessEvent e) {
        //TODO: should this be Process or Enqueue Event
        ItemListSorter.LinkJEITComparator();
    }

    @SubscribeEvent
    public void serverAboutToStart(@NotNull ServerAboutToStartEvent e) {
        server = e.getServer();
    }

    public void clientSetup(final FMLClientSetupEvent e){}

    @SubscribeEvent
    public void serverStopped(ServerStoppedEvent e) {
        server = null;
    }

    public void setServerAssistEnabled(boolean enabled) {
    }

    public void setServerHasInvTweaks(boolean hasInvTweaks) {
    }

    /* Action values:
     * 0: Standard Click
     * 1: Shift-Click
     * 2: Move item to/from hotbar slot (Depends on current slot and hotbar slot being full or empty)
     * 3: Duplicate item (only while in creative)
     * 4: Drop item
     * 5: Spread items (Drag behavior)
     * 6: Merge all valid items with held item
     */
    @OnlyIn(Dist.CLIENT)
    public void slotClick(int windowId, int slot, int data, ClickType action, Player player) {
    }

    public void sortComplete() {

    }

    @Override
    public void addOnLoadListener(IItemTreeListener listener) {

    }

    @Override
    public boolean removeOnLoadListener(IItemTreeListener listener) {
        return false;
    }

    @Override
    public void setSortKeyEnabled(boolean enabled) {
    }

    @Override
    public void setTextboxMode(boolean enabled) {
    }

    @Override
    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j) {
        return 0;
    }

    @Override
    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j, boolean onlyTreeSort) {
        return 0;
    }

    @Override
    public void sort(ContainerSection section, SortingMethod method) {
    }

    @SuppressWarnings("unused")
    public void addServerScheduledTask(@NotNull Runnable task) {
        server.submitAsync(task);
    }

    public void addClientScheduledTask(Runnable task) {
    }
}
