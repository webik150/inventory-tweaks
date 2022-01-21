package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.File;

public class InvTweaksConst {

    // Mod version
    public static final String MOD_VERSION = "@VERSION@";

    // Mod tree version
    // Change only when the tree evolves significantly enough to need to override all configs
    public static final String TREE_VERSION = "1.12.2";

    public static final String INVTWEAKS_CHANNEL = "main";

    // Network Protocol version
    public static final String PROTOCOL_VERSION = "2";
    public static final byte PACKET_LOGIN = 0x00;
    public static final byte PACKET_CLICK = 0x01;
    public static final byte PACKET_SORTCOMPLETE = 0x02;

    // Timing constants
    public static final int RULESET_SWAP_DELAY = 1000;
    public static final int POLLING_DELAY = 3;
    public static final int CHEST_ALGORITHM_SWAP_MAX_INTERVAL = 2000;
    public static final int TOOLTIP_DELAY = 800;

    // File constants
    public static final File MINECRAFT_DIR = Minecraft.getInstance().gameDirectory;
    public static final File MINECRAFT_CONFIG_DIR = new File(MINECRAFT_DIR, "config/");
    public static final File INVTWEAKS_CONFIG_DIR = new File(MINECRAFT_CONFIG_DIR, "InvTweaks/");
    public static final File INVTWEAKS_TREES_DIR = new File(INVTWEAKS_CONFIG_DIR, "trees/");
    public static final File CONFIG_PROPS_FILE = new File(INVTWEAKS_CONFIG_DIR, "InvTweaks.cfg");
    public static final File CONFIG_RULES_FILE = new File(INVTWEAKS_CONFIG_DIR, "InvTweaksRules.txt");
    public static final File CONFIG_TREE_FILE = new File(INVTWEAKS_CONFIG_DIR, "InvTweaksTree.txt");
    public static final File OLD_CONFIG_TREE_FILE = new File(MINECRAFT_CONFIG_DIR, "InvTweaksTree.txt");
    public static final File OLDER_CONFIG_TREE_FILE = new File(MINECRAFT_CONFIG_DIR, "InvTweaksTree.xml");
    public static final File OLDER_CONFIG_RULES_FILE = new File(MINECRAFT_DIR, "InvTweaksRules.txt");

    public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
    public static final File MERGED_TREE_FILE = new File(TEMP_DIR, "InvTweaksTree.txt");
    public static final File MERGED_TREE_FILE_ALT = new File(INVTWEAKS_TREES_DIR, "InvTweaksTree.txt");

    public static final String INVTWEAKS_RESOURCE_DOMAIN = "inventorytweaks";
    public static final ResourceLocation DEFAULT_CONFIG_FILE = new ResourceLocation(INVTWEAKS_RESOURCE_DOMAIN, "defaultconfig.dat");
    public static final ResourceLocation DEFAULT_CONFIG_TREE_FILE = new ResourceLocation(INVTWEAKS_RESOURCE_DOMAIN, "itemtree.xml");

    public static final String HELP_URL = "http://inventory-tweaks.readthedocs.org";
    public static final String TREE_URL = "https://github.com/IMarvinTPA/InventoryTweaksTrees";

    // Global mod constants
    public static final String INGAME_LOG_PREFIX = "InvTweaks: ";
    public static final Level DEBUG = Level.INFO;
    public static final int JIMEOWAN_ID = 54696386; // Used in GUIs

    // Minecraft constants
    public static final int INVENTORY_SIZE = 36;
    public static final int INVENTORY_ROW_SIZE = 9;
    public static final int HOTBAR_SIZE = 9;
    public static final int INVENTORY_HOTBAR_SIZE = INVENTORY_ROW_SIZE;
}
