package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandHistory.java

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
import eu.fluffici.bot.api.chart.dataset.TemporalBasis;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_FILE;

@CommandHandle
@SuppressWarnings("All")
public class CommandStatistics extends Command {

    public CommandStatistics(FluffBOT instance) {
        super("personal-statistics", "Get your moderation statistics for the week", CommandCategory.MODERATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        Pair<Boolean, Pair<String, FileUpload>> personalStatistics = FluffBOT.getInstance().getDefaultCharts().getSanctionsChartOwned(
                interaction.getUser(),
                FluffBOT.getInstance().getGameServiceManager().getAllSanctionsBy(interaction.getUser()),
                TemporalBasis.DAILY
        );

        if (personalStatistics.getLeft()) {
            interaction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setImage("attachment://".concat(personalStatistics.getRight().getLeft()))
                    .setAuthor(this.getLanguageManager().get("command.moderation.personal_stats"), "https://fluffici.eu", ICON_FILE)
                    .setDescription(this.getLanguageManager().get("command.moderation.personal_stats.desc"))
                    .setTimestamp(Instant.now())
                    .build()
            ).addFiles(personalStatistics.getRight().getRight()).setEphemeral(true).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.personal_stats.no_stats"))).setEphemeral(true).queue();
        }
    }
}