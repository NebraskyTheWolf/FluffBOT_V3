package eu.fluffici.bot.components.scheduler.sanction;

/*
---------------------------------------------------------------------------------
File Name : SendReport.java

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
import eu.fluffici.bot.api.beans.statistics.ReportNotification;
import eu.fluffici.bot.api.chart.dataset.TemporalBasis;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.bot.api.podium.PodiumBuilder;
import eu.fluffici.bot.api.podium.PodiumGenerator;
import eu.fluffici.bot.api.podium.impl.MessageLeaderboard;
import eu.fluffici.bot.api.podium.impl.PodiumType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("ALL")
public class SendReport extends Task {
    private final FluffBOT instance;

    private final Integer[] REWARDS = new Integer[]  {
       300,
       200,
       100
    };

    private final int BASE_POINTS_PRICE = 20;

    public SendReport(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading SendWeeklyReport scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                CompletableFuture.runAsync(() -> {
                    Pair<Boolean, ReportNotification> result = this.instance.getGameServiceManager().hasActiveReport(ReportNotification.ReportType.MONTHLY);

                    if (!result.getLeft()) {
                        Pair<Boolean, Pair<String, FileUpload>> messageChart = this.instance.getDefaultCharts().getMessageChart(FluffBOT.getInstance().getGameServiceManager().sumAll(),
                                TemporalBasis.MONTHLY
                        );

                        Pair<Boolean, Pair<String, FileUpload>> voiceChart = this.instance.getDefaultCharts().getVoiceActivitiesChart(FluffBOT.getInstance().getGameServiceManager().getVoiceActivities(),
                                TemporalBasis.MONTHLY
                        );

                        Pair<Boolean, Pair<String, FileUpload>> guildEngagement = this.instance.getDefaultCharts().getEngagementsChart(FluffBOT.getInstance().getGameServiceManager().getGuildEngagements());

                        if (messageChart.getLeft() && voiceChart.getLeft() && guildEngagement.getLeft()) {
                            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                    .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.report"))
                                    .sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.monthly"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876158965219399/file-description.png")
                                                    .setDescription(this.instance.getLanguageManager().get("task.report.weekly.monthly"))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.monthly.message"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(messageChart.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.monthly.voice"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(voiceChart.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.monthly.engagement"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(guildEngagement.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build())
                                    .addFiles(messageChart.getRight().getRight(), voiceChart.getRight().getRight(), guildEngagement.getRight().getRight())
                                    .queue();
                        } else {
                            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                    .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.report"))
                                    .sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.monthly.error"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876647425478686/square-forbid.png")
                                                    .setDescription(this.instance.getLanguageManager().get("task.report.monthly.error.description"))
                                                    .setColor(Color.RED)
                                                    .setTimestamp(Instant.now())
                                                    .build()
                                    ).queue();
                        }

                        this.instance.getGameServiceManager().insertGuildEngagementReport(ReportNotification.ReportType.MONTHLY);
                    }
                }).thenRunAsync(() -> {
                    Pair<Boolean, ReportNotification> result = this.instance.getGameServiceManager().hasActiveReport(ReportNotification.ReportType.WEEKLY);

                    if (!result.getLeft()) {
                        Pair<Boolean, Pair<String, FileUpload>> messageChart = this.instance.getDefaultCharts().getMessageChart(FluffBOT.getInstance().getGameServiceManager().sumAll(),
                                TemporalBasis.DAILY
                        );

                        Pair<Boolean, Pair<String, FileUpload>> voiceChart = this.instance.getDefaultCharts().getVoiceActivitiesChart(FluffBOT.getInstance().getGameServiceManager().getVoiceActivities(),
                                TemporalBasis.DAILY
                        );

                        Pair<Boolean, Pair<String, FileUpload>> guildEngagement = this.instance.getDefaultCharts().getEngagementsChart(FluffBOT.getInstance().getGameServiceManager().getGuildEngagements());

                        if (messageChart.getLeft() && voiceChart.getLeft() && guildEngagement.getLeft()) {
                            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                    .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.report"))
                                    .sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.weekly"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876158965219399/file-description.png")
                                                    .setDescription(this.instance.getLanguageManager().get("task.report.weekly.description"))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.weekly.message"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(messageChart.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.weekly.voice"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(voiceChart.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build(),
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.weekly.engagement"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876159196172308/file-report.png")
                                                    .setImage("attachment://".concat(guildEngagement.getRight().getLeft()))
                                                    .setTimestamp(Instant.now())
                                                    .build())
                                    .addFiles(messageChart.getRight().getRight(), voiceChart.getRight().getRight(), guildEngagement.getRight().getRight())
                                    .queue();
                        } else {
                            this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                                    .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.report"))
                                    .sendMessageEmbeds(
                                            this.instance.getEmbed()
                                                    .simpleAuthoredEmbed()
                                                    .setAuthor(this.instance.getLanguageManager().get("task.report.weekly.error"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245876647425478686/square-forbid.png")
                                                    .setDescription(this.instance.getLanguageManager().get("task.report.weekly.error.description"))
                                                    .setColor(Color.RED)
                                                    .setTimestamp(Instant.now())
                                                    .build()
                                    ).queue();
                        }

                        this.instance.getGameServiceManager().insertGuildEngagementReport(ReportNotification.ReportType.WEEKLY);
                    }
                }).thenRunAsync(() -> {
                    Pair<Boolean, ReportNotification> result = this.instance.getGameServiceManager().hasActiveReport(ReportNotification.ReportType.MONTHLY_MESSAGES);
                    if (!result.getLeft()) {
                        EmbedBuilder embed = this.instance.getEmbed().simpleAuthoredEmbed();
                        embed.setAuthor("Měsíční vyhodnocení - Nejaktivnější členové", "https://fluffici.eu", ICON_MEDAL);
                        embed.setColor(Color.decode("#90EE90"));
                        embed.setTimestamp(Instant.now());

                        this.generateMonthlyLeaderboard(new Callback() {
                            @Override
                            public void onTop3(List<MessageLeaderboard> messageList) {
                                for (int i = 0; i < messageList.size(); i++) {
                                    MessageLeaderboard message = messageList.get(i);
                                    PlayerBean player = instance.getUserManager().fetchUser(message.getUser());
                                    instance.getUserManager().addTokens(player, REWARDS[i]);
                                }
                            }

                            @Override
                            public void onAll(List<MessageLeaderboard> messageList) {
                                String callbackId = GameId.generateId();

                                TextChannel announcementChannel = instance.getJda().getTextChannelById(instance.getChannelConfig().getProperty("channel.announcement"));
                                if (announcementChannel != null && announcementChannel.canTalk()) {
                                    PodiumGenerator podiumGenerator = new PodiumGenerator(instance.getLanguageManager(), new PodiumBuilder(PodiumType.FULL, messageList));
                                    CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> podiumGenerator.generatePodium(callbackId));

                                    embed.setImage("attachment://".concat(callbackId).concat(".png"));

                                    announcementChannel
                                            .sendMessageEmbeds(embed.build())
                                            .setContent("<@&723518587314372659>")
                                            .addFiles(generatedProfile.join())
                                            .queue();
                                }

                                instance.getGameServiceManager().insertGuildEngagementReport(ReportNotification.ReportType.MONTHLY_MESSAGES);
                            }
                        });
                    }
                }).thenRunAsync(() -> {
                    Pair<Boolean, ReportNotification> result = this.instance.getGameServiceManager().hasActiveReport(ReportNotification.ReportType.PAYCHECK);
                    if (!result.getLeft()) {
                        EmbedBuilder embed = this.instance.getEmbed().simpleAuthoredEmbed();
                        embed.setAuthor(this.instance.getLanguageManager().get("task.report.monthly.paycheck.title"), "https://fluffici.eu", ICON_CLOCK);
                        embed.setColor(Color.decode("#90EE90"));
                        embed.setTimestamp(Instant.now());
                        embed.setDescription(this.instance.getLanguageManager().get("task.report.monthly.paycheck.description"));

                        Map<UserSnowflake, Long> paychecks = new HashMap<>();

                        this.instance.getGameServiceManager().getAllStaff().forEach(staff -> {
                            long points = staff.getPoints();
                            long base = new Random().nextInt(0, 100);
                            long total = (points / 10) * this.BASE_POINTS_PRICE;

                            if (total < 40 && staff.getPoints() > 0)
                                total += base;

                            paychecks.put(staff.getUser(), total);

                            this.instance.getUserManager().addTokens(this.instance.getUserManager().fetchUser(staff.getUser()), Math.abs((int)total));
                            this.instance.getGameServiceManager().updateStaffPaycheck(staff.getUser(), 0);
                        });

                        paychecks.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue())
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e1,
                                        LinkedHashMap::new
                                ));

                        List<String> results = paychecks.entrySet()
                                .stream()
                                .map(entry -> entry.getKey().getAsMention() + " - " + NumberFormat.getNumberInstance().format(entry.getValue()) + " :flufftoken:")
                                .toList();

                        embed.setDescription(String.join("\n", results));

                        TextChannel staffChannel = instance.getJda().getTextChannelById(instance.getChannelConfig().getProperty("channel.staff"));
                        if (staffChannel != null && staffChannel.canTalk()) {
                            staffChannel.sendMessageEmbeds(embed.build()).queue();
                        }

                        instance.getGameServiceManager().insertGuildEngagementReport(ReportNotification.ReportType.PAYCHECK);
                    }
                }).get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    /**
     * Generates a monthly leaderboard of message counts for users.
     *
     * @param callback The callback to handle the generated leaderboard.
     *                 The callback must implement the `Callback` interface and provide the following methods:
     *                 - `onAll(List<MessageLeaderboard> messageList)`: Called with the complete leaderboard list.
     *                 - `onTop3(List<MessageLeaderboard> messageList)`: Called with the top 3 entries from the leaderboard list.
     *
     * @see Callback
     * @see MessageLeaderboard
     *
     * @since 1.0.0
     */
    public void generateMonthlyLeaderboard(@NotNull Callback callback) {
        Map<User, Long> messageCountMap = this.instance.getGameServiceManager().sumAll()
                .stream()
                .filter(message -> this.instance.getJda().getUserById(message.getUserId()) != null)
                .filter(message ->
                        message.getCreatedAt().toLocalDateTime().isAfter(
                                ZonedDateTime.now().minus(1, ChronoUnit.MONTHS).toLocalDateTime()
                        )
                )
                .collect(Collectors.groupingBy(
                        message -> this.instance.getJda().getUserById(message.getUserId()),
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
