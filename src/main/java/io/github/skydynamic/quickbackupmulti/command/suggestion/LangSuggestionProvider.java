package io.github.skydynamic.quickbackupmulti.command.suggestion;

import io.github.skydynamic.quickbackupmulti.i18n.Translate;

public class LangSuggestionProvider {
    public static CustomSuggestionProvider lang() {
        return CustomSuggestionProvider.suggestion(Translate.supportLanguage.stream().toList());
    }
}
