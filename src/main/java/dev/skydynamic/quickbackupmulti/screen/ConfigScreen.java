package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.Messenger;
import dev.skydynamic.quickbackupmulti.utils.config.ConfigStorage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
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
        ButtonWidget saveConfigButton = ButtonWidget.builder(Messenger.literal(tr("quickbackupmulti.config_page.save_button")), (button) -> {
            tempConfig.config.lang = langTextField.getText();
            PacketByteBuf sendBuf = PacketByteBufs.create();
            sendBuf.writeString(gson.toJson(tempConfig.config));
            ClientPlayNetworking.send(SAVE_CONFIG_PACKET_ID, sendBuf);
        }).dimensions(width / 2 - totalWidth / 2, height - 70, 105, 20).build();
        ButtonWidget closeScreenButton = ButtonWidget.builder(Messenger.literal(tr("quickbackupmulti.config_page.close_button")), (button) -> this.close())
            .dimensions(width / 2 - totalWidth / 2 + 135, height - 70, 105, 20).build();

        ButtonWidget openScheduleConfigScreenButton = ButtonWidget.builder(Messenger.literal(tr("quickbackupmulti.config_page.open_schedule_config_button")), (button) -> client.setScreen(new ScheduleConfigScreen(this)))
            .dimensions(width / 2 - 100, 55, 200, 20).build();

        langTextField = new TextFieldWidget(textRenderer, width / 2 - 38, 90, 105, 15, Text.of(""));
        langTextField.setText(tempConfig.config.lang);
        langTextField.setMaxLength(5);
        langTextField.setEditable(true);

        this.addDrawableChild(langTextField);
        this.addDrawableChild(saveConfigButton);
        this.addDrawableChild(closeScreenButton);
        this.addDrawableChild(openScheduleConfigScreenButton);
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
    //$$ private void drawTitle(MatrixStack context) {
    //$$    drawCenteredTextWithShadow(context, textRenderer, text, x, y, color);
    //#endif
    }

}
