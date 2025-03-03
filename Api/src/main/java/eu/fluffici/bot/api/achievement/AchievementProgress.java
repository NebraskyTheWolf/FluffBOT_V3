package eu.fluffici.bot.api.achievement;

/*
---------------------------------------------------------------------------------
File Name : AchievementProgress.java

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
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
@Getter
@Setter
public class AchievementProgress {
    @Setter
    private long progressId;
    private int progress;
    private final Timestamp startTime;
    private Timestamp unlockTime;
    private boolean changed;

    AchievementProgress(long progressId, int progress, Timestamp startTime, Timestamp unlockTime, boolean changed)
    {
        this.progressId = progressId;
        this.progress = progress;
        this.startTime = startTime;
        this.unlockTime = unlockTime;
        this.changed = changed;
    }

    /**
     * Get an increment achievement progress for this player
     *
     * @return progress
     */
    public int getProgress()
    {
        return this.progress;
    }

    /**
     * Increment an achievement progress for this player
     *
     * @param amount Amount to increase
     */
    public void setProgress(int amount)
    {
        this.progress = amount;
        this.changed = true;
    }

    /**
     * Get start time for this achievement progress
     *
     * @return Start time
     */
    public Timestamp getStartTime()
    {
        return this.startTime;
    }

    /**
     * Get when this player unlocked this achievement
     *
     * @return Unlock time
     */
    public Timestamp getUnlockTime()
    {
        return this.unlockTime;
    }

    /**
     * Get this progress id
     *
     * @return Id
     */

    public long getProgressId()
    {
        return this.progressId;
    }

    /**
     * Internal
     * Unlock achievement
     */
    void unlock()
    {
        this.unlockTime = Timestamp.from(Instant.now());
        this.changed = true;
    }

    /**
     * Internal
     *
     * @return Check if this achievements progress as changed and must be updated
     */
    public boolean isChanged()
    {
        return this.changed;
    }
}
