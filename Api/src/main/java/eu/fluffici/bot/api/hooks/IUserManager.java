package eu.fluffici.bot.api.hooks;

/*
---------------------------------------------------------------------------------
File Name : IUserManager.java

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


import eu.fluffici.bot.api.CurrencyType;
import eu.fluffici.bot.api.beans.players.*;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.game.PlayerDeathInfo;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.item.InventoryStatus;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.List;
@SuppressWarnings("All")
public interface IUserManager {
    PlayerBean fetchUser(UserSnowflake snowflake);
    PlayerBean fetchUserAsync(UserSnowflake snowflake);

    void createPlayer(PlayerBean player);
    void saveUser(PlayerBean player);

    boolean isDeveloper(UserSnowflake snowflake);
    BirthdayBean fetchBirthdate(UserSnowflake snowflake);
    void createBirthdate(BirthdayBean birthday);

    public int calculateFreeSpace(UserSnowflake user);

    Pair<Boolean, String> transferTokens(PlayerBean issuer, CurrencyType currencyType, int amount, PlayerBean target);

    void addTokens(PlayerBean target, int amount);

    void removeTokens(PlayerBean target, int amount);

    boolean hasEnoughTokens(PlayerBean player, int amount);

    boolean hasEnoughCoins(PlayerBean player, int amount);

    RelationshipInviteBuilder getActiveRel(UserSnowflake snowflake);

    boolean hasEnoughUpvotes(PlayerBean player, int amount);

    void addCoins(PlayerBean target, int amount);

    void removeCoins(PlayerBean target, int amount);

    void addUpvote(PlayerBean target, int amount);

    void removeUpvote(PlayerBean target, int amount);

    void addEvent(PlayerBean target, int amount);

    void removeEvent(PlayerBean target, int amount);
    void removeKarma(PlayerBean target, int amount);
    void removeExperience(PlayerBean target, int amount);
    void removeLevel(PlayerBean target, int amount);

    void updateExperience(PlayerBean target, int amount);

    void updateLevel(PlayerBean target, int amount);

    boolean hasEnoughKarma(PlayerBean player, int amount);

    boolean hasEnoughEvents(PlayerBean player, int amount);

    boolean hasClan(PlayerBean player);

    UserClanBean fetchClan(PlayerBean player);

    void updateClan(UserClanBean clan);

    void addKarma(PlayerBean target, int amount);

    void updateBooster(PlayerBean target, int multiplier);

    InventoryBuilder fetchInventory(UserSnowflake snowflake);

    List<LeaderboardBuilder> getTopUsersByLevel(int limit);
    List<LeaderboardBuilder> getTopUsersByStats(int limit, String statistic);

    Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, InventoryItem item);
    Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, ItemDescriptionBean item);
    Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, ItemDescriptionBean item, int quantity);

    boolean isInventoryFull(UserSnowflake user);

    boolean fetchRelationship(UserSnowflake owner, UserSnowflake user);
    boolean fetchRelationship(UserSnowflake owner);
    List<RelationshipMember> fetchAllRelationshipMembers(UserSnowflake user);
    void acceptInvite(UserSnowflake snowflake);
    RelationshipInviteBuilder fetchInvite(UserSnowflake snowflake);

    void updateRelationships(UserSnowflake snowflake, UserSnowflake target);

    void sendInvite(RelationshipInviteBuilder relationshipInvite);

    void acknowledgeInvite(UserSnowflake user);

    void removeRelationship(UserSnowflake snowflake, UserSnowflake target);

    void incrementStatistics(UserSnowflake user, String statistics, int score);

    boolean isBlocked(UserSnowflake user, UserSnowflake target);

    void blockUser(UserSnowflake user, UserSnowflake target);

    void unblockUser(UserSnowflake user, UserSnowflake target);

    List<BlockedUser> blockedUsers(UserSnowflake user);

    boolean hasChannel(UserSnowflake user);

    ChannelBean getChannel(UserSnowflake user);

    void createChannel(ChannelBean channel);

    void deleteChannel(UserSnowflake user);

    void transferChannelOwner(UserSnowflake currentOwner, UserSnowflake newOwner);

    List<OutingSubscriber> fetchAllSubscriber();
    void createOutingSubscriber(UserSnowflake user);
    void removeOutingSubscriber(UserSnowflake user);
    boolean isOutingSubscribed(UserSnowflake user);
    boolean hasUprade(UserSnowflake user, int upgradeId);
    boolean hasItem(UserSnowflake user, String itemSlug);
    boolean hasItem(UserSnowflake user, String itemSlug, int quantity);
    boolean isEquipped(UserSnowflake user, String itemSlug);

    Pair<Boolean, String> isEquippedOrOwned(UserSnowflake user, String itemSlug);

    void decreaseSatiety(UserSnowflake user, double factor);
    void decreaseHealth(UserSnowflake user, double factor);
    void decreaseMana(UserSnowflake user, double factor);
    void hit(UserSnowflake user, UserSnowflake target);
    void respawn(UserSnowflake user);

    void killPlayer(UserSnowflake user, PlayerDeathInfo deathInfo);

    void heal(UserSnowflake user);

    public Pair<Boolean, InventoryStatus> addItem(UserSnowflake user, String itemSlug, int quantity);

    boolean isVerified(UserSnowflake user);

    void addPointToStaff(UserSnowflake user, int points);
}
