package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : RewardManager.java

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


import com.google.gson.Gson;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.Rarity;
import eu.fluffici.bot.api.hooks.IRewardManager;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.rewards.ItemBuilder;
import eu.fluffici.bot.api.rewards.RewardBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
/**
 * The RewardManager class is responsible for managing rewards in the Fluffbot application.
 * It initializes reward data, loads rewards from a specified directory,
 * and provides methods for adding rewards, drawing random rewards, and retrieving rewards.
 */
@Getter
public class RewardManager implements IRewardManager {
    private final FluffBOT fluffbot;

    public RewardManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    private final Gson gson = new Gson();
    private final Map<Rarity, Integer> previousRewards = new LinkedHashMap<>();
    private final Map<String, RewardBuilder> rewards = new LinkedHashMap<>();
    private final File root = new File(System.getProperty("user.dir") + "/data/rewards");
    private final Object[] lock = new Object[] {};

    /**
     * Initializes the RewardManager by setting up the previous rewards map and
     * loading rewards from a specified directory.
     */
    @Override
    public void init() {
        synchronized (this.lock) {
            this.previousRewards.put(Rarity.COMMON, 0);
            this.previousRewards.put(Rarity.UNCOMMON, 0);
            this.previousRewards.put(Rarity.RARE, 0);
            this.previousRewards.put(Rarity.EPIC, 0);
            this.previousRewards.put(Rarity.LEGENDARY, 0);
            this.previousRewards.put(Rarity.MYTHIC, 0);

            if (!this.root.exists()) {
                this.root.mkdirs();

                this.fluffbot.getLogger().info("'%s' reward folder created.", this.root.getAbsolutePath());
            }

            this.loadRewards(this.root.getAbsolutePath());
        }
    }

