package invtweaks;

import invtweaks.api.container.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

/**
 * Minecraft 1.3 Obfuscation layer
 *
 * @author Jimeo Wan
 */
public class InvTweaksObfuscation {
    public Minecraft mc;

    public InvTweaksObfuscation() {
        mc = Minecraft.getInstance();
    }

    // Minecraft members

    @Nullable
    public static String getNamespacedID(@Nullable String id) {
        if(id == null) {
            return null;
        } else if(id.indexOf(':') == -1) {
            return "minecraft:" + id;
        }
        return id;
    }

    public static int getDisplayWidth() {
        return Minecraft.getInstance().screen.width;
    }

    public static int getDisplayHeight() {
        return Minecraft.getInstance().screen.height;
    }

    public static boolean areItemStacksEqual(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
        return itemStack1.areShareTagsEqual(itemStack2) && itemStack1.getCount() == itemStack2.getCount();
    }

    @NotNull
    public static ItemStack getSlotStack(AbstractContainerMenu container, int i) {
        return container.getSlot(i).getItem(); // getStack
    }

   public static int getSlotNumber(Slot slot) {
       return slot.getContainerSlot();
    }

    @OnlyIn(Dist.CLIENT)
    public static Slot getSlotAtMousePosition(@Nullable ContainerScreen guiContainer) {
        if(guiContainer != null) {
            return guiContainer.getSlotUnderMouse();
        } else {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean getIsMouseOverSlot(@Nullable ContainerScreen guiContainer, @NotNull Slot slot, int x, int y) {
        if(guiContainer != null) {
            return guiContainer.getSlotUnderMouse().equals(slot);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private int getMouseX(@NotNull ContainerScreen guiContainer) {
        return (int) ((mc.mouseHandler.xpos() * guiContainer.width) / getDisplayWidth());
    }

    @OnlyIn(Dist.CLIENT)
    private int getMouseY(@NotNull ContainerScreen guiContainer) {
        return (int) (guiContainer.height - (mc.mouseHandler.ypos() * guiContainer.height) / getDisplayHeight() - 1);
    }

    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static int getSpecialChestRowSize(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$rowSize()"
        return 0;
    }

    // EntityPlayer members

    // Static access
    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isValidChest(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$validChest()"
        return false;
    }

    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isLargeChest(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$largeChest()"
        return false;
    }

    // InventoryPlayer members

    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isValidInventory(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$validInventory()"
        return false;
    }

    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean showButtons(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$showButtons()"
        return false;
    }

    @Contract("!null->_")
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static Map<ContainerSection, List<Slot>> getContainerSlotMap(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$slotMap()"
        HashMap<ContainerSection, List<Slot>> a = new HashMap<>();
        a.put(ContainerSection.INVENTORY, container.slots);
        a.put(ContainerSection.CHEST, container.slots);
        a.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots);
        a.put(ContainerSection.INVENTORY_HOTBAR, container.slots);
        a.put(ContainerSection.ARMOR, container.slots);
        a.put(ContainerSection.BREWING_BOTTLES, container.slots);
        return a;
    }

    public static boolean isGuiContainer(@Nullable Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof ContainerScreen;
    }

    public static boolean isGuiInventoryCreative(@Nullable Object o) { // GuiInventoryCreative
        return o != null && o.getClass().equals(CreativeModeInventoryScreen.class);
    }

    public static boolean isGuiInventory(@Nullable Object o) { // GuiInventory
        return o != null && o.getClass().equals(InventoryScreen.class);
    }

    public static boolean isGuiButton(@Nullable Object o) { // GuiButton
        return o != null && o instanceof Button;
    }

    // FontRenderer members

    public static boolean isGuiEditSign(@Nullable Object o) {
        return o != null && o.getClass().equals(SignEditScreen.class);
    }

    public static boolean isItemArmor(@Nullable Object o) { // ItemArmor
        return o != null && o instanceof ArmorItem;
    }

    public static boolean isBasicSlot(@Nullable Object o) { // Slot
        return o != null && (o.getClass().equals(Slot.class));
    }

    // Container members

    public static AbstractContainerMenu getCurrentContainer() {
        Minecraft mc = Minecraft.getInstance();
        AbstractContainerMenu currentContainer = mc.player.containerMenu;
        if(InvTweaksObfuscation.isGuiContainer(mc.screen)) {
            currentContainer = ((ContainerScreen) mc.screen).getMenu();
        }
        return currentContainer;
    }

    // Slot members

    public static boolean areSameItemType(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
        return !itemStack1.isEmpty() && !itemStack2.isEmpty() && (itemStack1.areShareTagsEqual(itemStack2) || (itemStack1.isDamageableItem() && itemStack1.getItem() == itemStack2.getItem()));
    }

    public static boolean areItemsStackable(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
        return !itemStack1.isEmpty() && !itemStack2.isEmpty() && itemStack1.areShareTagsEqual(itemStack2) && itemStack1.isStackable() && (itemStack1.getDamageValue() == itemStack2.getDamageValue()) && ItemStack.isSameItemSameTags(itemStack1, itemStack2);
    }



    public void addChatMessage(@NotNull String message) {
        if(mc.gui != null) {
            mc.gui.getChat().addMessage(new TextComponent(message));
        }
    }

    public Player getThePlayer() {
        return mc.player;
    }

    @Nullable
    public Screen getCurrentScreen() {
        return mc.screen;
    }

    public Font getFontRenderer() {
        return mc.font;
    }

    public void displayGuiScreen(Screen parentScreen) {
        mc.pushGuiLayer(parentScreen);
    }

    public Options getGameSettings() {
        return mc.options;
    }

    public int getKeyBindingForwardKeyCode() {
        return getGameSettings().keyUp.getKey().getValue();
    }

    // Classes

    public int getKeyBindingBackKeyCode() {
        return getGameSettings().keyDown.getKey().getValue();
    }

    public LazyOptional<IItemHandler> getInventoryPlayer() { // InventoryPlayer
        return getThePlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @NotNull
    public ItemStack getHeldStack() {
        return getThePlayer().getMainHandItem(); // getItemStack
    }

    //TODO: One of these is wrong
    @NotNull
    public ItemStack getFocusedStack() {
        return getThePlayer().getMainHandItem(); // getCurrentItem
    }

    public boolean hasTexture(@NotNull ResourceLocation texture) {
        try {
            mc.getResourceManager().getResource(texture);
        } catch(IOException e) {
            return false;
        }
        return true;
    }

    @NotNull
    public ItemStack getOffhandStack() {
        return getThePlayer().getOffhandItem();
    }
}
