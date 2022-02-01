package invtweaks.container;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {

    public static boolean moveItemStackTo(AbstractContainerMenu container, ItemStack stackToMove, int startIndex, int maxIndex, boolean reverse) {
        boolean flag = false;
        int i = startIndex;
        if (reverse) {
            i = maxIndex - 1;
        }

        if (stackToMove.isStackable()) {
            while(!stackToMove.isEmpty()) {
                if (reverse) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= maxIndex) {
                    break;
                }

                Slot slot = container.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stackToMove, itemstack)) {
                    int j = itemstack.getCount() + stackToMove.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stackToMove.getMaxStackSize());
                    if (j <= maxSize) {
                        stackToMove.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stackToMove.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverse) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stackToMove.isEmpty()) {
            if (reverse) {
                i = maxIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverse) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= maxIndex) {
                    break;
                }

                Slot slot1 = container.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stackToMove)) {
                    if (stackToMove.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stackToMove.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stackToMove.split(stackToMove.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverse) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }
}
