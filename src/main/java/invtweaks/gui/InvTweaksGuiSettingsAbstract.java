package invtweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksObfuscation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The inventory and chest settings menu.
 *
 * @author Jimeo Wan
 */
public abstract class InvTweaksGuiSettingsAbstract extends Screen {

    protected static final Logger log = InvTweaks.log;
    protected static String ON;
    protected static String OFF;
    protected static String LABEL_DONE;
    protected InvTweaksObfuscation obf;
    protected InvTweaksConfig config;
    protected Screen parentScreen;

    public InvTweaksGuiSettingsAbstract(Screen parentScreen_, InvTweaksConfig config_) {
        super((Component) parentScreen_);

        LABEL_DONE = I18n.get("invtweaks.settings.exit");
        ON = ": " + I18n.get("invtweaks.settings.on");
        OFF = ": " + I18n.get("invtweaks.settings.off");

        obf = new InvTweaksObfuscation();
        parentScreen = parentScreen_;
        config = config_;
    }

    protected InvTweaksGuiSettingsAbstract(Component p_96550_) {
        super(p_96550_);
    }


    @Override
    public void init() {
        @NotNull InvTweaksGuiSettings.Point p = new InvTweaksGuiSettings.Point();
        moveToButtonCoords(1, p);
        renderables.add(new Button(p.getX(), p.getY(), p.getX() + 55, height / 6 + 168, new TextComponent(LABEL_DONE), button -> obf.displayGuiScreen(parentScreen)));

    }

    @Override
    public void render(@NotNull PoseStack stack, int i, int j, float f) {
        renderBackground(stack);
        drawCenteredString(stack, obf.getFontRenderer(), I18n.get("invtweaks.settings.title"), width / 2, 20, 0xffffff);
        super.render(stack,i, j, f);
    }

/* TODO: Handle ESC
    @Override
    protected void keyTyped(char c, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            obf.displayGuiScreen(parentScreen);
        }
    }*/

    protected void moveToButtonCoords(int buttonOrder, @NotNull InvTweaksGuiSettings.Point p) {
        p.setX(width / 2 - 155 + ((buttonOrder + 1) % 2) * 160);
        p.setY(height / 6 + (buttonOrder / 2) * 24);
    }

    protected void toggleBooleanButton(@NotNull Button guibutton, @NotNull String property, String label) {
        boolean enabled = !Boolean.parseBoolean(config.getProperty(property));
        config.setProperty(property, Boolean.toString(enabled));
        guibutton.setMessage(new TextComponent(computeBooleanButtonLabel(property, label)));
    }

    @NotNull
    protected String computeBooleanButtonLabel(@NotNull String property, String label) {
        @NotNull String propertyValue = config.getProperty(property);
        boolean enabled = Boolean.parseBoolean(propertyValue);
        return label + ((enabled) ? ON : OFF);
    }

}
