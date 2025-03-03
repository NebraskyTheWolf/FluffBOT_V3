package eu.fluffici.bot.api.achievement;

/*
---------------------------------------------------------------------------------
File Name : Achievement.java

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
import java.util.HashMap;
import java.util.Map;
@Getter
public class Achievement {
    protected final int id;
    protected final String displayName;
    protected final AchievementCategory parentCategory;
    protected final String[] description;
    protected Map<String, AchievementProgress> progress;

    /**
     * Constructor
     *
     * @param id Achievement ID
     * @param displayName Achievement's display name in GUIs
     * @param parentCategory Achievement's parent category ID
     * @param description Achievement's description in GUIs
     */
    public Achievement(int id, String displayName, AchievementCategory parentCategory, String[] description)
    {
        this.id = id;
        this.displayName = displayName;
        this.parentCategory = parentCategory;
        this.description = new String[description.length];
        this.progress = new HashMap<>();
    }

    /**
     * Unlock this achievement for a given player
     *
     * @param userId Player
     */
    public void unlock(String userId, AchievementSucceedCallback callback)
    {
        if (this instanceof IncrementationAchievement)
            throw new IllegalStateException("Try to unlock incrementation achievement");

        AchievementProgress progress = this.progress.get(userId);

        if (progress != null && progress.getUnlockTime() != null)
            return;

        if (progress == null)
        {
            progress = new AchievementProgress(-1, 0, Timestamp.from(Instant.now()), null, true);
            this.progress.put(userId, progress);
        }

        progress.unlock();
        progress.setProgress(1);

        callback.done(userId, this, progress);
    }

    /**
     * Get the achievement's ID
     *
     * @return ID
     */
    public int getID()
    {
        return this.id;
    }

    /**
     * Get the achievement's parent category ID
     *
     * @return Parent category ID
     */
    public AchievementCategory getParentCategoryID()
    {
        return this.parentCategory;
    }

    /**
     * Get if this achievement is unlocked for a given player
     *
     * @param userId Player
     * @return {@code true} if unlocked
     */
    public boolean isUnlocked(String userId)
    {
        AchievementProgress progress = this.progress.get(userId);
        return progress != null && progress.getUnlockTime() != null;
    }

    /**
     * Internal function, should only be used by API
     *
     * @param userId Player
     * @param progressId Progress id
     * @param progress Progress
     * @param startTime Start time
     * @param unlockTime Unlock time
     */
    public void addProgress(String userId, long progressId, int progress, Timestamp startTime, Timestamp unlockTime)
    {
        this.progress.put(userId, new AchievementProgress(progressId, progress, startTime, unlockTime, false));
    }

    /**
     * Internal function, should only be used by API
     *
     * @param userId Player
     */
    public void removeProgress(String userId)
    {
        this.progress.remove(userId);
    }

    /**
     * Internal function, should only be used by API
     *
     * @param userId Player
     * @return Progress
     */
    public AchievementProgress getProgress(String userId)
    {
        return this.progress.get(userId);
    }
}
