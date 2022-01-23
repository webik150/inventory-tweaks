package invtweaks.gui;

import invtweaks.InvTweaks;
import invtweaks.InvTweaksConfig;
import invtweaks.InvTweaksConst;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URL;

/**
 * The inventory and chest settings menu.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSettings extends InvTweaksGuiSettingsAbstract {

    private static String labelMiddleClick;
    private static String labelShortcuts;
    private static String labelAutoRefill;
    private static String labelAutoRefillBeforeBreak;
    private static String labelMoreOptions;
    private static String labelBugSorting;

    @SuppressWarnings("unused")
    public InvTweaksGuiSettings(Screen parentScreen_) {
        this(parentScreen_, InvTweaks.getConfigManager().getConfig());
    }

    public InvTweaksGuiSettings(Screen parentScreen_, InvTweaksConfig config_) {
        super(parentScreen_, config_);

        labelMiddleClick = I18n.get("invtweaks.settings.middleclick");
        labelShortcuts = I18n.get("invtweaks.settings.shortcuts");
        labelAutoRefill = I18n.get("invtweaks.settings.autorefill");
        labelAutoRefillBeforeBreak = I18n.get("invtweaks.settings.beforebreak");
        labelMoreOptions = I18n.get("invtweaks.settings.moreoptions");
        labelBugSorting = I18n.get("invtweaks.help.bugsorting");
    }

    public static class Point{
        private int x;
        private int y;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Point() {
            this.x = 0;
            this.y = 0;
        }
    }


    @Override
    public void init() {
        super.init();

        @NotNull Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        int startingButtonIndex = renderables.size();
        addRenderableWidget(new Button(p.getX() + 55, height / 6 + 96, 150, 20, new TextComponent(I18n.get("invtweaks.settings.rulesfile")),button ->{
                try {
                    Desktop.getDesktop().open(InvTweaksConst.CONFIG_RULES_FILE);
                } catch(Exception e) {
                    InvTweaks.logInGameErrorStatic("invtweaks.settings.rulesfile.error", e);
                }
        }));
        addRenderableWidget(new Button(p.getX() + 55, height / 6 + 144, 150, 20, new TextComponent(I18n.get("invtweaks.settings.onlinehelp")), button -> {
                try {
                    Desktop.getDesktop().browse(new URL(InvTweaksConst.HELP_URL).toURI());
                } catch(Exception e) {
                    InvTweaks.logInGameErrorStatic("invtweaks.settings.onlinehelp.error", e);
                }
        }));
        moveToButtonCoords(11, p);
        addRenderableWidget(new Button(p.getX(), p.getY(), 150, 20, new TextComponent(I18n.get("invtweaks.settings.treefile")), button -> {
                try {
                    Desktop.getDesktop().open(InvTweaksConst.CONFIG_TREE_FILE);
                } catch(Exception e) {
                    InvTweaks.logInGameErrorStatic("invtweaks.settings.treefile.error", e);
                }

        }));
        moveToButtonCoords(10, p);
        addRenderableWidget(new Button(p.getX(), p.getY(), 150, 20, new TextComponent(I18n.get("invtweaks.settings.moddedtreefile")), button -> {
            try {
                Desktop.getDesktop().browse(new URL(InvTweaksConst.TREE_URL).toURI());
            } catch(Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.moddedtreefile.error", e);
            }
        }));


        // Create settings buttons

        moveToButtonCoords(i++, p);
        addRenderableWidget(new InvTweaksGuiTooltipButton(p.getX() + 130, p.getY(), 20, 20, new TextComponent("?"), "Shortcuts help", button -> obf.displayGuiScreen(new InvTweaksGuiShortcutsHelp( this, config))));
        @NotNull InvTweaksGuiTooltipButton shortcutsBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), 130, 20, new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SHORTCUTS, labelShortcuts)), I18n.get("invtweaks.settings.shortcuts.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_SHORTCUTS, labelShortcuts));
        addRenderableWidget(shortcutsBtn);

        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton beforeBreakBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK, labelAutoRefillBeforeBreak)), I18n.get("invtweaks.settings.beforebreak.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_AUTO_REFILL_BEFORE_BREAK, labelAutoRefillBeforeBreak));
        addRenderableWidget(beforeBreakBtn);

        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton autoRefillBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_AUTO_REFILL, labelAutoRefill)), I18n.get("invtweaks.settings.autorefill.tooltip"), button -> toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_AUTO_REFILL, labelAutoRefill));
        addRenderableWidget(autoRefillBtn);

        moveToButtonCoords(i++, p);
        addRenderableWidget(new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(labelMoreOptions), I18n.get("invtweaks.settings.moreoptions.tooltip"), button -> obf.displayGuiScreen(new InvTweaksGuiSettingsAdvanced(parentScreen, config))));

        addRenderableWidget(new InvTweaksGuiTooltipButton(5, this.height - 20, 100, 20, new TextComponent(labelBugSorting), null, false, button -> obf.displayGuiScreen(new InvTweaksGuiModNotWorking(parentScreen, config))));

        //noinspection UnusedAssignment
        moveToButtonCoords(i++, p);
        @NotNull InvTweaksGuiTooltipButton middleClickBtn = new InvTweaksGuiTooltipButton(p.getX(), p.getY(), new TextComponent(computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, labelMiddleClick)), I18n.get("invtweaks.settings.middleclick.tooltip"), button ->toggleBooleanButton(button, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, labelMiddleClick));
        addRenderableWidget(middleClickBtn);

        // Check if links to files are supported, if not disable the buttons
        if(!Desktop.isDesktopSupported()) {
            for (int j = startingButtonIndex; j < startingButtonIndex+3; j++) {
                if(renderables.size() > j)
                    ((Button)renderables.get(j)).active = false;
            }
        }

    }
}