    /**
     * Loads reward files from the specified directory.
     *
     * @param directoryPath the path of the directory containing reward files
     */
    private void loadRewards(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::loadRewardFile);
        } catch (IOException e) {
            this.fluffbot.getLogger().error("Unable to visit '" + directoryPath + "' directory.", e);
            e.printStackTrace();
        }
    }

    /**
     * Loads a reward file from the specified file path.
     *
     * @param filePath the path of the reward file to load
     */
    private void loadRewardFile(Path filePath) {
        try {
            String content = Files.readString(filePath);

            RewardBuilder reward = gson.fromJson(content, RewardBuilder.class);
            if (reward != null) {
                createReward(reward);
            }
        } catch (IOException e) {
            this.fluffbot.getLogger().error("Unable to load '" + filePath + "' reward file.", e);
            e.printStackTrace();
        }
    }

    /**
     * Creates a reward with the given reward builder.
     *
     * @param reward the reward builder used to create the reward
     */
    private void createReward(@NotNull RewardBuilder reward) {
        for (ItemBuilder item : reward.getItems()) {
            addReward(reward.getName(), RewardBuilder.builder()
                    .name(reward.getName())
                    .rarity(reward.getRarity())
                    .build()
                    .addItem(ItemBuilder.builder()
                            .itemId(item.getItemId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .rarity(item.getRarity())
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .isRewardOnly(item.isRewardOnly())
                            .isAccessible(item.isAccessible())
                            .build()
                    )
            );

            this.fluffbot.getLogger().info("Items: ");
            this.fluffbot.getLogger().info(" -> %s", item.toString());
        }

        if (this.rewards.isEmpty()) {
            this.fluffbot.getLogger().info("No rewards was found.");
        } else {
            this.fluffbot.getLogger().info("Rewards '%s' successfully loaded.", this.rewards.size());
        }
    }

    /**
     * Adds a reward to the rewards map with the specified reward name and reward builder.
     *
     * @param rewardName    the name of the reward
     * @param rewardBuilder the reward builder used to create the reward
     * @throws IllegalArgumentException if a reward with the same name already exists
     */
    @Override
    public void addReward(String rewardName, RewardBuilder rewardBuilder) {
        this.rewards.put(rewardName, rewardBuilder);
    }

    /**
     * This method is responsible for simulating a draw of a random reward based on
     * the chances of each reward and a randomly calculated multiplier.
     * <p>
     * First, the function calculates a multiplier using the lottery function.
     * This multiplier is a random value affected by several factors such as whether
     * a booster is active, a base multiplier, and outputs a suitable multiplier value.
     * <p>
     * The function then calculates the total weight of all rewards by multiplying
     * their individual chances by the calculated multiplier. The total weight gives
     * us the sum of all probabilities after they have been adjusted by the multiplier.
     * <p>
     * A random value is then generated, between 0 and the total weight.
     * This random value will decide which reward is selected.
     * <p>
     * The rewards are iterated over, and for each reward, its chance (multiplied by
     * the multiplier) is subtracted from the random value. When the random value
     * becomes less than zero, the current reward is returned. This creates a random
     * draw where the probability of each reward is proportional to its
     * chance adjusted by the multiplier.
     * <p>
     * If all rewards have zero chance, or an error occurs resulting in no reward
     * being selected, the function will return null indicating that no reward was drawn.
     *
     * @return the selected RewardBuilder, or null if no reward is drawn
     */
    @Override
    public RewardBuilder drawRandomReward(PlayerBean target) {
        double multiplier = this.fluffbot.getLevelUtil().lottery();

        // Create a navigable map where keys are summed weights and values are rewards
        NavigableMap<Double, RewardBuilder> weightedMap = new TreeMap<>();
        double totalWeight = 0.0;
        for (RewardBuilder reward : rewards.values()) {
            totalWeight += this.adjustForBias(reward, multiplier);
            weightedMap.put(totalWeight, reward);
        }

        // Select a random weight between 0 and total weight
        double randomWeight = Math.random() * totalWeight;

        // Find the first key in weightedMap that is greater than or equal to randomly generated weight
        Map.Entry<Double, RewardBuilder> selectedEntry = weightedMap.ceilingEntry(randomWeight);

        if (selectedEntry != null) {
            RewardBuilder selectedReward = selectedEntry.getValue();
            selectedReward.setMultiplier(multiplier);
            selectedReward.setRamdomizer(randomWeight);
            this.updatePreviousRewards(selectedReward);
            return selectedReward;
        }
        return null;
    }

    /**
     * Retrieves a RewardBuilder object for the given reward name.
     *
     * @param rewardName the name of the reward
     * @return the RewardBuilder object for the given reward name, or null if no reward is found
     */
    @Override
    public RewardBuilder getReward(String rewardName) {
        return this.rewards.get(rewardName);
    }

    /**
     * Adjusts the reward's chance for bias based on its rarity and previous rewards.
     * The adjusted chance is calculated by multiplying the reward's rarity chance,
     * the bias factor calculated from previous rewards of the same rarity, and
     * the provided multiplier.
     *
     * @param reward The reward to adjust the chance for bias.
     * @param multiplier The multiplier to apply to the adjusted chance.
     * @return The adjusted chance for the reward.
     */
    private double adjustForBias(@NotNull RewardBuilder reward, double multiplier) {
        return reward.getRarity().getChance() * calculateBiasFactor(reward) * multiplier;
    }

    /**
     * Calculates the bias factor for a given reward.
     * The bias factor is determined by the previous rewards of the same rarity.
     *
     * @param reward The reward to calculate the bias factor for.
     * @return The calculated bias factor.
     */
    private int calculateBiasFactor(@NotNull RewardBuilder reward) {
        return this.previousRewards.getOrDefault(reward.getRarity(), 0) + 1;
    }

    /**
     * Updates the previous rewards map with the given reward.
     * The previous rewards map keeps track of the number of rewards of each rarity
     * that have been drawn previously.
     *
     * @param reward The reward to update the previous rewards map with.
     */
    private void updatePreviousRewards(@NotNull RewardBuilder reward) {
        this.previousRewards.put(reward.getRarity(), this.previousRewards.getOrDefault(reward.getRarity(), 0) + 1);
    }
}
