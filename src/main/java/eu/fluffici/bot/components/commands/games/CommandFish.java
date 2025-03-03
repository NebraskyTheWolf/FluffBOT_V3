package eu.fluffici.bot.components.commands.games;

/*
---------------------------------------------------------------------------------
File Name : CommandFish.java

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
import eu.fluffici.bot.api.Rarity;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.fish.environment.Environment;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_FISH;
import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

@CommandHandle
public class CommandFish extends Command {

    private final Random rand = new Random();

    public CommandFish() {
        super("fish", "A game where you can get some fish!", CommandCategory.GAMES);

        this.getOptions().put("noSelfUsage", true);
        this.getOptions().put("rate-limit", true);
    }

    @Override
    @SneakyThrows
    public void execute(@NotNull CommandInteraction interaction) {
        Pair<Boolean, String> result = this.getUserManager().isEquippedOrOwned(interaction.getMember(), "fishing_rod");
        if (result.getLeft()) {
            interaction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.getLanguageManager().get("common.not_so_fast"), "https://fluffici.eu", ICON_QUESTION_MARK)
                    .setDescription(result.getRight())
                    .build()
            ).queue();
            return;
        }

       //  this.getUserManager().decreaseSatiety(interaction.getMember(), 0.000006);

        eu.fluffici.bot.api.game.fish.map.Map playerMap = new eu.fluffici.bot.api.game.fish.map.Map(300, 200);
        Environment environment = playerMap.getPrimaryEnv();

        if (environment != null) {
            double fishingChance = calculateFishingChance(environment);
            if (rand.nextDouble() < fishingChance) {
                ItemDescriptionBean fish = drawRandomFish(environment);
                if (fish != null) {
                    this.getUserManager().addItem(interaction.getMember(), fish);
                    interaction.replyEmbeds(this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("command.fish.caught_a_fish"), "https://fluffici.eu", ICON_FISH)
                            .setThumbnail((fish.getAssetPath().contains("https://") ? fish.getAssetPath() : null))
                            .setDescription(this.getLanguageManager().get(fish.getItemDesc()))
                            .addField(this.getLanguageManager().get("common.inventory.rarity"), this.getLanguageManager().get("rarity.".concat(fish.getItemRarity().name().toLowerCase())), true)
                            .addField(this.getLanguageManager().get("common.value.token"), NumberFormat.getNumberInstance().format(fish.getPriceTokens()), true)

                            .addField(this.getLanguageManager().get("common.weather"), this.getLanguageManager().get("common.weather.".concat(environment.getWeatherCondition().name().toLowerCase())), true)
                            .addField(this.getLanguageManager().get("common.humidity"), environment.getHumidity() + "%", false)
                            .addField(this.getLanguageManager().get("common.temperature"), environment.getTemperature() + "°C", true)

                            .setFooter(this.getLanguageManager().get("command.fish.shop.info"), ICON_QUESTION_MARK)
                            .build()
                    ).queue();

                    FluffBOT.getInstance()
                            .getGameServiceManager()
                            .updateDurability(
                                    interaction.getMember(),
                                    "fishing_rod",
                                    this.calculateDamage(fish.getItemRarity())
                            );
                    return;
                }
            }
        }

        FluffBOT.getInstance()
                .getGameServiceManager()
                .updateDurability(
                        interaction.getMember(),
                        "fishing_rod",
                        this.calculateDamage(Rarity.COMMON)
                );

        // No fish was caught
        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("command.fish.oh_no"), "https://fluffici.eu", ICON_FISH)
                .setDescription(this.getLanguageManager().get("command.fish.no_fish"))
                .setTimestamp(Instant.now())
                .build()
        ).queue();
    }

    /**
     * Draws a random fish based on the environment's influence on fishing.
     *
     * @param environment The environment in which fishing is taking place.
     * @return A randomly selected fish ItemDescriptionBean, or null if no fish is drawn based on the defined chance.
     */
    private ItemDescriptionBean drawRandomFish(Environment environment) {
        Map<Rarity, List<ItemDescriptionBean>> fishesByRarity =
                FluffBOT.getInstance()
                        .getGameServiceManager()
                        .fetchAllFishable()
                        .stream()
                        .collect(Collectors.groupingBy(ItemDescriptionBean::getItemRarity));

        double sunnyFactor = 1.0;
        double cloudyFactor = 0.8;
        double rainyFactor = 0.6;
        double stormyFactor = 0.4;

        return switch (environment.getWeatherCondition()) {
            case SUNNY -> getRandomFish(fishesByRarity, sunnyFactor);
            case CLOUDY -> getRandomFish(fishesByRarity, cloudyFactor);
            case RAINY -> getRandomFish(fishesByRarity, rainyFactor);
            case STORMY -> getRandomFish(fishesByRarity, stormyFactor);
        };
    }

    @Contract(pure = true)
    private int calculateDamage(@NotNull Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 4;
            case EPIC -> 8;
            case LEGENDARY -> 12;
            case MYTHIC -> 16;
        };
    }

    /**
     * Gets a random fish from the specified list of fish based on their rarity.
     *
     * @param fishesByRarity A map of fishes grouped by rarity.
     * @param factor         The factor by which the fishing chance is multiplied.
     * @return A randomly selected fish ItemDescriptionBean, or null if no fish is drawn based on the defined chance.
     */
    private ItemDescriptionBean getRandomFish(@NotNull Map<Rarity, List<ItemDescriptionBean>> fishesByRarity, double factor) {
        List<Rarity> keys = new ArrayList<>(fishesByRarity.keySet());
        Rarity randomRarity = keys.get(rand.nextInt(keys.size()));

        List<ItemDescriptionBean> fishesOfSelectedRarity = fishesByRarity.get(randomRarity);

        return fishesOfSelectedRarity.get(rand.nextInt(fishesOfSelectedRarity.size()));
    }

    /**
     * Calculates the chance of catching a fish based on the environmental factors.
     *
     * @param environment The environment in which fishing is taking place.
     * @return The calculated chance of catching a fish.
     */
    @Contract(pure = true)
    private double calculateFishingChance(@NotNull Environment environment) {
        double baseChance = 0.5;

        switch (environment.getWeatherCondition()) {
            case SUNNY:
                baseChance += 0.2;
                break;
            case CLOUDY:
                baseChance += 0.1;
                break;
            case RAINY:
                baseChance -= 0.1;
                break;
            case STORMY:
                baseChance -= 0.2;
                break;
        }

        return baseChance;
    }
}
