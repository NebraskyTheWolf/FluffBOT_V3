package eu.fluffici.bot.modules.achievements.impl;

/*
---------------------------------------------------------------------------------
File Name : AchievementManager.java

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
import eu.fluffici.bot.api.achievement.*;
import eu.fluffici.bot.api.beans.achievements.AchievementBean;
import eu.fluffici.bot.api.beans.achievements.AchievementCategoryBean;
import eu.fluffici.bot.api.beans.achievements.AchievementProgressBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AchievementManager implements IAchievementManager {
    private Achievement[] achievementsCache;
    private AchievementCategory[] achievementCategoriesCache;
    private final FluffBOT fluffbot;

    /**
     * Initializes an instance of the AchievementManager class.
     * This constructor takes a FluffBOT instance as a parameter and initializes the necessary fields.
     * It also asynchronously loads the achievements, synchronizes the members, and performs an update.
     * If an error occurs during the process, it is logged.
     *
     * @param fluffbot The FluffBOT instance.
     */
    public AchievementManager(FluffBOT fluffbot)
    {
        this.fluffbot = fluffbot;
        this.achievementsCache = new Achievement[0];
        this.achievementCategoriesCache = new AchievementCategory[0];

        CompletableFuture.runAsync(this::loadAchievements)
                .thenRunAsync(this::syncMembers)
                .thenRunAsync(this::update)
                .exceptionally(ex -> {
                    this.fluffbot.getLogger().error("A error occurred while loading the achievements data.", ex);
                    return null;
                });
    }

    /**
     * Loads the achievements into the cache.
     */
    private void loadAchievements() {
        try {
            List<AchievementCategoryBean> categoryBeanList = this.fluffbot.getGameServiceManager().getAchievementCategories();
            List<AchievementCategory> categories = new ArrayList<>();

            categoryBeanList.forEach(achievementCategoryBean -> categories.add(new AchievementCategory(achievementCategoryBean.getCategoryId(), achievementCategoryBean.getCategoryName(), achievementCategoryBean.getIcon(), achievementCategoryBean.getCategoryDescription().split("/n"), achievementCategoryBean.getParentId() < categories.size() && achievementCategoryBean.getParentId() >= 0 ? categories.get(achievementCategoryBean.getParentId()) : null)));

            List<AchievementBean> allAchievements = this.fluffbot.getGameServiceManager().getAchievements();
            int n = allAchievements.size();
            int n2 = categoryBeanList.size();

            Achievement[] achievementsCache = new Achievement[n == 0 ? 0 : Math.max(n, allAchievements.get(n - 1).getAchievementId())];

            for (AchievementBean bean : allAchievements)
            {
                AchievementCategory category = categories.stream().filter(achievementCategory -> achievementCategory.getId() == bean.getCategoryId()).findFirst().orElse(null);

                if (bean.getProgressTarget() == 1)
                    achievementsCache[bean.getAchievementId() - 1] = new Achievement(bean.getAchievementId(), bean.getAchievementName(), category, bean.getAchievementDescription().split("/n"));
                else
                    achievementsCache[bean.getAchievementId() - 1] = new IncrementationAchievement(bean.getAchievementId(), bean.getAchievementName(), category, bean.getAchievementDescription().split("/n"), bean.getProgressTarget());
            }

            AchievementCategory[] achievementCategoriesCache = new AchievementCategory[n2 == 0 ? 0 : Math.max(n2, categories.get(n2 - 1).getId())];
            categories.forEach(achievementCategory -> achievementCategoriesCache[achievementCategory.getId() - 1] = achievementCategory);

            this.achievementsCache = achievementsCache;
            this.achievementCategoriesCache = achievementCategoriesCache;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Synchronizes the members of the Discord server with the achievements data.
     * This method retrieves the list of non-bot, non-system users from the JDA instance,
     * and for each user, loads their achievement progress from the GameServiceManager.
     * The loaded achievements are then added to the corresponding Achievement instance.
     * If an exception occurs during the process, it is logged.
     */
    private void syncMembers() {
        List<User> users = this.fluffbot.getJda().getUsers()
                .stream()
                .filter(Objects::nonNull)
                .filter(user -> !user.isBot())
                .filter(user -> !user.isSystem())
                .toList();

        for (User user : users) {
            try {
                List<AchievementProgressBean> list = this.fluffbot.getGameServiceManager().getAchievementProgresses(user.getId());
                this.fluffbot.getLogger().debug("Achievement progression loaded " + list.size() + " for " + user.getEffectiveName() + "(" + user.getId() + ")");
                list.forEach(bean ->
                {
                    Achievement achievement = this.getAchievementByID(bean.getAchievementId());
                    if (achievement != null)
                        achievement.addProgress(user.getId(), bean.getProgressId(), bean.getProgress(), bean.getStartDate(), bean.getUnlockDate());
                });
                list.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    public void update() {
        this.fluffbot.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            for (User user : this.fluffbot.getJda().getUsers()) {
                if (user.isBot() || user.isSystem())
                    continue;

                CompletableFuture.runAsync(() -> {
                    for (Achievement achievement : this.achievementsCache)
                    {
                        AchievementProgress progress = achievement.getProgress(user.getId());

                        if (progress == null || !progress.isChanged())
                            continue;

                        AchievementProgressBean bean = new AchievementProgressBean(user.getId(), progress.getProgressId(), achievement.getID(), progress.getProgress(), progress.getStartTime(), progress.getUnlockTime());

                        try
                        {
                            if (progress.getProgressId() == -1) {
                                int newProgressId = this.fluffbot.getGameServiceManager().createAchievementProgress(user.getId(), bean);
                                progress.setProgressId(newProgressId);
                            } else {
                                this.fluffbot.getGameServiceManager().updateAchievementProgress(bean);
                            }
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }, 20, 60, TimeUnit.SECONDS);
    }

    @Override
    public void incrementAchievement(String userId, IncrementationAchievement incrementationAchievement, int amount, AchievementSucceedCallback callback)
    {
        incrementationAchievement.increment(userId, amount, callback);
    }

    @Override
    public void incrementAchievement(String uuid, int id, int amount)
    {
        Achievement achievement = this.getAchievementByID(id);
        if (achievement instanceof IncrementationAchievement)
            ((IncrementationAchievement)achievement).increment(uuid, amount, (userId, achievement1, progress) -> {
                if (achievement1.isUnlocked(userId))
                    this.sendMessage(UserSnowflake.fromId(uuid), achievement1, progress);
            });
        else
            throw new IllegalArgumentException("Achievement is not incremental");
    }

    @Override
    public void unlock(UserSnowflake user, int id) {
        Achievement achievement = this.getAchievementByID(id);
        if (!(achievement instanceof IncrementationAchievement))
            achievement.unlock(user.getId(), (userId, achievement1, progress) -> {
                if (achievement1.isUnlocked(userId))
                    this.sendMessage(user, achievement1, progress);
            });
        else
            throw new IllegalArgumentException("Achievement is incremental");
    }

    @Override
    public void incrementAchievements(String userId, int[] ids, int amount)
    {
        for (int id : ids)
        {
            Achievement achievement = this.getAchievementByID(id);
            if (achievement instanceof IncrementationAchievement)
                ((IncrementationAchievement)achievement).increment(userId, amount, (user, achievement1, progress) -> {
                    if (achievement1.isUnlocked(userId))
                        this.sendMessage(UserSnowflake.fromId(user), achievement1, progress);
                });
        }
    }

    @Override
    public Achievement getAchievementByID(int id)
    {
        for (Achievement achievement : this.achievementsCache)
            if (achievement.getID() == id) {
                return achievement;
            }

        return null;
    }

    @Override
    public AchievementCategory getAchievementCategoryByID(int id)
    {
        for (AchievementCategory achievementCategory : this.achievementCategoriesCache)
            if (achievementCategory.getId() == id)
                return achievementCategory;

        return null;
    }

    @Override
    public List<Achievement> getAchievements()
    {
        return Arrays.asList(this.achievementsCache);
    }

    @Override
    public List<AchievementCategory> getAchievementsCategories()
    {
        return Arrays.asList(this.achievementCategoriesCache);
    }

    @Override
    public boolean isUnlocked(String uuid, Achievement achievement)
    {
        return achievement.isUnlocked(uuid);
    }

    @Override
    public boolean isUnlocked(String uuid, int id)
    {
        Achievement achievement = this.getAchievementByID(id);
        return achievement.isUnlocked(uuid);
    }

    private final int BASE_REWARD_AMOUNT = 10;

    @Override
    public void sendMessage(UserSnowflake userId, Achievement achievement, AchievementProgress progress) {
        User user = this.fluffbot.getJda().getUserById(userId.getId());

        ZoneId timezone = ZoneId.of("Europe/Prague");
        LocalDate unlockDate = progress.getUnlockTime().toInstant()
                .atZone(timezone)
                .toLocalDate();
        LocalDate today = LocalDate.now(timezone);

        if (!unlockDate.equals(today)) {
            return;
        }

        double tokens = this.fluffbot.getLevelUtil().lottery();
        int finalAmount = Math.abs((int) (this.BASE_REWARD_AMOUNT * tokens));

        // A safety feature is in place to prevent the system from becoming excessively generous.
        if (finalAmount >= 40) {
            finalAmount = finalAmount / 2;
        }

        assert user != null;
        Objects.requireNonNull(Objects.requireNonNull(this.fluffbot.getJda().getGuildById(this.fluffbot.getDefaultConfig().getProperty("main.guild"))).getTextChannelById(this.fluffbot.getDefaultConfig().getProperty("channel.achievements"))).sendMessageEmbeds(
                this.fluffbot.getEmbed().simpleFieldedEmbed(
                                this.fluffbot.getLanguageManager().get("event.achievement.unlocked"),
                                this.fluffbot.getLanguageManager().get("event.achievement.description", achievement.getDisplayName(), progress.getProgress()),
                        Color.GREEN,
                        Collections.emptyList()
                ).setTimestamp(progress.getUnlockTime().toInstant())
                        .addField("Odměna", finalAmount + " <:flufftoken:820777573046812693>", false)
                        .setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl())
                        .build()
        ).setContent(user.getAsMention()).queue();

        PlayerBean player = this.fluffbot.getUserManager().fetchUser(user);
        if (player != null) {
            this.fluffbot.getUserManager().addCoins(player, finalAmount);
        }
    }
}
