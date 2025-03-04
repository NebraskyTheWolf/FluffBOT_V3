/*
---------------------------------------------------------------------------------
File Name : CommandFeedback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.misc;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_FILE;
import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

public class CommandFeedback extends FCommand {
    public CommandFeedback() {
        super("feedback", "Do you have some suggestion? then tell us what you wish to see on the bot!", CommandCategory.MISC);
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        handleCustomModal(interaction, this.getLanguageManager().get("command.feedback.modal.title"), listOf(ActionRow.of(
                TextInput.create("row:subject", this.getLanguageManager().get("common.subject"), TextInputStyle.SHORT)
                        .setMaxLength(200)
                        .setMinLength(2)
                        .build()
        ), ActionRow.of(
                TextInput.create("row:feedback", this.getLanguageManager().get("common.feedback"), TextInputStyle.PARAGRAPH)
                        .setMaxLength(4000)
                        .setMinLength(2)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            EmbedBuilder feedbackMessage = this.getEmbed().simpleAuthoredEmbed();
            feedbackMessage.setAuthor(this.getLanguageManager().get("command.feedback.title", modalInteraction.getValue("row:subject").getAsString()), "https://frdb.fluffici.eu", ICON_FILE.getUrl());
            feedbackMessage.setDescription(
                    """
                    **Feedback**:
                    ```
                    %s
                    ```
                    """.formatted(modalInteraction.getValue("row:feedback").getAsString())
            );
            feedbackMessage.setColor(Color.GREEN);
            feedbackMessage.setThumbnail(interaction.getUser().getAvatarUrl());
            feedbackMessage.addField(this.getLanguageManager().get("common.user.id"), interaction.getUser().getId(), false);
            feedbackMessage.addField(this.getLanguageManager().get("common.user.name"), interaction.getUser().getGlobalName(), false);
            feedbackMessage.setFooter("From: ".concat(interaction.getGuild().getName()), ICON_QUESTION_MARK.getUrl());
            feedbackMessage.setTimestamp(Instant.now());

            interaction.getJDA().getTextChannelById(1255326402652143666L).sendMessageEmbeds(feedbackMessage.build()).queue();

            modalInteraction.replyEmbeds(buildSuccess(this.getLanguageManager().get("command.feedback.sent"))).setEphemeral(true).queue();
        });
    }
}