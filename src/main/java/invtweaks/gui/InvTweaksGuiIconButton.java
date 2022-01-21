package invtweaks.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaksConfigManager;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiIconButton extends InvTweaksGuiTooltipButton {

    @NotNull
    private static ResourceLocation resourceButtonCustom = new ResourceLocation(InvTweaksMod.MOD_ID, "textures/gui/button10px.png");
    @NotNull
    private static ResourceLocation resourceButtonDefault = new ResourceLocation("textures/gui/widgets.png");
    protected InvTweaksConfigManager cfgManager;
    private boolean useCustomTexture;

    public InvTweaksGuiIconButton(InvTweaksConfigManager cfgManager_, int x, int y, int w, int h, Component displayString_, String tooltip, boolean useCustomTexture_, Button.OnPress action) {
        super(x, y, w, h, displayString_, tooltip, action);
        cfgManager = cfgManager_;
        useCustomTexture = useCustomTexture_;
    }

    @Override
    public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial) {
        super.renderButton(mStack, mouseX, mouseY, partial);
        // Draw background (use the 4 corners of the texture to fit best its small size)
        int k = isHoveredOrFocused()? 1:0;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if(useCustomTexture) {
            RenderSystem.setShaderTexture(0,resourceButtonCustom);
            blit(mStack, x, y, (k - 1) * 10, 0, width, height);
        } else {
            RenderSystem.setShaderTexture(0,resourceButtonDefault);
            blit(mStack, x, y, 1, 46 + k * 20 + 1, width / 2, height / 2);
            blit(mStack, x, y + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
            blit(mStack, x + width / 2, y, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
            blit(mStack, x + width / 2, y + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2, height / 2);
        }
    }

}