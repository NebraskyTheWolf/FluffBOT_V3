package eu.fluffici.bot.components.scheduler.channel;

/*
---------------------------------------------------------------------------------
File Name : SendDailyStatistics.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.DurationUtil;
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.api.interactions.Task;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("All")
public class SendDailyStatistics extends Task {
    public static double XP_BOOST = 1.0;
    private final FluffBOT instance;

    @Getter
    private static StatisticRegression regression;

    public SendDailyStatistics(FluffBOT instance) {
        this.instance = instance;
        regression = new StatisticRegression();
    }

    @Override
    public void execute() {
        this.instance.getLogger().info("Setting up 'SendDailyStatistics#execute' scheduled task.");
        regression.initDataset();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Date midnightTime = calculateNextMidnight();
                DurationUtil.DurationData time = DurationUtil.getDuration(midnightTime.getTime());
                if (time.getDays() > 0 || time.getHours() > 0 || time.getMinutes() > 0) {
                    instance.getLogger().debug("(Safety triggered): Next schedule: " + time);
                    return;
                }

                EmbedBuilder statisticMessage = instance.getEmbed().simpleAuthoredEmbed();
                statisticMessage.setAuthor(instance.getLanguageManager().get("task.statistics.title"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1245429771576414301/medal.png");
                statisticMessage.setDescription(instance.getLanguageManager().get("task.statistics.description"));

                CompletableFuture<List<Message>> message = CompletableFuture.supplyAsync(() -> instance.getGameServiceManager().sumAll());
                try {
                    message.whenCompleteAsync((messages, throwable) -> {
                        long todayCount = messages.stream()
                                .filter(message1 ->
                                        message1.getCreatedAt().toLocalDateTime().toLocalDate().isEqual(LocalDate.now(ZoneId.systemDefault())))
                                .count();
                        long lastDayCount = messages.stream()
                                .filter(message1 ->
                                        message1.getCreatedAt().toLocalDateTime().toLocalDate().isEqual(LocalDate.now(ZoneId.systemDefault()).minusDays(1)))
                                .count();

                        Pair<Boolean, EmbedBuilder> boostState = determineBoostState(todayCount, lastDayCount);

                        int shifts = instance.getGameServiceManager().getDailyShiftSum();

                        statisticMessage.addField(instance.getLanguageManager().get("common.messages.stats"), NumberFormat.getNumberInstance().format(todayCount), true);
                        statisticMessage.addField(instance.getLanguageManager().get("common.messages.shifts"), NumberFormat.getNumberInstance().format(shifts), true);

                        TextChannel mainChannel = Objects.requireNonNull(instance.getJda()
                                        .getGuildById(instance.getDefaultConfig().getProperty("main.guild")))
                                .getTextChannelById(instance.getDefaultConfig().getProperty("channel.main"));

                        if (mainChannel != null) {
                            mainChannel.sendMessageEmbeds(boostState.getRight().build(), statisticMessage.build()).queue();
                        } else {
                            instance.getLogger().error("The mainChannel does not exists.", null);
                        }
                    }).get(30, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }

                instance.getLogger().debug("Task executed at: " + new Date());
            }
        };

        Timer timer = new Timer();
        Date midnightTime = calculateNextMidnight();

        long period = 24 * 60 * 60 * 1000;
        timer.scheduleAtFixedRate(task, midnightTime, period);

        this.instance.getLogger().info("'SendDailyStatistics#execute' task scheduled to start at: " + DurationUtil.getDuration(midnightTime.getTime()).toString());
    }


    public Date calculateNextMidnight() {
        Calendar calendar = Calendar.getInstance();

        TimeZone pragueTimeZone = TimeZone.getTimeZone("Europe/Prague");
        calendar.setTimeZone(pragueTimeZone);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date midnightTime = calendar.getTime();

        if (midnightTime.before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            midnightTime = calendar.getTime();
        }

        return midnightTime;
    }

    @SneakyThrows
    private Pair<Boolean, EmbedBuilder> determineBoostState(long today, long last) {
        Pair<Boolean, StatisticRegression.BoostStatus> data = regression.determineBoostState(today, last);
        return switch (data.getRight()) {
            case GATHERING -> Pair.of(true, this.gatheringBoostMessage());
            case UNCHANGED -> Pair.of(true, this.noneBoostMessage());
            case GRANTED -> Pair.of(true, this.activeBoostMessage());
            case DENIED -> Pair.of(true, this.inactiveBoostMessage());
        };
    }

    private EmbedBuilder activeBoostMessage() {
        XP_BOOST = 2.0;

        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("task.statistics.boost.active"), "https://fluffici.eu", ICON_UNLOCK.getUrl())
                .setDescription(this.instance.getLanguageManager().get("task.statistics.boost.active.desc"))
                .setFooter(this.instance.getLanguageManager().get("task.statistics.boost.footer"));
    }

    private EmbedBuilder noneBoostMessage() {
        XP_BOOST = 1.0;

        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("task.statistics.boost.none"), "https://fluffici.eu", ICON_ALERT.getUrl())
                .setDescription(this.instance.getLanguageManager().get("task.statistics.boost.none.desc"))
                .setFooter(this.instance.getLanguageManager().get("task.statistics.boost.none.footer"));
    }

    private EmbedBuilder gatheringBoostMessage() {
        XP_BOOST = 1.0;

        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("task.statistics.boost.gathering"), "https://fluffici.eu", ICON_ALERT.getUrl())
                .setDescription(this.instance.getLanguageManager().get("task.statistics.boost.gathering.desc"))
                .setFooter(this.instance.getLanguageManager().get("task.statistics.boost.gathering.footer"));
    }

    private EmbedBuilder inactiveBoostMessage() {
        XP_BOOST = 1.0;

        return instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("task.statistics.boost.inactive"), "https://fluffici.eu", ICON_LOCK.getUrl())
                .setDescription(this.instance.getLanguageManager().get("task.statistics.boost.inactive.desc"))
                .setFooter(this.instance.getLanguageManager().get("task.statistics.boost.inactive.footer"));
    }
}
