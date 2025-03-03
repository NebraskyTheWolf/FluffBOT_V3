/*
---------------------------------------------------------------------------------
File Name : ReminderRemind

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.verification;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static eu.fluffici.bot.api.IconRegistry.ICON_WARNING;
import static eu.fluffici.bot.api.MessageUtil.updateInteraction;

public class ReminderRemind extends ButtonBuilder {
    public ReminderRemind() {
        super("row:reminder-remind", FluffBOT.getInstance().getLanguageManager().get("button.reminder.remind.label"), ButtonStyle.SECONDARY);
    }

    /**
     * Executes the button interaction by fetching expired reminders and handling them,
     * then updating the interaction's message.
     *
     * @param interaction The button interaction.
     */
    @Override
    public void execute(@NotNull ButtonInteraction interaction) {
        FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchExpiredRemindersLocked()
                .forEach(this::handleRemind);

        interaction.deferEdit().queue();

        updateInteraction(interaction.getMessage());
    }

    /**
     * Handles a reminder for a specific user and timestamp.
     *
     * @param user      The user associated with the reminder.
     * @param timestamp The timestamp of the reminder.
     */
    @SuppressWarnings("All")
    private void handleRemind(@NotNull UserSnowflake user, @NotNull Timestamp timestamp) {
        PrivateChannel selfChannel = FluffBOT.getInstance()
                .getJda().getUserById(user.getId())
                .openPrivateChannel()
                .complete();

        EmbedBuilder message = this.getEmbed().simpleAuthoredEmbed();
        message.setColor(Color.RED);
        message.setAuthor(this.getLanguageManager().get("button.remind.message.title"), "https://fluffici.eu", ICON_WARNING);
        message.setDescription(this.getLanguageManager().get("button.remind.message.desc", timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss"))));
        message.setFooter(this.getLanguageManager().get("button.remind.message.footer"));
        message.setTimestamp(Instant.now());

        if (selfChannel.canTalk()) {
            selfChannel.sendMessageEmbeds(message.build()).queue();
        }
    }
}