package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksConst;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * The inventory and chest advanced settings menu.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSettingsAdvanced extends InvTweaksGuiSettingsAbstract {
    private static String labelChestButtons;
    private static String labelSortOnPickup;
    private static String labelEquipArmor;
    private static String labelEnableSounds;
    private static String labelServerAssist;
    private static String labelDisplayTooltip;

    public InvTweaksGuiSettingsAdvanced(Screen parentScreen_, InvTweaksConfig config_) {
        super(parentScreen_, config_);

        labelSortOnPickup = I18n.get("invtweaks.settings.advanced.sortonpickup");
        labelEquipArmor = I18n.get("invtweaks.settings.advanced.autoequip");
        labelEnableSounds = I18n.get("invtweaks.settings.advanced.sounds");
        labelChestButtons = I18n.get("invtweaks.settings.chestbuttons");
        labelServerAssist = I18n.get("invtweaks.settings.advanced.serverassist");
        labelDisplayTooltip = I18n.get("invtweaks.settings.displaytooltip");
    }

    @Override
    public void init() {
        super.init();

        @NotNull InvTweaksGuiSettings.Point p = new InvTweaksGuiSettings.Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        renderables.add(new Button(p.getX() + 55, height / 6 + 144, 120, 60, new TextComponent(I18n.get("invtweaks.settings.advanced.mappingsfile")), p_93751_ -> {
            try {
                Desktop.getDesktop().open(InvTweaksConst.CONFIG_PROPS_FILE);
            } catch(Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.advanced.mappingsfile.error", e);
            }
        }));

        // Create settings buttons

        i += 2;
        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup)), I18n.get("invtweaks.settings.advanced.sortonpickup.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup));
        renderables.add(sortOnPickupBtn);

        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton enableSoundsBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds)), I18n.get("invtweaks.settings.advanced.sounds.tooltip"), button ->  toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds));
        renderables.add(enableSoundsBtn);

        moveToButtonCoords(i++, p);
        renderables.add(new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons)), I18n.get("invtweaks.settings.chestbuttons.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons)));

        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton autoEquipArmorBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor)), I18n.get("invtweaks.settings.advanced.autoequip.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor));
        renderables.add(autoEquipArmorBtn);

        //noinspection UnusedAssignment
        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton serverAssistBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP, labelServerAssist)), I18n.get("invtweaks.settings.advanced.serverassist.tooltip"), button ->{
            toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP, labelServerAssist);
            InvTweaksMod.proxy.setServerAssistEnabled(!InvTweaks.getConfigManager().getConfig().getProperty(InvTweaksConfig.PROP_ENABLE_SERVER_ITEMSWAP).equals(InvTweaksConfig.VALUE_FALSE));
        });
        renderables.add(serverAssistBtn);

        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton displayTooltipBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_TOOLTIP_PATH, labelDisplayTooltip)), I18n.get("invtweaks.settings.displaytooltip.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_TOOLTIP_PATH, labelDisplayTooltip));
        renderables.add(displayTooltipBtn);

        // Check if links to files are supported, if not disable the buttons
        if(!Desktop.isDesktopSupported()) {
            renderables.forEach(widget -> {
                Button button = (Button)widget;
                if(button.getMessage().getContents().equals(I18n.get("invtweaks.settings.advanced.mappingsfile"))) {
                    button.active = false;
                }
            });
        }
    }

    @Override
    public void render(@NotNull PoseStack stack, int i, int j, float f) {
        super.render(stack, i, j, f);

        int x = width / 2;
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.settings.pvpwarning.pt1"), x, 40, 0x999999);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.settings.pvpwarning.pt2"), x, 50, 0x999999);
    }
}
