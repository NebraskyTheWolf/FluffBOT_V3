package eu.fluffici.bot.api.chart;

/*
---------------------------------------------------------------------------------
File Name : DefaultCharts.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import eu.fluffici.bot.api.beans.players.EconomyHistory;
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.api.beans.players.PlayerVoiceActivity;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.beans.statistics.GuildEngagement;
import eu.fluffici.bot.api.chart.dataset.CombinedDataset;
import eu.fluffici.bot.api.chart.dataset.DatasetUtil;
import eu.fluffici.bot.api.chart.dataset.TemporalBasis;
import eu.fluffici.bot.api.chart.impl.ChartDetails;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jfree.chart.plot.PlotOrientation;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCharts {
    private final Chart messageChart = new Chart();
    private final ChartDetails details = new ChartDetails();

    public DefaultCharts() {
        this.details.setOrientation(PlotOrientation.VERTICAL);
        this.details.setWidth(1135);
        this.details.setHeight(600);
    }

    /**
     * Generates a chart of message counts based on the given list of messages and temporal basis.
     *
     * @param messages      A List of Message objects representing the messages.
     * @param temporalBasis A TemporalBasis object representing the basis for temporal aggregation.
     * @return A Pair object containing a Boolean indicating the success of the chart generation,
     *         and a Pair object containing a String representing the chart filename,
     *         and a FileUpload object representing the generated chart image.
     */
    public Pair<Boolean, Pair<String, FileUpload>> getMessageChart(List<Message> messages, TemporalBasis temporalBasis) {
        Map<? extends Temporal, Long> dailyMessageCounts = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(messages, message -> true);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(messages, message -> true);
            case YEARLY -> DatasetUtil.countToYearlyBasis(messages, message -> true);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(messages,message -> true);
        };

        messageChart.createTemporalDataset("Messages", dailyMessageCounts);

        this.details.setTitle(switch (temporalBasis) {
            case DAILY -> "Denní počty zpráv";
            case MONTHLY -> "Měsíční počty zpráv";
            case YEARLY -> "Roční počty zpráv";
            case TRIMESTER -> "Počet zpráv za trimestr";
        });

        this.details.setYAxisTitle("Počet zpráv");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }

    /**
     * Generates a chart of voice activities based on the given list of player voice activities and temporal basis.
     *
     * @param voiceActivities A list of PlayerVoiceActivity objects representing the voice activities of the players.
     * @param temporalBasis   A TemporalBasis object representing the basis for temporal aggregation.
     * @return A Pair object containing a Boolean indicating the success of the chart generation, and a Pair
     *         containing a String representing the chart filename, and a FileUpload object representing the generated chart image.
     */
    public Pair<Boolean, Pair<String, FileUpload>> getVoiceActivitiesChart(List<PlayerVoiceActivity> voiceActivities, TemporalBasis temporalBasis) {
        Map<? extends Temporal, Long> voiceActivitiesSum = switch (temporalBasis) {
            case DAILY -> DatasetUtil.sumToDailyBasis(voiceActivities, voiceActivity -> true);
            case MONTHLY -> DatasetUtil.sumToMonthlyBasis(voiceActivities, voiceActivity -> true);
            case YEARLY -> DatasetUtil.sumToYearlyBasis(voiceActivities, voiceActivity -> true);
            case TRIMESTER -> DatasetUtil.sumToTrimesterBasis(voiceActivities, voiceActivity -> true);
        };

        messageChart.createTemporalDataset("Voice Activities", voiceActivitiesSum);

        this.details.setTitle(switch (temporalBasis) {
            case DAILY -> "Denní aktivita hlasového chatu";
            case MONTHLY -> "Měsíční aktivita hlasového chatu";
            case YEARLY -> "Roční aktivita hlasového chatu";
            case TRIMESTER -> "Aktivita hlasového chatu za trimestr";
        });

        this.details.setYAxisTitle("Čas strávený na sekundové bázi");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }

    /**
     * Generates a chart of guild engagements.
     *
     * @param guildEngagements A list of GuildEngagement objects representing the engagements of the guild.
     * @return A Pair object containing a Boolean indicating the success of the chart generation, and a Pair
     *         containing a String representing the chart filename, and a FileUpload object representing the generated chart image.
     */
    @SuppressWarnings("ALL")
    public Pair<Boolean, Pair<String, FileUpload>> getEngagementsChart(List<GuildEngagement> guildEngagements) {
        Map<LocalDate, Long> newMembers = DatasetUtil.countToDailyBasis(guildEngagements, guildEngagement -> guildEngagement.getGuildAction() == GuildEngagement.Action.GUILD_JOIN);
        Map<LocalDate, Long> lostMembers = DatasetUtil.countToDailyBasis(guildEngagements, guildEngagement -> guildEngagement.getGuildAction() == GuildEngagement.Action.GUILD_LEAVE);

        double ratio = Math.abs(newMembers.size() / (lostMembers.size() <= 0 ? 1 : lostMembers.size()));

        Map<String, Map<LocalDate, Long>> datasets = new HashMap<>();
        datasets.put("New Member(s)", newMembers);
        datasets.put("Lost Member(s)", lostMembers);

        messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));

        this.details.setTitle("Denní zapojení guildy (Ratio: ".concat(String.valueOf(this.calculateRatio(newMembers, lostMembers)).concat(")")));

        this.details.setYAxisTitle("Poměr příchodů/odchodů uživatelů");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }

    /**
     * Calculates the ratio between the sizes of two given lists.
     *
     * @param newMembers  A List of objects representing the new members.
     * @param lostMembers A List of objects representing the lost members.
     * @return The ratio between the sizes of the two lists. If the lostMembers list is empty, the ratio
     * is calculated relative to the size of the newMembers list. If both lists are empty, return 0.
     */
    private double calculateRatio(Map<LocalDate, Long>  newMembers, Map<LocalDate, Long>  lostMembers) {
        if (lostMembers.isEmpty()) {
            return newMembers.size();
        } else {
            return (double) newMembers.size() / lostMembers.size();
        }
    }

    /**
     * Retrieves economic chart based on the given list of economy histories and temporal basis.
     *
     * @param economyHistories A list of EconomyHistory objects representing the economic histories.
     * @param temporalBasis    A TemporalBasis object representing the basis for temporal aggregation.
     * @return A Pair object containing a Boolean indicating the success of the chart generation, and a Pair
     *         containing a String representing the chart filename, and a FileUpload object representing the generated chart image.
     */
    @SuppressWarnings("ALL")
    public Pair<Boolean, Pair<String, FileUpload>> getEconomicChart(List<EconomyHistory> economyHistories, TemporalBasis temporalBasis) {
        Map<? extends Temporal, Long> creditOperation = switch (temporalBasis) {
            case DAILY -> DatasetUtil.sumToDailyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.CREDIT);
            case MONTHLY -> DatasetUtil.sumToMonthlyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.CREDIT);
            case YEARLY -> DatasetUtil.sumToYearlyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.CREDIT);
            case TRIMESTER -> DatasetUtil.sumToTrimesterBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.CREDIT);
        };

        Map<? extends Temporal, Long> debitOperation = switch (temporalBasis) {
            case DAILY -> DatasetUtil.sumToDailyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.DEBIT);
            case MONTHLY -> DatasetUtil.sumToMonthlyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.DEBIT);
            case YEARLY -> DatasetUtil.sumToYearlyBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.DEBIT);
            case TRIMESTER -> DatasetUtil.sumToTrimesterBasis(economyHistories, economyHistory -> economyHistory.getOperation() == EconomyHistory.Operation.DEBIT);
        };

        switch (temporalBasis) {
            case DAILY -> {
                Map<String, Map<LocalDate, Long>> datasets = new HashMap<>();
                datasets.put("Credit", (Map<LocalDate, Long>) creditOperation);
                datasets.put("Debit", (Map<LocalDate, Long>) debitOperation);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case MONTHLY, TRIMESTER -> {
                Map<String, Map<YearMonth, Long>> datasets = new HashMap<>();
                datasets.put("Credit", (Map<YearMonth, Long>) creditOperation);
                datasets.put("Debit", (Map<YearMonth, Long>) debitOperation);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case YEARLY -> {
                Map<String, Map<Year, Long>> datasets = new HashMap<>();
                datasets.put("Credit", (Map<Year, Long>) creditOperation);
                datasets.put("Debit", (Map<Year, Long>) debitOperation);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
        }

        this.details.setTitle(switch (temporalBasis) {
            case DAILY -> "Denní ekonomické operace";
            case MONTHLY -> "Měsíční ekonomické operace";
            case YEARLY -> "Roční ekonomické operace";
            case TRIMESTER -> "Ekonomické operace za trimestr";
        });

        this.details.setYAxisTitle("Operace úvěru/debitu");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }

    @SuppressWarnings("ALL")
    public Pair<Boolean, Pair<String, FileUpload>> getSanctionsChart(List<SanctionBean> sanctions, TemporalBasis temporalBasis) {
        Map<? extends Temporal, Long> warns = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
        };

        Map<? extends Temporal, Long> mutes = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
        };

        Map<? extends Temporal, Long> bans = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
        };

        Map<? extends Temporal, Long> kicks = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
        };

        switch (temporalBasis) {
            case DAILY -> {
                Map<String, Map<LocalDate, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<LocalDate, Long>) warns);
                datasets.put("Umlčení", (Map<LocalDate, Long>) mutes);
                datasets.put("Ban(y)", (Map<LocalDate, Long>) bans);
                datasets.put("Vykopnutí", (Map<LocalDate, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case MONTHLY, TRIMESTER -> {
                Map<String, Map<YearMonth, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<YearMonth, Long>) warns);
                datasets.put("Umlčení", (Map<YearMonth, Long>) mutes);
                datasets.put("Ban(y)", (Map<YearMonth, Long>) bans);
                datasets.put("Vykopnutí", (Map<YearMonth, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case YEARLY -> {
                Map<String, Map<Year, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<Year, Long>) warns);
                datasets.put("Umlčení", (Map<Year, Long>) mutes);
                datasets.put("Ban(y)", (Map<Year, Long>) bans);
                datasets.put("Vykopnutí", (Map<Year, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
        }

        this.details.setTitle(switch (temporalBasis) {
            case DAILY -> "Denní uplatněné sankce";
            case MONTHLY -> "Měsíční uplatněné sankce";
            case YEARLY -> "Roční uplatněné sankce";
            case TRIMESTER -> "Uplatněné sankce za trimestr";
        });

        this.details.setYAxisTitle("Uplatněné sankce");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }

    @SuppressWarnings("ALL")
    public Pair<Boolean, Pair<String, FileUpload>> getSanctionsChartOwned(User mod, List<SanctionBean> sanctions, TemporalBasis temporalBasis) {
        Map<? extends Temporal, Long> warns = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.WARN);
        };

        Map<? extends Temporal, Long> mutes = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.MUTE);
        };

        Map<? extends Temporal, Long> bans = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.BAN);
        };

        Map<? extends Temporal, Long> kicks = switch (temporalBasis) {
            case DAILY -> DatasetUtil.countToDailyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case MONTHLY -> DatasetUtil.countToMonthlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case YEARLY -> DatasetUtil.countToYearlyBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
            case TRIMESTER -> DatasetUtil.countToTrimesterBasis(sanctions, economyHistory -> economyHistory.getTypeId() == SanctionBean.KICK);
        };

        switch (temporalBasis) {
            case DAILY -> {
                Map<String, Map<LocalDate, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<LocalDate, Long>) warns);
                datasets.put("Timeout", (Map<LocalDate, Long>) mutes);
                datasets.put("Ban(y)", (Map<LocalDate, Long>) bans);
                datasets.put("Vykopnutí", (Map<LocalDate, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case MONTHLY, TRIMESTER -> {
                Map<String, Map<YearMonth, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<YearMonth, Long>) warns);
                datasets.put("Umlčení", (Map<YearMonth, Long>) mutes);
                datasets.put("Ban(y)", (Map<YearMonth, Long>) bans);
                datasets.put("Vykopnutí", (Map<YearMonth, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
            case YEARLY -> {
                Map<String, Map<Year, Long>> datasets = new HashMap<>();
                datasets.put("Varování", (Map<Year, Long>) warns);
                datasets.put("Umlčení", (Map<Year, Long>) mutes);
                datasets.put("Ban(y)", (Map<Year, Long>) bans);
                datasets.put("Vykopnutí", (Map<Year, Long>) kicks);

                messageChart.createCombinedDatasets(new CombinedDataset<>(datasets));
            }
        }

        this.details.setTitle("Statistiky sankcí moderátora " + mod.getEffectiveName() + ".");

        this.details.setYAxisTitle("Uplatněné sankce");
        this.details.setXAxisTitle("Datum");

        return messageChart.saveChartAsPNG(details);
    }
}
