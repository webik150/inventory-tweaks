package invtweaks.container;

import invtweaks.InvTweaksConst;
import invtweaks.api.container.ContainerSection;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class VanillaSlotMaps {
    @NotNull
    public static Map<ContainerSection, List<Slot>> containerPlayerSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.CRAFTING_OUT, container.slots.subList(0, 1));
        slotRefs.put(ContainerSection.CRAFTING_IN, container.slots.subList(1, 5));
        slotRefs.put(ContainerSection.ARMOR, container.slots.subList(5, 9));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(9, 45));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(9, 36));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(36, 45));

        return slotRefs;
    }

    /*@SideOnly(Side.CLIENT)
    public static boolean containerCreativeIsInventory(CreativeScreen.ContainerCreative container) {
        @Nullable Screen currentScreen = FMLClientHandler.instance().getClient().currentScreen;
        return currentScreen instanceof CreativeScreen && ((CreativeScreen) currentScreen).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex();
    }

    @NotNull
    @SideOnly(Side.CLIENT)
    public static Map<ContainerSection, List<Slot>> containerCreativeSlots(@NotNull Creati container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.ARMOR, container.slots.subList(5, 9));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(9, 45));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(9, 36));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(36, 45));

        return slotRefs;
    }*/

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerChestDispenserSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        int size = container.slots.size();

        slotRefs.put(ContainerSection.CHEST, container.slots.subList(0, size - InvTweaksConst.INVENTORY_SIZE));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size - InvTweaksConst.HOTBAR_SIZE));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(size - InvTweaksConst.HOTBAR_SIZE, size));

        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerHorseSlots(@NotNull HorseInventoryMenu container, AbstractHorse horse) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        int size = container.slots.size();

        if(horse instanceof AbstractChestedHorse && ((AbstractChestedHorse)horse).hasChest()){ // Chest slots are only added if chest is added. Saddle/armor slots always exist.
            slotRefs.put(ContainerSection.CHEST, container.slots.subList(2, size - InvTweaksConst.INVENTORY_SIZE));
        }
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size - InvTweaksConst.HOTBAR_SIZE));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(size - InvTweaksConst.HOTBAR_SIZE, size));

        return slotRefs;
    }

    public static boolean containerHorseIsInventory(@NotNull HorseInventoryMenu container, AbstractHorse horse) {
        return horse instanceof AbstractChestedHorse && ((AbstractChestedHorse)horse).hasChest();
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerFurnaceSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.FURNACE_IN, container.slots.subList(0, 1));
        slotRefs.put(ContainerSection.FURNACE_FUEL, container.slots.subList(1, 2));
        slotRefs.put(ContainerSection.FURNACE_OUT, container.slots.subList(2, 3));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(3, 39));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(3, 30));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(30, 39));
        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerWorkbenchSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.CRAFTING_OUT, container.slots.subList(0, 1));
        slotRefs.put(ContainerSection.CRAFTING_IN, container.slots.subList(1, 10));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(10, 46));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(10, 37));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(37, 46));

        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerEnchantmentSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.ENCHANTMENT, container.slots.subList(0, 1));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(2, 38));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(2, 29));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(29, 38));

        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerBrewingSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.BREWING_BOTTLES, container.slots.subList(0, 3));
        slotRefs.put(ContainerSection.BREWING_INGREDIENT, container.slots.subList(3, 4));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(4, 40));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(4, 31));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(31, 40));

        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> containerRepairSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.CRAFTING_IN, container.slots.subList(0, 2));
        slotRefs.put(ContainerSection.CRAFTING_OUT, container.slots.subList(2, 3));
        slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(3, 39));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(3, 30));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(30, 39));

        return slotRefs;
    }

    @NotNull
    public static Map<ContainerSection, List<Slot>> unknownContainerSlots(@NotNull AbstractContainerMenu container) {
        @NotNull Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        int size = container.slots.size();

        if(size >= InvTweaksConst.INVENTORY_SIZE) {
            // Assuming the container ends with the inventory, just like all vanilla containers.
            slotRefs.put(ContainerSection.CHEST, container.slots.subList(0, size - InvTweaksConst.INVENTORY_SIZE));
            slotRefs.put(ContainerSection.INVENTORY, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size));
            slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, container.slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size - InvTweaksConst.HOTBAR_SIZE));
            slotRefs.put(ContainerSection.INVENTORY_HOTBAR, container.slots.subList(size - InvTweaksConst.HOTBAR_SIZE, size));
        } else {
            slotRefs.put(ContainerSection.CHEST, container.slots.subList(0, size));
        }

        return slotRefs;
    }
}
