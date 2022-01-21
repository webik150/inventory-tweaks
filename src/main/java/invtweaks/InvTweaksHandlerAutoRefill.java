package invtweaks;

import invtweaks.api.IItemTreeItem;
import invtweaks.api.container.ContainerSection;
import invtweaks.container.ContainerSectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the auto-refilling of the hotbar.
 *
 * @author Jimeo Wan
 */
public class InvTweaksHandlerAutoRefill extends InvTweaksObfuscation {

    private static final Logger log = InvTweaks.log;

    @NotNull
    private InvTweaksConfig config;

    public InvTweaksHandlerAutoRefill(Minecraft mc_, @NotNull InvTweaksConfig config_) {
        super();
        config = config_;
    }

    public void setConfig(@NotNull InvTweaksConfig config_) {
        config = config_;
    }

    /**
     * Auto-refill
     *
     * @throws Exception
     */
    public void autoRefillSlot(int slot, @NotNull String wantedId, CompoundTag wantedTag) throws Exception {

        @NotNull ContainerSectionManager container = new ContainerSectionManager(ContainerSection.INVENTORY);
        @NotNull ItemStack candidateStack, replacementStack = ItemStack.EMPTY;
        int replacementStackSlot = -1;
        boolean refillBeforeBreak = config.getProperty(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK).equals(InvTweaksConfig.VALUE_TRUE);
        boolean hasSubtypes = false;

        // TODO: ResourceLocation
        @Nullable Item original = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wantedId));

        @NotNull List<InvTweaksConfigSortingRule> matchingRules = new ArrayList<>();
        List<InvTweaksConfigSortingRule> rules = config.getRules();
        InvTweaksItemTree tree = config.getTree();

        // Check that the item is in the tree
        if(!tree.isItemUnknown(wantedId, wantedTag)) {

            //// Search replacement

            @NotNull List<IItemTreeItem> items = tree.getItems(wantedId, wantedTag);

            // Find rules that match the slot
            for(@NotNull IItemTreeItem item : items) {
                if(NbtUtils.compareNbt(item.getTag(),wantedTag,true)) {
                    // Since we search a matching item using rules,
                    // create a fake one that matches the exact item first
                    matchingRules.add(new InvTweaksConfigSortingRule(tree, "D" + (slot - 26), item.getName(), InvTweaksConst.INVENTORY_SIZE, InvTweaksConst.INVENTORY_ROW_SIZE));
                }
            }

            // Fallback to wildcard entry for items with subtypes only if no other entries are found
            if(matchingRules.isEmpty()) {
                for(@NotNull IItemTreeItem item : items) {
                    if(item.getTag().isEmpty()) {
                        // Since we search a matching item using rules,
                        // create a fake one that matches the exact item first
                        matchingRules.add(new InvTweaksConfigSortingRule(tree, "D" + (slot - 26), item.getName(), InvTweaksConst.INVENTORY_SIZE, InvTweaksConst.INVENTORY_ROW_SIZE));
                    }
                }
            }

            for(@NotNull InvTweaksConfigSortingRule rule : rules) {
                if(rule.getType() == InvTweaksConfigSortingRuleType.SLOT || rule.getType() == InvTweaksConfigSortingRuleType.COLUMN) {
                    for(int preferredSlot : rule.getPreferredSlots()) {
                        if(slot == preferredSlot) {
                            matchingRules.add(rule);
                            break;
                        }
                    }
                }
            }

            // Look only for a matching stack
            // First, look for the same item,
            // else one that matches the slot's rules
            for(@NotNull InvTweaksConfigSortingRule rule : matchingRules) {
                for(int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                    candidateStack = container.getItemStack(i);
                    if(!candidateStack.isEmpty()) {
                        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
                        @NotNull List<IItemTreeItem> candidateItems = tree.getItems(candidateStack.getItem().getRegistryName().toString(), candidateStack.getTag());
                        if(tree.matches(candidateItems, rule.getKeyword())) {
                            // Choose tool of highest damage value
                            if(candidateStack.getMaxStackSize() == 1) {
                                // Item
                                if((replacementStack.isEmpty() || candidateStack.getDamageValue() > replacementStack.getDamageValue()) && (!refillBeforeBreak || candidateStack.getMaxDamage() - candidateStack.getDamageValue() > config.getIntProperty(InvTweaksConfig.PROP_AUTO_REFILL_DAMAGE_THRESHHOLD))) {
                                    replacementStack = candidateStack;
                                    replacementStackSlot = i;
                                }
                            }
                            // Choose stack of lowest size
                            else if(replacementStack.isEmpty() || candidateStack.getCount() < replacementStack.getCount()) {
                                replacementStack = candidateStack;
                                replacementStackSlot = i;
                            }
                        }
                    }
                }
            }
        }

        // If item is unknown, look for exact same item
        else {
            for(int i = 0; i < InvTweaksConst.INVENTORY_SIZE; i++) {
                candidateStack = container.getItemStack(i);
                // TODO: ResourceLocation
                if(!candidateStack.isEmpty() && Objects.equals(candidateStack.getItem().getRegistryName().toString(), wantedId) && NbtUtils.compareNbt(candidateStack.getTag(), wantedTag, true)) {
                    replacementStack = candidateStack;
                    replacementStackSlot = i;
                    break;
                }
            }
        }

        //// Proceed to replacement

        if(!replacementStack.isEmpty() || (refillBeforeBreak && !container.getSlot(slot).getItem().isEmpty())) {

            log.info("Automatic stack replacement.");

            /*
             * This allows to have a short feedback
             * that the stack/tool is empty/broken.
             */
            InvTweaks.getInstance().addScheduledTask(new Runnable() {

                private ContainerSectionManager containerMgr;
                private int targetedSlot;
                private int i;
                @Nullable
                private String expectedItemId;
                private boolean refillBeforeBreak;

                @NotNull
                public Runnable init(int i_, int currentItem, boolean refillBeforeBreak_) throws Exception {
                    containerMgr = new ContainerSectionManager(ContainerSection.INVENTORY);
                    targetedSlot = currentItem;
                    if(i_ != -1) {
                        i = i_;
                        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
                        expectedItemId = containerMgr.getItemStack(i).getItem().getRegistryName().toString();
                    } else {
                        i = containerMgr.getFirstEmptyIndex();
                        expectedItemId = null;
                    }
                    refillBeforeBreak = refillBeforeBreak_;
                    return this;
                }

                public void run() {
                    if(i < 0 || targetedSlot < 0) {
                        return;
                    }

                    // TODO: Look for better update detection now that this runs tick-based. It'll probably fail a bit if latency is > 50ms (1 tick)
                    // Since last tick, things might have changed
                    @NotNull ItemStack stack = containerMgr.getItemStack(i);

                    // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
                    if(!stack.isEmpty() && StringUtils.equals(stack.getItem().getRegistryName().toString(), expectedItemId) || this.refillBeforeBreak) {
                        if(containerMgr.move(targetedSlot, i) || containerMgr.move(i, targetedSlot)) {
                            if(!config.getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS).equals(InvTweaksConfig.VALUE_FALSE)) {
                                mc.player.playSound(SoundEvents.CHICKEN_EGG, 1.0f, 1.0f);
                            }
                            // If item are swapped (like for mushroom soups),
                            // put the item back in the inventory if it is in the hotbar
                            if(!containerMgr.getItemStack(i).isEmpty() && i >= 27) {
                                for(int j = 0; j < InvTweaksConst.INVENTORY_SIZE; j++) {
                                    if(containerMgr.getItemStack(j).isEmpty()) {
                                        containerMgr.move(i, j);
                                        break;
                                    }
                                }
                            }

                            // Make sure the inventory resyncs
                            containerMgr.applyChanges();
                        } else {
                            log.warn("Failed to move stack for autoreplace, despite of prior tests.");
                        }
                    }
                }

            }.init(replacementStackSlot, slot, refillBeforeBreak));

        }
    }

}
