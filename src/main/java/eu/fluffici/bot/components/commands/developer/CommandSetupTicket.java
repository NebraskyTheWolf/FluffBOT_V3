/*
---------------------------------------------------------------------------------
File Name : CommandSetupTicket

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 19/07/2024
Last Modified : 19/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.developer;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

public class CommandSetupTicket extends Command {
    public CommandSetupTicket() {
        super("setup-ticket", "Create the interaction for the tickets", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    public void execute(CommandInteraction interaction) {
        if (interaction.getChannel() instanceof TextChannel textChannel) {
            textChannel.sendMessageEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                            .setAuthor("Podpora", "https://fluffici.eu", ICON_QUESTION_MARK)
                            .setColor(0x00FF00)
                            .setDescription(" K vytvoření ticketu zareaguj s <:ticket:1221209101544194149>")
                            .setFooter(interaction.getJDA().getSelfUser().getName(), interaction.getJDA().getSelfUser().getAvatarUrl())
                    .build()
            ).addActionRow(
                    FluffBOT.getInstance().getButtonManager().findByName("row:open-ticket").build(Emoji.fromCustom("ticket", 1221209101544194149L, false))
            ).queue();

            interaction.reply("The channel was successfully settled up.").setEphemeral(true).queue();
        } else {
            interaction.reply(
            """
                   You cannot setup the ticket form on a private channel.
                   
                   -# Please read the [Documentation](https://wiki.fluffici.eu) for more information.
                   """
            ).setEphemeral(true).queue();
        }
    }
}