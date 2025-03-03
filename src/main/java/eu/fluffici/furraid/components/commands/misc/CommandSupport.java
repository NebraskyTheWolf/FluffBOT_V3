/*
---------------------------------------------------------------------------------
File Name : CommandSupport

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.misc;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

public class CommandSupport extends FCommand {
    public CommandSupport() {
        super("support", "Need assistance? use this command!", CommandCategory.MISC);
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("command.support.title"), "https://frdb.fluffici.eu", ICON_QUESTION_MARK)
                .setDescription(this.getLanguageManager().get("command.support.description"))
                .setTimestamp(Instant.now())
                .setColor(Color.ORANGE)
                .build()
        ).addActionRow(
                Button.link("https://discord.gg/gAy6AQB8HK", "Support")
        ).queue();
    }
}