package eu.fluffici.bot.components.commands.developer;

/*
---------------------------------------------------------------------------------
File Name : CommandShutdown.java

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
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.podium.PodiumBuilder;
import eu.fluffici.bot.api.podium.PodiumGenerator;
import eu.fluffici.bot.api.podium.impl.MessageLeaderboard;
import eu.fluffici.bot.api.podium.impl.PodiumType;
import eu.fluffici.bot.components.scheduler.sanction.SendReport;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_MEDAL;

@CommandHandle
public class CommandTestPodium extends Command {
    public CommandTestPodium() {
        super("test-podium", "Podium dev test", CommandCategory.DEVELOPER);

        this.getOptions().put("isDeveloper", true);
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        EmbedBuilder embed = this.getEmbed().simpleAuthoredEmbed();
        embed.setAuthor("Měsíční vyhodnocení - Nejaktivnější členové", "https://fluffici.eu", ICON_MEDAL.getUrl());
        embed.setColor(Color.decode("#90EE90"));
        embed.setTimestamp(Instant.now());

        this.generateMonthlyLeaderboard(new SendReport.Callback() {
            @Override
            public void onTop3(List<MessageLeaderboard> messageList) {}

            @Override
            public void onAll(List<MessageLeaderboard> messageList) {
                String callbackId = GameId.generateId();

                PodiumGenerator podiumGenerator = new PodiumGenerator(FluffBOT.getInstance().getLanguageManager(), new PodiumBuilder(PodiumType.FULL, messageList));
                CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> podiumGenerator.generatePodium(callbackId));
                generatedProfile.whenComplete((fileUpload, throwable) -> embed.setImage("attachment://".concat(callbackId).concat("_result.png")));

                interaction.reply("owo")
                        .addFiles(generatedProfile.join())
                        .queue();
            }
        });
    }

    /**
     * Generates a monthly leaderboard of message counts for users.
     *
     * @param callback The callback to handle the generated leaderboard.
     *                 The callback must implement the `Callback` interface and provide the following methods:
     *                 - `onAll(List<MessageLeaderboard> messageList)`: Called with the complete leaderboard list.
     *                 - `onTop3(List<MessageLeaderboard> messageList)`: Called with the top 3 entries from the leaderboard list.
     *
     * @see SendReport.Callback
     * @see MessageLeaderboard
     *
     * @since 1.0.0
     */
    public void generateMonthlyLeaderboard(@NotNull SendReport.Callback callback) {
        Map<User, Long> messageCountMap = FluffBOT.getInstance().getGameServiceManager().sumAll()
                .stream()
                .filter(message -> FluffBOT.getInstance().getJda().getUserById(message.getUserId()) != null)
                .filter(message ->
                        message.getCreatedAt().toLocalDateTime().isAfter(
                                ZonedDateTime.now().minus(1, ChronoUnit.MONTHS).toLocalDateTime()
                        )
                )
                .distinct()
                .collect(Collectors.groupingBy(
                        message -> FluffBOT.getInstance().getJda().getUserById(message.getUserId()),
                        Collectors.counting()
                ));

        List<MessageLeaderboard> messageList = messageCountMap.entrySet()
                .stream()
                .map(entry -> new MessageLeaderboard(entry.getKey(), entry.getValue().intValue()))
                .sorted((ml1, ml2) -> Integer.compare(ml2.getCount(), ml1.getCount()))
                .limit(10)
                .collect(Collectors.toList());

        callback.onAll(messageList);
        callback.onTop3(messageList.subList(0, Math.min(3, messageList.size())));
    }


    public static interface Callback {
        void onTop3(List<MessageLeaderboard> messageList);
        void onAll(List<MessageLeaderboard> messageList);
    }
}
