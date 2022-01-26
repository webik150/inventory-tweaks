package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaksConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;

/**
 * A help menu for the NoCheatPlus conflict.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiModNotWorking extends InvTweaksGuiSettingsAbstract {
    public InvTweaksGuiModNotWorking(InvTweaksConfig config_) {
        super(config_);
    }

    @Override
    public void render(@NotNull PoseStack stack, int i, int j, float f) {
        super.render(stack, i, j, f);


        int x = width / 2;

        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.help.bugsorting.pt1"), x, 80, 0xBBBBBB);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.help.bugsorting.pt2"), x, 95, 0xBBBBBB);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.help.bugsorting.pt3"), x, 110, 0xBBBBBB);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.help.bugsorting.pt4"), x, 150, 0xFFFF99);

    }

}
