package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : ItemCraftManager.java

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
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.crafting.ItemCraftBuilder;
import eu.fluffici.bot.api.crafting.ItemCraftingMaterials;
import eu.fluffici.bot.api.hooks.ICraftManager;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ItemCraftManager class is responsible for managing item crafts.
 * It allows for the initialization of the craft manager, loading crafts from JSON files,
 * adding new crafts, and retrieving crafts by slug.
 *
 * This class implements the ICraftManager interface.
 *
 * @see ICraftManager
 */
@Getter
public class ItemCraftManager implements ICraftManager {
    private final FluffBOT fluffbot;

    public ItemCraftManager(FluffBOT fluffbot) {
        this.fluffbot = fluffbot;
    }

    private final Gson gson = new Gson();
    private final Map<String, ItemCraftBuilder> rewards = new LinkedHashMap<>();
    private final File root = new File(System.getProperty("user.dir") + "/data/crafting");
    private final Object[] lock = new Object[] {};

    /**
     * Initializes the ItemCraftManager.
     * This method creates the required folder if it doesn't exist,
     * loads all JSON food files from the folder, and adds the crafts to the ItemCraftManager.
     */
    @Override
    public void init() {
        synchronized (this.lock) {
            if (!this.root.exists()) {
                this.root.mkdirs();
            }

            this.load(this.root.getAbsolutePath());
        }
    }

    /**
     * Loads all JSON food files from the specified directory path and adds the crafts to the ItemCraftManager.
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
     * Loads a JSON food file and adds the craft to the ItemCraftManager.
     *
     * @param filePath the path of the JSON food file to be loaded
     */
    private void loadFoodFile(Path filePath) {
        try {
            ItemCraftBuilder food = gson.fromJson(Files.readString(filePath), ItemCraftBuilder.class);
            addCraft(food.getSlug(), ItemCraftBuilder.builder()
                    .slug(food.getSlug())
                    .quantity(food.getQuantity())
                    .materials(food.getMaterials())
                    .requiredLevel(food.getRequiredLevel())
                    .build()
            );
        } catch (IOException e) {
            this.fluffbot.getLogger().error("Unable to load '" + filePath + "' food file.", e);
            e.printStackTrace();
        }
    }

    /**
     * Adds a craft to the ItemCraftManager.
     *
     * @param craftName    the name of the craft to be added
     * @param craftBuilder the builder object representing the craft
     * @throws IllegalArgumentException if a craft with the same name is already registered
     */
    @Override
    public void addCraft(String craftName, ItemCraftBuilder craftBuilder) {
        if (this.rewards.containsKey(craftName))
            throw new IllegalArgumentException(String.format("Craft '%s' already registered.", craftName));
        this.rewards.put(craftName, craftBuilder);
    }

    /**
     * Retrieves the ItemCraftBuilder object associated with the specified slug.
     *
     * @param slug the unique identifier for the craft
     * @return the ItemCraftBuilder object if found, null otherwise
     */
    @Override
    public ItemCraftBuilder getCraft(String slug) {
        return this.rewards.get(slug);
    }

