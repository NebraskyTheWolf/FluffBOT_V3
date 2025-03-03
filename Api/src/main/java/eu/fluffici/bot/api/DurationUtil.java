package eu.fluffici.bot.api;

/*
---------------------------------------------------------------------------------
File Name : DurationUtil.java

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


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
public class DurationUtil {


    /**
     * Calculate the duration between the current time and a future instant.
     *
     * @param future the future instant to calculate the duration to
     * @return the duration data containing the years, months, days, hours, minutes, and seconds
     */
    public static DurationData getDuration(long future) {
        return getDuration(Instant.ofEpochMilli(future));
    }

    /**
     * Calculate the duration between the current time and a future instant.
     *
     * @param future the future instant to calculate the duration to
     * @return the duration data containing the years, months, days, hours, minutes, and seconds
     */

    public static DurationData getDuration(Instant future) {
        Duration duration = Duration.between(Instant.now(), future);
        long totalSeconds = duration.getSeconds();

        long years = Math.abs(totalSeconds / (60 * 60 * 24 * 365));
        long months = Math.abs((totalSeconds % (60 * 60 * 24 * 365)) / (60 * 60 * 24 * 30));
        long daysLeft = Math.abs((totalSeconds % (60 * 60 * 24 * 30)) / (60 * 60 * 24));
        long hours = Math.abs((totalSeconds % (60 * 60 * 24)) / (60 * 60));
        long minutes = Math.abs((totalSeconds % (60 * 60)) / 60);
        long seconds = Math.abs(totalSeconds % 60);

        return DurationData.builder()
                .years(years)
                .months(months)
                .days(daysLeft)
                .hours(hours)
                .minutes(minutes)
                .seconds(seconds)
                .build();
    }

    /**
     * Converts a DurationData object into a string representation.
     *
     * @param durationData the DurationData object to convert
     * @return a string representation of the duration, including years, months, days, hours, minutes, and seconds
     */
    @Deprecated(forRemoval = true, since = "v1.0.5")
    public static String toTimeString(DurationData durationData) {
        StringBuilder duration = new StringBuilder();
        if (durationData.getYears() > 0)
            duration.append("Years: ").append(durationData.getYears()).append(", ");
        if (durationData.getMonths() > 0)
            duration.append("Months: ").append(durationData.getMonths()).append(", ");
        if (durationData.getDays() > 0)
            duration.append("Days: ").append(durationData.getDays()).append(", ");
        if (durationData.getHours() > 0)
            duration.append("Hours: ").append(durationData.getHours()).append(", ");
        if (durationData.getMinutes() > 0)
            duration.append("Minutes: ").append(durationData.getMinutes()).append(", ");
        if (durationData.getSeconds() > 0)
            duration.append("Seconds: ").append(durationData.getSeconds()).append(", ");
        return duration.toString();
    }

    /**
     * This class represents duration data consisting of years, months, days, hours, minutes, and seconds.
     * It provides methods to calculate and convert durations.
     */
    @Getter
    @Setter
    @Builder
    public static class DurationData {
        private long years;
        private long months;
        private long days;
        private long hours;
        private long minutes;
        private long seconds;

        /**
         * Returns a string representation of the DurationData object.
         *
         * The string representation is in the format of years, months, days, hours, minutes, and seconds.
         * Each time unit is followed by the corresponding plural or singular label.
         * The resulting string is trimmed and empty if the duration is zero.
         * If the duration is zero, the method returns the default string "Less than a minute ago."
         *
         * @return a string representation of the DurationData object
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (years > 0) {
                sb.append(years).append(years > 1 ? " years " : " year ").append(", ");
            }
            if (months > 0) {
                sb.append(months).append(months > 1 ? " months " : " month ").append(", ");
            }
            if (days > 0) {
                sb.append(days).append(days > 1 ? " days " : " day ").append(", ");
            }
            if (hours > 0) {
                sb.append(hours).append("h ").append(", ");
            }
            if (minutes > 0) {
                sb.append(minutes).append("m ").append(", ");
            }
            if (seconds > 0) {
                sb.append(seconds).append("s").append(", ");
            }

            String durationString = sb.toString().trim();
            return durationString.isEmpty() ? "Less than a minute ago." : durationString; // handle the 0 time case
        }
    }
}
