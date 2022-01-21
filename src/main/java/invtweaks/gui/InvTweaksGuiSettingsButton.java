package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.*;
import invtweaks.api.container.ContainerSection;
import invtweaks.container.ContainerSectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Button that opens the inventory & chest settings screen.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSettingsButton extends InvTweaksGuiIconButton {

    private static final Logger log = InvTweaks.log;

    public InvTweaksGuiSettingsButton(InvTweaksConfigManager cfgManager_, int x, int y, int w, int h, Component displayString_, String tooltip, boolean useCustomTexture, OnPress action) {
        super(cfgManager_, x, y, w, h, displayString_, tooltip, useCustomTexture, action);
    }

    @Override
    public void renderButton(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(stack, mouseX, mouseY, partialTicks);
        // Display string
        @NotNull InvTweaksObfuscation obf = new InvTweaksObfuscation();
        drawCenteredString(stack, obf.getFontRenderer(), getMessage().getString(), x + 5, y - 1, getTextColor(mouseX, mouseY));
    }

    /**
     * Displays inventory settings GUI
     */
    @Override
    public boolean mouseClicked(double i, double j, int what) {

        @NotNull InvTweaksObfuscation obf = new InvTweaksObfuscation();
        @Nullable InvTweaksConfig config = cfgManager.getConfig();

        if(Minecraft.getInstance().player.isSpectator()) {
            return false;
        }

        if(super.mouseClicked(i, j, what)) {
            // Put hold item down if necessary
            ContainerSectionManager containerMgr;

            try {
                containerMgr = new ContainerSectionManager(ContainerSection.INVENTORY);
                if(!obf.getHeldStack().isEmpty()) {
                    // Put hold item down
                    for(int k = containerMgr.getSize() - 1; k >= 0; k--) {
                        if(containerMgr.getItemStack(k).isEmpty()) {
                            containerMgr.leftClick(k);
                            break;
                        }
                    }
                }
            } catch(Exception e) {
                log.error("mousePressed", e);
            }

            // Refresh config
            cfgManager.makeSureConfigurationIsLoaded();

            // Display menu
            obf.displayGuiScreen(new InvTweaksGuiSettings( obf.getCurrentScreen(), config));
            return true;
        } else {
            return false;
        }
    }

}
