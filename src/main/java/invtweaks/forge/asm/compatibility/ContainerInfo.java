package invtweaks.forge.asm.compatibility;

import invtweaks.api.container.ContainerSection;
import invtweaks.container.VanillaSlotMaps;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnusedDeclaration")
public class ContainerInfo {
    public boolean showButtons = false;
    public boolean validInventory = false;
    public boolean validChest = false;
    public boolean largeChest = false;
    public short rowSize = 9;
    public Function<AbstractContainerMenu, Map<ContainerSection, List<Slot>>> defaultMethod = VanillaSlotMaps::unknownContainerSlots;
    @Nullable
    public Function<AbstractContainerMenu, Map<ContainerSection, List<Slot>>> rowSizeMethod = null;
    @Nullable
    public Function<AbstractContainerMenu, Map<ContainerSection, List<Slot>>> largeChestMethod = null;

    public ContainerInfo() {
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
        largeChest = largeCh;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, @NotNull Function<AbstractContainerMenu, Map<ContainerSection, List<Slot>>> slotMap) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
        defaultMethod = slotMap;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
        rowSize = rowS;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh, short rowS) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
        largeChest = largeCh;
        rowSize = rowS;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS, Function<AbstractContainerMenu, Map<ContainerSection, List<Slot>>> slotMap) {
        showButtons = standard;
        validInventory = validInv;
        validChest = validCh;
        rowSize = rowS;
        defaultMethod = slotMap;
    }
}
