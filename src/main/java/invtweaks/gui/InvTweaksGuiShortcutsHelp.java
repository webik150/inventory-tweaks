package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksObfuscation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class InvTweaksGuiShortcutsHelp extends Screen {
    private InvTweaksObfuscation obf;
    private Screen parentScreen;
    private InvTweaksConfig config;

    public InvTweaksGuiShortcutsHelp(Screen parentScreen_, InvTweaksConfig config_) {
        super(new TextComponent(""));
        obf = new InvTweaksObfuscation();
        parentScreen = parentScreen_;
        config = config_;
    }

    public void init() {
        addWidget(new Button(width / 2 - 100, height / 6 + 168, 150, 20, new TextComponent("Done"), p_93751_ ->
                // or   obf.displayGuiScreen(parentScreen);
                obf.displayGuiScreen(new InvTweaksGuiSettings(parentScreen, config))));
    }

    @Override
    public void render(PoseStack stack, int i, int j, float f) {

        // Note: 0x0000EEFF = blue color (currently unused)

        renderBackground(stack);
        drawCenteredString(stack, obf.getFontRenderer(), "WARNING: Since 1.3.1, shortcuts won't work as expected. Looking for a workaround...", width / 2, 5, 0xff0000);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.help.shortcuts.title"), width / 2, 20, 0xffffff); // Gui.drawCenteredString
        @NotNull String clickLabel = I18n.get("invtweaks.help.shortcuts.click");

        int y = height / 6 - 2;

        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.onestack"), "LSHIFT " + I18n.get("invtweaks.help.shortcuts.or") + " RSHIFT + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(stack, "", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_UP, obf.getGameSettings().keyUp, I18n.get("invtweaks.help.shortcuts.forward")) + " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(stack, "", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_DOWN, obf.getGameSettings().keyDown, I18n.get("invtweaks.help.shortcuts.backwards")) + " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.oneitem"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.allitems"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.everything"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_EVERYTHING) + " + " + clickLabel, 0x00FFFF00, y);
        y += 19;

        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.hotbar"), "0-9 + " + clickLabel, 0x0000FF33, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.emptyslot"), I18n.get("invtweaks.help.shortcuts.rightclick"), 0x0000FF33, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.drop"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + " + clickLabel, 0x0000FF33, y);
        y += 19;

        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.craftall"), "LSHIFT, RSHIFT + " + clickLabel, 0x00FF8800, y);
        y += 12;
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.craftone"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel, 0x00FF8800, y);
        y += 19;

        String sortKeyName = config.getSortKeyMapping().getName();
        drawShortcutLine(stack, I18n.get("invtweaks.help.shortcuts.selectconfig"), "0-9 + " + sortKeyName, 0x0088FFFF, y);

        super.render(stack, i, j, f);
    }

    private String buildUpOrDownLabel(@NotNull String shortcutProp, KeyMapping key, String defaultKeyName) {
        @NotNull String shortcutLabel = config.getProperty(shortcutProp);
        String keyLabel = key.getName();
        if(keyLabel.equals(shortcutLabel)) {
            return keyLabel;
        } else {
            return keyLabel + "/" + shortcutLabel;
        }
    }

    private void drawShortcutLine(@NotNull PoseStack stack, @NotNull String label, @Nullable String value, int color, int y) {
        drawString(stack, obf.getFontRenderer(), label, 30, y, -1); // drawString
        if(value != null) {
            drawString(stack, obf.getFontRenderer(), value.contains("DEFAULT") ? "-" : value.replaceAll(", ", " " + I18n.get("invtweaks.help.shortcuts.or") + " "), width / 2 - 30, y, color); // drawString
        }
    }

}
