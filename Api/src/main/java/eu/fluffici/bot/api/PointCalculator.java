package eu.fluffici.bot.api;

/*
---------------------------------------------------------------------------------
File Name : PointCalculator.java

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

import eu.fluffici.bot.api.hooks.PlayerBean;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ALL")
public class PointCalculator {

    /**
     * Calculates the total points based on the given parameters.
     *
     * @param messages the number of messages sent
     * @param fluffTokens the number of fluff tokens earned
     * @param coins the number of coins earned
     * @param shifts the number of shifts
     * @param karma the karma value
     * @return the total points earned
     */
    public static long calculatePoints(@NotNull PlayerBean player, int messages, int mmr) {
        long points = 0;

        // Messages
        points += calculateExponentialPoints(messages, 1.05);

        // Flufftokens
        points += calculateExponentialPoints(player.getTokens(), 1.02);

        // Coins
        points += calculateExponentialPoints(player.getCoins(), 1.01);

        // Karma
        if (player.getKarma() < 0)
            points -= Math.abs(player.getKarma()) * 0.1;
        else
            points += Math.log(player.getKarma() + 1);

        points += calculateExponentialPoints(player.getExperience(), 1.03);
        points += calculateExponentialPoints(mmr, 1.04);

        return points;
    }

    public static double calculateExponentialPoints(long value, double base) {
        return Math.abs(value * value / 100000.0);
    }

    public static long calculateRr(long points) {
        return points;
    }
}