package io.github.skydynamic.quickbackupmulti.command.suggestion;

import io.github.skydynamic.quickbackupmulti.config.Config;

import java.util.ArrayList;
import java.util.List;

public class AutoRestartModeSuggestionProvider {
    public static CustomSuggestionProvider mode() {
        List<String> list = new ArrayList<>();
        for (Config.AutoRestartMode mode : Config.AutoRestartMode.values()) {
            list.add(mode.name());
        }
        return CustomSuggestionProvider.suggestion(list);
    }
}
