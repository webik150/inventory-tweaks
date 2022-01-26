package invtweaks;

import com.google.common.collect.Multimap;
import invtweaks.api.IItemTree;
import invtweaks.api.IItemTreeItem;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.container.ContainerSectionManager;
import invtweaks.container.DirectContainerManager;
import invtweaks.container.IContainerManager;
import invtweaks.forge.ClientProxy;
import invtweaks.forge.InvTweaksMod;
import invtweaks.forge.InputEventHandler;
import invtweaks.gui.InvTweaksGuiSettings;
import invtweaks.gui.InvTweaksGuiSettingsButton;
import invtweaks.gui.InvTweaksGuiSortingButton;
import invtweaks.integration.ItemListChecker;
import invtweaks.network.ITPacketHandler;
import invtweaks.network.packets.ITPacketAddStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Main class for Inventory Tweaks, which maintains various hooks and dispatches the events to the correct handlers.
 *
 * @author Jimeo Wan
 * <p>
 * Contact: jimeo.wan (at) gmail (dot) com Website: <a href="https://inventory-tweaks.readthedocs.org/">https://inventory-tweaks.readthedocs.org/</a>
 * Source code: <a href="https://github.com/kobata/inventory-tweaks">GitHub</a> License: MIT
 */
public class InvTweaks extends InvTweaksObfuscation {

    private static InvTweaks instance;
    @NotNull
    private final ItemStack[] hotbarClone = new ItemStack[InvTweaksConst.INVENTORY_HOTBAR_SIZE];
    @NotNull
    private final List<String> queuedMessages = new ArrayList<>();
    private final ItemListChecker itemListChecker = new ItemListChecker();

    /**
     * The configuration loader.
     */
    @Nullable
    private InvTweaksConfigManager cfgManager = null;
    /**
     * Attributes to remember the status of chest sorting while using middle clicks.
     */
    private SortingMethod chestAlgorithm = SortingMethod.DEFAULT;
    private long chestAlgorithmClickTimestamp = 0;
    private boolean chestAlgorithmButtonDown = false;
    /**
     * Various information concerning the context, stored on each tick to allow for certain features (auto-refill,
     * sorting on pick up...)
     */
    @NotNull
    private ItemStack storedStack = ItemStack.EMPTY;
    @Nullable
    private String storedStackId = null;
    private CompoundTag storedTag = null;
    private boolean mouseWasDown = false;
    private boolean wasInGUI = false;
    private boolean previousRecipeBookVisibility = false;
    /**
     * Allows to trigger some logic only every Const.POLLING_DELAY.
     */
    private int tickNumber = 0, lastPollingTickNumber = -InvTweaksConst.POLLING_DELAY;
    /**
     * Stores when the sorting key was last pressed (allows to detect long key holding)
     */
    private long sortingKeyPressedDate = 0;
    private boolean sortKeyDown = false;
    private boolean sortKeyEnabled = true;
    private boolean textboxMode = false;
    private boolean itemPickupPending = false;
    private int itemPickupTimeout = 0;
    /**
     * Debug tools:
     */
    private String mostRecentComparison = "";
    private boolean debugTree = true;
    private int storedFocusedSlot;

    /**
     * Creates an instance of the mod, and loads the configuration from the files, creating them if necessary.
     */
    public InvTweaks() {
        super();

        for(int i = 0; i < hotbarClone.length; ++i) {
            hotbarClone[i] = ItemStack.EMPTY;
        }
        //log.setLevel(InvTweaksConst.DEFAULT_LOG_LEVEL);

        // Store instance
        instance = this;

        // Load config files
        cfgManager = new InvTweaksConfigManager(mc);
        if(cfgManager.makeSureConfigurationIsLoaded()) {
            InvTweaksMod.log.info("Mod initialized");
        } else {
            InvTweaksMod.log.error("Mod failed to initialize!");
        }
    }

    public static void logInGameStatic(@NotNull String message) {
        InvTweaks.getInstance().logInGame(message);
    }

    public static void logInGameErrorStatic(@NotNull String message, @NotNull Exception e) {
        InvTweaks.getInstance().logInGameError(message, e);
    }

    /**
     * @return InvTweaks instance
     */
    public static InvTweaks getInstance() {
        return instance;
    }

    public static Minecraft getMinecraftInstance() {
        return instance.mc;
    }

    @Nullable
    public static InvTweaksConfigManager getConfigManager() {
        if(instance == null) { return null; }
        return instance.cfgManager;
    }

    @NotNull
    public static IContainerManager getContainerManager(AbstractContainerMenu container) {
        // TODO: Reenable when it doesn't just break everything
        //if(getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_CONTAINER_MIRRORING).equals(InvTweaksConfig.VALUE_TRUE)) {
        //    return new MirroredContainerManager(container);
        //} else {

        return new DirectContainerManager(container);
        //}
    }

    @NotNull
    public static IContainerManager getCurrentContainerManager() {
        return getContainerManager(InvTweaksObfuscation.getCurrentContainer());
    }

    public static int getContainerRowSize(@NotNull AbstractContainerScreen guiContainer) {
        return getSpecialChestRowSize(guiContainer.getMenu());
    }
   public static int getContainerMenuRowSize(@NotNull AbstractContainerMenu menu) {
        return getSpecialChestRowSize(menu);
    }

    @NotNull
    private static String buildLogString(@NotNull Level level, String message, @Nullable Exception e) {
        if(e != null) {
            StackTraceElement[] trace = e.getStackTrace();

            if(trace.length == 0) {
                return buildLogString(level, message) + ": " + e.getMessage();
            }

            StackTraceElement exceptionLine = trace[0];
            if(exceptionLine != null && exceptionLine.getFileName() != null) {
                return buildLogString(level, message) + ": " + e.getMessage() + " (l" + exceptionLine.getLineNumber() + " in " + exceptionLine.getFileName().replace("InvTweaks", "") + ")";
            } else {
                return buildLogString(level, message) + ": " + e.getMessage();
            }
        } else {
            return buildLogString(level, message);
        }
    }

