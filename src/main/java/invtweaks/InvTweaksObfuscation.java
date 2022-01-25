package invtweaks;

import invtweaks.api.container.ContainerSection;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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
        //TODO: Add the rest of the classes
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
    public static Slot getSlotAtMousePosition(@Nullable AbstractContainerScreen guiContainer) {
        if(guiContainer != null) {
            return guiContainer.getSlotUnderMouse();
        } else {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean getIsMouseOverSlot(@Nullable AbstractContainerScreen guiContainer, @NotNull Slot slot, int x, int y) {
        if(guiContainer != null) {
            return guiContainer.getSlotUnderMouse().equals(slot);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private int getMouseX(@NotNull AbstractContainerScreen guiContainer) {
        return (int) ((mc.mouseHandler.xpos() * guiContainer.width) / getDisplayWidth());
    }

    @OnlyIn(Dist.CLIENT)
    private int getMouseY(@NotNull AbstractContainerScreen guiContainer) {
        return (int) (guiContainer.height - (mc.mouseHandler.ypos() * guiContainer.height) / getDisplayHeight() - 1);
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static int getSpecialChestRowSize(AbstractContainerMenu container) {
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).rowSize;
        }
        return 0;
    }

    // EntityPlayer members

    // Static access
    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isValidChest(AbstractContainerMenu container) {
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).validChest;
        }
        return false;
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isLargeChest(AbstractContainerMenu container) {
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).largeChest;
        }
        return false;
    }

    // InventoryPlayer members

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean isValidInventory(AbstractContainerMenu container) {
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).validInventory;
        }
        return false;
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static boolean showButtons(AbstractContainerMenu container) {
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).showButtons;
        }
        return false;
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public static Map<ContainerSection, List<Slot>> getContainerSlotMap(AbstractContainerMenu container) {
        // This method gets replaced by the transformer with "return container.invtweaks$slotMap()"
        if(InvTweaksMod.configClasses.containsKey(container.getClass().getTypeName())){
            return InvTweaksMod.configClasses.get(container.getClass().getTypeName()).defaultMethod.apply(container);
        }
        return new HashMap<>();
    }

    public static boolean isGuiContainer(@Nullable Object o) { // GuiContainer (abstract class)
        return o != null && o instanceof AbstractContainerScreen;
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
            currentContainer = ((AbstractContainerScreen) mc.screen).getMenu();
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

    public void hideScreen() {
        mc.popGuiLayer();
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
