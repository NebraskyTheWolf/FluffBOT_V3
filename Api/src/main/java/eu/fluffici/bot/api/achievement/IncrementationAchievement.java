package eu.fluffici.bot.api.achievement;

/*
---------------------------------------------------------------------------------
File Name : IncrementationAchievement.java

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


import lombok.Getter;

import java.sql.Timestamp;
import java.time.Instant;
@Getter
public class IncrementationAchievement  extends Achievement {
    private final int objective;

    /**
     * Constructor
     *
     * @param id             Achievement ID
     * @param displayName    Achievement's display name in GUIs
     * @param parentCategory Achievement's parent category ID
     * @param description    Achievement's description in GUIs
     * @param objective      Achievement's goal to reach
     */
    public IncrementationAchievement(int id, String displayName, AchievementCategory parentCategory, String[] description, int objective) {
        super(id, displayName, parentCategory, description);
        this.objective = objective;
    }

    /**
     * Increase the progress of a given player
     *
     * @param userId Player
     * @param amount Amount
     */
    public void increment(String userId, int amount, AchievementSucceedCallback callback) {
        AchievementProgress progress = this.progress.get(userId);

        if (progress == null) {
            progress = new AchievementProgress(-1, 0, Timestamp.from(Instant.now()), null, true);
            this.progress.put(userId, progress);
        }

        if (progress.getProgress() + amount > this.objective && progress.getUnlockTime() == null) {
            progress.unlock();
            progress.setProgress(this.objective);
            callback.done(userId, this, progress);
        } else if (progress.getUnlockTime() == null) {
            progress.setProgress(progress.getProgress() + amount);
        }
    }

    /**
     * Get the given player's progress of the achievement
     *
     * @param userId Player
     * @return Actual progress
     */
    public int getActualState(String userId) {
        AchievementProgress progress = this.progress.get(userId);
        return progress == null ? 0 : progress.getProgress();
    }
}