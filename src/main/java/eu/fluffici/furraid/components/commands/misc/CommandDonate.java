/*
---------------------------------------------------------------------------------
File Name : CommandDonate

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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_HEART;

public class CommandDonate extends FCommand {
    public CommandDonate() {
        super("donate", "Want to donate us? this command is made for this! X3", CommandCategory.MISC);
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
                        .setAuthor(this.getLanguageManager().get("command.donate.title"), "https://frdb.fluffici.eu", ICON_HEART.getUrl())
                        .setDescription(this.getLanguageManager().get("command.donate.description"))
                        .setTimestamp(Instant.now())
                        .setColor(Color.ORANGE)
                .build()
        ).addActionRow(
                Button.link("https://ko-fi.com/fluffici", "Ko-fi").withEmoji(Emoji.fromCustom("kofi", 1252762912200724522L, false))
        ).queue();
    }
}