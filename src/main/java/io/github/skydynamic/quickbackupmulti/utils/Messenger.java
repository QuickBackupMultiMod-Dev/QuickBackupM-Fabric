package io.github.skydynamic.quickbackupmulti.utils;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
//#if MC<11900
//$$ import net.minecraft.text.LiteralText;
//#endif

public class Messenger {
    public static void sendMessage(ServerCommandSource commandSource, Text text) {
        //#if MC>=11900
        commandSource.sendMessage(text);
        //#else
        //$$ commandSource.sendFeedback(text, false);
        //#endif
    }

    public static MutableText literal(String string) {
        //#if MC>=11900
        return Text.literal(string);
        //#else
        //$$ return new LiteralText(string);
        //#endif
    }
}
