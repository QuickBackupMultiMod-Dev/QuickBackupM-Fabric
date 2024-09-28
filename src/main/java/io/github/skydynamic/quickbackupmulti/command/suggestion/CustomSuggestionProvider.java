package io.github.skydynamic.quickbackupmulti.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    List<String> list;

    public CustomSuggestionProvider(List<String> list) {
        this.list = list;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        for (String value : list) {
            builder.suggest(value);
        }
        return builder.buildFuture();
    }

    public static CustomSuggestionProvider suggestion(List<String> suggestionList) {
        return new CustomSuggestionProvider(suggestionList);
    }
}
