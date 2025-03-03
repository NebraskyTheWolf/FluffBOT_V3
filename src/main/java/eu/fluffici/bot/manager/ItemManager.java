package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : FoodItemManager.java

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
import eu.fluffici.bot.api.ItemInformationBuilder;
import eu.fluffici.bot.api.hooks.IItemManager;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The FoodItemManager class is responsible for managing food items in the FluffBOT application.
 * It implements the IFoodManager interface, which defines the methods for initializing the manager,
 * adding food items, and retrieving food items.
 */
@Getter
public class ItemManager implements IItemManager {
    private final FluffBOT fluffbot;

    public ItemManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    private final Gson gson = new Gson();
    private final Map<String, ItemInformationBuilder> rewards = new LinkedHashMap<>();
    private final File root = new File(System.getProperty("user.dir") + "/data/items");
    private final Object[] lock = new Object[] {};

    /**
     * Initializes the ItemManager.
     *
     * If the root directory does not exist, it creates the directory and logs the creation.
     * Then, it loads all JSON food files from the root directory.
     */
    @Override
    public void init() {
        if (!this.root.exists()) {
            this.root.mkdirs();
        }

        this.load(this.root.getAbsolutePath());
    }

    /**
     * Loads all JSON food files from the specified directory path.
     *
     * @param directoryPath the path of the directory containing the JSON food files
     */
    private void load(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::loadFoodFile);
        } catch (IOException e) {
            this.fluffbot.getLogger().error("Unable to visit '" + directoryPath + "' directory.", e);
            e.printStackTrace();
        }
    }

    /**
     * Loads a JSON food file from the specified path and adds its information to the ItemManager.
     *
     * @param filePath the path of the JSON food file to be loaded
     */
    private void loadFoodFile(Path filePath) {
        try {
            ItemInformationBuilder food = gson.fromJson(Files.readString(filePath), ItemInformationBuilder.class);
            addItem(food.getSlug(), ItemInformationBuilder.builder()
                    .slug(food.getSlug())
                    .damageFactor(food.getDamageFactor())
                    .build()
            );


        } catch (IOException e) {
            this.fluffbot.getLogger().error("Unable to load '" + filePath + "' food file.", e);
            e.printStackTrace();
        }
    }

    /**
     * Adds an item to the ItemManager with the specified item name and ItemInformationBuilder.
     *
     * @param itemName   the name of the item to be added
     * @param itemBuilder the ItemInformationBuilder object containing the item information
     * @throws IllegalArgumentException if the item name is already registered
     */
    @Override
    public void addItem(String itemName, ItemInformationBuilder itemBuilder) {
        System.out.println(itemName);
        if (this.rewards.containsKey(itemName))
            throw new IllegalArgumentException(String.format("Food '%s' already registered.", itemName));
        this.rewards.put(itemName, itemBuilder);
    }


    /**
     * Retrieves the item information for the specified slug.
     *
     * @param slug the slug of the item
     * @return the ItemInformationBuilder object containing the item information
     */
    @Override
    public ItemInformationBuilder getItem(String slug) {
        return this.rewards.get(slug);
    }
}
