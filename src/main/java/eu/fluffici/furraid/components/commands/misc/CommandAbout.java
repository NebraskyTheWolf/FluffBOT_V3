/*
---------------------------------------------------------------------------------
File Name : CommandAbout

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
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Properties;

public class CommandAbout extends FCommand {
    public CommandAbout() {
        super("about", "Get all information about FurRaidDB", CommandCategory.MISC);
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        Properties git = FurRaidDB.getInstance().getGitProperties();

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor("Information about FurRaidDB", "https://frdb.fluffici.eu", interaction.getGuild().getSelfMember().getAvatarUrl())
                .setTitle("Version: V3 b".concat(git.getProperty("git.build.version", "unknown")))
                .setDescription("Last change: \n ".concat(git.getProperty("git.commit.message.full")))
                .addField("Lead Developer: ", "Vakea <vakea@fluffici.eu>", true)
                .addField("Author of the last commit: ", git.getProperty("git.commit.user.name").replace("NebraskyTheWolf", "Vakea"), true)
                .addField("JDA Version: ", JDAInfo.VERSION, false)
                .addField("Java Version: ", Runtime.version().toString(), false)
                .setFooter("This application is licensed under the proprietary license of Fluffici, z.s. and can be found via the button below.")
                .build()
        ).addActionRow(
                Button.link("https://frdb.fluffici.eu", "Website"),
                Button.link("https://frdbdocs.fluffici.eu", "Docs"),
                Button.link("https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf", "License")
        ).queue();
    }
}