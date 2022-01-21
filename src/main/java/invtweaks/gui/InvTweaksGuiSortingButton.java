package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfigManager;
import invtweaks.InvTweaksHandlerSorting;
import invtweaks.api.SortingMethod;
import invtweaks.api.container.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Chest sorting button
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSortingButton extends InvTweaksGuiIconButton {

    private final ContainerSection section = ContainerSection.CHEST;

    private SortingMethod algorithm;
    private int rowSize;

    public InvTweaksGuiSortingButton(InvTweaksConfigManager cfgManager_, int x, int y, int w, int h, Component displayString_, String tooltip, SortingMethod algorithm_, int rowSize_, boolean useCustomTexture, OnPress action) {
        super(cfgManager_, x, y, w, h, displayString_, tooltip, useCustomTexture, action);
        algorithm = algorithm_;
        rowSize = rowSize_;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(stack, mouseX, mouseY, partialTicks);

        // Display symbol
        int textColor = getTextColor(mouseX, mouseY);
        switch (super.getMessage().getContents()) {
            case "h" -> {
                fill(stack, x + 3, y + 3, x + width - 3, y + 4, textColor);
                fill(stack, x + 3, y + 6, x + width - 3, y + 7, textColor);
            }
            case "v" -> {
                fill(stack, x + 3, y + 3, x + 4, y + height - 3, textColor);
                fill(stack, x + 6, y + 3, x + 7, y + height - 3, textColor);
            }
            default -> {
                fill(stack, x + 3, y + 3, x + width - 3, y + 4, textColor);
                fill(stack, x + 5, y + 4, x + 6, y + 5, textColor);
                fill(stack, x + 4, y + 5, x + 5, y + 6, textColor);
                fill(stack, x + 3, y + 6, x + width - 3, y + 7, textColor);
            }
        }
    }

    /**
     * Sort container
     */
    @Override
    public boolean mouseClicked(double i, double j, int arg) {
        if(Minecraft.getInstance().player.isSpectator()) {
            return false;
        }

        if(super.mouseClicked(i, j, arg)) {
            try {
                new InvTweaksHandlerSorting(cfgManager.getConfig(), section, algorithm, rowSize).sort();
            } catch(Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.sort.chest.error", e);
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }

    }

}
