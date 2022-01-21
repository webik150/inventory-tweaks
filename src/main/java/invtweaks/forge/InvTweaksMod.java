package invtweaks.forge;

import invtweaks.api.IItemTreeListener;
import invtweaks.api.InvTweaksAPI;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import invtweaks.gui.InvTweaksGuiSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

import static net.minecraftforge.network.NetworkConstants.IGNORESERVERONLY;

/**
 * ModLoader entry point to load and configure the mod.
 *
 * @author Jimeo Wan
 * <p>
 * Contact: jimeo.wan (at) gmail (dot) com Website: <a href="https://inventory-tweaks.readthedocs.org/">https://inventory-tweaks.readthedocs.org/</a>
 * Source code: <a href="https://github.com/kobata/inventory-tweaks">GitHub</a> License: MIT
 */
@Mod(InvTweaksMod.MOD_ID)
public class InvTweaksMod implements InvTweaksAPI {
    public static final String MOD_ID = "inventorytweaks";
    public static InvTweaksMod instance;

    public static CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public InvTweaksMod() {
        instance = this;
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        // Register the enqueueIMC method for modloading
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);

        //ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IGNORESERVERONLY, (a, b) -> true));

        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new InvTweaksGuiSettings(screen)));
    }

    // Helper for ASM transform of GuiTextField to disable sorting on focus.
    @SuppressWarnings("unused")
    public static void setTextboxModeStatic(boolean enabled) {
        instance.setTextboxMode(enabled);
    }

    @SuppressWarnings("unused")
    public void commonSetup(FMLCommonSetupEvent e) {
        proxy.commonSetup(e);
    }

    @SuppressWarnings("unused")
    public void postInit(final InterModProcessEvent e) {
        proxy.postInit(e);
    }

    @Override
    public void addOnLoadListener(IItemTreeListener listener) {
        proxy.addOnLoadListener(listener);
    }

    @Override
    public boolean removeOnLoadListener(IItemTreeListener listener) {
        return proxy.removeOnLoadListener(listener);
    }

    @Override
    public void setSortKeyEnabled(boolean enabled) {
        proxy.setSortKeyEnabled(enabled);
    }

    @Override
    public void setTextboxMode(boolean enabled) {
        proxy.setTextboxMode(enabled);
    }

    @Override
    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j) {
        return proxy.compareItems(i, j);
    }

    @Override
    public int compareItems(@NotNull ItemStack i, @NotNull ItemStack j, boolean onlyTreeSort) {
        return proxy.compareItems(i, j, onlyTreeSort);
    }

    @Override
    public void sort(ContainerSection section, SortingMethod method) {
        proxy.sort(section, method);
    }
}
