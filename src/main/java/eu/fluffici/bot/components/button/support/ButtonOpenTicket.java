/*
---------------------------------------------------------------------------------
File Name : ButtonVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.support;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

public class ButtonOpenTicket extends ButtonBuilder {
    public ButtonOpenTicket() {
        super("row:open-ticket", "Potřebuji pomoc!", ButtonStyle.SECONDARY);
    }


    /**
     * Executes the button interaction for opening a ticket.
     *
     * @param interaction The button interaction triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull ButtonInteraction interaction) {
        boolean hasTicket = FluffBOT.getInstance()
                .getGameServiceManager()
                .hasTicket(interaction.getUser());

        if (hasTicket) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.ticket.open.already_has_ticket"))).setEphemeral(true).queue();
            return;
        }

       handleConfirmation(interaction,
               this.getLanguageManager().get("button.ticket.open.confirm"),
               this.getLanguageManager().get("button.ticket.open.confirm.button"),
               new ConfirmCallback() {
                   @Override
                   public void confirm(ButtonInteraction interaction) throws Exception {
                       TextChannel channel = FluffBOT.getInstance()
                               .getTicketManager()
                               .createTicket(interaction.getGuild(), interaction.getUser(), false);

                       if (channel == null) {
                           interaction.reply("Failed to create ticket").setEphemeral(true).queue();
                           return;
                       }

                       interaction.replyEmbeds(buildError(getLanguageManager().get("button.ticket.open.opened")))
                               .setEphemeral(true)
                               .addActionRow(Button.link("https://discord.com/channels/606534136806637589/".concat(channel.getId()), "Your ticket"))
                               .queue();
                   }

                   @Override
                   public void cancel(ButtonInteraction interaction) throws Exception {
                       interaction.replyEmbeds(buildError(getLanguageManager().get("button.ticket.open.cancelled"))).setEphemeral(true).queue();
                   }
               }
       );
    }
}