/*
---------------------------------------------------------------------------------
File Name : MessageUtil

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtil {
    /**
     * Updates the interaction message by editing the embeds and action rows.
     *
     * @param message The message instance to update.
     */
    public static void updateInteraction(@NotNull Message message) {
        List<ItemComponent> components = message.getActionRows().stream()
                .map(ActionRow::asDisabled)
                .flatMap(actionRow -> actionRow.getComponents().stream())
                .collect(Collectors.toList());

        message.editMessageComponents(ActionRow.of(components)).queue();
    }
}