    /**
     * Checks if the user has the required item for crafting.
     *
     * @param user      the UserSnowflake object representing the user
     * @param itemSlug  the unique identifier for the item
     * @return true if the user has the required item, false otherwise
     */
    @Override
    @SneakyThrows
    public boolean hasRequiredItem(UserSnowflake user, String itemSlug) {
        ItemCraftBuilder crafting = this.getCraft(itemSlug);
        if (crafting == null)
            throw new IllegalArgumentException("Invalid item slug: " + itemSlug);

        InventoryBuilder inventory = this.fluffbot.getGameServiceManager().fetchInformation(user);
        List<InventoryItem> userItems = inventory.getItems();

        for (ItemCraftingMaterials requiredItem : crafting.getMaterials()) {
            boolean hasRequiredQuantity = userItems.stream().anyMatch(item ->
                    item.getItemSlug().equals(requiredItem.getMaterialSlug()) &&
                            item.getQuantity() >= requiredItem.getQuantity()
            );
            if (!hasRequiredQuantity) {
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieves the required items for crafting a specific item.
     *
     * @param user     The UserSnowflake object representing the user.
     * @param itemSlug The unique identifier for the item.
     * @return A List of ItemDescriptionBean objects representing the required items. Returns an empty list if there are no required items.
     */
    @Override
    @SneakyThrows
    public List<ItemDescriptionBean> getRequiredItems(UserSnowflake user, String itemSlug) {
        ItemCraftBuilder crafting = this.getCraft(itemSlug);
        if (crafting == null)
            throw new IllegalArgumentException("Invalid item slug: " + itemSlug);
        return crafting.getMaterials().stream()
                .map(material -> {
                    try {
                        return this.fluffbot.getGameServiceManager().fetchItem(material.getMaterialSlug());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a string listing all missing items required for crafting a specific item.
     *
     * @param user     the UserSnowflake object representing the user.
     * @param itemSlug the unique identifier for the item.
     * @return a String listing all missing items and their quantities. Returns an empty string if no items are missing.
     */
    @Override
    @SneakyThrows
    public String getMissingItems(UserSnowflake user, String itemSlug) {
        ItemCraftBuilder crafting = this.getCraft(itemSlug);
        if (crafting == null)
            throw new IllegalArgumentException("Invalid item slug: " + itemSlug);

        InventoryBuilder inventory = this.fluffbot.getGameServiceManager().fetchInformation(user);
        List<InventoryItem> userItems = inventory.getItems();

        StringBuilder missingItems = new StringBuilder();

        for (ItemCraftingMaterials requiredItem : crafting.getMaterials()) {
            InventoryItem userItem = userItems.stream()
                    .filter(item -> item.getItemSlug().equals(requiredItem.getMaterialSlug()))
                    .findFirst()
                    .orElse(null);

            if (userItem == null || userItem.getQuantity() < requiredItem.getQuantity()) {
                int missingQuantity = (userItem == null) ? requiredItem.getQuantity() : requiredItem.getQuantity() - userItem.getQuantity();
                missingItems.append(this.fluffbot.getLanguageManager().get("command.craft.missing_items", this.fluffbot.getGameServiceManager()
                        .fetchItem(requiredItem.getMaterialSlug()).getItemName(), missingQuantity)).append("\n");
            }
        }

        return missingItems.toString().trim();
    }


    /**
     * Checks if the user can craft a specific item.
     *
     * @param user     the UserSnowflake object representing the user.
     * @param itemSlug the unique identifier for the item.
     * @return a Pair object representing whether the user can craft the item and an optional error message.
     */
    @Override
    public Pair<Boolean, String> canCraft(UserSnowflake user, String itemSlug) {
        PlayerBean player = this.fluffbot.getUserManager().fetchUser(user);
        ItemCraftBuilder crafting = this.getCraft(itemSlug);
        if (crafting == null)
            throw new IllegalArgumentException("Invalid item slug: " + itemSlug);

        boolean isInventoryFull = this.fluffbot.getUserManager().isInventoryFull(user);
        boolean isLevelAcquired = player.getLevel() >= crafting.getRequiredLevel();
        boolean hasRequiredItems = this.hasRequiredItem(user, itemSlug);

        if (!isLevelAcquired)
            return Pair.of(false, "command.craft.level_insufficient");
        if (isInventoryFull)
            return Pair.of(false, "command.craft.inventory.full");
        if (!hasRequiredItems)
            return Pair.of(false, "command.craft.insufficient_materials");

        return Pair.of(true, null);
    }

    /**
     * Crafts an item for the user if all conditions are met.
     *
     * @param user     the UserSnowflake object representing the user.
     * @param itemSlug the unique identifier for the item to be crafted.
     * @return a Pair object representing whether the crafting was successful and an optional error message.
     */
    @Override
    public Pair<Boolean, String> craftItem(UserSnowflake user, String itemSlug) {
        try {
            Pair<Boolean, String> canCraftResult = canCraft(user, itemSlug);
            if (!canCraftResult.getLeft()) {
                return canCraftResult;
            }

            ItemCraftBuilder crafting = this.getCraft(itemSlug);
            if (crafting == null)
                throw new IllegalArgumentException("Invalid item slug: " + itemSlug);

            InventoryBuilder inventory = this.fluffbot.getGameServiceManager().fetchInformation(user);
            List<InventoryItem> userItems = inventory.getItems();

            for (ItemCraftingMaterials requiredItem : crafting.getMaterials()) {
                InventoryItem userItem = userItems.stream()
                        .filter(item -> item.getItemSlug().equals(requiredItem.getMaterialSlug()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("User does not have the required item: " + requiredItem.getMaterialSlug()));

                userItem.setQuantity(userItem.getQuantity() - requiredItem.getQuantity());

                FluffBOT.getInstance().getGameServiceManager().decrementQuantity(user, fluffbot.getGameServiceManager().fetchItem(userItem.getItemId()), requiredItem.getQuantity());
            }

            ItemDescriptionBean craftedItem = this.fluffbot.getGameServiceManager().fetchItem(crafting.getSlug());
            this.fluffbot.getUserManager().addItem(user, craftedItem, crafting.getQuantity());
            this.fluffbot.getLogger().info("Crafted item: " + craftedItem.getItemName());
            return Pair.of(true, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Pair.of(true, "");
    }
}
