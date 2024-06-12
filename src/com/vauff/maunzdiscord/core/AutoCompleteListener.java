package com.vauff.maunzdiscord.core;

import com.vauff.maunzdiscord.commands.templates.AbstractCommand;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import reactor.core.publisher.Mono;

import java.util.List;

public class AutoCompleteListener {
    public static Mono<Void> process(ChatInputAutoCompleteEvent event){
        AbstractCommand<ChatInputInteractionEvent> command = Main.commands.get(event.getCommandName());
        if (command == null){
            return null;
        }
        if (!command.hasAutocomplete())
            return null;

        ApplicationCommandInteractionOption option = event.getFocusedOption();
        String current = option.getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        try{
            List<ApplicationCommandOptionChoiceData> suggests = command.autoComplete(event, option, current);
            return event.respondWithSuggestions(suggests);
        }catch (Exception e){
            Logger.log.error("AutocompleteError: " + e.getMessage());
        }
        return null;
    }
}