    @NotNull
    private static String buildLogString(@NotNull Level level, String message) {
        return InvTweaksConst.INGAME_LOG_PREFIX + ((level.equals(Level.SEVERE)) ? "[ERROR] " : "") + message;
    }

    private static int compareMaxDamage(ItemStack i, ItemStack j) {
        //Use durability to sort, favoring more durable items.
        int maxDamage1 = i.getMaxDamage() <= 0 ? Integer.MAX_VALUE : i.getMaxDamage();
        int maxDamage2 = j.getMaxDamage() <= 0 ? Integer.MAX_VALUE : j.getMaxDamage();
        return maxDamage2 - maxDamage1;
    }

    private static int compareCurDamage(ItemStack i, ItemStack j) {
        //Use remaining durability to sort, favoring more damaged.
        int curDamage1 = i.getDamageValue();
        int curDamage2 = j.getDamageValue();
        if(i.isDamageableItem() && !getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_INVERT_TOOL_DAMAGE).equals(InvTweaksConfig.VALUE_TRUE)) {
            return curDamage2 - curDamage1;
        } else {
            return curDamage1 - curDamage2;
        }
    }

    public static String getToolClass(ItemStack itemStack, Item item) {
        if(itemStack == null || item == null) { return ""; }
        //TODO: How tf do I change this
        Set<String> toolClassSet = new HashSet<>();//item.getToolClasses(itemStack);
        InvTweaksMod.log.warn("Item tags:");
        InvTweaksMod.log.warn(item.getTags());
        if(toolClassSet.contains("sword")) {
            //Swords are not "tools".
            Set<String> newClassSet = new HashSet<String>();
            for(String toolClass : toolClassSet) { if(toolClass != "sword") { newClassSet.add(toolClass); } }
            toolClassSet = newClassSet;
        }

        //Minecraft hoes, shears, and fishing rods don't have tool class names.
        if(toolClassSet.isEmpty()) {
            if(item instanceof HoeItem) { return "hoe"; }
            if(item instanceof ShearsItem) { return "shears"; }
            if(item instanceof FishingRodItem) { return "fishingrod"; }
            return "";
        }

        //Get the only thing.
        if(toolClassSet.size() == 1) { return (String) toolClassSet.toArray()[0]; }

        //We have a preferred type to list tools under, primarily the pickaxe for harvest level.
        String[] prefOrder = {"pickaxe", "axe", "shovel", "hoe", "shears", "wrench"};
        for(int i = 0; i < prefOrder.length; i++) { if(toolClassSet.contains(prefOrder[i])) { return prefOrder[i]; } }

        //Whatever happens to be the first thing:
        return (String) toolClassSet.toArray()[0];
    }

    public void addScheduledTask(Runnable task) {
        InvTweaksMod.proxy.addClientScheduledTask(task);
    }

    /**
     * To be called on each tick during the game (except when in a menu). Handles the auto-refill.
     */
    public void onTickInGame() {
        synchronized(this) {
            if(!onTick()) {
                return;
            }
            handleAutoRefill();
            if(wasInGUI) {
                wasInGUI = false;
                textboxMode = false;
            }
        }
    }

    /**
     * To be called on each tick when a menu is open. Handles the GUI additions and the middle clicking.
     */
    public void onTickInGUI(Screen guiScreen) {
        if(mc.player.isSpectator()) {
            onTick();
            return;
        }
        synchronized(this) {
            handleMiddleClick(guiScreen); // Called before the rest to be able to trigger config reload
            if(!onTick()) {
                return;
            }
            if(isTimeForPolling()) {
                unlockKeysIfNecessary();
            }
            if(isGuiContainer(guiScreen)) {
                //handleGUILayout((ContainerScreen) guiScreen);
            }
            if(!wasInGUI) {
                // Right-click is always true on initial open of GUI.
                // Ignore it to prevent erroneous trigger of shortcuts.
                mouseWasDown = true;
            }
            if(isGuiContainer(guiScreen)) {
                handleShortcuts((AbstractContainerScreen) guiScreen);
            }

            // Copy some info about current selected stack for auto-refill
            @NotNull ItemStack currentStack = getFocusedStack();

            // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
            storedStackId = (currentStack.isEmpty()) ? null : currentStack.getItem().getRegistryName().toString();
            if(!wasInGUI) {
                wasInGUI = true;
            }
        }
    }

    /**
     * To be called every time the sorting key is pressed. Sorts the inventory.
     */
    public final void onSortingKeyPressed() {
        synchronized(this) {

            // Check config loading success
            if(!cfgManager.makeSureConfigurationIsLoaded()) {
                return;
            }

            // Check current GUI
            @Nullable Screen guiScreen = getCurrentScreen();
            if(guiScreen == null || (isGuiContainer(guiScreen) && (isValidChest(((AbstractContainerScreen) guiScreen).getMenu()) || isValidInventory(((AbstractContainerScreen) guiScreen).getMenu())))) {
                // Sorting!
                handleSorting(guiScreen);
            }
        }
    }

    /**
     * To be called everytime a stack has been picked up. Moves the picked up item in another slot that matches best the
     * current configuration.
     */
    public void onItemPickup() {

        if(!cfgManager.makeSureConfigurationIsLoaded()) {
            return;
        }
        @Nullable InvTweaksConfig config = cfgManager.getConfig();
        // Handle option to disable this feature
        if(cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP).equals("false")) {
            itemPickupPending = false;
            return;
        }

        try {
            @NotNull ContainerSectionManager containerMgr = new ContainerSectionManager(ContainerSection.INVENTORY);

            // Find stack slot (look in hotbar only).
            // We're looking for a brand new stack in the hotbar
            // (not an existing stack whose amount has been increased)
            int currentSlot = -1;
            for(int i = 0; i < InvTweaksConst.INVENTORY_HOTBAR_SIZE; i++) {
                @NotNull ItemStack currentHotbarStack = containerMgr.getItemStack(i + 27);
                // Don't move already started stacks
                if(!currentHotbarStack.isEmpty() && /*currentHotbarStack.getAnimationsToGo() > 0 TODO: Find out what does &&*/ hotbarClone[i].isEmpty()) {
                    currentSlot = i + 27;
                }
            }

            if(currentSlot != -1) {
                itemPickupPending = false;

                // Find preferred slots
                IItemTree tree = config.getTree();
                @NotNull ItemStack stack = containerMgr.getItemStack(currentSlot);

                // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
                List<IItemTreeItem> items = tree.getItems(stack.getItem().getRegistryName().toString(), stack.getTag());

                List<Integer> preferredPositions = config.getRules().stream().filter(rule -> tree.matches(items, rule.getKeyword())).flatMapToInt(e -> Arrays.stream(e.getPreferredSlots())).boxed().collect(Collectors.toList());

                // Find best slot for stack
                boolean hasToBeMoved = true;
                for(int newSlot : preferredPositions) {
                    // Already in the best slot!
                    if(newSlot == currentSlot) {
                        hasToBeMoved = false;
                        break;
                    }
                    // Is the slot available?
                    else if(containerMgr.getItemStack(newSlot).isEmpty()) {
                        // TODO: Check rule level before to move
                        if(containerMgr.move(currentSlot, newSlot)) {
                            break;
                        }
                    }
                }

                // Else, put the slot anywhere
                if(hasToBeMoved) {
                    for(int i = 0; i < containerMgr.getSize(); i++) {
                        if(containerMgr.getItemStack(i).isEmpty()) {
                            if(containerMgr.move(currentSlot, i)) {
                                break;
                            }
                        }
                    }
                }

                // Sync after pickup movements.
                containerMgr.applyChanges();

            } else {
                if(--itemPickupTimeout == 0) {
                    itemPickupPending = false;
                }
            }

        } catch(Exception e) {
            logInGameError("Failed to move picked up stack", e);
            itemPickupPending = false;
        }
    }

    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j) {
        return compareItems(i, j, getItemOrder(i), getItemOrder(j), false);
    }

    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j, boolean onlyTreeSort) {
        return compareItems(i, j, getItemOrder(i), getItemOrder(j), onlyTreeSort);
    }

    int compareItems(@NotNull ItemStack i, @NotNull ItemStack j, int orderI, int orderJ) {
        return compareItems(i, j, orderI, orderJ, false);
    }

    int compareItems(@NotNull ItemStack i, @NotNull ItemStack j, int orderI, int orderJ, boolean api) {
        if(i.isEmpty() && j.isEmpty()) {
            //Technically, if both are empty, they are equal.
            if(debugTree) { mostRecentComparison = "Both stacks are Empty."; }
            return 0;
        } else if(j.isEmpty()) {
            if(debugTree) { mostRecentComparison = "J is Empty."; }
            return -1;
        } else if(i.isEmpty() || orderI == -1) {
            if(debugTree) { mostRecentComparison = "I is Empty or orderI was -1."; }
            return 1;
        } else {
            if(debugTree) { mostRecentComparison = ""; }
            if(api) {
                if(debugTree) { mostRecentComparison = "API Active, "; }
                int lastOrder = cfgManager.getConfig().getTree().getLastTreeOrder();
                if(orderI > lastOrder) { orderI = Integer.MAX_VALUE; }
                if(orderJ > lastOrder) { orderJ = Integer.MAX_VALUE; }
            }

            if(debugTree) { mostRecentComparison += "I: " + orderI + ", J: " + orderJ; }

            //If items are in different order slots, they are inherently comparator contract friendly.
            if(orderI != orderJ) {
                if(debugTree) { mostRecentComparison += ", Normal: " + (orderI - orderJ); }
                return orderI - orderJ;
            }

            //All items in the same sort slot need to be treated the same for the comparator contract.

            //Allow external sorting systems to take control of unsorted items not handled by the tree.
            if(orderI == Integer.MAX_VALUE && orderJ == Integer.MAX_VALUE && api == true) {
                if(debugTree) { mostRecentComparison += ", API Bailout."; }
                return 0;
            }

            Item iItem = i.getItem(), jItem = j.getItem();
            //Sort By Tool type then Harvest Level, (Better first.)
            int cTool = compareTools(i, j, iItem, jItem);
            if(debugTree) { mostRecentComparison += ", Tool: " + cTool; }
            if(cTool != 0) { return cTool; }

            //Sort by main-hand damage capability:  (Higher first, faster first for same damage)
            //Most tools also do damage, so they were tested as tools first.
            //If a tool reaches here, it has the same max durabilty, harvest level, and tool class.
            int cSword = compareSword(i, j, iItem, jItem);
            if(debugTree) { mostRecentComparison += ", Sword: " + cSword; }
            if(cSword != 0) { return cSword; }

            //Sort By Armor utility:  (More First)
            int cArmor = compareArmor(i, j, iItem, jItem);
            if(debugTree) { mostRecentComparison += ", Armor: " + cArmor; }
            if(cArmor != 0) { return cArmor; }

            //Sort my display name:
            int cName = compareNames(i, j);
            if(debugTree) { mostRecentComparison += ", Name" + cName; }
            if(cName != 0) { return cName; }

            //Sort By enchantments:
            int cEnchant = compareEnchantment(i, j);
            if(cEnchant != 0) { return cEnchant; }

            //Use durability to sort, favoring more durable items.  (Non-Tools, Non-Armor, Non-Weapons.)
            int maxDamage = compareMaxDamage(i, j);
            if(debugTree) { mostRecentComparison += ", Max Damage: " + maxDamage; }
            if(maxDamage != 0) { return maxDamage; }

            //Use remaining durability to sort, favoring config option on damaged.
            int curDamage = compareCurDamage(i, j);
            if(debugTree) { mostRecentComparison += ", Current Damage: " + curDamage; }
            if(curDamage != 0) { return curDamage; }

            //Use stack size to put bigger stacks first.
            if(j.getCount() != i.getCount()) {
                if(debugTree) { mostRecentComparison += ", Stack Size"; }
                return j.getCount() - i.getCount();
            }

            //Final catch all:
            if(debugTree) {
                mostRecentComparison += ", Final: " + ObjectUtils.compare(i.getItem().getRegistryName().toString(), j.getItem().getRegistryName().toString());
            }
            // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
            return ObjectUtils.compare(i.getItem().getRegistryName().toString(), j.getItem().getRegistryName().toString());

        }
    }

    private int compareNames(ItemStack i, ItemStack j) {
        boolean iHasName = i.hasCustomHoverName();
        boolean jHasName = j.hasCustomHoverName();
        Component iDisplayName = i.getDisplayName();
        Component jDisplayName = j.getDisplayName();

        //Custom named items come first.
        if(iHasName || jHasName) {
            if(!iHasName) {
                if(debugTree) { mostRecentComparison += ", J has custom Name"; }
                return -1;
            } else if(!jHasName) {
                if(debugTree) { mostRecentComparison += ", I has custom Name"; }
                return 1;
            }
        }
        //Differently named items (either both custom or both default, like bees or resource chickens.)
        if(!iDisplayName.equals(jDisplayName)) {
            if(debugTree) { mostRecentComparison += ", Name: " + iDisplayName.getContents().compareTo(jDisplayName.getContents()); }
            return iDisplayName.getContents().compareTo(jDisplayName.getContents());
        }

        return 0;
    }

    private int compareTools(ItemStack i, ItemStack j, Item iItem, Item jItem) {
        String toolClass1 = getToolClass(i, iItem);
        String toolClass2 = getToolClass(j, jItem);

        if(debugTree) { mostRecentComparison += ", ToolClass (" + toolClass1 + ", " + toolClass2 + ")"; }
        boolean isTool1 = toolClass1 != "";
        boolean isTool2 = toolClass2 != "";
        if(!isTool1 || !isTool2) {
            //This should catch any instances where one of the stacks is null.
            return Boolean.compare(isTool2, isTool1);
        } else {
            int toolClassComparison = toolClass1.compareTo(toolClass2);
            if(toolClassComparison != 0) {
                return toolClassComparison;
            }
            // If they were the same type, sort with the better harvest level first.
            int harvestLevel1 = 0;//TODO: Fix when tools classses work iItem.getHarvestLevel(i, toolClass1, null, null);
            int harvestLevel2 = 0;//jItem.getHarvestLevel(j, toolClass2, null, null);
            int toolLevelComparison = harvestLevel2 - harvestLevel1;
            if(debugTree) { mostRecentComparison += ", HarvestLevel (" + harvestLevel1 + ", " + harvestLevel2 + ")"; }
            if(toolLevelComparison != 0) {
                return Integer.compare(harvestLevel2, harvestLevel1);
            }
        }

        return compareMaxDamage(i, j);

    }

    private int compareSword(ItemStack itemStack1, ItemStack itemStack2, Item iItem, Item jItem) {
        Multimap<Attribute, AttributeModifier> multimap1 = itemStack1 != null ? itemStack1.getAttributeModifiers(EquipmentSlot.MAINHAND) : null;
        Multimap<Attribute, AttributeModifier> multimap2 = itemStack2 != null ? itemStack2.getAttributeModifiers(EquipmentSlot.MAINHAND) : null;

        final Attribute attackDamageName = Attributes.ATTACK_DAMAGE;
        final Attribute attackSpeedName = Attributes.ATTACK_SPEED;

        boolean hasDamage1 = itemStack1 != null && multimap1.containsKey(attackDamageName);
        boolean hasDamage2 = itemStack2 != null && multimap2.containsKey(attackDamageName);
        boolean hasSpeed1 = itemStack1 != null && multimap1.containsKey(attackSpeedName);
        boolean hasSpeed2 = itemStack2 != null && multimap2.containsKey(attackSpeedName);

        if(debugTree) { mostRecentComparison += ", HasDamage (" + hasDamage1 + ", " + hasDamage2 + ")"; }

        if(!hasDamage1 || !hasDamage2) {
            return Boolean.compare(hasDamage2, hasDamage1);
        } else {
            Collection<AttributeModifier> damageMap1 = multimap1.get(attackDamageName);
            Collection<AttributeModifier> damageMap2 = multimap2.get(attackDamageName);
            Double attackDamage1 = ((AttributeModifier) damageMap1.toArray()[0]).getAmount();
            Double attackDamage2 = ((AttributeModifier) damageMap2.toArray()[0]).getAmount();
            // This funny comparison is because Double == Double never seems to work.
            int damageComparison = Double.compare(attackDamage2, attackDamage1);
            if(damageComparison == 0 && hasSpeed1 && hasSpeed2) {
                // Same damage, sort faster weapon first.
                Collection<AttributeModifier> speedMap1 = multimap1.get(attackSpeedName);
                Collection<AttributeModifier> speedMap2 = multimap2.get(attackSpeedName);
                Double speed1 = ((AttributeModifier) speedMap1.toArray()[0]).getAmount();
                Double speed2 = ((AttributeModifier) speedMap2.toArray()[0]).getAmount();
                int speedComparison = Double.compare(speed2, speed1);
                if(speedComparison != 0) { return speedComparison; }

            } else if(damageComparison != 0) {
                // Higher damage first.
                return damageComparison;
            }
            return compareMaxDamage(itemStack1, itemStack2);
        }
    }

    private int compareArmor(ItemStack i, ItemStack j, Item iItem, Item jItem) {
        int isArmor1 = (iItem instanceof ArmorItem) ? 1 : 0;
        int isArmor2 = (jItem instanceof ArmorItem) ? 1 : 0;
        if(isArmor1 == 0 || isArmor2 == 0) {
            //This should catch any instances where one of the stacks is null.
            return isArmor2 - isArmor1;
        } else {
            ArmorItem a1 = (ArmorItem) iItem;
            ArmorItem a2 = (ArmorItem) jItem;
            if(a1.getSlot() != a2.getSlot()) {
                return a2.getSlot().compareTo(a1.getSlot());
            } else if(a1.getDefense() != a2.getDefense()) {
                return a2.getDefense() - a1.getDefense();
            } else if(a1.getToughness() != a2.getToughness()) {
                return a2.getToughness() > a1.getToughness() ? -1 : 1;
            }
            return compareMaxDamage(i, j);
        }
    }

    private int compareEnchantment(ItemStack i, ItemStack j) {
        @NotNull Map<Enchantment, Integer> iEnchs = EnchantmentHelper.getEnchantments(i);
        @NotNull Map<Enchantment, Integer> jEnchs = EnchantmentHelper.getEnchantments(j);

        //Pick the item with the most enchantments first.
        if(iEnchs.size() != jEnchs.size()) {
            if(debugTree) { mostRecentComparison += ", Enchantment Count"; }
            return jEnchs.size() - iEnchs.size();
        }

        String iEnchMaxId = "";
        int iEnchMaxLvl = 0;
        String jEnchMaxId = "";
        int jEnchMaxLvl = 0;

        // TODO: This is really arbitrary but there's not really a good way to do this generically.
        for(@NotNull Map.Entry<Enchantment, Integer> ench : iEnchs.entrySet()) {
            String enchId = ench.getKey().getRegistryName().toString();
            if(ench.getValue() > iEnchMaxLvl) {
                iEnchMaxId = enchId;
                iEnchMaxLvl = ench.getValue();
            } else if(ench.getValue() == iEnchMaxLvl && enchId.compareTo(iEnchMaxId) > 0) {
                iEnchMaxId = enchId;
            }
        }

        for(@NotNull Map.Entry<Enchantment, Integer> ench : jEnchs.entrySet()) {
            String enchId = ench.getKey().getRegistryName().toString();
            if(ench.getValue() > jEnchMaxLvl) {
                jEnchMaxId = enchId;
                jEnchMaxLvl = ench.getValue();
            } else if(ench.getValue() == jEnchMaxLvl && enchId.compareTo(jEnchMaxId) > 0) {
                jEnchMaxId = enchId;
            }
        }

        //The highest enchantment ID, (random actual enchantment.)
        if(!iEnchMaxId.equals(jEnchMaxId)) {
            if(debugTree) { mostRecentComparison += ", Highest Enchantment"; }
            return jEnchMaxId.compareTo(iEnchMaxId);
        }

        //Highest level if they both have the same coolest enchantment.
        if(iEnchMaxLvl != jEnchMaxLvl) {
            if(debugTree) { mostRecentComparison += ", Highest Enchantment Level"; }
            return jEnchMaxLvl - iEnchMaxLvl;
        }

        //Enchantments aren't different.
        if(debugTree) { mostRecentComparison += ", Enchantment Level same"; }
        return 0;
    }

    public void setItemPickupPending(boolean value) {
        itemPickupPending = value;
        itemPickupTimeout = 5;
    }

    public void setSortKeyEnabled(boolean enabled) {
        sortKeyEnabled = enabled;
    }

    public void setTextboxMode(boolean enabled) {
        textboxMode = enabled;
    }

    public void logInGame(@NotNull String message) {
        logInGame(message, false);
    }

    public void printQueuedMessages() {
        if(mc.screen != null && !queuedMessages.isEmpty()) {
            queuedMessages.forEach(this::addChatMessage);
            queuedMessages.clear();
        }
    }

    public void logInGame(@NotNull String message, boolean alreadyTranslated) {
        @NotNull String formattedMsg = buildLogString(Level.INFO, (alreadyTranslated) ? message : I18n.get(message));

        if(mc.screen == null) {
            queuedMessages.add(formattedMsg);
        } else {
            addChatMessage(formattedMsg);
        }

        InvTweaksMod.log.info(formattedMsg);
    }

    public void logInGameError(@NotNull String message, @NotNull Exception e) {
        @NotNull String formattedMsg = buildLogString(Level.SEVERE, I18n.get(message), e);
        InvTweaksMod.log.error(formattedMsg, e);

        if(mc.screen == null) {
            queuedMessages.add(formattedMsg);
        } else {
            addChatMessage(formattedMsg);
        }
    }

    private boolean onTick() {
        printQueuedMessages();

        tickNumber++;

        if(mc.player.isSpectator()) {
            return false;
        }

        // Not calling "cfgManager.makeSureConfigurationIsLoaded()" for performance reasons
        @Nullable InvTweaksConfig config = cfgManager.getConfig();
        if(config == null) {
            return false;
        }

        // Clone the hotbar to be able to monitor changes on it
        if(itemPickupPending) {
            onItemPickup();
        }
        @Nullable Screen currentScreen = getCurrentScreen();
        if(currentScreen == null || isGuiInventory(currentScreen)) {
            cloneHotbar();
        }

        // Handle sort key
        if(isSortingShortcutDown()) {
            if(!sortKeyDown) {
                sortKeyDown = true;
                onSortingKeyPressed();
            }
        } else {
            sortKeyDown = false;
        }

        // Handle config switch
        //handleConfigSwitch();

        return true;

    }

    /*private void handleConfigSwitch() {

        @Nullable InvTweaksConfig config = cfgManager.getConfig();
        @Nullable Screen currentScreen = getCurrentScreen();

        // Switch between configurations (shortcut)
        cfgManager.getShortcutsHandler().updatePressedKeys();
        @Nullable InvTweaksShortcutMapping switchMapping = cfgManager.getShortcutsHandler().isShortcutDown(InvTweaksShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT);
        if(isSortingShortcutDown() && switchMapping != null) {
            @Nullable String newRuleset = null;
            int pressedKey = switchMapping.getKeyCodes().get(0);
            if(pressedKey >= Keyboard.KEY_1 && pressedKey <= Keyboard.KEY_9) {
                newRuleset = config.switchConfig(pressedKey - Keyboard.KEY_1);
            } else {
                switch(pressedKey) {
                    case Keyboard.KEY_NUMPAD1:
                        newRuleset = config.switchConfig(0);
                        break;
                    case Keyboard.KEY_NUMPAD2:
                        newRuleset = config.switchConfig(1);
                        break;
                    case Keyboard.KEY_NUMPAD3:
                        newRuleset = config.switchConfig(2);
                        break;
                    case Keyboard.KEY_NUMPAD4:
                        newRuleset = config.switchConfig(3);
                        break;
                    case Keyboard.KEY_NUMPAD5:
                        newRuleset = config.switchConfig(4);
                        break;
                    case Keyboard.KEY_NUMPAD6:
                        newRuleset = config.switchConfig(5);
                        break;
                    case Keyboard.KEY_NUMPAD7:
                        newRuleset = config.switchConfig(6);
                        break;
                    case Keyboard.KEY_NUMPAD8:
                        newRuleset = config.switchConfig(7);
                        break;
                    case Keyboard.KEY_NUMPAD9:
                        newRuleset = config.switchConfig(8);
                        break;
                }
            }

            if(newRuleset != null) {
                logInGame(String.format(I18n.format("invtweaks.loadconfig.enabled"), newRuleset), true);
                // Hack to prevent 2nd way to switch configs from being enabled
                sortingKeyPressedDate = Integer.MAX_VALUE;
            }
        }

        // Switch between configurations (by holding the sorting key)
        if(isSortingShortcutDown()) {
            long currentTime = System.currentTimeMillis();
            if(sortingKeyPressedDate == 0) {
                sortingKeyPressedDate = currentTime;
            } else if(currentTime - sortingKeyPressedDate > InvTweaksConst.RULESET_SWAP_DELAY && sortingKeyPressedDate != Integer.MAX_VALUE) {
                @Nullable String previousRuleset = config.getCurrentRulesetName();
                @Nullable String newRuleset = config.switchConfig();
                // Log only if there is more than 1 ruleset
                if(previousRuleset != null && newRuleset != null && !previousRuleset.equals(newRuleset)) {
                    logInGame(String.format(I18n.format("invtweaks.loadconfig.enabled"), newRuleset), true);
                    handleSorting(currentScreen);
                }
                sortingKeyPressedDate = currentTime;
            }
        } else {
            sortingKeyPressedDate = 0;
        }

    }*/

    private String ListOfClassNameKind(Object o) {
        String resString = "";
        Class testClass = o.getClass();
        while(testClass != null) {
            resString += testClass.getName().toLowerCase();
            //The secret sauce:
            testClass = testClass.getSuperclass();
            if(testClass != null) { resString += ", "; }
        }
        return resString;
    }

    @SuppressWarnings("unused")
    private void handleSorting(Screen guiScreen) {
        getInventoryPlayer().ifPresent(mainInventory->{
            @NotNull ItemStack selectedItem;
            selectedItem = getThePlayer().getInventory().getSelected();

            if(debugTree && selectedItem != null && !selectedItem.isEmpty()) {
                logInGame("Hand Item Details:", true);
                logInGame(selectedItem.toString(), true);
                logInGame("Classes: " + ListOfClassNameKind(selectedItem.getItem()));
                logInGame("Item Order Index: " + getItemOrder(selectedItem), true);
                @NotNull ItemStack offhandStack = getOffhandStack();
                if(offhandStack != null && !offhandStack.isEmpty()) {
                    logInGame("Off-Hand Item Details:", true);
                    logInGame(offhandStack.toString(), true);
                    logInGame("Item Order Index: " + getItemOrder(offhandStack), true);
                    logInGame("Comparator result: " + compareItems(selectedItem, offhandStack), true);
                    logInGame("Comparator debug: " + mostRecentComparison, true);
                }
            }

            // Sorting
            try {
                new InvTweaksHandlerSorting(cfgManager.getConfig(), ContainerSection.INVENTORY, SortingMethod.INVENTORY, InvTweaksConst.INVENTORY_ROW_SIZE).sort();
            } catch(Exception e) {
                logInGameError("invtweaks.sort.inventory.error", e);
                e.printStackTrace();
            }

            playClick();
        });
    }

    private void handleAutoRefill() {
        @NotNull ItemStack currentStack = getFocusedStack();
        @NotNull ItemStack offhandStack = getOffhandStack();

        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
        @Nullable String currentStackId = (currentStack.isEmpty()) ? null : currentStack.getItem().getRegistryName().toString();
        int focusedSlot = getThePlayer().getInventory().selected + 27; // Convert to container slots index
        CompoundTag currentStackDamage = (currentStack.isEmpty()) ? null : currentStack.getTag();
        @Nullable InvTweaksConfig config = cfgManager.getConfig();

        if(storedFocusedSlot != focusedSlot) { // Filter selection change
            storedFocusedSlot = focusedSlot;
        }else if(!ItemStack.isSame(currentStack, storedStack) && storedStackId != null) {
            if(!storedStack.isEmpty() && !ItemStack.isSameItemSameTags(offhandStack, storedStack)) { // Checks not switched to offhand
                if(currentStack.isEmpty() || (currentStack.getItem() == Items.BOWL && Objects.equals(storedStackId, "minecraft:mushroom_stew"))
                        // Handle eaten mushroom soup
                        && (getCurrentScreen() == null || // Filter open inventory or other window
                        isGuiEditSign(getCurrentScreen()))) { // TODO: This should be more expandable on 'equivalent' items (API?) and allowed GUIs

                    if(config.isAutoRefillEnabled(storedStackId, storedTag)) {
                        try {
                            InvTweaksMod.log.info("{} - {}", storedStackId, storedTag);
                            cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, storedStackId, storedTag);
                        } catch(Exception e) {
                            logInGameError("invtweaks.sort.autorefill.error", e);
                        }
                    }
                } else {
                    // Item
                    int itemMaxDamage = currentStack.getMaxDamage();
                    int autoRefillThreshhold = config.getIntProperty(InvTweaksConfig.PROP_AUTO_REFILL_DAMAGE_THRESHHOLD);
                    if(canToolBeReplaced(currentStack.getDamageValue(), itemMaxDamage, autoRefillThreshhold) && config.getProperty(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK).equals(InvTweaksConfig.VALUE_TRUE) && config.isAutoRefillEnabled(storedStackId, storedTag)) {
                        // Trigger auto-refill before the tool breaks
                        try {
                            cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, storedStackId, storedTag);
                        } catch(Exception e) {
                            logInGameError("invtweaks.sort.autorefill.error", e);
                        }
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

    private void handleMiddleClick(Screen guiScreen) {
        if(InputEventHandler.isMiddleDown()) {

            if(!cfgManager.makeSureConfigurationIsLoaded()) {
                return;
            }
            @Nullable InvTweaksConfig config = cfgManager.getConfig();

            // Check that middle click sorting is allowed
            if(config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK).equals(InvTweaksConfig.VALUE_TRUE) && isGuiContainer(guiScreen)) {

                @NotNull AbstractContainerScreen<?> guiContainer = (AbstractContainerScreen<?>) guiScreen;
                AbstractContainerMenu container = guiContainer.getMenu();

                if(!chestAlgorithmButtonDown) {
                    chestAlgorithmButtonDown = true;

                    @NotNull IContainerManager containerMgr = getContainerManager(container);
                    Slot slotAtMousePosition = InvTweaksObfuscation.getSlotAtMousePosition((AbstractContainerScreen<?>) getCurrentScreen());
                    @Nullable ContainerSection target = null;
                    if(slotAtMousePosition != null) {
                        target = containerMgr.getSlotSection(getSlotNumber(slotAtMousePosition));
                    }

                    if(isValidChest(container)) {

                        // Check if the middle click target the chest or the inventory
                        // (copied GuiContainer.getSlotAtPosition algorithm)

                        if(ContainerSection.CHEST.equals(target)) {

                            // Play click
                            playClick();

                            long timestamp = System.currentTimeMillis();
                            if(timestamp - chestAlgorithmClickTimestamp > InvTweaksConst.CHEST_ALGORITHM_SWAP_MAX_INTERVAL || getContainerRowSize(guiContainer) > 9) {
                                chestAlgorithm = SortingMethod.DEFAULT;
                            }
                            try {
                                new InvTweaksHandlerSorting(cfgManager.getConfig(), ContainerSection.CHEST, chestAlgorithm, getContainerRowSize(guiContainer)).sort();
                            } catch(Exception e) {
                                logInGameError("invtweaks.sort.chest.error", e);
                                e.printStackTrace();
                            }
                            // TODO: Better replacement for this.
                            chestAlgorithm = SortingMethod.values()[(chestAlgorithm.ordinal() + 1) % 3];
                            chestAlgorithmClickTimestamp = timestamp;

                        } else if(ContainerSection.CRAFTING_IN.equals(target) || ContainerSection.CRAFTING_IN_PERSISTENT.equals(target)) {
                            try {
                                new InvTweaksHandlerSorting(cfgManager.getConfig(), target, SortingMethod.EVEN_STACKS, (containerMgr.getSize(target) == 9) ? 3 : 2).sort();
                            } catch(Exception e) {
                                logInGameError("invtweaks.sort.crafting.error", e);
                                e.printStackTrace();
                            }

                        } else if(ContainerSection.INVENTORY_HOTBAR.equals(target) || (ContainerSection.INVENTORY_NOT_HOTBAR.equals(target))) {
                            handleSorting(guiScreen);
                        }

                    } else if(isValidInventory(container)) {
                        if(ContainerSection.CRAFTING_IN.equals(target) || ContainerSection.CRAFTING_IN_PERSISTENT.equals(target)) {
                            // Crafting stacks evening
                            try {
                                new InvTweaksHandlerSorting(cfgManager.getConfig(), target, SortingMethod.EVEN_STACKS, (containerMgr.getSize(target) == 9) ? 3 : 2).sort();
                            } catch(Exception e) {
                                logInGameError("invtweaks.sort.crafting.error", e);
                                e.printStackTrace();
                            }
                        } else {
                            // Sorting
                            handleSorting(guiScreen);
                        }
                    }
                }
            }
        } else {
            chestAlgorithmButtonDown = false;
        }
    }


    // NOTE: This *will* only work for vanilla GUIs. Blame Mojang for making it next to impossible to find out generically.
    private boolean hasRecipeButton(@NotNull AbstractContainerScreen<?> guiContainer) {
        if(guiContainer instanceof InventoryScreen) {
            return true;
        } else return guiContainer instanceof CraftingScreen;
    }

    // See note above
    private boolean isRecipeBookVisible(@NotNull AbstractContainerScreen<?> guiContainer) {
        if(guiContainer instanceof InventoryScreen) {
            return ((InventoryScreen) guiContainer).getRecipeBookComponent().isVisible();
        } else if(guiContainer instanceof CraftingScreen) {
            return ((CraftingScreen) guiContainer).getRecipeBookComponent().isVisible();
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public void onInitGuiEvent(@NotNull ScreenEvent.InitScreenEvent.Post initScreenEvent) {
        if(!(initScreenEvent.getScreen() instanceof AbstractContainerScreen<?> containerScreen)){
            return;
        }
        @Nullable InvTweaksConfig config = cfgManager.getConfig();
        AbstractContainerMenu container = containerScreen.getMenu();

        boolean isValidChest = isValidChest(container);

        if(showButtons(container)) {
            int w = 10, h = 10;

            // Re-layout when NEI/JEI changes states.
            final boolean isItemListVisible = itemListChecker.isVisible();
            final boolean wasItemListVisible = itemListChecker.wasVisible();
            final boolean isRecipeBookVisible = isRecipeBookVisible(containerScreen);
            final boolean wasRecipeBookVisible = previousRecipeBookVisibility;
            final boolean relayout = (isItemListVisible != wasItemListVisible) || (isRecipeBookVisible != wasRecipeBookVisible);
            previousRecipeBookVisibility = isRecipeBookVisible;

            // Look for the mods buttons
            boolean customButtonsAdded = false;

            if(!customButtonsAdded) {

                // Check for custom button texture
                boolean customTextureAvailable = hasTexture(new ResourceLocation("inventorytweaks", "textures/gui/button10px.png"));

                int x = containerScreen.getGuiLeft() + containerScreen.getXSize() - 16, y = containerScreen.getGuiTop() + 5;
                // Inventory button
                if(!isValidChest) {
                    /*if(hasRecipeButton(guiContainer)) {
                        x -= 20;
                    }*/
                    initScreenEvent.addListener(new InvTweaksGuiSettingsButton(cfgManager, x, y, w, h, new TextComponent("..."), I18n.get("invtweaks.button.settings.tooltip"), customTextureAvailable, btn -> InvTweaks.super.displayGuiScreen(new InvTweaksGuiSettings(containerScreen))));
                }

                // Chest buttons
                else {
                    // Reset sorting algorithm selector
                    chestAlgorithmClickTimestamp = 0;

                    boolean isChestWayTooBig = isLargeChest(container);

                    // NotEnoughItems/JustEnoughItems compatibility
                    if(isChestWayTooBig && isItemListVisible) {
                        x -= 20;
                        y += 50;
                    }/* else if(hasRecipeButton(guiContainer)) {
                        x -= 20;
                    }*/

                    // Settings button
                    initScreenEvent.addListener(new InvTweaksGuiSettingsButton(cfgManager, (isChestWayTooBig) ? x + 22 : x - 1, (isChestWayTooBig) ? y - 3 : y, w, h, new TextComponent("..."), I18n.get("invtweaks.button.settings.tooltip"), customTextureAvailable, btn -> InvTweaks.super.displayGuiScreen(new InvTweaksGuiSettings(containerScreen))));

                    // Sorting buttons
                    if(!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals("false")) {
                        int rowSize = getContainerRowSize(containerScreen);
                        @NotNull Button button = new InvTweaksGuiSortingButton(cfgManager, (isChestWayTooBig) ? x + 22 : x - 37, (isChestWayTooBig) ? y + 38 : y, w, h, new TextComponent("s"), I18n.get("invtweaks.button.chest1.tooltip"), SortingMethod.DEFAULT, rowSize, customTextureAvailable, new Button.OnPress() {
                            @Override
                            public void onPress(Button p_93751_) {
                                logInGame("Pressed first button", true);
                            }
                        });
                        initScreenEvent.addListener(button);

                        if(rowSize <= 9) {
                            button = new InvTweaksGuiSortingButton(cfgManager, (isChestWayTooBig) ? x + 22 : x - 13, (isChestWayTooBig) ? y + 12 : y, w, h, new TextComponent("h"), I18n.get("invtweaks.button.chest3.tooltip"), SortingMethod.HORIZONTAL, rowSize, customTextureAvailable, new Button.OnPress() {
                                @Override
                                public void onPress(Button p_93751_) {
                                    logInGame("Pressed second button", true);
                                }
                            });
                            initScreenEvent.addListener(button);

                            //noinspection UnusedAssignment (Using ++ for extensibility)
                            button = new InvTweaksGuiSortingButton(cfgManager, (isChestWayTooBig) ? x + 22 : x - 25, (isChestWayTooBig) ? y + 25 : y, w, h, new TextComponent("v"), I18n.get("invtweaks.button.chest2.tooltip"), SortingMethod.VERTICAL, rowSize, customTextureAvailable, new Button.OnPress() {
                                @Override
                                public void onPress(Button p_93751_) {
                                    logInGame("Pressed third button", true);
                                }
                            });
                            initScreenEvent.addListener(button);
                        }

                    }
                }
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onGuiInit(ScreenEvent.InitScreenEvent.InitScreenEvent event) {

    }

    private void handleShortcuts(@NotNull AbstractContainerScreen<?> guiScreen) {
        // Check open GUI
        if(!(isValidChest(guiScreen.getMenu()) || isValidInventory(guiScreen.getMenu()))) {
            return;
        }

        // Configurable shortcuts
        if(InputEventHandler.isLeftDown() || InputEventHandler.isRightDown()) {
            if(!mouseWasDown) {
                mouseWasDown = true;

                // The mouse has just been clicked,
                // trigger a shortcut according to the pressed keys.
                if(cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS).equals("true")) {
                    cfgManager.getShortcutsHandler().handleShortcut();
                }

            }
        } else {
            mouseWasDown = false;
        }

    }

    private int getItemOrder(@NotNull ItemStack itemStack) {
        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
        List<IItemTreeItem> items = cfgManager.getConfig().getTree().getItems(itemStack.getItem().getRegistryName().toString(), itemStack.getTag());
        return (items.size() > 0) ? items.get(0).getOrder() : Integer.MAX_VALUE;
    }

    private boolean isSortingShortcutDown() {
        if(sortKeyEnabled && !textboxMode) {
            return InputEventHandler.isKeyDown(ClientProxy.KEYBINDING_SORT);
        } else {
            return false;
        }
    }

    private boolean isTimeForPolling() {
        if(tickNumber - lastPollingTickNumber >= InvTweaksConst.POLLING_DELAY) {
            lastPollingTickNumber = tickNumber;
        }
        return tickNumber - lastPollingTickNumber == 0;
    }

    /**
     * When Minecraft gains focus, reset all pressed keys to avoid the "stuck keys" bug.
     */
    private void unlockKeysIfNecessary() {
        return;
        /* TODO: is necessary?
        boolean hasFocus = Display.isActive();
        if(!hadFocus && hasFocus) {
            Keyboard.destroy();
            boolean firstTry = true;
            while(!Keyboard.isCreated()) {
                try {
                    Keyboard.create();
                } catch(Exception e) {
                    if(firstTry) {
                        logInGameError("invtweaks.keyboardfix.error", e);
                        firstTry = false;
                    }
                }
            }
            if(!firstTry) {
                logInGame("invtweaks.keyboardfix.recover");
            }
        }
        hadFocus = hasFocus;*/
    }

    /**
     * Allows to maintain a clone of the hotbar contents to track changes (especially needed by the "on pickup"
     * features).
     */
    private void cloneHotbar() {
        NonNullList<ItemStack> mainInventory = getThePlayer().getInventory().items;
        for(int i = 0; i < 9; i++) {
            hotbarClone[i] = mainInventory.get(i).copy();
        }
    }

    private void playClick() {
        if(!cfgManager.getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SOUNDS).equals(InvTweaksConfig.VALUE_FALSE)) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

}
