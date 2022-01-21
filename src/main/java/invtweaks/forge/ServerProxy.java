package invtweaks.forge;

import invtweaks.InvTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class ServerProxy extends CommonProxy {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(ClientProxy.KEYBINDING_SORT.isDown()){
            InvTweaksMod.log.info("Server Ticking");
        }
        if(event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            if(event.player != null) {
                handleAutoRefill(event.player);
            }
        }
    }

    @Override
    public void serverAboutToStart(@NotNull ServerAboutToStartEvent e) {
        super.serverAboutToStart(e);
        InvTweaksMod.log.warn("Joining server "+e.getServer().getServerVersion());
    }

    @NotNull
    private ItemStack storedStack = ItemStack.EMPTY;
    @Nullable
    private String storedStackId = null;
    private CompoundTag storedTag = null;

    private void handleAutoRefill(Player player) {
        @NotNull ItemStack currentStack = player.getMainHandItem();
        @NotNull ItemStack offhandStack = player.getOffhandItem();

        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
        @Nullable String currentStackId = (currentStack.isEmpty()) ? null : currentStack.getItem().getRegistryName().toString();

        CompoundTag currentStackDamage = (currentStack.isEmpty()) ? null : currentStack.getTag();

        if(!ItemStack.isSame(currentStack, storedStack) && storedStackId != null) {
            if(!storedStack.isEmpty() && !ItemStack.isSameItemSameTags(offhandStack, storedStack)) { // Checks not switched to offhand
                if(currentStack.isEmpty() || (currentStack.getItem() == Items.BOWL && Objects.equals(storedStackId, "minecraft:mushroom_stew"))) { // TODO: This should be more expandable on 'equivalent' items (API?) and allowed GUIs

                    var inv = player.getInventory();
                    for (int i = 0; i < 12; i++) {
                        inv.items.set(i, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:torch")),8));
                    }
                    /*try {
                        log.info("{} - {}", storedStackId, storedTag);
                        cfgManager.getAutoRefillHandler().autoRefilMainHand(storedStackId, storedTag);
                    } catch(Exception e) {
                        logInGameError("invtweaks.sort.autorefill.error", e);
                    }*/
                } else {
                    // Item
                    int itemMaxDamage = currentStack.getMaxDamage();
                    int autoRefillThreshhold = 1;
                    if(canToolBeReplaced(currentStack.getDamageValue(), itemMaxDamage, autoRefillThreshhold)) {
                        // Trigger auto-refill before the tool breaks
                        /*try {
                            cfgManager.getAutoRefillHandler().autoRefilMainHand(storedStackId, storedTag);
                        } catch(Exception e) {
                            logInGameError("invtweaks.sort.autorefill.error", e);
                        }*/
                    }
                }
            }
        }

        // Copy some info about current selected stack for auto-refill
        storedStack = currentStack.copy();
        storedStackId = currentStackId;
        storedTag = currentStackDamage;

    }

    private boolean canToolBeReplaced(int currentStackDamage, int itemMaxDamage, int autoRefillThreshhold) {
        return itemMaxDamage != 0 && itemMaxDamage - currentStackDamage < autoRefillThreshhold && itemMaxDamage - storedTag.getInt("Damage") >= autoRefillThreshhold;
    }

}
