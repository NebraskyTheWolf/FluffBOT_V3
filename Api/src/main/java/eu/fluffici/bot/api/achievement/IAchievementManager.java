package eu.fluffici.bot.api.achievement;

/*
---------------------------------------------------------------------------------
File Name : IAchievementManager.java

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


import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;
public interface IAchievementManager
{
    /**
     * Increase the progress of a given achievement to a given player
     *
     * @param userId Player
     * @param achievement Achievement
     * @param amount Amount
     */
    void incrementAchievement(String userId, IncrementationAchievement achievement, int amount, AchievementSucceedCallback callback);

    /**
     * Increase the progress of a given achievement to a given player
     *
     * @param userId Player
     * @param achievement Achievement
     * @param amount Amount
     */
    void incrementAchievement(String userId, int achievement, int amount);

    /**
     * Increase achievements progress, useful for linked achievements
     *
     * @param userId Player
     * @param achievements Achievement id array
     * @param amount Amount
     */
    void incrementAchievements(String userId, int[] achievements, int amount);

    /**
     * Get the achievement with the given ID
     *
     * @param id ID
     *
     * @return Achievement
     */
    Achievement getAchievementByID(int id);

    /**
     * Get the achievement category with the given ID
     *
     * @param id ID
     *
     * @return Achievement category
     */
    AchievementCategory getAchievementCategoryByID(int id);

    /**
     * Get all the achievements of the database
     *
     * @return Achievements
     */
    List<Achievement> getAchievements();

    /**
     * Get all the achievement categories of the database
     *
     * @return Achievement categories
     */
    List<AchievementCategory> getAchievementsCategories();

    /**
     * Return if the given player has unlocked the given achievement
     *
     * @param userId Player
     * @param achievement Achievement
     *
     * @return {@code true} if unlocked
     */
    boolean isUnlocked(String userId, Achievement achievement);

    /**
     * Return if the given player has unlocked the given achievement ID
     *
     * @param userId Player
     * @param id Achievement's ID
     *
     * @return {@code true} if unlocked
     */
    boolean isUnlocked(String userId, int id);

    void sendMessage(UserSnowflake user, Achievement achievement, AchievementProgress progress);

    void unlock(UserSnowflake user, int id);
}