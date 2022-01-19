package invtweaks.forge;

import invtweaks.InvTweaksGuiSettings;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
/**
 * Todo: figure out what to do with this lol.
 */
public class ModGuiFactory {

    public ModGuiFactory(Object o, Object o1) {

    }

    @NotNull
    public Screen createConfigGui(Screen parentScreen) {
        // TODO: Find out if we can just cache this?
        return new InvTweaksGuiSettings(parentScreen);
    }

}
