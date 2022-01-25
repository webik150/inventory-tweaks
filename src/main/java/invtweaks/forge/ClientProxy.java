package invtweaks.forge;

import com.mojang.blaze3d.platform.InputConstants;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksItemTreeLoader;
import invtweaks.InvTweaksObfuscation;
import invtweaks.api.IItemTreeListener;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.network.ITPacketHandler;
import invtweaks.network.packets.ITPacket;
import invtweaks.network.packets.ITPacketClick;
import invtweaks.network.packets.ITPacketLogin;
import invtweaks.network.packets.ITPacketSortComplete;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static invtweaks.InvTweaksConst.PROTOCOL_VERSION;

public class ClientProxy extends CommonProxy {
    public static final KeyMapping KEYBINDING_SORT = new KeyMapping("invtweaks.key.sort", KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "invtweaks.key.category");
    public boolean serverSupportEnabled = false;
    public boolean serverSupportDetected = false;
    private InvTweaks instance;

    @Override
    public void commonSetup(FMLCommonSetupEvent e) {
        super.commonSetup(e);
        MinecraftForge.EVENT_BUS.register(ToolTipEvent.class);
        // Instantiate mod core
        instance = new InvTweaks();
    }

    @Override
    public void clientSetup(FMLClientSetupEvent e) {
        super.clientSetup(e);
        ClientRegistry.registerKeyBinding(KEYBINDING_SORT);
        e.enqueueWork(()->{
                    //Minecraft registrations
        });
    }

    @SubscribeEvent
    public void onRawMouse(InputEvent.RawMouseEvent event){
        //TODO: listen to input
        InvTweaksMod.log.info("Clicked button {} with action {} and modifiers {}", event.getButton(), event.getAction(), event.getModifiers());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(ClientProxy.KEYBINDING_SORT.isDown()){
            InvTweaksMod.log.info("Server Ticking");
        }
        if(event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            if(event.player != null) {
                //handleAutoRefill(event.player);
            }
        }
    }

    @SubscribeEvent
    public void onTick(@NotNull TickEvent.ClientTickEvent tick) {

        if(tick.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if(mc.level != null && mc.player != null) {
                if(mc.screen != null) {
                    instance.onTickInGUI(mc.screen);
                } else {
                    instance.onTickInGame();
                }
            }
        }
    }

    @SubscribeEvent
    public void notifyPickup(PlayerEvent.ItemPickupEvent e) {
        instance.setItemPickupPending(true);
    }

    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent.Open e){
        InvTweaksMod.log.info("Opened container "+ e.getContainer().toString());
        InvTweaksMod.log.info("Its size is: "+ InvTweaks.getContainerMenuRowSize(e.getContainer()));
    }

    @Override
    public void setServerAssistEnabled(boolean enabled) {
        serverSupportEnabled = serverSupportDetected && enabled;
        InvTweaksMod.log.info("Server has support: " + serverSupportDetected + " support enabled: " + serverSupportEnabled);
    }

    @Override
    public void setServerHasInvTweaks(boolean hasInvTweaks) {
        serverSupportDetected = hasInvTweaks;
        serverSupportEnabled = hasInvTweaks && !InvTweaks.getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP).equals(InvTweaksConfig.VALUE_FALSE);
        //InvTweaks.log.info("Server has support: " + hasInvTweaks + " support enabled: " + serverSupportEnabled);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(@NotNull PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getPlayer() instanceof ServerPlayer)
            ITPacketHandler.sendTo((ITPacket) new ITPacketLogin(PROTOCOL_VERSION), (ServerPlayer) e.getPlayer());
    }

    @Override
    public void serverAboutToStart(@NotNull ServerAboutToStartEvent e) {
        super.serverAboutToStart(e);
        InvTweaksMod.log.warn("Joining server "+e.getServer().getServerVersion());
    }

    @Override
    public void slotClick(int windowId, int slot, int data, @NotNull ClickType action, Player player) {
        //int modiferKeys = (shiftHold) ? 1 : 0 /* XXX Placeholder */;
        if(serverSupportEnabled) {
            player.containerMenu.clicked(slot, data, action, player);

            ITPacketHandler.sendToServer(new ITPacketClick(slot, data, action, windowId));
        } else {
            //Minecraft.getInstance().windowClick(windowId, slot, data, action, player); //TODO: uuuggggghhhh
            KeyMapping.click(Minecraft.getInstance().options.keyAttack.getKey());
        }
    }

    @Override
    public void sortComplete() {
        if(serverSupportEnabled) {
            InvTweaksMod.log.info("Sending sort complete packet");
            ITPacketHandler.sendToServer(new ITPacketSortComplete());
        }
    }

    @Override
    public void addOnLoadListener(IItemTreeListener listener) {
        InvTweaksItemTreeLoader.addOnLoadListener(listener);
    }

    @Override
    public boolean removeOnLoadListener(IItemTreeListener listener) {
        return InvTweaksItemTreeLoader.removeOnLoadListener(listener);
    }

    @Override
    public void setSortKeyEnabled(boolean enabled) {
        instance.setSortKeyEnabled(enabled);
    }

    @Override
    public void setTextboxMode(boolean enabled) {
        instance.setTextboxMode(enabled);
    }

    @Override
    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j) {
        return instance.compareItems(i, j);
    }

    @Override
    public void sort(ContainerSection section, SortingMethod method) {
        // TODO: This seems like something useful enough to be a util method somewhere.
        Minecraft mc = Minecraft.getInstance();

        //TODO: what to do with this... change sorting so that it doesn't use container?
        NonNullList<ItemStack> currentContainer = mc.player.containerMenu.getItems();

        if(InvTweaksObfuscation.isGuiContainer(mc.screen)) {
            currentContainer = ((ContainerScreen) mc.screen).getMenu().getItems();
        }

        try {
            //TODO: fix
            //new InvTweaksHandlerSorting(mc, InvTweaks.getConfigManager().getConfig(), section, method, InvTweaksObfuscation.getSpecialChestRowSize(currentContainer)).sort();
        } catch(Exception e) {
            InvTweaks.logInGameErrorStatic("invtweaks.sort.chest.error", e);
            e.printStackTrace();
        }
    }


    @Override
    public void addClientScheduledTask(@NotNull Runnable task) {
        Minecraft.getInstance().submitAsync(task);
    }

    @SubscribeEvent
    public void onConnectionToServer(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        setServerHasInvTweaks(false);
    }
}
