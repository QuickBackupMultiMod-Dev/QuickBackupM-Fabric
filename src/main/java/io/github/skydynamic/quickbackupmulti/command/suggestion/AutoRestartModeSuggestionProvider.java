package io.github.skydynamic.quickbackupmulti.command.suggestion;

import io.github.skydynamic.quickbackupmulti.config.AutoRestartMode;

import java.util.ArrayList;
import java.util.List;

public class AutoRestartModeSuggestionProvider {
    public static CustomSuggestionProvider mode() {
        List<String> list = new ArrayList<>();
        for (AutoRestartMode mode : AutoRestartMode.values()) {
            list.add(mode.name());
        }
        return CustomSuggestionProvider.suggestion(list);
    }
}
