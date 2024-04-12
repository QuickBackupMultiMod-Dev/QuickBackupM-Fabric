package dev.skydynamic.quickbackupmulti.screen;

import dev.skydynamic.quickbackupmulti.utils.Messenger;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ScreenUtils {
    public static ButtonWidget buildButton(String text, int x, int y, int width, int height, ButtonWidget.PressAction action) {
        //#if MC>=11903
        return ButtonWidget.builder(Messenger.literal(text), action).dimensions(x, y, width, height).build();
        //#else
        //$$ return new ButtonWidget(x, y, width, height, Messenger.literal(text), action);
        //#endif
    }
}
