package eu.fluffici.bot.manager;

/*
---------------------------------------------------------------------------------
File Name : UserManager.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 08/06/2024

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
import eu.fluffici.bot.api.CurrencyType;
import eu.fluffici.bot.api.ItemInformationBuilder;
import eu.fluffici.bot.api.beans.players.*;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.events.UserTransferEvent;
import eu.fluffici.bot.api.game.DeathType;
import eu.fluffici.bot.api.game.PlayerDeathInfo;
import eu.fluffici.bot.api.hooks.IUserManager;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.item.InventoryStatus;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

/**
 * UserManager class is responsible for managing user-related operations, such as fetching user information, managing relationships, managing inventory, and leaderboard related operations
 *.
 */
@SuppressWarnings("All")
public class UserManager implements IUserManager {

    private final FluffBOT instance;

    public UserManager(FluffBOT instance) {
        this.instance = instance;
    }

    /**
     * Fetches the user with the given user snowflake.
     *
     * @param snowflake the user snowflake to fetch the user for
     * @return the {@link PlayerBean} representing the user, or {@code null} if an error occurred
     */
    @Override
    @SneakyThrows
    public PlayerBean fetchUser(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().getPlayer(snowflake.getId());
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            this.instance.getLogger().error(this.instance.getGameServiceManager().getDatabaseManager().getSourcesStats(
                    this.instance.getGameServiceManager().getDatabaseManager().getDataSource()
            ), e.fillInStackTrace());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches a user asynchronously.
     *
     * @param snowflake the UserSnowflake object representing the user
     * @return the PlayerBean object representing the fetched user
     * @throws Exception if any error occurs while fetching the user
     */
    @Override
    @SneakyThrows
    public PlayerBean fetchUserAsync(UserSnowflake snowflake) {
        return this.instance.getGameServiceManager().getPlayer(snowflake.getId());
    }

    /**
     * Fetches the inventory for the specified user snowflake.
     *
     * @param snowflake the user snowflake to fetch inventory for
     * @return the InventoryBuilder object containing the fetched inventory,
     *         or null if an exception occurred while fetching the inventory
     */
    @Override
    public InventoryBuilder fetchInventory(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().fetchInformation(snowflake);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches the relationship between the owner user and the specified user.
     *
     * @param owner The Snowflake ID of the owner user.
     * @param user The Snowflake ID of the specified user.
     * @return {@code true} if the relationship was successfully fetched, {@code false} otherwise.
     */
    @Override
    public boolean fetchRelationship(UserSnowflake owner, UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().fetchRelationship(owner, user);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches the relationship status between the given user and the current instance.
     *
     * @param user The user for which the relationship status needs to be fetched.
     * @return True if the relationship status was successfully fetched, false otherwise.
     */
    @Override
    public boolean fetchRelationship(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().fetchRelationship(user, user);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches all relationship members for a given user.
     *
     * @param user the UserSnowflake representing the user
     * @return a List of RelationshipMember objects representing the members of the user's relationships
     */
    @Override
    public List<RelationshipMember> fetchAllRelationshipMembers(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().fetchAllRelationshipMembers(user);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Accepts a relationship invite from a user specified by the given snowflake ID.
     *
     * @param snowflake the snowflake ID of the user who sent the invite
     */
    @Override
    public void acceptInvite(UserSnowflake snowflake) {
        try {
            this.instance.getGameServiceManager().acceptelationshipInvite(snowflake);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Fetches the active RelationshipInvite for a given UserSnowflake.
     *
     * @param snowflake the UserSnowflake for which to fetch the RelationshipInvite
     * @return the active RelationshipInvite for the given UserSnowflake, or null if an error occurs
     */
    @Override
    public RelationshipInviteBuilder fetchInvite(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().fetchActiveRelationshipInvite(snowflake);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes a relationship between two users.
     *
     * @param snowflake The user who wants to remove the relationship.
     * @param target The user with whom the relationship will be removed.
     */
    @Override
    public void removeRelationship(UserSnowflake snowflake, UserSnowflake target) {
        try {
            this.instance.getGameServiceManager().removeRelationship(snowflake, target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the active relationship invite for the specified user.
     *
     * @param snowflake the Snowflake ID of the user
     * @return the RelationshipInviteBuilder object representing the active relationship invite, or null if not found
     */
    @Override
    public RelationshipInviteBuilder getActiveRel(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().fetchActiveRelationshipInvite(snowflake);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Updates the relationships between two users.
     *
     * @param snowflake The snowflake of the user whose relationships should be updated.
     * @param target The snowflake of the user with whom the relationships should be updated.
     */
    @Override
    public void updateRelationships(UserSnowflake snowflake, UserSnowflake target) {
        try {
            this.instance.getGameServiceManager().updateRelationship(snowflake, target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Sends an invite for a relationship.
     *
     * @param relationshipInviteBuilder the builder object containing the details of the invitation
     */
    @Override
    public void sendInvite(RelationshipInviteBuilder relationshipInviteBuilder) {
        try {
            this.instance.getLogger().debug(relationshipInviteBuilder.getRelationshipOwner().getId());
            this.instance.getLogger().debug(relationshipInviteBuilder.getUserId().getId());
            this.instance.getGameServiceManager().createRelationshipInvite(relationshipInviteBuilder);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Acknowledges the invite for the user.
     *
     * @param user the user to acknowledge the invite for
     */
    @Override
    public void acknowledgeInvite(UserSnowflake user) {
        try {
            this.instance.getGameServiceManager().acknowledgeInviteRel(user);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of LeaderboardBuilder objects representing the top users by level.
     *
     * @param limit the maximum number of users to retrieve
     *
     * @return a list of LeaderboardBuilder objects representing the top users by level, an empty list if an error occurs
     */
    @Override
    public List<LeaderboardBuilder> getTopUsersByLevel(int limit) {
        List<LeaderboardBuilder> builders;

        try {
            builders = new ArrayList<>(this.instance.getGameServiceManager().fetchLeaderboard(limit));
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return Collections.emptyList();
        }

        return builders;
    }

    /**
     * Retrieves the top users by statistics.
     *
     * @param limit the maximum number of users to return
     * @param statistic the statistic to rank the users by
     * @return a list of LeaderboardBuilder objects representing the top users by the specified statistic
     */
    @Override
    public List<LeaderboardBuilder> getTopUsersByStats(int limit, String statistic) {
        List<LeaderboardBuilder> builders;

        try {
            builders = new ArrayList<>(this.instance.getGameServiceManager().getTopUsersByStats(limit, statistic));
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return Collections.emptyList();
        }

        return builders;
    }

    /**
     * Adds an item to the inventory of a user.
     *
     * @param user The user variable of type UserSnowflake to whom the item will be added.
     * @param items The items variable of type InventoryItem which contains the item details.
     * @return A Pair object consisting of a Boolean value indicating the success of adding the item and an InventoryStatus representing the current inventory status.
     */
    @Override
    public Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, InventoryItem items) {
        try {
            InventoryBuilder inventory = this.instance.getGameServiceManager().fetchInformation(user);
            if (inventory.isInventoryFull()) {
                return Pair.of(false, InventoryStatus.FULL);
            }

            ItemDescriptionBean item = this.instance.getGameServiceManager().fetchItem(items.getItemId());

            InventoryItem playerQuantity = FluffBOT.getInstance().getGameServiceManager().getPlayerItem(user, item);
            if (playerQuantity != null && playerQuantity.getQuantity() > 0) {
                playerQuantity.setQuantity(playerQuantity.getQuantity() + items.getQuantity());
                this.instance.getGameServiceManager().incrementQuantity(user, playerQuantity);
            } else {
                this.instance.getGameServiceManager().addInventoryItem(user, items, items.getQuantity());
            }
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return Pair.of(true, InventoryStatus.NORMAL);
    }

    /**
     * Add an item to the inventory of a user.
     *
     * @param user      The snowflake ID of the user.
     * @param itemSlug  The slug of the item to be added.
     * @param quantity  The quantity of the item to be added.
     * @return A Pair object consisting of a boolean indicating whether the item was added successfully,
     *         and an InventoryStatus enum representing the current status of the inventory.
     */
    @Override
    public Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, String itemSlug, int quantity) {
        try {
            InventoryBuilder inventory = this.instance.getGameServiceManager().fetchInformation(user);
            if (inventory.isInventoryFull()) {
                return Pair.of(false, InventoryStatus.FULL);
            }

            System.out.println(itemSlug);

            ItemDescriptionBean item = this.instance.getGameServiceManager().fetchItem(itemSlug);

            InventoryItem playerQuantity = FluffBOT.getInstance().getGameServiceManager().getPlayerItem(user, item);
            if (playerQuantity != null && playerQuantity.getQuantity() > 0) {
                playerQuantity.setQuantity(playerQuantity.getQuantity() + quantity);
                this.instance.getGameServiceManager().incrementQuantity(user, playerQuantity);
            } else {
                this.instance.getGameServiceManager().addInventoryItem(user, InventoryItem
                        .builder()
                        .itemId(item.getItemId())
                        .isBreakable(item.getDurability() > 0)
                        .durability(item.getDurability())
                        .isEquipped(false)
                        .build(),
                        quantity
                );
            }
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return Pair.of(true, InventoryStatus.NORMAL);
    }

    /**
     * Checks if a user is verified in a guild.
     *
     * @param user The UserSnowflake instance representing the user.
     * @return true if the user is verified, false otherwise.
     */
    @Override
    public boolean isVerified(UserSnowflake user) {
        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));
        Member member = guild.getMember(user);

        Role verifiedRole = guild.getRoleById(this.instance.getDefaultConfig().getProperty("roles.verified"));

        return member.getRoles().contains(verifiedRole);
    }

    @Override
    public void addPointToStaff(UserSnowflake user, int points) {
        // Safe guard
        if (points < 0 || points > 100)
            return;

        if (FluffBOT.getInstance().getGameServiceManager().hasStaffPaycheck(user)) {
            FluffBOT.getInstance().getGameServiceManager().updateStaffPaycheck(user, Math.abs(points));
        }
    }

    /**
     * Adds an item to the inventory of a user.
     *
     * @param user the user's snowflake
     * @param item the item to be added
     * @return a pair of a boolean value indicating the success of the operation and an inventory status
     */
    @Override
    public Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, ItemDescriptionBean item) {
        try {
            InventoryItem items = InventoryItem.builder()
                    .itemId(item.getItemId())
                    .rarity(item.getItemRarity())
                    .name(item.getItemName())
                    .description(FluffBOT.getInstance().getLanguageManager().get(item.getItemDesc()))
                    .quantity(1)
                    .isEquipped(false)
                    .isBreakable(item.getDurability() > 0)
                    .isStackable(item.isStackable())
                    .durability(item.getDurability())
                    .maxDurability(item.getDurability())
                    .build();

            InventoryBuilder inventory = this.instance.getGameServiceManager().fetchInformation(user);
            if (inventory.isInventoryFull()) {
                return Pair.of(false, InventoryStatus.FULL);
            }

            InventoryItem playerQuantity = FluffBOT.getInstance().getGameServiceManager().getPlayerItem(user, item);
            if (playerQuantity != null && playerQuantity.getQuantity() > 0) {
                playerQuantity.setQuantity(playerQuantity.getQuantity() + items.getQuantity());
                this.instance.getGameServiceManager().incrementQuantity(user, playerQuantity);
            } else {
                this.instance.getGameServiceManager().addInventoryItem(user, items, items.getQuantity());
            }
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return Pair.of(true, InventoryStatus.NORMAL);
    }

    /**
     * Adds an item to the user's inventory with the specified quantity.
     *
     * @param user The user to add the item to.
     * @param item The item to be added.
     * @param quantity The quantity of the item to be added.
     * @return A Pair object containing a Boolean value indicating whether the item was added successfully,
     *         and an InventoryStatus enum indicating the status of the user's inventory after the item was added.
     */
    @Override
    public Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, ItemDescriptionBean item, int quantity) {
        try {
            InventoryItem items = InventoryItem.builder()
                    .itemId(item.getItemId())
                    .rarity(item.getItemRarity())
                    .name(item.getItemName())
                    .description(FluffBOT.getInstance().getLanguageManager().get(item.getItemDesc()))
                    .quantity(quantity)
                    .isEquipped(false)
                    .isBreakable(item.getDurability() > 0)
                    .isStackable(item.isStackable())
                    .durability(item.getDurability())
                    .maxDurability(item.getDurability())
                    .build();

            if (this.isInventoryFull(user)) {
                return Pair.of(false, InventoryStatus.FULL);
            }

            InventoryItem playerQuantity = FluffBOT.getInstance().getGameServiceManager().getPlayerItem(user, item);
            if (playerQuantity != null && playerQuantity.getQuantity() > 0) {
                playerQuantity.setQuantity(playerQuantity.getQuantity() + quantity);
                this.instance.getGameServiceManager().incrementQuantity(user, playerQuantity);
            } else {
                this.instance.getGameServiceManager().addInventoryItem(user, items, quantity);
            }
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return Pair.of(true, InventoryStatus.NORMAL);
    }

    /**
     * Checks if the inventory of specified user is full.
     *
     * @param user the UserSnowflake object representing the user
     * @return true if the inventory is full, false otherwise
     * @throws Exception if an error occurs while retrieving the inventory information
     */
    @Override
    @SneakyThrows
    public boolean isInventoryFull(UserSnowflake user) {
        InventoryBuilder inventory = this.instance.getGameServiceManager()
                .fetchInformation(user);
        return inventory.isInventoryFull();
    }

    /**
     * Calculates the free space in the inventory for the given user.
     *
     * @param user the UserSnowflake object representing the user
     * @return the amount of free space in the inventory
     * @throws Exception if an error occurs during the calculation
     */
    @Override
    @SneakyThrows
    public int calculateFreeSpace(UserSnowflake user) {
        InventoryBuilder inventory = this.instance.getGameServiceManager()
                .fetchInformation(user);
        return inventory.calculateFreeSpace();
    }

    /**
     * Creates a player by invoking the createPlayer method of the GameServiceManager.
     * If an exception occurs, the error message is logged and the stack trace is printed.
     *
     * @param player the PlayerBean object representing the player to be created
     */
    @Override
    @SneakyThrows
    public void createPlayer(PlayerBean player) {
        try {
            this.instance.getGameServiceManager().createPlayer(player);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Saves the given player user.
     *
     * @param player the PlayerBean object containing the details of the player to be saved
     */
    @Override
    public void saveUser(PlayerBean player) {
        try {
            this.instance.getGameServiceManager().updatePlayer(player);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user with the given snowflake is a developer.
     *
     * @param snowflake The snowflake of the user to check.
     * @return True if the user is a developer, false otherwise.
     */
    @Override
    public boolean isDeveloper(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().isDeveloper(snowflake.getId());
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the birthdate of a user.
     *
     * @param snowflake the UserSnowflake object representing the user.
     * @return the BirthdayBean object containing the birthdate of the user.
     *         Returns null if an error occurs while retrieving the birthdate.
     */
    @Override
    public BirthdayBean fetchBirthdate(UserSnowflake snowflake) {
        try {
            return this.instance.getGameServiceManager().getBirthdate(snowflake.getId());
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new birthday record in the game service manager.
     *
     * @param birthday the BirthdayBean object containing the details of the birthday
     */
    @Override
    public void createBirthdate(BirthdayBean birthday) {
        try {
            this.instance.getGameServiceManager().createBirthdate(birthday);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Transfers tokens from one player to another.
     *
     * @param issuer       the player issuing the transfer
     * @param currencyType the type of currency to transfer
     * @param amount       the amount of currency to transfer
     * @param target       the player receiving the transfer
     * @return a Pair object indicating the success of the transfer and an error message if applicable
     *         - First element of the Pair:
     *              - true if the transfer was successful
     *              - false if the transfer failed
     *         - Second element of the Pair:
     *              - an empty string if the transfer was successful
     *              - an error message if the transfer failed
     */
    @Override
    public Pair<Boolean, String> transferTokens(PlayerBean issuer, CurrencyType currencyType, int amount, PlayerBean target) {
        try {
            switch (currencyType) {
                case COINS -> {
                    if (issuer.getCoins() < amount)
                        return Pair.of(false, "Insufficient coins.");
                    issuer.setCoins(issuer.getCoins() - amount);
                    target.setCoins(target.getCoins() + amount);

                    this.createEconomyRecord(
                            issuer,
                            EconomyHistory.Operation.DEBIT,
                            EconomyHistory.Currency.COINS,
                            amount
                    );

                    this.createEconomyRecord(
                            target,
                            EconomyHistory.Operation.CREDIT,
                            EconomyHistory.Currency.COINS,
                            amount
                    );

                    this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "coins", amount);

                    this.updateAchievement(target.getUserId(), new int[] {
                        13, 14, 15, 16, 17, 18
                    }, amount);
                }
                case TOKENS -> {
                    if (issuer.getTokens() < amount)
                        return Pair.of(false, "Insufficient tokens.");
                    issuer.setTokens(issuer.getTokens() - amount);
                    target.setTokens(target.getTokens() + amount);

                    this.createEconomyRecord(
                            issuer,
                            EconomyHistory.Operation.DEBIT,
                            EconomyHistory.Currency.TOKEN,
                            amount
                    );

                    this.createEconomyRecord(
                            target,
                            EconomyHistory.Operation.CREDIT,
                            EconomyHistory.Currency.TOKEN,
                            amount
                    );

                    this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "tokens", amount);

                    this.updateAchievement(target.getUserId(), new int[] {
                        19, 20, 21, 22
                    }, amount);
                }
                case UPVOTE -> {
                    if (issuer.getUpvote() < amount)
                        return Pair.of(false, "Insufficient upvote.");
                    issuer.setUpvote(issuer.getUpvote() - amount);
                    target.setUpvote(target.getUpvote() + amount);

                    this.createEconomyRecord(
                            issuer,
                            EconomyHistory.Operation.DEBIT,
                            EconomyHistory.Currency.UPVOTE,
                            amount
                    );

                    this.createEconomyRecord(
                            target,
                            EconomyHistory.Operation.CREDIT,
                            EconomyHistory.Currency.UPVOTE,
                            amount
                    );

                    this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "upvotes", amount);

                    this.updateAchievement(target.getUserId(), new int[] {
                        28, 29, 30, 31, 32
                    }, amount);
                }
                default -> {
                    return Pair.of(false, "Unknown currency type.");
                }
            }

            this.saveUser(issuer);
            this.saveUser(target);

            this.instance.getEventBus().post(new UserTransferEvent(
                    issuer,
                    target,
                    currencyType,
                    UUID.randomUUID().toString(),
                    amount
            ));

            return Pair.of(true, "");
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();

            return Pair.of(false, "A error occurred.");
        }
    }
    /**
     * Increase the number of tokens for a given player.
     *
     * @param target The player whose tokens will be increased.
     * @param amount The number of tokens to add.
     */
    @Override
    public void addTokens(PlayerBean target, int amount) {
        try {
            target.setTokens(target.getTokens() + amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.CREDIT,
                    EconomyHistory.Currency.TOKEN,
                    amount
            );

            this.saveUser(target);

            this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "tokens", amount);

            this.updateAchievement(target.getUserId(), new int[] {
                    19, 20, 21, 22
            }, amount);

            this.instance.getEventBus().post(new UserTransferEvent(
                    this.getSystemUser(),
                    target,
                    CurrencyType.TOKENS,
                    UUID.randomUUID().toString(),
                    amount
            ));

        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Adds a specified amount of coins to a player's account.
     *
     * @param target  the PlayerBean object representing the player whose coins will be increased
     * @param amount  the amount of coins to add to the player's account
     */
    @Override
    public void addCoins(PlayerBean target, int amount) {
        try {
            target.setCoins(target.getCoins() + amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.CREDIT,
                    EconomyHistory.Currency.COINS,
                    amount
            );

            this.saveUser(target);

            this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "coins", amount);
            this.updateAchievement(target.getUserId(), new int[] { 13, 14, 15, 16, 17, 18 }, amount);

        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Increases the number of events for the specified player by the given amount.
     * This method also updates the user's events in the database, increments the "events" statistic,
     * updates the achievement progress for achievements 23, 24, 25, 26, and 27, and posts an event
     * to the event bus.
     *
     * @param target the player for whom to add events
     * @param amount the amount of events to add
     */
    @Override
    public void addEvent(PlayerBean target, int amount) {
        try {
            target.setEvents(target.getEvents() + amount);
            this.saveUser(target);

            this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "events", amount);
            this.updateAchievement(target.getUserId(), new int[] { 23, 24, 25, 26, 27 }, amount);

            this.instance.getEventBus().post(new UserTransferEvent(
                    this.getSystemUser(),
                    target,
                    CurrencyType.EVENT,
                    UUID.randomUUID().toString(),
                    amount
            ));

        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Increases the upvote count of a player by the specified amount.
     *
     * @param target The player to add upvotes to.
     * @param amount The number of upvotes to add.
     */
    @Override
    public void addUpvote(PlayerBean target, int amount) {
        try {
            target.setUpvote(target.getUpvote() + amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.CREDIT,
                    EconomyHistory.Currency.UPVOTE,
                    amount
            );

            this.saveUser(target);

            this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "upvotes", amount);

            this.updateAchievement(target.getUserId(), new int[] {
                    28, 29, 30, 31, 32
            }, amount);

            this.instance.getEventBus().post(new UserTransferEvent(
                    this.getSystemUser(),
                    target,
                    CurrencyType.UPVOTE,
                    UUID.randomUUID().toString(),
                    amount
            ));
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Adds karma points to the specified player's karma.
     *
     * @param target The player to add karma points to.
     * @param amount The amount of karma points to add.
     */
    @Override
    public void addKarma(PlayerBean target, int amount) {
        try {
            target.setKarma(target.getKarma() + amount);


            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.CREDIT,
                    EconomyHistory.Currency.KARMA,
                    amount
            );

            this.incrementStatistics(UserSnowflake.fromId(target.getUserId()), "karmas", amount);

            this.saveUser(target);

            this.instance.getEventBus().post(new UserTransferEvent(
                    this.getSystemUser(),
                    target,
                    CurrencyType.KARMA,
                    UUID.randomUUID().toString(),
                    amount
            ));
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Removes a specified number of tokens from a player's account.
     *
     * @param target The target player from whose account the tokens will be deducted.
     * @param amount The number of tokens to be removed from the player's account.
     */
    @Override
    public void removeTokens(PlayerBean target, int amount) {
        try {
            target.setTokens(target.getTokens() - amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.DEBIT,
                    EconomyHistory.Currency.TOKEN,
                    amount
            );

            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the player has a sufficient amount of tokens.
     *
     * @param player the PlayerBean object representing the player (can be null)
     * @param amount the amount of tokens required
     * @return true if the player has a sufficient amount of tokens, false otherwise
     */
    @Override
    public boolean hasEnoughTokens(PlayerBean player, int amount) {
        return hasSufficientAmount(player, player != null ? player.getTokens() : 0, amount);
    }

    /**
     * Checks if the player has a sufficient amount of coins.
     *
     * @param player the PlayerBean object representing the player (can be null)
     * @param amount the amount of coins required
     * @return true if the player has a sufficient amount of coins, false otherwise
     */
    @Override
    public boolean hasEnoughCoins(PlayerBean player, int amount) {
        return hasSufficientAmount(player, player != null ? player.getCoins() : 0, amount);
    }

    /**
     * Checks if the player has a sufficient amount of upvotes.
     *
     * @param player the PlayerBean object representing the player (can be null)
     * @param amount the amount of upvotes required
     * @return true if the player has a sufficient amount of upvotes, false otherwise
     */
    @Override
    public boolean hasEnoughUpvotes(PlayerBean player, int amount) {
        return hasSufficientAmount(player, player != null ? player.getUpvote() : 0L, amount);
    }

    /**
     * Checks if the player has a sufficient amount of a specific currency.
     *
     * @param player         the PlayerBean object representing the player
     * @param playerAmount   the amount of the currency the player currently has
     * @param requiredAmount the amount of the currency required
     * @return true if the player has a sufficient amount of the currency, false otherwise
     */
    private boolean hasSufficientAmount(PlayerBean player, long playerAmount, long requiredAmount) {
        return player != null && playerAmount >= requiredAmount;
    }

    /**
     * Removes a specified amount of coins from the player's balance.
     *
     * @param target the player whose coins will be deducted
     * @param amount the amount of coins to remove from the player's balance
     */
    @Override
    public void removeCoins(PlayerBean target, int amount) {
        try {
            target.setCoins(target.getCoins() - amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.DEBIT,
                    EconomyHistory.Currency.COINS,
                    amount
            );

            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Decreases the number of events for a specific player by the specified amount
     *
     * @param target the player to remove events from
     * @param amount the amount of events to remove
     */
    @Override
    public void removeEvent(PlayerBean target, int amount) {
        try {
            target.setEvents(target.getEvents() - amount);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Removes the specified number of upvotes from the target player.
     *
     * @param target The player whose upvotes should be removed.
     * @param amount The number of upvotes to be removed.
     */
    @Override
    public void removeUpvote(PlayerBean target, int amount) {
        try {
            target.setUpvote(target.getUpvote() - amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.DEBIT,
                    EconomyHistory.Currency.UPVOTE,
                    amount
            );

            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Decreases the karma of a player by a specified amount.
     *
     * @param target The PlayerBean object representing the player whose karma is to be decreased.
     * @param amount The amount by which the karma should be decreased.
     */
    @Override
    public void removeKarma(PlayerBean target, int amount) {
        try {
            target.setKarma(target.getKarma() - amount);

            this.createEconomyRecord(
                    target,
                    EconomyHistory.Operation.DEBIT,
                    EconomyHistory.Currency.KARMA,
                    amount
            );

            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Removes the specified amount of experience from the given player.
     *
     * @param target The player whose experience will be updated.
     * @param amount The amount of experience to be removed.
     */
    @Override
    public void removeExperience(PlayerBean target, int amount) {
        try {
            target.setExperience(target.getExperience() - amount);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }
    /**
     * Removes the specified amount from the level of the target player.
     * Saves the updated player information.
     *
     * @param target The player on which the level will be modified.
     * @param amount The amount to be subtracted from the player's level.
     * @throws Exception If an error occurs during the level modification or saving process.
     */
    @Override
    public void removeLevel(PlayerBean target, int amount) {
        try {
            target.setLevel(target.getLevel() - amount);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Update the experience of a player by a given amount.
     *
     * @param target the PlayerBean object representing the player whose experience needs to be updated
     * @param amount the amount by which the player's experience should be increased
     */
    @Override
    public void updateExperience(PlayerBean target, int amount) {
        try {
            target.setExperience(target.getExperience() + amount);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Updates the level of a player.
     *
     * @param target The player whose level is to be updated.
     * @param amount The amount by which to update the level. This can be positive or negative.
     */
    @Override
    public void updateLevel(PlayerBean target, int amount) {
        try {
            target.setLevel(target.getLevel() + amount);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the player has enough karma.
     *
     * @param player the player to check the karma for
     * @param amount the amount of karma to check for
     * @return true if the player has enough karma, false otherwise
     */
    @Override
    public boolean hasEnoughKarma(PlayerBean player, int amount) {
        return player.getKarma() >= amount;
    }

    /**
     * Determines whether a player has enough events.
     * @param player the player whose events are checked
     * @param amount the minimum number of events required
     * @return true if the player has at least the specified amount of events, false otherwise
     */
    @Override
    public boolean hasEnoughEvents(PlayerBean player, int amount) {
        return player.getEvents() >= amount;
    }

    /**
     * Checks if the given player has a clan.
     *
     * @param player the player to check
     * @return true if the player has a clan, false otherwise
     */
    @Override
    public boolean hasClan(PlayerBean player) {
        try {
            return this.instance.getGameServiceManager().hasClan(player.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches user's clan information.
     *
     * @param player the player bean representing the user
     * @return the clan bean representing the user's clan information, or null if an exception occurs
     * @throws NullPointerException if the player is null
     */
    @Override
    public UserClanBean fetchClan(PlayerBean player) {
        try {
            return this.instance.getGameServiceManager().getClan(player.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates a clan for a user.
     *
     * @param clan The UserClanBean representing the clan to be updated.
     */
    @Override
    public void updateClan(UserClanBean clan) {
        try {
            this.instance.getGameServiceManager().updateUserClan(clan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the achievement progress for the given player based on the specified achievement ID and amount.
     *
     * @param id     The ID of the achievement to update.
     * @param ids    An array of player IDs related to the achievement.
     * @param amount The amount by which to increment the achievement progress.
     */
    private void updateAchievement(String id, int[] ids, int amount) {
        this.instance.getAchievementManager().incrementAchievements(id, ids, amount);
    }

    /**
     * Updates the booster of a player by multiplying it with a given multiplier.
     *
     * @param target    the player whose booster is to be updated
     * @param multiplier    the multiplier to be applied to the player's booster
     */
    @Override
    public void updateBooster(PlayerBean target, int multiplier) {
        try {
            target.setBooster(target.getBooster() * multiplier);
            this.saveUser(target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Creates an economy record for a given user.
     *
     * @param user The player for whom the economy record is being created.
     * @param operation The operation performed on the user's economy.
     * @param currency The currency used in the operation.
     * @param amount The amount of the currency involved in the operation.
     */
    private void createEconomyRecord(PlayerBean user, EconomyHistory.Operation operation, EconomyHistory.Currency currency, int amount) {
        try {
            EconomyHistory history = new EconomyHistory(user.getUserId(), amount, currency, operation, new Timestamp(Instant.now().toEpochMilli()));
            this.instance.getGameServiceManager().createEconomyRecord(history);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Increments the statistics for a user by a given score.
     *
     * @param user       the UserSnowflake representing the user for whom the statistics are being incremented
     * @param statistics the String representing the name of the statistics to be incremented
     * @param score      the int value representing the score by which the statistics should be incremented
     */
    @Override
    public void incrementStatistics(UserSnowflake user, String statistics, int score) {
        try {
            boolean check = this.instance.getGameServiceManager().hasStatistics(user, statistics);
            if (!check)
                this.instance.getGameServiceManager().createUserStatistics(user, statistics);

            this.instance.getGameServiceManager().incrementPlayerStatistics(user, statistics, score);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user is blocked by another user.
     *
     * @param user   the user that might be blocked
     * @param target the user that might have blocked the user
     * @return true if the user is blocked by the target user, false otherwise
     */
    @Override
    public boolean isBlocked(UserSnowflake user, UserSnowflake target) {
        try {
            return this.instance.getGameServiceManager().isBlocked(user, target) != null;
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Blocks a user, preventing them from interacting with the system.
     *
     * @param user   the user who is blocking another user
     * @param target the user who is being blocked
     */
    @Override
    public void blockUser(UserSnowflake user, UserSnowflake target) {
        try {
            this.instance.getGameServiceManager().addToBlocklist(user, target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Unblocks a user by removing them from the blocklist.
     *
     * @param user The user who is unblocking another user.
     * @param target The user who is being unblocked.
     */
    @Override
    public void unblockUser(UserSnowflake user, UserSnowflake target) {
        try {
            this.instance.getGameServiceManager().remToBlocklist(user, target);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the list of blocked users for the given user.
     *
     * @param user the user for which to retrieve the blocked users
     * @return a list of BlockedUser objects representing the blocked users for the given user
     */
    @Override
    public List<BlockedUser> blockedUsers(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().getBlockedUsers(user);
        } catch (Exception e) {
            this.instance.getLogger().error(e.getMessage(), e.fillInStackTrace());
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * Returns true if the user has a channel.
     *
     * @param user the user to check
     * @return true if the user has a channel, false otherwise
     */
    @Override
    public boolean hasChannel(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().hasChannel(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves the channel associated with the specified user.
     *
     * @param user the UserSnowflake object representing the user
     * @return the ChannelBean object representing the channel associated with the user, or null if an error occurs
     */
    @Override
    public ChannelBean getChannel(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().getChannel(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new channel.
     *
     * @param channel the ChannelBean object representing the channel to be created
     */
    @Override
    public void createChannel(ChannelBean channel) {
        try {
            this.instance.getGameServiceManager().createChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the specified channel for a user.
     *
     * @param user the UserSnowflake representing the user whose channel is to be deleted.
     */
    @Override
    public void deleteChannel(UserSnowflake user) {
        try {
            this.instance.getGameServiceManager().deleteChannel(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Transfers the ownership of a channel to a new owner.
     *
     * @param currentOwner the current owner of the channel
     * @param newOwner the new owner to transfer the channel to
     */
    @Override
    public void transferChannelOwner(UserSnowflake currentOwner, UserSnowflake newOwner) {
        try {
            this.instance.getGameServiceManager().transferChannelOwner(currentOwner, newOwner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all subscribers from the game service manager.
     *
     * @return a list of outing subscribers
     * @throws Exception if an error occurs while retrieving the subscribers
     */
    @Override
    public List<OutingSubscriber> fetchAllSubscriber() {
        try {
            return this.instance.getGameServiceManager().getAllSubscribers();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * Creates a new outing subscriber for the given user.
     *
     * @param user The user to create an outing subscriber for.
     */
    @Override
    public void createOutingSubscriber(UserSnowflake user) {
        try {
            this.instance.getGameServiceManager().createNewSubscriber(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a subscriber from the outing service.
     *
     * @param user the user (identified by user snowflake) to be removed as a subscriber
     */
    @Override
    public void removeOutingSubscriber(UserSnowflake user) {
        try {
            this.instance.getGameServiceManager().removeSubscriber(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user is subscribed to outing.
     *
     * @param user the user to check subscription for
     * @return true if the user is subscribed to outing, false otherwise
     */
    @Override
    public boolean isOutingSubscribed(UserSnowflake user) {
        try {
            return this.instance.getGameServiceManager().isSubscribed(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if a specific upgrade is available for a given user.
     *
     * @param user The user for whom to check the upgrade availability.
     * @param upgradeId The ID of the upgrade to check for availability.
     * @return true if the upgrade is available for the user, false otherwise.
     */
    @Override
    public boolean hasUprade(UserSnowflake user, int upgradeId) {
        try {
            return this.instance.getGameServiceManager().hasUpgrade(user, upgradeId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if a user has a specific item.
     *
     * @param user The user for whom to check the item.
     * @param itemSlug The slug of the item to check.
     * @return true if the user has the item, false otherwise.
     */
    @Override
    public boolean hasItem(UserSnowflake user, String itemSlug) {
        try {
            return this.instance.getGameServiceManager().hasItem(user, itemSlug);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if the specified user has the given item in the specified quantity.
     *
     * @param user         the user to check
     * @param itemSlug     the slug of the item to check
     * @param quantity     the quantity of the item to check
     * @return true if the user has the item in the specified quantity, false otherwise
     */
    @Override
    public boolean hasItem(UserSnowflake user, String itemSlug, int quantity) {
        try {
            return this.instance.getGameServiceManager().hasItem(user, quantity, itemSlug);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if the specified user has the specified item equipped.
     *
     * @param user The user snowflake object.
     * @param itemSlug The item slug to check.
     * @return True if the user has the item equipped, false otherwise.
     */
    @Override
    public boolean isEquipped(UserSnowflake user, String itemSlug) {
        try {
            return this.instance.getGameServiceManager().isEquipped(user, itemSlug);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if a user has a specific item equipped or owned.
     *
     * @param user The user to check.
     * @param itemSlug The slug of the item to check.
     * @return A pair indicating whether the item is equipped or owned. The first element of the pair is a boolean value,
     *         where true indicates that the item is equipped or owned, and false indicates otherwise. The second element
     *         of the pair is a string message that provides additional information about the item. If the item does not
     *         exist, the message will be "This item does not exist." If the user does not own the item, the message will
     *         be "The user does not own {item name}". If the item is not equipped, the message will be "The item
     *         {item name} is not equipped."
     */
    @Override
    @SneakyThrows
    public Pair<Boolean, String> isEquippedOrOwned(UserSnowflake user, String itemSlug) {
        ItemDescriptionBean item = this.instance.getGameServiceManager().fetchItem(itemSlug);
        if (item == null) {
            return Pair.of(true, "This item does not exist.");
        }

        if (!this.hasItem(user, item.getItemSlug())) {
            return Pair.of(true, FluffBOT.getInstance().getLanguageManager().get("common.inventory.item_not_owned", item.getItemName()));
        } else if (!this.isEquipped(user, item.getItemSlug())) {
            return Pair.of(true, FluffBOT.getInstance().getLanguageManager().get("common.inventory.item_not_equipped", item.getItemName()));
        }

        return Pair.of(false, "");
    }

    /**
     * Decreases the satiety of a user by a specified factor.
     *
     * @param user   the UserSnowflake object representing the user
     * @param factor the factor by which to decrease the satiety
     */
    @Override
    public void decreaseSatiety(UserSnowflake user, double factor) {
        try {
           PlayerBean player = this.instance.getGameServiceManager().getPlayer(user.getId());
            double satiety = Math.max(0, player.getHungerPercentage() - factor);

           if (satiety <= 0.01) {
               this.decreaseHealth(user, factor);
           } else {
               player.setHungerPercentage(satiety);
               this.saveUser(player);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decreases the health of a user by a specified factor.
     *
     * @param user   the UserSnowflake object representing the user
     * @param factor the factor by which to decrease the health
     */
    @Override
    public void decreaseHealth(UserSnowflake user, double factor) {
        try {
            PlayerBean player = this.instance.getGameServiceManager().getPlayer(user.getId());

            double health = player.getHealthPercentage() - factor;

            if (health <= 0.01)
                this.killPlayer(user, PlayerDeathInfo
                        .builder()
                        .deathType(DeathType.ENVIRONMENT)
                        .build()
                );
            else {
                player.setHealthPercentage(health);
                this.saveUser(player);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decreases the health of a user by a specified factor.
     *
     * @param hitter the UserSnowflake object representing the user who caused the damage
     * @param target the UserSnowflake object representing the target user
     * @param factor the factor by which to decrease the health
     */
    private void decreaseHealthByHit(UserSnowflake hitter, UserSnowflake target, InventoryItem hitterSword, double factor) {
        try {
            PlayerBean player = this.instance.getGameServiceManager().getPlayer(target.getId());

            double health = player.getHealthPercentage() - factor;

            if (health < .01)
                this.killPlayer(target, PlayerDeathInfo
                        .builder()
                        .deathType(DeathType.PLAYER)
                        .from(hitter)
                        .hitter(hitterSword)
                        .build()
                );
            else {
                player.setHealthPercentage(health);
                this.saveUser(player);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decreases the mana of a user by a specified factor.
     *
     * @param user   the UserSnowflake object representing the user
     * @param factor the factor by which to decrease the mana
     */
    @Override
    public void decreaseMana(UserSnowflake user, double factor) {
        try {
            PlayerBean player = this.instance.getGameServiceManager().getPlayer(user.getId());
            player.setManaPercentage(player.getManaPercentage() - factor);

            this.saveUser(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Decreases the health of a user by a specified factor as a result of a hit action.
     *
     * @param user   the UserSnowflake object representing the user who caused the hit
     * @param target the UserSnowflake object representing the target user
     */
    @Override
    public void hit(UserSnowflake user, UserSnowflake target) {
        try {
            PlayerBean targetPlayer = this.fetchUser(target);
            if (targetPlayer != null) {
                InventoryItem hitterSword = this.instance.getGameServiceManager().getPlayerItemBySlot(user, EquipmentSlot.SWORD);
                if (hitterSword != null) {
                    ItemInformationBuilder hitterSwordInfo = this.instance.getItemManager().getItem(hitterSword.getItemSlug());
                    if (hitterSwordInfo != null) {
                        InventoryBuilder inventory = this.fetchInventory(target);
                        double targetResistance = inventory.totalResistance();
                        double damage = Math.abs(hitterSwordInfo.getDamageFactor() / targetResistance) * 2;
                        this.decreaseHealthByHit(user, target, hitterSword, damage);

                        inventory.getItems()
                                .stream()
                                .filter(item -> item.getEquipmentType() == EquipmentType.ARMOR)
                                .forEach(item -> {
                                    try {
                                        FluffBOT.getInstance().getGameServiceManager().updateDurability(target, item.getItemSlug(), (int) damage + new Random().nextInt(0, 100));
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });

                    } else {
                        // Fallback
                        this.decreaseHealthByHit(user, target, hitterSword, 0.00003);
                    }

                    FluffBOT.getInstance().getGameServiceManager().updateDurability(target, hitterSword.getItemSlug(), new Random().nextInt(0, 100));
                } else {
                    // Simulate a fist-fight
                    this.decreaseHealthByHit(user, target, null, 0.003);
                }
            } else {
                throw new Exception("Target player not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Respawns a user.
     *
     * @param user the UserSnowflake object representing the user
     */
    @Override
    public void respawn(UserSnowflake user) {
        try {
            PlayerBean player = this.fetchUser(user);
            player.setHealthPercentage(1.0);
            player.setHungerPercentage(1.0);
            player.setManaPercentage(0.0);

            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .updatePlayer(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Kills a player by updating their status or performing necessary actions upon death.
     *
     * @param user the UserSnowflake object representing the player
     * @param deathInfo information about the player's death, including the user who caused the death and the type of death
     */
    @Override
    public void killPlayer(UserSnowflake user, PlayerDeathInfo deathInfo) {
        try {
            User target = this.instance.getJda().getUserById(user.getId());
            EmbedBuilder killMessage = this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.player.death", target.getGlobalName()), "https://fluffici.eu", ICON_ALERT);

            switch (deathInfo.getDeathType()) {
                case PLAYER -> {
                    User killer = this.instance.getJda().getUserById(deathInfo.getFrom().getId());
                    killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.player", target.getGlobalName(), killer.getGlobalName(), deathInfo.getHitter().getName()));
                }
                case MOB -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.mob"));
                case EFFECT -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.effect"));
                case ENVIRONMENT -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.environment"));
                case LAVA -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.lava"));
                case ZONE -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.zone"));
                case VOID -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.void"));
                case MAGIC -> killMessage.setDescription(this.instance.getLanguageManager().get("common.slain.magic"));
            }

            killMessage.setTimestamp(Instant.now());
            killMessage.setThumbnail(target.getAvatarUrl());
            this.instance.getJda().getTextChannelById(this.instance.getChannelConfig().getProperty("channel.level")).sendMessageEmbeds(killMessage.build()).queue();

            this.respawn(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Heals a user.
     *
     * @param user the UserSnowflake object representing the user
     */
    @Override
    public void heal(UserSnowflake user) {
        try {
            PlayerBean player = this.instance.getGameServiceManager().getPlayer(user.getId());
            player.setHealthPercentage( Math.abs(player.getHealthPercentage() + 0.1));

            this.saveUser(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the system user as a PlayerBean.
     *
     * @return The system user as a PlayerBean.
     */
    private PlayerBean getSystemUser() {
        SelfUser selfUser = this.instance.getJda().getSelfUser();

        if (!this.hasSystemUser()) {
            this.createPlayer(new PlayerBean(
                    selfUser.getId(),
                    null,
                    null,
                    false,
                    this.instance.getLevelUtil().calculateXPNeeded(500),
                    500,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    0,
                    0,
                    null,
                    100,
                    100,
                    100,
                    100,
                    0,
                    "",
                    0
            ));
            this.instance.getLogger().info("[SystemUser] the system user was created with success.");
        }

        return this.fetchUser(selfUser);
    }

    /**
     * Checks if the system user exists.
     *
     * @return true if the system user exists, false otherwise.
     */
    private boolean hasSystemUser() {
        return this.fetchUser(this.instance.getJda().getSelfUser()) != null;
    }
}
