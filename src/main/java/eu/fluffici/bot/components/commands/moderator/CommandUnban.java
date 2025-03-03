package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandUnban.java

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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@CommandHandle
@SuppressWarnings("All")
public class CommandUnban extends Command {

    private final FluffBOT instance;

    public CommandUnban(FluffBOT instance) {
        super("unban", "Unban someone in the server.", CommandCategory.MODERATOR);

        this.instance = instance;

        this.getOptionData().add(new OptionData(OptionType.STRING, "user", "Enter the ID of the user", true));
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String user = interaction.getOption("user").getAsString();

        Guild.Ban isBanned = interaction.getGuild().retrieveBan(UserSnowflake.fromId(interaction.getUser().getId())).complete();
        if (isBanned == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.unban.not_banned", user))).setEphemeral(true).queue();
        } else {
            this.instance.getSanctionManager().unban(
                    interaction.getGuild(),
                    user,
                    interaction.getUser()
            );

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.unban.success", user))).setEphemeral(true).queue();
        }
    }
}
