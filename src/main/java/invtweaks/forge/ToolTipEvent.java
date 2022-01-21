package invtweaks.forge;

import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksConfigManager;
import invtweaks.api.IItemTreeCategory;
import invtweaks.api.IItemTreeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToolTipEvent {

    @SubscribeEvent
    public static void tTipEvent(ItemTooltipEvent event) {
        ItemStack current = event.getItemStack();
        if(current.isEmpty()) {
            return;
        }
        InvTweaksConfigManager cfgManager = InvTweaks.getConfigManager();
        if(cfgManager == null) { return; }

        if(cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_TOOLTIP_PATH).equals("true")) {
            List<IItemTreeItem> items = cfgManager.getConfig().getTree().getItems(current.getItem().getRegistryName().toString(), current.getTag());
            if(items.isEmpty()) { return; }

            Set<String> paths = new HashSet<>();
            IItemTreeCategory root = cfgManager.getConfig().getTree().getRootCategory();
            int unsortedZone = cfgManager.getConfig().getTree().getLastTreeOrder();
            int minOrder = Integer.MAX_VALUE;
            for(IItemTreeItem item : items) {
                String path = item.getPath();
                String altPath = root.findKeywordPath(item.getName());
                int itemOrder = item.getOrder();
                minOrder = Integer.min(minOrder, itemOrder);
                //event.getToolTip().add( + "");

                //If we have reported a match in the tree, don't report the unsorted matches.
                //This can happen if a damage id is filtered and not all of the items have been
                //placed in the tree.
                if(minOrder <= unsortedZone && itemOrder > unsortedZone) { break; }

                if(path.equals(altPath) || itemOrder > unsortedZone) {
                    if(!paths.contains(path)) {
                        event.getToolTip().add(new TextComponent(ChatFormatting.DARK_GRAY + path + " (" + item.getOrder() + ")"));
                        paths.add(path);
                    }
                } else {
                    if(!paths.contains(path)) {
                        event.getToolTip().add(new TextComponent(ChatFormatting.DARK_GRAY + "T:" + path + " (" + item.getOrder() + ")"));
                        paths.add(path);
                    }
                    if(!paths.contains(altPath)) {
                        event.getToolTip().add(new TextComponent(ChatFormatting.DARK_GRAY + "M:" + altPath + " (" + item.getOrder() + ")"));
                        paths.add(altPath);
                    }
                }
            }
        }
    }
}
