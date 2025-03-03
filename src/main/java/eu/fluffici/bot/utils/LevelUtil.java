package eu.fluffici.bot.utils;

/*
---------------------------------------------------------------------------------
File Name : LevelUtil.java

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
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.scheduler.channel.SendDailyStatistics;
import eu.fluffici.bot.logger.Logger;

import java.util.Random;
public class LevelUtil {

    private final Logger logger = new Logger("LevelSystem");

    private final FluffBOT instance;
    private final int XP_PER_MESSAGE;
    private final int XP_PER_HOUR;
    private final int XP_THRESHOLD_FACTOR;
    private final double XP_EXPONENTIAL_FACTOR;

    public static final int MAX_LEVEL = 150; // Define the maximum level

    public LevelUtil(FluffBOT instance) {
        this.instance = instance;
        this.XP_PER_MESSAGE = Integer.parseInt(this.instance.getDefaultConfig().getProperty("level.experience", "30"));
        this.XP_PER_HOUR = Integer.parseInt(this.instance.getDefaultConfig().getProperty("level.voice.experience", "60"));
        this.XP_THRESHOLD_FACTOR = Integer.parseInt(this.instance.getDefaultConfig().getProperty("level.threshold.factor", "5"));
        this.XP_EXPONENTIAL_FACTOR = Double.parseDouble(this.instance.getDefaultConfig().getProperty("level.exponential.factor", "3.5"));

        this.logger.setDebug(this.instance.getDebug());

        this.logger.debug("\n");
        this.logger.debug("-- ----------------------- -------------------- --");
        this.logger.debug("(Exponential factor): " + this.XP_EXPONENTIAL_FACTOR);
        this.logger.debug("(Threshold factor): " + this.XP_THRESHOLD_FACTOR);
        this.logger.debug("(Per message): " + this.XP_PER_MESSAGE);
        for (int i = 1; i < 10; i++) {
            this.logger.debug("(Estimated XP for level " + i + "): " + this.calculateXPNeeded(i));
            this.logger.debug("(Estimated MSG required for level " + i + "): " + this.getRemainingMessagesToNextLevelFromXP(i));
        }
        this.logger.debug("-- ----------------------- -------------------- --");
        this.logger.debug("\n");
    }

    public long calculateXPNeeded(long currentLevel) {
        return (long) (this.XP_THRESHOLD_FACTOR * (Math.pow(currentLevel, this.XP_EXPONENTIAL_FACTOR) + 100 * currentLevel + 200)) * (int) Math.abs(SendDailyStatistics.XP_BOOST);
    }

    public boolean addXP(PlayerBean player) {
        long originalLevel = player.getLevel();
        if (originalLevel >= MAX_LEVEL) {
            return false;
        }

        double multiplier = this.lottery();

        int boostedXP = (int) Math.round(this.XP_PER_MESSAGE * this.instance.getGlobalMultiplier() * multiplier);
        player.setExperience(player.getExperience() + boostedXP);

        int newLevel = calculateLevel(player);
        player.setLevel(newLevel);

        this.instance.getUserManager().saveUser(player);
        return newLevel > originalLevel;
    }

    public boolean addXPVoice(PlayerBean player, int hours) {
        if (hours <= 0) {
            return false;
        }

        long originalLevel = player.getLevel();
        if (originalLevel >= MAX_LEVEL) {
            return false;
        }

        double multiplier = this.lottery();

        int boostedXP = (int) Math.round((this.XP_PER_HOUR * hours) + (this.instance.getGlobalMultiplier() * multiplier));
        player.setExperience(player.getExperience() + boostedXP);

        int newLevel = calculateLevel(player);
        player.setLevel(newLevel);

        this.instance.getUserManager().saveUser(player);
        return newLevel > originalLevel;
    }

    public long getXp(PlayerBean player) {
        return player.getExperience();
    }

    public int calculateLevel(PlayerBean player) {
        long totalXp = getXp(player);
        int level = 0;
        while (totalXp >= calculateXPNeeded(level + 1)) {
            level++;
        }
        return level;
    }

    public long getTotalXp(PlayerBean player) {
        return getXp(player);
    }

    public long getMaxExp() {
        return this.calculateXPNeeded(MAX_LEVEL);
    }

    public long getXpToNextLevel(PlayerBean player) {
        long totalXP = getXp(player);
        int currentLevel = calculateLevel(player);
        long xpNeededForNextLevel = calculateXPNeeded(currentLevel + 1);

        // Calculate the remaining XP needed to reach the next level
        return xpNeededForNextLevel - totalXP;
    }

    public int getRemainingMessagesToNextLevel(PlayerBean player) {
        long remainingXpToNextLevel = getXpToNextLevel(player);
        if (player.getLevel() >= MAX_LEVEL) {
            return 0;
        }

        return (int)Math.ceil((double)remainingXpToNextLevel / this.XP_PER_MESSAGE);
    }

    public long getRemainingMessagesToNextLevelFromXP(int currentLevel) {
        long remainingXpToNextLevel = calculateXPNeeded(currentLevel);
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }

        return (long)Math.ceil((double)remainingXpToNextLevel / this.XP_PER_MESSAGE);
    }

    public double getRemainingHoursToNextLevelFromXP(PlayerBean playerBean) {
        if (playerBean == null) {
            throw new IllegalArgumentException("PlayerBean cannot be null");
        }

        long currentLevel = playerBean.getLevel();
        if (currentLevel >= MAX_LEVEL) {
            return 0.0;
        }

        long currentExperience = playerBean.getExperience();

        if (currentLevel < 0 || currentExperience < 0) {
            throw new IllegalArgumentException("Level and experience must be non-negative");
        }

        long xpNeededForNextLevel = calculateXPNeeded(currentLevel + 1);

        // Ensure experience does not exceed the experience needed for the next level
        long remainingXpToNextLevel = xpNeededForNextLevel - currentExperience;
        if (remainingXpToNextLevel <= 0) {
            return 0.0; // Already at or above the required XP for the next level
        }

        return (double) remainingXpToNextLevel / this.XP_PER_HOUR;
    }

    public boolean updateLevel(PlayerBean player) {
        if (player == null) {
            return false;
        }

        long originalLevel = player.getLevel();
        int newLevel = calculateLevel(player);
        player.setLevel(newLevel);
        this.instance.getUserManager().saveUser(player);

        return newLevel > originalLevel;
    }

    public double lottery() {
        Random random = new Random();

        boolean hasBooster = random.nextBoolean();
        double baseMultiplier = random.nextInt(30);
        double rankMultiplier = baseMultiplier > 1 ? Double.parseDouble("1." + ((int)baseMultiplier < 6 ? 5 : (int)(baseMultiplier - 1))) : 1.0;
        int pearlChance = (int) ((baseMultiplier / 2) * rankMultiplier * (hasBooster ? 1.2 : 0.8));

        double multiplier = 1;
        if (random.nextInt(100) < pearlChance) {
            multiplier = hasBooster ? 1.5 : 0.5;
        }

        return multiplier;
    }
}