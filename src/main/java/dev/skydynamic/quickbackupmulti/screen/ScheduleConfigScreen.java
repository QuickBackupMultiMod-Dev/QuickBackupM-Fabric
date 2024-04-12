package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.Messenger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.screen.ScreenUtils.buildButton;
import static dev.skydynamic.quickbackupmulti.screen.TempConfig.tempConfig;

@Environment(EnvType.CLIENT)
public class ScheduleConfigScreen  extends Screen {
    private final Screen parent;
    private TextFieldWidget cronTextField;
    private TextFieldWidget intervalTextField;

    public ScheduleConfigScreen(Screen parent) {
        super(Messenger.literal("ScheduleSetting"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int totalWidth = 2 * 120 + 20;

        ButtonWidget backButton = buildButton(tr("quickbackupmulti.config_page.back"), width / 2 - 100, height - 70, 200, 20,
            (button) -> {
            tempConfig.config.scheduleCron = cronTextField.getText();
            tempConfig.config.scheduleInterval = Integer.parseInt(intervalTextField.getText());
            this.client.setScreen(parent);
        });

        ButtonWidget enableScheduleBackupButton = buildButton(tr("quickbackupmulti.config_page.schedule.switch", tempConfig.config.getScheduleBackup() ? "On" : "Off"),
            width / 2 - totalWidth / 2, 50, 120, 20,
            (button) -> {
            if (button.getMessage().toString().contains("On")) {
                button.setMessage(Messenger.literal(tr("quickbackupmulti.config_page.schedule.switch", "Off")));
                tempConfig.config.scheduleBackup = false;
            } else {
                button.setMessage(Messenger.literal(tr("quickbackupmulti.config_page.schedule.switch", "On")));
                tempConfig.config.scheduleBackup = true;
            }
        });
        ButtonWidget switchScheduleModeButton = buildButton(tr("quickbackupmulti.config_page.schedule.mode.switch", tempConfig.config.getScheduleMode()),
            width / 2 - totalWidth / 2 + 105 + 20, 50, 120, 20,
            (button) -> {
            if (button.getMessage().toString().contains("interval")) {
                button.setMessage(Messenger.literal(tr("quickbackupmulti.config_page.schedule.mode.switch", "cron")));
                tempConfig.config.scheduleMode = "cron";
            } else {
                button.setMessage(Messenger.literal(tr("quickbackupmulti.config_page.schedule.mode.switch", "interval")));
                tempConfig.config.scheduleMode = "interval";
            }
        });

        cronTextField = new TextFieldWidget(textRenderer, width / 2, 80, 105, 15, Text.of(""));
        cronTextField.setText(tempConfig.config.scheduleCron);
        cronTextField.setEditable(true);

        intervalTextField = new TextFieldWidget(textRenderer, width / 2, 120, 105, 15, Text.of("")) {
            @Override
            public void write(String text) {
                if (text.matches("\\d*")) {
                    super.write(text);
                }
            }
        };
        intervalTextField.setText(String.valueOf(tempConfig.config.scheduleInterval));
        intervalTextField.setEditable(true);

        addChild(cronTextField);
        addChild(intervalTextField);
        addChild(enableScheduleBackupButton);
        addChild(switchScheduleModeButton);
        addChild(backButton);
    }

    @Override
    //#if MC>=12000
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //#else
        //$$ public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
        //#endif
        this.renderBackground(context);
        drawCenteredTextWithShadow(context, tr("quickbackupmulti.config_page.title"), width / 2, 20, 0xFFFFFF);
        drawCenteredTextWithShadow(context, tr("quickbackupmulti.config_page.schedule.cron"), width / 2 - 46, 83, 0xFFFFFF);
        drawCenteredTextWithShadow(context, tr("quickbackupmulti.config_page.schedule.interval"), width / 2 - 45, 123, 0xFFFFFF);
        cronTextField.render(context, mouseX, mouseY, delta);
        intervalTextField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.execute(() -> client.setScreen(parent));
    }

    //#if MC>=12000
    private void drawCenteredTextWithShadow(DrawContext context, String text, int x, int y, int color) {
        context.drawCenteredTextWithShadow(textRenderer, text, x, y, color);
        //#else
        //$$ private void drawCenteredTextWithShadow(MatrixStack context, String text, int x, int y, int color) {
        //$$    drawCenteredTextWithShadow(context, textRenderer, text, x, y, color);
        //#endif
    }

    //#if MC>=11701
    private void addChild(ButtonWidget value) {
        addDrawableChild(value);
    }

    private void addChild(TextFieldWidget value) {
        addDrawableChild(value);
    }
    //#endif
}
