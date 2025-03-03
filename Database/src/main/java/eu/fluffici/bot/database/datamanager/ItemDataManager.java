/*
---------------------------------------------------------------------------------
File Name : ItemDataManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager;

import eu.fluffici.bot.api.Rarity;
import eu.fluffici.bot.api.beans.players.DropItemBuilder;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.inventory.InventoryMetadata;
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.upgrade.UpgradesBuilder;
import eu.fluffici.bot.api.upgrade.UserUpgradeBuilder;
import eu.fluffici.bot.database.GameServiceManager;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class ItemDataManager {

    private GameServiceManager instance;

    public ItemDataManager(GameServiceManager instance) {
        this.instance = instance;
    }

    /**
     * Retrieves the specified item from the player's inventory.
     *
     * @param user the UserSnowflake representing the user
     * @param item the ItemDescriptionBean representing the item to retrieve
     * @param dataSource the DataSource representing the data source for retrieving item data
     * @return the InventoryItem representing the retrieved item, or null if not found
     */
    public InventoryItem getPlayerItem(UserSnowflake user, ItemDescriptionBean item, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, quantity, durability, is_breakable, is_equipped from inventory where user_id = ? and item_id = ?")) {
                statement.setString(1, user.getId());
                statement.setInt(2, item.getItemId());

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        int quantity = resultset.getInt("quantity");
                        int durability = resultset.getInt("durability");
                        boolean isBreakable = resultset.getBoolean("is_breakable");
                        boolean isEquipped = resultset.getBoolean("is_equipped");

                        ItemDescriptionBean itemDesc = this.fetchItem(itemId, dataSource);

                        return InventoryItem.builder()
                                .itemId(itemId)
                                .quantity(quantity)
                                .name(itemDesc.getItemName())
                                .itemSlug(itemDesc.getItemSlug())
                                .description(itemDesc.getItemDesc())
                                .rarity(itemDesc.getItemRarity())
                                .durability(durability)
                                .isBreakable(isBreakable)
                                .isStackable(itemDesc.isStackable())
                                .equipmentType(itemDesc.getEquipmentType())
                                .equipmentSlug(itemDesc.getEquipmentSlug())
                                .maxDurability(itemDesc.getDurability())
                                .isEquipped(isEquipped)
                                .resistance(itemDesc.getResistance())
                                .isEatable(itemDesc.isEatable())
                                .build();
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves the specified item from the player's inventory.
     *
     * @param user the UserSnowflake representing the user
     * @param item the ItemDescriptionBean representing the item to retrieve
     * @param dataSource the DataSource representing the data source for retrieving item data
     * @return the InventoryItem representing the retrieved item, or null if not found
     */
    public InventoryItem getPlayerItem(UserSnowflake user, EquipmentSlot slot, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, quantity, durability, is_breakable, is_equipped from inventory where user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        int quantity = resultset.getInt("quantity");
                        int durability = resultset.getInt("durability");
                        boolean isBreakable = resultset.getBoolean("is_breakable");
                        boolean isEquipped = resultset.getBoolean("is_equipped");

                        ItemDescriptionBean itemDesc = this.fetchItem(itemId, dataSource);
                        if (itemDesc.getEquipmentSlug() != slot || !isEquipped)
                            continue;

                        return InventoryItem.builder()
                                .itemId(itemId)
                                .quantity(quantity)
                                .name(itemDesc.getItemName())
                                .itemSlug(itemDesc.getItemSlug())
                                .description(itemDesc.getItemDesc())
                                .rarity(itemDesc.getItemRarity())
                                .durability(durability)
                                .isBreakable(isBreakable)
                                .isStackable(itemDesc.isStackable())
                                .equipmentType(itemDesc.getEquipmentType())
                                .equipmentSlug(itemDesc.getEquipmentSlug())
                                .maxDurability(itemDesc.getDurability())
                                .isEquipped(isEquipped)
                                .resistance(itemDesc.getResistance())
                                .isEatable(itemDesc.isEatable())
                                .build();
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves the specified item from the player's inventory.
     *
     * @param user the UserSnowflake representing the user
     * @param item the ItemDescriptionBean representing the item to retrieve
     * @param dataSource the DataSource representing the data source for retrieving item data
     * @return the InventoryItem representing the retrieved item, or null if not found
     */
    public List<InventoryItem> getPlayerItemByGroup(UserSnowflake user, EquipmentSlot slot, DataSource dataSource) {
        List<InventoryItem> inventoryItems = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, quantity, durability, is_breakable, is_equipped from inventory where user_id = ?")) {
                statement.setString(1, user.getId());

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        int quantity = resultset.getInt("quantity");
                        int durability = resultset.getInt("durability");
                        boolean isBreakable = resultset.getBoolean("is_breakable");
                        boolean isEquipped = resultset.getBoolean("is_equipped");

                        ItemDescriptionBean itemDesc = this.fetchItem(itemId, dataSource);
                        if (itemDesc.getEquipmentSlug() != slot || !isEquipped)
                            continue;

                        inventoryItems.add(InventoryItem.builder()
                                .itemId(itemId)
                                .quantity(quantity)
                                .name(itemDesc.getItemName())
                                .itemSlug(itemDesc.getItemSlug())
                                .description(itemDesc.getItemDesc())
                                .rarity(itemDesc.getItemRarity())
                                .durability(durability)
                                .isBreakable(isBreakable)
                                .isStackable(itemDesc.isStackable())
                                .equipmentType(itemDesc.getEquipmentType())
                                .equipmentSlug(itemDesc.getEquipmentSlug())
                                .maxDurability(itemDesc.getDurability())
                                .isEquipped(isEquipped)
                                .resistance(itemDesc.getResistance())
                                .isEatable(itemDesc.isEatable())
                                .build());
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return inventoryItems;
    }

    /**
     * Fetches the inventory for the given user from the specified data source.
     *
     * @param user       The user for which to fetch the inventory.
     * @param dataSource The data source to fetch the inventory from.
     * @return The inventory builder object containing the retrieved inventory information.
     * @throws SQLException In case of any SQL related exception.
     */
    @SneakyThrows
    public InventoryBuilder fetchInventory(UserSnowflake user, DataSource dataSource) {
        List<InventoryItem> items = new ArrayList<>();
        List<UserUpgradeBuilder> upgrades = new ArrayList<>();
        InventoryBuilder.InventoryBuilderBuilder inventory = InventoryBuilder.builder();

        PlayerBean playerBean = this.instance.getPlayer(user.getId());

        InventoryMetadata metadata = InventoryMetadata
                .builder()
                .owner(playerBean)
                .maximumSlots(playerBean.getInventorySize())
                .maximumUpgrades(4)
                .build();
        inventory.metadata(metadata);

        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, quantity, durability, is_breakable, is_equipped from inventory where user_id = ?")) {
                    statement.setString(1, user.getId());

                    try (ResultSet resultset = statement.executeQuery()) {
                        while (resultset.next()) {
                            int itemId = resultset.getInt("item_id");
                            int quantity = resultset.getInt("quantity");
                            int durability = resultset.getInt("durability");
                            boolean isBreakable = resultset.getBoolean("is_breakable");
                            boolean isEquipped = resultset.getBoolean("is_equipped");

                            ItemDescriptionBean itemDesc = this.fetchItem(itemId, dataSource);

                            items.add(InventoryItem.builder()
                                    .itemId(itemId)
                                    .quantity(quantity)
                                    .name(itemDesc.getItemName())
                                    .itemSlug(itemDesc.getItemSlug())
                                    .description(itemDesc.getItemDesc())
                                    .rarity(itemDesc.getItemRarity())
                                    .durability(durability)
                                    .isBreakable(isBreakable)
                                    .isStackable(itemDesc.isStackable())
                                    .equipmentType(itemDesc.getEquipmentType())
                                    .equipmentSlug(itemDesc.getEquipmentSlug())
                                    .maxDurability(itemDesc.getDurability())
                                    .isEquipped(isEquipped)
                                    .resistance(itemDesc.getResistance())
                                    .isEatable(itemDesc.isEatable())
                                    .build()
                            );
                        }
                    }
                }
            }

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, upgrade_id, level from user_upgrades where user_id = ?")) {
                    statement.setString(1, user.getId());

                    try (ResultSet resultset = statement.executeQuery()) {
                        while (resultset.next()) {
                            String userId = resultset.getString("user_id");
                            int upgradeId = resultset.getInt("upgrade_id");
                            int level = resultset.getInt("level");

                            UpgradesBuilder upgrade = this.fetchUpgrade(upgradeId, dataSource);

                            UserUpgradeBuilder item = UserUpgradeBuilder.builder()
                                    .userId(userId)
                                    .upgradeId(upgradeId)
                                    .level(level)
                                    .name(upgrade.getName())
                                    .rarity(upgrade.getRarity())
                                    .description(upgrade.getDescription())
                                    .build();

                            upgrades.add(item);
                        }
                    }
                }
            }

            inventory.items(items);
            inventory.upgrades(upgrades);

            return inventory.build();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Checks if a user has a specific upgrade.
     *
     * @param user       The UserSnowflake representing the user.
     * @param upgradeId  The ID of the upgrade to check.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return true if the user has the upgrade, false otherwise.
     */
    public boolean hasUpgrade(UserSnowflake user, int upgradeId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user_id, upgrade_id, level from user_upgrades where user_id = ? and upgrade_id = ?")) {
                statement.setString(1, user.getId());
                statement.setInt(2, upgradeId);

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a user has a specific item.
     *
     * @param user       The UserSnowflake representing the user.
     * @param itemSlug   The String representing the slug of the item.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return true if the user has the item, false otherwise.
     * @throws SQLException If there is an error executing a database query.
     */
    @SneakyThrows
    public boolean hasItem(UserSnowflake user, String itemSlug, DataSource dataSource) {
        ItemDescriptionBean itemHandle = this.fetchItem(itemSlug, dataSource);
        if (itemHandle == null)
            return false;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, user_id, quantity, is_breakable, durability, is_equipped from inventory where user_id = ? and item_id = ?")) {
                statement.setString(1, user.getId());
                statement.setInt(2, itemHandle.getItemId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the specified user has the given item in their inventory with the specified quantity.
     *
     * @param user         The UserSnowflake representing the user.
     * @param itemSlug     The slug of the item.
     * @param quantity     The desired quantity of the item.
     * @param dataSource   The DataSource used to retrieve the item information and query the inventory.
     * @return True if the user has the item with the specified quantity in their inventory,
     *                     false otherwise or if an exception occurs during the process.
     */
    @SneakyThrows
    public boolean hasItem(UserSnowflake user, String itemSlug, int quantity, DataSource dataSource) {
        ItemDescriptionBean itemHandle = this.fetchItem(itemSlug, dataSource);
        if (itemHandle == null)
            return false;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, user_id, quantity, is_breakable, durability, is_equipped from inventory where user_id = ? and item_id = ?")) {
                statement.setString(1, user.getId());
                statement.setInt(2, itemHandle.getItemId());

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        return resultset.getInt("quantity") >= quantity;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a user has a specific item equipped.
     *
     * @param user       The UserSnowflake representing the user.
     * @param itemSlug   The String representing the slug of the item.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return true if the user has the item equipped, false otherwise.
     */
    @SneakyThrows
    public boolean isEquipped(UserSnowflake user, String itemSlug, DataSource dataSource) {
        ItemDescriptionBean itemHandle = this.fetchItem(itemSlug, dataSource);
        if (itemHandle == null)
            return false;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT item_id, user_id, quantity, is_breakable, durability, is_equipped from inventory where user_id = ? and item_id = ? and is_equipped = 1")) {
                statement.setString(1, user.getId());
                statement.setInt(2, itemHandle.getItemId());

                try (ResultSet resultset = statement.executeQuery()) {
                    return resultset.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the durability of an item for a specific user in the inventory.
     *
     * @param user       The UserSnowflake representing the user.
     * @param itemSlug   The String representing the slug of the item.
     * @param durability The int representing the durability to be deducted.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @throws Exception If there is an error executing a database query or update.
     */
    public void updateDurability(UserSnowflake user, String itemSlug, int durability, DataSource dataSource) throws Exception {
        InventoryBuilder inventory = this.fetchInventory(user, dataSource);
        if (inventory == null)
            return;

        inventory.getItems().forEach(item -> {
            if (item.getItemSlug().equals(itemSlug)) {
                if (item.isEquipment()) {
                    int newDurability = (item.getDurability() - durability);
                    ItemDescriptionBean itemHandle = this.fetchItem(itemSlug, dataSource);

                    try (Connection connection = dataSource.getConnection()) {
                        if (newDurability <= 0) {
                            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM inventory WHERE user_id = ? AND item_id = ?")) {
                                statement.setString(1, user.getId());
                                statement.setInt(2, itemHandle.getItemId());

                                statement.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET durability = ? WHERE user_id = ? AND item_id = ?")) {
                                statement.setInt(1, newDurability);
                                statement.setString(2, user.getId());
                                statement.setInt(3, itemHandle.getItemId());

                                statement.executeUpdate();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setEquipped(UserSnowflake user, int itemId, boolean isEquipped, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET is_equipped = ? WHERE user_id = ? AND item_id = ?")) {
                statement.setBoolean(1, isEquipped);
                statement.setString(2, user.getId());
                statement.setInt(3, itemId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the details of an item from the database based on the given item ID.
     *
     * @param itemId the ID of the item to fetch
     * @param dataSource the data source to use for fetching the item
     * @return the ItemDescriptionBean containing the details of the fetched item, or null if no item found
     */
    public ItemDescriptionBean fetchItem(int itemId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, required_level, enchantability, durability, is_stackable, equipment_type, equipment_slug, required_farming_equipment, is_equipment, resistance from item_description where item_id = ? and is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, itemId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType;
                        if (isEquipment) {
                            equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));
                        } else {
                            equipmentType = EquipmentType.ITEM;
                        }

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        return new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        );
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches an internal item description from the database based on the given slug and data source.
     *
     * @param slug       The String representing the slug of the item.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return An ItemDescriptionBean object representing the fetched internal item description,
     *         or null if no internal item is found.
     */
    public ItemDescriptionBean fetchItem(String slug, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance from item_description where item_slug = ? and is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, slug);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType;
                        if (isEquipment) {
                            equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));
                        } else {
                            equipmentType = EquipmentType.ITEM;
                        }

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        return new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        );
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches an internal item description from the database based on the given slug and data source.
     *
     * @param slug       The String representing the slug of the item.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return An ItemDescriptionBean object representing the fetched internal item description,
     *         or null if no internal item is found.
     */
    public ItemDescriptionBean fetchInternalItem(String slug, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance from item_description where item_slug = ? and is_internal = 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, slug);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType;
                        if (isEquipment) {
                            equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));
                        } else {
                            equipmentType = EquipmentType.ITEM;
                        }

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        return new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        );
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches all fishable item descriptions from the database.
     *
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A List of ItemDescriptionBean objects representing the fishable item descriptions fetched from the database.
     */
    public List<ItemDescriptionBean> fetchAllFishable(DataSource dataSource) {
        List<ItemDescriptionBean> itemDescriptionBeans = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance FROM item_description WHERE is_fishable = 1  and is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType;
                        if (isEquipment) {
                            equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));
                        } else {
                            equipmentType = EquipmentType.ITEM;
                        }

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        itemDescriptionBeans.add(new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return itemDescriptionBeans;
    }

    /**
     * Fetches all unlocked items for a given level from the database.
     *
     * @param level      The int representing the level to filter the unlocked items.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A List of ItemDescriptionBean objects representing the unlocked items fetched from the database.
     */
    public List<ItemDescriptionBean> fetchAllUnlockedItems(int level, DataSource dataSource) {
        List<ItemDescriptionBean> itemDescriptionBeans = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance FROM item_description WHERE required_level = ? and is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, level);

                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        itemDescriptionBeans.add(new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return itemDescriptionBeans;
    }

    /**
     * Fetches all items from the database.
     *
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A List of ItemDescriptionBean objects representing the items fetched from the database.
     */
    public List<ItemDescriptionBean> fetchAllItems(DataSource dataSource) {
        List<ItemDescriptionBean> itemDescriptionBeans = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance FROM item_description WHERE is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        itemDescriptionBeans.add(new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return itemDescriptionBeans;
    }

    public List<ItemDescriptionBean> getAllInternalItems(DataSource dataSource) {
        List<ItemDescriptionBean> itemDescriptionBeans = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance FROM item_description WHERE is_internal = 1";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        itemDescriptionBeans.add(new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return itemDescriptionBeans;
    }

    /**
     * Fetches all purchasable items from the database.
     *
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A List of ItemDescriptionBean objects representing the purchasable items fetched from the database.
     */
    public List<ItemDescriptionBean> fetchAllItemsPurchasable(DataSource dataSource) {
        List<ItemDescriptionBean> itemDescriptionBeans = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT item_id, item_slug, item_name, asset_path, item_desc, price_coins, price_stars, item_rarity, is_purchasable, is_fishable, is_enchantable, is_craftable, is_eatable, is_drinkable, is_equipment, is_stackable, equipment_type, equipment_slug, required_farming_equipment, required_level, enchantability, durability, resistance FROM item_description WHERE is_purchasable = 1 AND is_internal = 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultset = statement.executeQuery()) {
                    while (resultset.next()) {
                        int itemId = resultset.getInt("item_id");
                        String itemName = resultset.getString("item_name");
                        String itemSlug = resultset.getString("item_slug");
                        String assetPath = resultset.getString("asset_path");
                        String itemDesc = resultset.getString("item_desc");
                        int priceCoins = resultset.getInt("price_coins");
                        int priceTokens = resultset.getInt("price_stars");
                        int durability = resultset.getInt("durability");
                        String itemRarity = resultset.getString("item_rarity");
                        boolean isPurchasable = resultset.getBoolean("is_purchasable");
                        boolean isFishable = resultset.getBoolean("is_fishable");

                        boolean isEnchantable = resultset.getBoolean("is_enchantable");
                        boolean isCraftable = resultset.getBoolean("is_craftable");
                        boolean isEatable = resultset.getBoolean("is_eatable");
                        boolean isDrinkable = resultset.getBoolean("is_drinkable");
                        boolean isStackable = resultset.getBoolean("is_stackable");
                        boolean isEquipment = resultset.getBoolean("is_equipment");
                        int requiredLevel = resultset.getInt("required_level");
                        int enchantability = resultset.getInt("enchantability");
                        double resistance = resultset.getDouble("resistance");

                        EquipmentType equipmentType = EquipmentType.valueOf(resultset.getString("equipment_type"));

                        EquipmentSlot equipmentSlug;
                        if (isEquipment) {
                            equipmentSlug = EquipmentSlot.valueOf(resultset.getString("equipment_slug"));
                        } else {
                            equipmentSlug = EquipmentSlot.NONE;
                        }

                        String farmingTool = resultset.getString("required_farming_equipment");

                        EquipmentSlot requiredFarmingEquipment;
                        if (farmingTool != null) {
                            requiredFarmingEquipment = EquipmentSlot.valueOf(farmingTool);
                        } else {
                            requiredFarmingEquipment = EquipmentSlot.NONE;
                        }

                        itemDescriptionBeans.add(new ItemDescriptionBean(
                                itemId,
                                itemSlug,
                                itemName,
                                assetPath,
                                itemDesc,
                                priceCoins,
                                priceTokens,
                                Rarity.valueOf(itemRarity),
                                isPurchasable,
                                isFishable,
                                isEnchantable,
                                isCraftable,
                                isEatable,
                                isDrinkable,
                                isEquipment,
                                isStackable,
                                equipmentType,
                                equipmentSlug,
                                requiredFarmingEquipment,
                                requiredLevel,
                                enchantability,
                                durability,
                                resistance
                        ));
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return itemDescriptionBeans;
    }

    /**
     * Fetches an upgrade from the database based on the given upgrade ID.
     *
     * @param upgradeId   The ID of the upgrade.
     * @param dataSource  The DataSource used to establish a connection to the database.
     * @return A UpgradesBuilder object representing the fetched upgrade, or null if no upgrade is found.
     */
    public UpgradesBuilder fetchUpgrade(int upgradeId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "select name, description, price, rarity from upgrades where upgrade_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, upgradeId);

                try (ResultSet resultset = statement.executeQuery()) {
                    if (resultset.next()) {
                        String upgradeName = resultset.getString("name");
                        String upgradeDesc = resultset.getString("description");
                        double priceCoins = resultset.getInt("price");
                        String upgradeRarity = resultset.getString("rarity");

                        return UpgradesBuilder
                                .builder()
                                .name(upgradeName)
                                .description(upgradeDesc)
                                .price(priceCoins)
                                .rarity(Rarity.valueOf(upgradeRarity))
                                .build();
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserts an inventory item for a specific user into the database.
     *
     * @param user       The UserSnowflake representing the user.
     * @param item       The InventoryItem representing the item to be added.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @throws Exception If there is an error executing a database query or update.
     */
    public void addInventoryItem(UserSnowflake user, InventoryItem item, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO `inventory` (item_id, user_id, quantity, is_breakable, durability, is_equipped) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, item.getItemId());
                statement.setString(2, user.getId());
                statement.setInt(3, item.getQuantity());
                statement.setBoolean(4, item.isBreakable());
                statement.setInt(5, item.getDurability());
                statement.setBoolean(6, item.isEquipped());

                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an inventory item to the given user's inventory with the specified quantity.
     *
     * @param user the user to add the inventory item to
     * @param item the inventory item to add
     * @param quantity the quantity of the inventory item to add
     * @param dataSource the data source to retrieve the database connection from
     * @throws Exception if there is an error executing the database query
     */
    public void addInventoryItem(UserSnowflake user, InventoryItem item, int quantity, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO `inventory` (item_id, user_id, quantity, is_breakable, durability, is_equipped) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, item.getItemId());
                statement.setString(2, user.getId());
                statement.setInt(3, quantity);
                statement.setBoolean(4, item.isBreakable());
                statement.setInt(5, item.getDurability());
                statement.setBoolean(6, item.isEquipped());

                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the quantity of an inventory item for a specific user in the database.
     *
     * @param user       The UserSnowflake representing the user.
     * @param item       The InventoryItem representing the item to be updated.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @throws Exception If there is an error executing a database query or update.
     */
    public void incrementQuantity(UserSnowflake user, InventoryItem item, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "UPDATE inventory SET quantity = ? WHERE user_id = ? AND item_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, item.getQuantity());
                statement.setString(2, user.getId());
                statement.setInt(3, item.getItemId());

                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decrements the quantity of a specific item in the user's inventory.
     *
     * @param user       The UserSnowflake representing the user.
     * @param item_      The ItemDescriptionBean representing the item to decrement.
     * @param quantity   The int value representing the quantity to decrement.
     * @param dataSource The DataSource used to establish a connection to the database.
     * @return A Pair<Boolean, String> representing the success status and the result message.
     */
    public Pair<Boolean, String> decrementQuantity(UserSnowflake user, ItemDescriptionBean item_, int quantity, DataSource dataSource) {
        InventoryBuilder inventory = fetchInventory(user, dataSource);
        if (inventory == null) {
            return Pair.of(false, "inventory.not_found");
        }

        boolean itemFound = false;

        for (InventoryItem item : inventory.getItems()) {
            if (item.getItemSlug() != null && item.getItemSlug().equals(item_.getItemSlug())) {
                if ((item.isBreakable() && item.getDurability() != item.getMaxDurability()) || item.isEquipped()) {
                    continue; // Skip breakable or equipped items
                }
                itemFound = true;
                int availableQuantity = item.getQuantity();

                if (availableQuantity < quantity) {
                    return Pair.of(false, "You don't have enough '" + item_.getItemName() + "' to sell. (Available: " + availableQuantity + ")");
                } else {
                    int newQuantity = availableQuantity - quantity;
                    if (newQuantity <= 0) {
                        deleteItem(user.getId(), item_.getItemId(), dataSource);
                    } else {
                        updateQuantity(user.getId(), item_.getItemId(), newQuantity, dataSource);
                    }
                    return Pair.of(true, "");
                }
            }
        }

        if (!itemFound) {
            return Pair.of(false, "You don't have '" + item_.getItemName() + "' in your inventory.");
        }

        // This should not be reached under normal circumstances
        return Pair.of(false, "An unexpected error occurred.");
    }


    /**
     * Deletes an item from the inventory for a specific user.
     *
     * @param userId      The ID of the user.
     * @param itemId      The ID of the item to be deleted.
     * @param dataSource  The DataSource used to establish a connection to the database.
     *
     * @throws SQLException If there is an error executing a database query or update.
     */
    private void deleteItem(String userId, int itemId, DataSource dataSource)  {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM inventory WHERE user_id = ? AND item_id = ?")) {
            statement.setString(1, userId);
            statement.setInt(2, itemId);
            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the durability of an item for a specific user in the inventory.
     *
     * @param userId      The String representing the user ID.
     * @param itemId      The int representing the ID of the item.
     * @param newQuantity The int value representing the updated quantity/durability of the item.
     * @param dataSource  The DataSource used to establish a connection to the database.
     *
     * @throws SQLException if there is an error executing a database query or update.
     */
    private void updateQuantity(String userId, int itemId, int newQuantity, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET quantity = ? WHERE user_id = ? AND item_id = ?")) {
            statement.setInt(1, newQuantity);
            statement.setString(2, userId);
            statement.setInt(3, itemId);
            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createItemDrop(DropItemBuilder dropItem, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO players_item_drop (drop_id, dropped_by, item_id, quantity) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, dropItem.getDropId());
                statement.setString(2, dropItem.getDroppedBy().getId());
                statement.setInt(3, dropItem.getItemId());
                statement.setInt(4, dropItem.getQuantity());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DropItemBuilder fetchDroppedItem(String dropId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT drop_id, dropped_by, claimed_by, is_claimed, item_id, quantity FROM players_item_drop WHERE drop_id = ?")) {
                statement.setString(1, dropId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new DropItemBuilder(
                                resultSet.getString("drop_id"),
                                UserSnowflake.fromId(resultSet.getString("dropped_by")),
                                (resultSet.getString("claimed_by") != null ? UserSnowflake.fromId(resultSet.getString("claimed_by")) : null),
                                resultSet.getBoolean("is_claimed"),
                                resultSet.getInt("item_id"),
                                resultSet.getInt("quantity")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void claimDrop(UserSnowflake user, String dropId, DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE players_item_drop SET is_claimed = 1, claimed_by = ? WHERE drop_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, dropId);
                int rows = statement.executeUpdate();

                System.out.println(rows);
            }
        } catch (SQLException e) {
            throw e;
        }
    }
}