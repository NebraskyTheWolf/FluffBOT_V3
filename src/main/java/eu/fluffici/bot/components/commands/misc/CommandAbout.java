package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandAbout.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Properties;

@CommandHandle
public class CommandAbout extends Command {
    public CommandAbout() {
        super("about", "Získat informace o botovi a poslední změny.", CommandCategory.MISC);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        Properties git = FluffBOT.getInstance().getGitProperties();

        List<String> betaTesters =interaction.getGuild().getMembers()
                .stream()
                .filter(member -> FluffBOT.getInstance().getAchievementManager().isUnlocked(member.getId(), 35))
                .map(IMentionable::getAsMention)
                .toList();

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor("Informace o FluffBOTovi", "https://fluffici.eu", interaction.getGuild().getSelfMember().getAvatarUrl())
                .setTitle("Verze: V3 build: ".concat(git.getProperty("git.build.version", "neznámé")))
                .addField("Vedoucí vývojář: ", "Vakea [vakea@fluffici.eu](mailto:vakea@fluffici.eu) [Website](https://nebraskythewolf.work/cz)", false)
                .addField("Beta testery: ", String.join("\n", betaTesters), false)
                .addField("Verze JDA: ", JDAInfo.VERSION, false)
                .addField("Verze Java: ", Runtime.version().toString(), false)
                .setFooter("Tato aplikace je licencována pod proprietární licencí společnosti Fluffici, z.s. a naleznete ji na tlačítku níže.")
                .build()
        ).addActionRow(
                Button.link("https://fluffici.eu", "Website"),
                Button.link("https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf", "Licence"),
                FluffBOT.getInstance()
                        .getButtonManager()
                        .findByName("developer:thanks")
                        .build(Emoji.fromUnicode("\uD83D\uDC99"))
        ).queue();
    }
}
