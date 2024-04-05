package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import static dev.skydynamic.quickbackupmulti.QbmConstant.SAVE_CONFIG_PACKET_ID;
import static dev.skydynamic.quickbackupmulti.QbmConstant.gson;
import static dev.skydynamic.quickbackupmulti.i18n.Translate.tr;
import static dev.skydynamic.quickbackupmulti.screen.TempConfig.tempConfig;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget langTextField;

    int totalWidth = 2 * 100 + 20;

    public ConfigScreen(Screen parent, ConfigStorage config) {
        super(Messenger.literal("ConfigScreen"));
        this.parent = parent;
        tempConfig.setConfig(config);
    }

    @Override
    protected void init() {
        // save & close
        ButtonWidget saveConfigButton = buildButton(tr("quickbackupmulti.config_page.save_button"),
            width / 2 - totalWidth / 2, height - 70, 105, 20,
            (button) -> {
            tempConfig.config.lang = langTextField.getText();
            PacketByteBuf sendBuf = PacketByteBufs.create();
            sendBuf.writeString(gson.toJson(tempConfig.config));
            ClientPlayNetworking.send(SAVE_CONFIG_PACKET_ID, sendBuf);
        });
        ButtonWidget closeScreenButton = buildButton(tr("quickbackupmulti.config_page.close_button"),
            width / 2 - totalWidth / 2 + 135, height - 70, 105, 20, (button) -> this.close());

        ButtonWidget openScheduleConfigScreenButton = buildButton(tr("quickbackupmulti.config_page.open_schedule_config_button"),
            width / 2 - 100, 55, 200, 20, (button) -> client.setScreen(new ScheduleConfigScreen(this)));

        langTextField = new TextFieldWidget(textRenderer, width / 2 - 38, 90, 105, 15, Text.of(""));
        langTextField.setText(tempConfig.config.lang);
        langTextField.setMaxLength(5);
        langTextField.setEditable(true);

        addChild(langTextField);
        addChild(saveConfigButton);
        addChild(closeScreenButton);
        addChild(openScheduleConfigScreenButton);
    }

    @Override
    //#if MC>=12000
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    //#else
    //$$ public void render(MatrixStack context, int mouseX, int mouseY, float delta) {
    //#endif
        this.renderBackground(context);
        drawCenteredTextWithShadow(context, tr("quickbackupmulti.config_page.title"), width / 2, 20, 0xFFFFFF);
        drawCenteredTextWithShadow(context, tr("quickbackupmulti.config_page.lang"), width / 2 - 70, 93, 0xFFFFFF);
        langTextField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(parent);
        tempConfig.config = null;
    }

    //#if MC>=12000
    private void drawCenteredTextWithShadow(DrawContext context, String text, int x, int y, int color) {
        context.drawCenteredTextWithShadow(textRenderer, text, x, y, color);
    //#else
    //$$ private void drawCenteredTextWithShadow(MatrixStack context, String text, int x, int y, int color) {
    //$$    drawCenteredTextWithShadow(context, textRenderer, text, x, y, color);
    //#endif
    }

    private ButtonWidget buildButton(String text, int x, int y, int width, int height, ButtonWidget.PressAction action) {
        //#if MC>=11903
        return ButtonWidget.builder(Messenger.literal(text), action).dimensions(x, y, width, height).build();
        //#else
        //$$ return new ButtonWidget(x, y, width, height, Messenger.literal(text), action);
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
