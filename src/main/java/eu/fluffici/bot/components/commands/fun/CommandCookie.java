package eu.fluffici.bot.components.commands.fun;

/*
---------------------------------------------------------------------------------
File Name : CommandCookie.java

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


import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@CommandHandle
public class CommandCookie extends Command {
    public CommandCookie() {
        super("cookie", "Give a cookie", CommandCategory.FUN);
        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select the user for this interaction", true));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User target = interaction.getOption("user").getAsUser();

        if (this.getUserManager().isBlocked(target, interaction.getUser())) {
            interaction.reply(this.getLanguageManager().get("command.fun.interaction_blocked")).setEphemeral(true).queue();
            return;
        }

        interaction.replyEmbeds(this.getEmbed()
                .image(this.getLanguageManager().get("command.cookie.title", interaction.getUser().getEffectiveName(), target.getEffectiveName()),
                        "", "https://autumn.fluffici.eu/attachments/ystmRycjgISxQse2DtDbZ0O9UD7IIs5QomOCbQHwPd")
                .build()
        ).mention(target).queue();
    }
}
