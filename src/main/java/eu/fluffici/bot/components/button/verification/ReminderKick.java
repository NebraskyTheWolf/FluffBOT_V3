/*
---------------------------------------------------------------------------------
File Name : ReminderKick

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.verification;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static eu.fluffici.bot.api.MessageUtil.updateInteraction;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;


public class ReminderKick extends ButtonBuilder {
    public ReminderKick() {
        super("row:reminder-kick", FluffBOT.getInstance().getLanguageManager().get("button.reminder.kick.label"), ButtonStyle.PRIMARY);
    }

    @Override
    @SuppressWarnings("All")
    public void execute(ButtonInteraction interaction) {
        handleCustomModal(interaction, this.getLanguageManager().get("common.reminder.kick.title"), listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();

            FluffBOT.getInstance().getGameServiceManager().fetchExpiredRemindersLocked().forEach((user, timestamp) -> interaction.getGuild().getMember(user).kick().reason(reason).queue());

            updateInteraction(interaction.getMessage());

            this.getUserManager().addPointToStaff(interaction.getUser(), 15);

            modalInteraction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.getLanguageManager().get("common.reminder.kick.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                    .setDescription(this.getLanguageManager().get("common.reminder.kick.success.description"))
                    .build()
            ).setEphemeral(true).queue();
        });
    }
}