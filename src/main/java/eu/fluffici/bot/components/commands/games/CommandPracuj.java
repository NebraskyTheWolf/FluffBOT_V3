package eu.fluffici.bot.components.commands.games;

/*
---------------------------------------------------------------------------------
File Name : CommandPracuj.java

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
import eu.fluffici.bot.api.beans.players.PlayerJob;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_TRUCK;

@CommandHandle
public class CommandPracuj extends Command {

    // Drastically nerfing the pracuj command.
    private final int MIN = 1;
    private final int MAX = 100;

    public CommandPracuj() {
        super("pracuj", "Make your shift and get your paycheck.", CommandCategory.GAMES);

        this.getOptions().put("channelRestricted", true);
    }

    @Override
    @SneakyThrows
    public void execute(@NotNull CommandInteraction interaction) {
        User currentUser = interaction.getUser();
        Pair<Boolean, PlayerJob> job = FluffBOT.getInstance()
                .getGameServiceManager()
                .getPlayerLastShift(currentUser);

        if (job.getLeft()) {
            PlayerJob playerJob = job.getRight();

            Instant now = Instant.now();
            Instant lastShift = playerJob.getLastShift().toInstant();
            Duration timeElapsed = Duration.between(lastShift, now);

            if (timeElapsed.toHours() >= 4) {
                this.handleShift(currentUser, interaction);
            } else {
                Instant nextShift = lastShift.plus(4, ChronoUnit.HOURS);
                Duration remainingTime = Duration.between(now, nextShift);

                long seconds = remainingTime.getSeconds();
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long remainingSeconds = seconds % 60;

                interaction.replyEmbeds(
                        this.buildError(this.getLanguageManager().get("command.pracuj.error.desc", hours, minutes, remainingSeconds))
                ).queue();
            }
        } else {
            this.handleShift(currentUser, interaction);
        }
    }

    @SneakyThrows
    private void handleShift(User currentUser, CommandInteraction interaction) {
        PlayerBean player = this.getUserManager().fetchUser(currentUser);
        this.getUserManager().incrementStatistics(currentUser, "shifts", 1);
        int shifts = FluffBOT.getInstance()
                .getGameServiceManager().getPlayerStatistics(currentUser, "shifts").getScore();


        int randomCurrency = (int) Math.abs((Math.random() * (this.MAX - this.MIN + 1) + this.MIN));
        long newBalance = Math.abs(player.getTokens() + randomCurrency);

        this.getUserManager().addTokens(player, randomCurrency);

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("command.pracuj.success.title"), "https://fluffici.eu", ICON_TRUCK)
                        .addField(this.getLanguageManager().get("command.pracuj.winnings"), String.format("%s <:flufftoken:820777573046812693>", randomCurrency), true)
                        .addField(this.getLanguageManager().get("command.pracuj.new_balance"), String.format("%s <:flufftoken:820777573046812693>", newBalance), true)
                        .addField(this.getLanguageManager().get("command.pracuj.shitfs"), String.format("%s", shifts), true)
                        .addField(this.getLanguageManager().get("command.pracuj.expiration"), "1h 59m 59s", false)
                        .setTimestamp(Instant.now())
                        .setFooter(currentUser.getEffectiveName(), currentUser.getAvatarUrl())
                .build()
        ).queue();

        FluffBOT.getInstance().getGameServiceManager().createPlayerLastShift(currentUser);
    }
}
