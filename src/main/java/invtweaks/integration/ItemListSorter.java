package invtweaks.integration;

import invtweaks.InvTweaks;
import invtweaks.forge.ClientProxy;
import invtweaks.forge.CommonProxy;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

;

public class ItemListSorter {

    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static void LinkJEITComparator() {
        if(ModList.get().isLoaded("jei")) {
            try {
                Class<?> ingredientListElementComparator = Class.forName("mezz.jei.ingredients.IngredientListElementComparator");
                Class<?> clientConfig = Class.forName("mezz.jei.config.Config");
                if(ingredientListElementComparator != null && clientConfig != null) {
                    Class[] cArg = new Class[2];
                    cArg[0] = String.class;
                    cArg[1] = Comparator.class;
                    Method addItemStackComparison = ingredientListElementComparator.getMethod("addItemStackComparison", cArg);
                    Method updateSortOrder = clientConfig.getMethod("updateSortOrder");
                    if(addItemStackComparison != null && updateSortOrder != null) {
                        Object[] oArg = new Object[2];
                        //The tree-only sorting.
                        oArg[0] = "invtweakstree";
                        oArg[1] = new ItemListComparator();
                        addItemStackComparison.invoke(null, oArg);

                        //The aggressive sorting.
                        oArg[0] = "invtweaksall";
                        oArg[1] = new ItemListComparator2();
                        addItemStackComparison.invoke(null, oArg);
                        updateSortOrder.invoke(null);
                    }
                }
            } catch(@NotNull ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException ignored) {
                return;
            }
        }
    }

    public static void ReloadItemList() {
        if(ModList.get().isLoaded("jei")) {
            try {
                Class<?> ProxyCommonClient = Class.forName("mezz.jei.startup.ProxyCommonClient");
                if(ProxyCommonClient != null) {
                    Method reloadItemList = ProxyCommonClient.getMethod("reloadItemList");
                    if(reloadItemList != null) {
                        reloadItemList.invoke(null);
                    }
                }
            } catch(@NotNull ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException ignored) {
                return;
            }
        }

    }

    public static class ItemListComparator implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            if(o1 == null && o2 == null) { return 0; } else if(o1 == null) { return 1; } else if(o2 == null) {
                return -1;
            }
            return InvTweaks.getInstance().compareItems(o1, o2, true);
        }
    }

    public static class ItemListComparator2 implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            if(o1 == null && o2 == null) { return 0; } else if(o1 == null) { return 1; } else if(o2 == null) {
                return -1;
            }
            return InvTweaks.getInstance().compareItems(o1, o2, false);
        }
    }

}
