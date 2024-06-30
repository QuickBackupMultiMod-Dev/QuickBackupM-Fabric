package io.github.skydynamic.quickbackupmulti.screen;

import io.github.skydynamic.quickbackupmulti.i18n.Translate;
import io.github.skydynamic.quickbackupmulti.utils.Messenger;
import io.github.skydynamic.quickbackupmulti.config.ConfigStorage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//#if MC>=12005
//$$ import dev.skydynamic.quickbackupmulti.Packets;
//#endif
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

import static io.github.skydynamic.quickbackupmulti.QbmConstant.SAVE_CONFIG_PACKET_ID;
import static io.github.skydynamic.quickbackupmulti.QbmConstant.gson;
import static io.github.skydynamic.quickbackupmulti.screen.ScreenUtils.buildButton;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget langTextField;

    int totalWidth = 2 * 100 + 20;

    public ConfigScreen(Screen parent, ConfigStorage config) {
        super(Messenger.literal("ConfigScreen"));
        this.parent = parent;
        TempConfig.tempConfig.setConfig(config);
    }

    @Override
    protected void init() {
        // save & close
        ButtonWidget saveConfigButton = buildButton(Translate.tr("quickbackupmulti.config_page.save_button"),
            width / 2 - totalWidth / 2, height - 70, 105, 20,
            (button) -> {
            TempConfig.tempConfig.config.setLang(langTextField.getText());
            //#if MC>=12005
            //$$ ClientPlayNetworking.send(new Packets.SaveConfigPacket(gson.toJson(tempConfig.config)));
            //#else
            PacketByteBuf sendBuf = PacketByteBufs.create();
            sendBuf.writeString(gson.toJson(TempConfig.tempConfig.config));
            ClientPlayNetworking.send(SAVE_CONFIG_PACKET_ID, sendBuf);
            //#endif
        });
        ButtonWidget closeScreenButton = buildButton(Translate.tr("quickbackupmulti.config_page.close_button"),
            width / 2 - totalWidth / 2 + 135, height - 70, 105, 20, (button) -> this.close());

        ButtonWidget openScheduleConfigScreenButton = buildButton(
            Translate.tr("quickbackupmulti.config_page.open_schedule_config_button"),
            width / 2 - 100, 55, 200, 20, (button) -> client.setScreen(new ScheduleConfigScreen(this))
        );

        langTextField = new TextFieldWidget(textRenderer, width / 2 - 38, 90, 105, 15, Text.of(""));
        langTextField.setText(TempConfig.tempConfig.config.getLang());
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
        //#if MC>=12003
        //$$ this.renderBackground(context, mouseX, mouseY, delta);
        //#else
        this.renderBackground(context);
        //#endif
        drawCenteredTextWithShadow(
            context,
            Translate.tr("quickbackupmulti.config_page.title"),
            width / 2,
            20,
            0xFFFFFF
        );
        drawCenteredTextWithShadow(
            context,
            Translate.tr("quickbackupmulti.config_page.lang"),
            width / 2 - 70,
            93,
            0xFFFFFF
        );
        langTextField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.execute(() -> client.setScreen(parent));
        TempConfig.tempConfig.config = null;
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
