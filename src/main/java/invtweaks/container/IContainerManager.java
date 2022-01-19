package invtweaks.container;

import invtweaks.api.container.ContainerSection;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IContainerManager {
    int DROP_SLOT = -999;

    /**
     * Moves a stack from source to destination, adapting the behavior according to the context: - If destination is
     * empty, the source stack is moved. - If the items can be merged, as much items are possible are put in the
     * destination, and the eventual remains go back to the source. - If the items cannot be merged, they are swapped.
     *
     * @param srcSection  The source section
     * @param srcIndex    The source slot
     * @param destSection The destination section
     * @param destIndex   The destination slot
     * @return false if the source slot is empty or the player is holding an item that couln't be put down.
     */
    // TODO: Server helper directly implementing this as a swap without the need for intermediate slots.
    boolean move(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex);

    /**
     * Moves some items from source to destination.
     *
     * @param srcSection  The source section
     * @param srcIndex    The source slot
     * @param destSection The destination section
     * @param destIndex   The destination slot
     * @param amount      The amount of items to move. If <= 0, does nothing. If > to the source stack size, moves as
     *                    much as possible from the stack size. If not all can be moved to the destination, only moves
     *                    as much as possible.
     * @return false if the destination slot is already occupied by a different item (meaning items cannot be moved to
     * destination).
     */
    boolean moveSome(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex, int amount);

    // TODO: Server helper directly implementing this.
    default boolean drop(ContainerSection srcSection, int srcIndex) {
        return move(srcSection, srcIndex, null, DROP_SLOT);
    }

    // TODO: Server helper directly implementing this.
    default boolean dropSome(ContainerSection srcSection, int srcIndex, int amount) {
        return moveSome(srcSection, srcIndex, null, DROP_SLOT, amount);
    }

    /**
     * If an item is in hand (= attached to the cursor), puts it down.
     *
     * @return true unless the item could not be put down
     */
    boolean putHoldItemDown(ContainerSection destSection, int destIndex);

    default void leftClick(ContainerSection section, int index) {
        click(section, index, false);
    }

    default void rightClick(ContainerSection section, int index) {
        click(section, index, true);
    }

    void click(ContainerSection section, int index, boolean rightClick);

    boolean hasSection(ContainerSection section);

    List<Slot> getSlots(ContainerSection section);

    /**
     * @return The size of the whole container
     */
    int getSize();

    /**
     * Returns the size of a section of the container.
     *
     * @return The size, or 0 if there is no such section.
     */
    int getSize(ContainerSection section);

    /**
     * @return -1 if no slot is free
     */
    int getFirstEmptyIndex(ContainerSection section);

    /**
     * @return true if the specified slot exists and is empty, false otherwise.
     */
    boolean isSlotEmpty(ContainerSection section, int slot);

    @Nullable
    Slot getSlot(ContainerSection section, int index);

    /**
     * @return -1 if not found
     */
    default int getSlotIndex(int slotNumber) {
        return getSlotIndex(slotNumber, false);
    }

    /**
     * @param preferInventory Set to true if you prefer to have the index according to the whole inventory, instead of a
     *                        more specific section (hotbar/not hotbar)
     * @return Full index of slot in the container
     */
    int getSlotIndex(int slotNumber, boolean preferInventory);

    /**
     * Note: Prefers INVENTORY_HOTBAR/NOT_HOTBAR instead of INVENTORY.
     *
     * @return null if the slot number is invalid.
     */
    @Nullable
    ContainerSection getSlotSection(int slotNumber);

    /**
     * Returns an ItemStack from the wanted section and slot.
     *
     * @return An ItemStack or null.
     */
    @NotNull
    ItemStack getItemStack(ContainerSection section, int index);

    AbstractContainerMenu getContainer();

    void applyChanges();
}
