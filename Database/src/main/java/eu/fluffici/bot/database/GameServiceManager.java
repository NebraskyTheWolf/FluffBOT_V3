package eu.fluffici.bot.database;

/*
---------------------------------------------------------------------------------
File Name : GameServiceManager.java

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

import eu.fluffici.bot.api.beans.furraid.*;
import eu.fluffici.bot.api.beans.furraid.verification.Verification;
import eu.fluffici.bot.api.beans.staff.StaffPaycheck;
import eu.fluffici.bot.api.beans.telegram.TelegramVerification;
import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.ticket.TicketMessageBuilder;
import eu.fluffici.bot.api.beans.verification.VerificationBuilder;
import eu.fluffici.bot.api.beans.achievements.AchievementBean;
import eu.fluffici.bot.api.beans.achievements.AchievementCategoryBean;
import eu.fluffici.bot.api.beans.achievements.AchievementProgressBean;
import eu.fluffici.bot.api.beans.channel.AutoReactionBuilder;
import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.clans.ClanMembersBean;
import eu.fluffici.bot.api.beans.clans.ClanRequestBean;
import eu.fluffici.bot.api.beans.game.CasinoGameBuilder;
import eu.fluffici.bot.api.beans.level.LevelReward;
import eu.fluffici.bot.api.beans.players.*;
import eu.fluffici.bot.api.beans.roles.PurchasableRoles;
import eu.fluffici.bot.api.beans.roles.PurchasedRoles;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.beans.statistics.GuildEngagement;
import eu.fluffici.bot.api.beans.statistics.ReportNotification;
import eu.fluffici.bot.api.furraid.permissive.Permissions;
import eu.fluffici.bot.api.furraid.permissive.UserEntity;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import eu.fluffici.bot.api.interactions.Interactions;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.database.datamanager.*;
import eu.fluffici.bot.database.datamanager.furraiddb.FurRaidManager;
import eu.fluffici.bot.database.datamanager.furraiddb.FurRaidSanctionData;
import eu.fluffici.bot.database.datamanager.furraiddb.FurRaidTicketManager;
import eu.fluffici.bot.database.datamanager.furraiddb.FurRaidVerificationManager;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
@SuppressWarnings("All")
public class GameServiceManager {

    private final DatabaseManager databaseManager;
    private final AchievementManager achievementManager;
    private final SanctionData sanctionManager;
    private final UserData playerManager;
    private final ClanDataManager clanDataManager;
    private final InteractionData interactionData;
    private final DiscordBoostData discordBoostData;
    private final ChannelDataManager channelDataManager;
    private final StatisticsManager statisticsManager;
    private final CasinoDataManager casinoDataManager;
    private final EconomyDataManager economyDataManager;
    private final ItemDataManager itemDataManager;
    private final RelationshipDataManager relationshipDataManager;
    private final RolesDataManager rolesDataManager;
    private final RestrictionDataManager restrictionDataManager;
    private final GuildDataManager guildDataManager;
    private final VerificationManager verificationManager;
    private final TicketManager ticketManager;
    private final FurRaidManager furRaidManager;
    private final FurRaidSanctionData furRaidSanctionData;
    private final FurRaidVerificationManager furRaidVerificationManager;
    private final FurRaidTicketManager furRaidTicketManager;
    private final TelegramManager telegramManager;
    private final FurSonaDataManager furSonaDataManager;
    private final ApplicationManager applicationManager;

    public GameServiceManager(String url, String name, String password, int minPoolSize, int maxPoolSize)
    {
        this.databaseManager = DatabaseManager.getInstance(url, name, password, minPoolSize, maxPoolSize);
        this.achievementManager = new AchievementManager();
        this.sanctionManager = new SanctionData();
        this.playerManager = new UserData(this);
        this.clanDataManager = new ClanDataManager();
        this.interactionData = new InteractionData();
        this.discordBoostData = new DiscordBoostData();
        this.channelDataManager = new ChannelDataManager();
        this.statisticsManager = new StatisticsManager();
        this.casinoDataManager = new CasinoDataManager();
        this.economyDataManager = new EconomyDataManager();
        this.itemDataManager = new ItemDataManager(this);
        this.rolesDataManager = new RolesDataManager();
        this.relationshipDataManager = new RelationshipDataManager();
        this.restrictionDataManager = new RestrictionDataManager();
        this.guildDataManager = new GuildDataManager();
        this.verificationManager = new VerificationManager();
        this.ticketManager = new TicketManager();
        this.furRaidManager = new FurRaidManager();
        this.furRaidSanctionData = new FurRaidSanctionData();
        this.furRaidVerificationManager = new FurRaidVerificationManager();
        this.furRaidTicketManager = new FurRaidTicketManager();
        this.telegramManager = new TelegramManager();
        this.furSonaDataManager = new FurSonaDataManager();
        this.applicationManager = new ApplicationManager();
    }

    /*============================================
      Part of player manager
    ============================================*/

    // Get the player by UUID
    public PlayerBean getPlayer(String userId)throws Exception
    {
        // Get the PlayerBean
        return this.playerManager.getPlayer(userId, this.databaseManager.getDataSource());
    }

    public CompletableFuture<PlayerBean> getPlayerAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.playerManager.getPlayer(userId, this.databaseManager.getDataSource());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Fetches the inventory information for a user.
     *
     * @param user The UserSnowflake object representing the user.
     * @return An InventoryBuilder object containing the inventory information.
     * @throws Exception If an error occurs during the fetching process.
     */
    public InventoryBuilder fetchInformation(UserSnowflake user) throws Exception {
        return this.itemDataManager.fetchInventory(user,
                this.databaseManager.getDataSource()
        );
    }

    public InventoryItem getPlayerItem(UserSnowflake user, ItemDescriptionBean item) {
        return this.itemDataManager.getPlayerItem(user, item, this.databaseManager.getDataSource());
    }

    public InventoryItem getPlayerItemBySlot(UserSnowflake user, EquipmentSlot slot) {
        return this.itemDataManager.getPlayerItem(user, slot, this.databaseManager.getDataSource());
    }

    public List<InventoryItem> getPlayerItemBySlotGroup(UserSnowflake user, EquipmentSlot slot) {
        return this.itemDataManager.getPlayerItemByGroup(user, slot, this.databaseManager.getDataSource());
    }

    public List<LeaderboardBuilder> fetchLeaderboard(int limit) throws Exception {
        return this.statisticsManager.getTopUsersByLevel(limit, this.databaseManager.getDataSource());
    }

    public List<LeaderboardBuilder> getTopUsersByStats(int limit, String statistics) throws Exception {
        return this.statisticsManager.getTopUsersByStats(limit, statistics, this.databaseManager.getDataSource());
    }

    public LeaderboardBuilder getPlayerStatistics(UserSnowflake user, String name) throws Exception {
        return this.statisticsManager.getPlayerStatistics(user, name, this.databaseManager.getDataSource());
    }

    public BlockedUser isBlocked(UserSnowflake user, UserSnowflake target) throws Exception {
        return this.playerManager.isBlocked(user, target, this.databaseManager.getDataSource());
    }

    public boolean hasSubscriptionStatus(UUID eventId, String status) {
        return this.playerManager.hasSubscriptionStatus(eventId, status, this.databaseManager.getDataSource());
    }

    public void createOrUpdateStatus(UUID eventId, String status) {
        this.playerManager.createOrUpdateStatus(eventId, status, this.databaseManager.getDataSource());
    }

    public void addToBlocklist(UserSnowflake user, UserSnowflake target) throws Exception {
        this.playerManager.addToBlocklist(user, target, this.databaseManager.getDataSource());
    }

    public void remToBlocklist(UserSnowflake user, UserSnowflake target) throws Exception {
        this.playerManager.remToBlocklist(user, target, this.databaseManager.getDataSource());
    }

    public List<BlockedUser> getBlockedUsers(UserSnowflake user) throws Exception {
        return this.playerManager.getBlockedUsers(user, this.databaseManager.getDataSource());
    }

    public void createUserStatistics(UserSnowflake user, String statistic) throws Exception {
        this.statisticsManager.createUserStatistics(user, statistic, this.databaseManager.getDataSource());
    }

    public void incrementPlayerStatistics(UserSnowflake user, String statistic, int value) throws Exception {
        this.statisticsManager.incrementUserStatistics(user, statistic, value, this.databaseManager.getDataSource());
    }

    public void setUserStatistics(UserSnowflake user, String statistic, long value) throws Exception {
        this.statisticsManager.setUserStatistics(user, statistic, value, this.databaseManager.getDataSource());
    }


    public boolean hasStatistics(UserSnowflake user, String statistic) throws Exception {
        return this.statisticsManager.hasStatistics(user, statistic, this.databaseManager.getDataSource());
    }

    public ItemDescriptionBean fetchItem(int itemId) {
        return this.itemDataManager.fetchItem(itemId, this.databaseManager.getDataSource());
    }

    public List<ItemDescriptionBean> fetchAllFishable() {
        return this.itemDataManager.fetchAllFishable(this.databaseManager.getDataSource());
    }

    public void addInventoryItem(UserSnowflake user, InventoryItem item) throws Exception {
        this.itemDataManager.addInventoryItem(user, item, this.databaseManager.getDataSource());
    }

    public void addInventoryItem(UserSnowflake user, InventoryItem item, int quantity) throws Exception {
        this.itemDataManager.addInventoryItem(user, item, quantity, this.databaseManager.getDataSource());
    }

    public void incrementQuantity(UserSnowflake user, InventoryItem item) throws Exception {
        this.itemDataManager.incrementQuantity(user, item, this.databaseManager.getDataSource());
    }

    public Pair<Boolean, String> decrementQuantity(UserSnowflake user, ItemDescriptionBean item, int quantity) {
        return this.itemDataManager.decrementQuantity(user, item, quantity, this.databaseManager.getDataSource());
    }

    public void createItemDrop(DropItemBuilder dropItem) {
        this.itemDataManager.createItemDrop(dropItem, this.databaseManager.getDataSource());
    }

    public DropItemBuilder fetchDroppedItem(String dropId) {
        return this.itemDataManager.fetchDroppedItem(dropId, this.databaseManager.getDataSource());
    }

    public void claimDrop(UserSnowflake user, String dropId) throws SQLException {
        this.itemDataManager.claimDrop(user, dropId, this.databaseManager.getDataSource());
    }

    public Pair<Boolean, PlayerJob> getPlayerLastShift(UserSnowflake user) throws Exception {
        return this.playerManager.getPlayerLastShift(user, this.databaseManager.getDataSource());
    }

    public int getDailyShiftSum() {
        return this.playerManager.getSumAllPlayersShiftsToday(this.databaseManager.getDataSource());
    }

    public List<OutingSubscriber> getAllSubscribers() throws Exception {
        return this.playerManager.fetchAllSubscribers(this.databaseManager.getDataSource());
    }

    public void createNewSubscriber(UserSnowflake userSnowflake) throws Exception {
        this.playerManager.createOutingSubscriber(userSnowflake, this.databaseManager.getDataSource());
    }

    public void removeSubscriber(UserSnowflake userSnowflake) throws Exception {
        this.playerManager.removeOutingSubscriber(userSnowflake, this.databaseManager.getDataSource());
    }

    public boolean isSubscribed(UserSnowflake userSnowflake) throws Exception {
        return this.playerManager.isSubscribed(userSnowflake, this.databaseManager.getDataSource());
    }

    public void createPlayerLastShift(UserSnowflake user) throws Exception {
        this.playerManager.createPlayerLastShift(user, this.databaseManager.getDataSource());
    }

    // Update the player
    public void updatePlayer(PlayerBean player)
    {
        // Update datas of player
        this.playerManager.updatePlayer(player, this.databaseManager.getDataSource());
    }

    // Create the player
    public void createPlayer(PlayerBean player)throws Exception
    {
        // Create the player
        this.playerManager.createPlayer(player, this.databaseManager.getDataSource());
    }

    public void deletePlayer(String player)throws Exception
    {
        // Create the player
        this.playerManager.deletePlayer(player, this.databaseManager.getDataSource());
    }

    public List<Message> sumById(String player)throws Exception
    {
        return this.playerManager.sumById(player, this.databaseManager.getDataSource());
    }

    public List<Message> sumAll()
    {
        return this.playerManager.sumAll(this.databaseManager.getDataSource());
    }

    public void incrementMessage(Message message) throws Exception
    {
        this.playerManager.incrementMessage(message, this.databaseManager.getDataSource());
    }

    public boolean isDeveloper(String id) throws Exception {
        return this.playerManager.isDeveloper(id, this.databaseManager.dataSource);
    }

    public boolean fetchRelationship(UserSnowflake ownerId, UserSnowflake userId) throws Exception {
        return this.relationshipDataManager.fetchRelationship(ownerId, userId, this.databaseManager.getDataSource(), false);
    }

    public void removeRelationship(UserSnowflake user, UserSnowflake target) throws Exception {
        this.relationshipDataManager.removeRelationship(user, target, this.databaseManager.getDataSource());
    }

    public void leaveRelationship(UserSnowflake user) {
        this.relationshipDataManager.remMemberToRelationship(user, this.databaseManager.getDataSource());
    }

    public boolean isRealationshipOwner(UserSnowflake user) {
        return this.relationshipDataManager.isRelationshipOwner(user, this.databaseManager.getDataSource());
    }

    public void updateRelationship(UserSnowflake owner, UserSnowflake target) throws Exception {
        this.relationshipDataManager.addMemberToRelationship(owner, target, this.databaseManager.getDataSource());
    }

    public void createRelationshipInvite(RelationshipInviteBuilder user) throws Exception {
        this.relationshipDataManager.createRelationshipInvite(user, this.databaseManager.getDataSource());
    }

    public void acceptelationshipInvite(UserSnowflake user) throws Exception {
        this.relationshipDataManager.acceptRelationshipInvite(user, this.databaseManager.getDataSource());
    }

    public List<RelationshipMember> fetchAllRelationshipMembers(UserSnowflake user) throws Exception {
        return this.relationshipDataManager.fetchAllRelationshipMembers(user, this.databaseManager.getDataSource());
    }

    public boolean hasClan(String id) throws Exception {
        return this.playerManager.hasClan(id, this.databaseManager.dataSource);
    }

    public UserClanBean getClan(String id) throws Exception {
        return this.playerManager.getUserClan(id, this.databaseManager.dataSource);
    }

    public void updateUserClan(UserClanBean clanBean) throws Exception {
        this.playerManager.saveUserClan(clanBean, this.databaseManager.dataSource);
    }

    public void deleteClan(ClanBean clanBean) throws Exception {
        this.playerManager.deleteClan(clanBean, this.databaseManager.dataSource);
    }

    public void deleteUserClan(ClanMembersBean clanBean) throws Exception {
        this.playerManager.deleteUserClan(clanBean, this.databaseManager.dataSource);
    }

    public BirthdayBean getBirthdate(String id) throws Exception {
        return this.playerManager.getUserBirthdate(id, this.databaseManager.dataSource);
    }

    public List<BirthdayBean> getAllBirthdate() throws Exception {
        return this.playerManager.getAllBirthdates(this.databaseManager.dataSource);
    }

    public void createBirthdate(BirthdayBean birthday) throws Exception {
        this.playerManager.createBirthdate(birthday, this.databaseManager.dataSource);
    }

    public void updateBirthdate(BirthdayBean birthday) throws Exception {
        this.playerManager.updateBirthdate(birthday, this.databaseManager.dataSource);
    }

    public void createEconomyRecord(EconomyHistory economyHistory) throws Exception {
        this.economyDataManager.createEconomyHistory(economyHistory,  this.databaseManager.getDataSource());
    }

    public List<EconomyHistory> getAllEconomyRecord() throws Exception {
        return this.economyDataManager.getAllEconomy(this.databaseManager.getDataSource());
    }


    public void acknowledgeInviteRel(UserSnowflake user) throws Exception {
        this.relationshipDataManager.acceptRelationshipInvite(user, this.databaseManager.getDataSource());
    }

    public RelationshipInviteBuilder fetchActiveRelationshipInvite(UserSnowflake user) throws Exception {
        return this.relationshipDataManager.fetchRelationshipInvite(user, this.databaseManager.getDataSource());
    }

     /*============================================
      Part of discord boost manager
    ============================================*/

    @SneakyThrows
    public void applyBoost(PlayerBoost playerBoost) {
        this.discordBoostData.applyBoost(playerBoost, this.databaseManager.getDataSource());
    }

    @SneakyThrows
    public boolean isBoosting(UserSnowflake user) {
        return this.discordBoostData.isBoosting(user, this.databaseManager.getDataSource());
    }

    @SneakyThrows
    public PlayerBoost fetchBoost(UserSnowflake user) {
        return this.discordBoostData.fetchBoost(user, this.databaseManager.getDataSource());
    }

    @SneakyThrows
    public void updateBoost(PlayerBoost playerBoost) {
        this.discordBoostData.updateBoost(playerBoost, this.databaseManager.getDataSource());
    }

    /*============================================
      Part of sanction manager
    ============================================*/

    // Apply a sanction to a player
    public void applySanction(int sanctionType, SanctionBean sanction)throws Exception
    {
        // Do the sanction
        this.sanctionManager.applySanction(sanctionType, sanction, this.databaseManager.getDataSource());
    }

    public void removeSanction(int sanctionType, PlayerBean player)throws Exception
    {
        // Remove the sanction
        this.sanctionManager.removeSanction(sanctionType, player, this.databaseManager.getDataSource());
    }

    // Check if a player is banned
    @SneakyThrows
    public SanctionBean getPlayerBanned(String player)
    {
        // Check the ban status
        return this.sanctionManager.getPlayerBanned(player, this.databaseManager.getDataSource());
    }

    // Check if a player is muted
    public SanctionBean getPlayerMuted(String player)throws Exception
    {
        // Check the mute status
        return this.sanctionManager.getPlayerMuted(player, this.databaseManager.getDataSource());
    }

    // Get all sanctions for a player and type
    public List<SanctionBean> getAllSanctions() throws Exception
    {
        // Get sanctions
        return this.sanctionManager.getAllSanctions(this.databaseManager.getDataSource());
    }

    public List<SanctionBean> getAllSanctionsBy(UserSnowflake moderator) throws Exception
    {
        // Get sanctions
        return this.sanctionManager.getAllSanctionsBy(moderator, this.databaseManager.getDataSource());
    }

    public List<SanctionBean> getAllWarns(String userId) throws Exception {
        return this.sanctionManager.getAllWarns(userId, this.databaseManager.getDataSource());
    }

    public List<SanctionBean> getAllActiveWarns(String userId) throws Exception {
        return this.sanctionManager.getAllActiveWarns(userId, this.databaseManager.getDataSource());
    }


    // Get all actives sanctions for a player and type
    public List<SanctionBean> getAllActiveSanctions(String userId, int sanctionType) throws Exception
    {
        // Get sanctions
        return this.sanctionManager.getAllActiveSanctions(userId, sanctionType, this.databaseManager.getDataSource());
    }

    public void updateSanction(long sanctionId, boolean status) throws Exception {
        this.sanctionManager.updateSanctionStatus(sanctionId, status, this.getDatabaseManager().dataSource);
    }

    public List<SanctionBean> updateAllSanctions() throws Exception {
        return this.sanctionManager.updateExpiredSanctions(this.databaseManager.dataSource);
    }

    /*============================================
      Part of achievement manager
    ============================================*/

    // Get a achievement category by id
    public AchievementCategoryBean getAchievementCategory(int categoryId) throws Exception
    {
        return this.achievementManager.getAchievementCategory(categoryId, this.databaseManager.getDataSource());
    }

    // Get all achievement categories
    public List<AchievementCategoryBean> getAchievementCategories() throws Exception
    {
        return this.achievementManager.getAchievementCategories(this.databaseManager.getDataSource());
    }

    // Get a achievement by id
    public AchievementBean getAchievement(int achievementId) throws Exception
    {
        return this.achievementManager.getAchievement(achievementId, this.databaseManager.getDataSource());
    }

    // Get all achievements
    public List<AchievementBean> getAchievements() throws Exception
    {
        return this.achievementManager.getAchievements(this.databaseManager.getDataSource());
    }

    // Create a achievement progress
    public int createAchievementProgress(String userId, AchievementProgressBean progress) throws Exception
    {
        return this.achievementManager.createAchievementProgress(userId, progress, this.databaseManager.getDataSource());
    }

    // Get a achievement progress
    public AchievementProgressBean getAchievementProgress(String userId, int achievementId) throws Exception
    {
        return this.achievementManager.getAchievementProgress(userId, achievementId, this.databaseManager.getDataSource());
    }

    // Get a achievement progress
    public List<AchievementProgressBean> getAchievementProgresses(String player) throws Exception
    {
        return this.achievementManager.getAchievementProgresses(player, this.databaseManager.getDataSource());
    }

    // Update a achievement progress
    public void updateAchievementProgress(AchievementProgressBean progress) throws Exception
    {
        this.achievementManager.updateAchievementProgress(progress, this.databaseManager.getDataSource());
    }

    /*============================================
      Part of clan manager
    ============================================*/

    public void createClan(ClanBean clan) throws Exception {
        this.clanDataManager.createClan(clan, this.databaseManager.getDataSource());
    }

    public ClanBean fetchClan(String clanId) throws Exception {
        return this.clanDataManager.getClan(clanId, this.databaseManager.getDataSource());
    }

    public boolean hasPrefix(String prefix) throws Exception {
        return this.clanDataManager.hasPrefix(prefix, this.databaseManager.getDataSource());
    }

    public boolean hasTitle(String title) throws Exception {
        return this.clanDataManager.hasTitle(title, this.databaseManager.getDataSource());
    }

    public void updateClan(ClanBean clan) throws Exception {
        this.clanDataManager.updateClan(clan, this.databaseManager.getDataSource());
    }

    public List<ClanMembersBean> fetchClanMembers(String clanId) throws Exception {
        return this.clanDataManager.getClanMembers(clanId, this.databaseManager.getDataSource());
    }

    public ClanMembersBean fetchClanMember(PlayerBean player) throws Exception {
        return this.clanDataManager.getClanMember(player, this.databaseManager.getDataSource());
    }

    public void deleteClanMember(ClanMembersBean clanId) throws Exception {
        this.clanDataManager.deleteClanMember(clanId, this.databaseManager.getDataSource());
    }

    public void addClanMember(ClanMembersBean member) throws Exception {
        this.clanDataManager.addClanMember(member, this.databaseManager.getDataSource());
    }

    public void createClanInvite(ClanRequestBean request) throws Exception {
        this.clanDataManager.createClanInvite(request, this.databaseManager.getDataSource());
    }

    public  void acknowledgeInvite(String inviteId) throws Exception {
        this.clanDataManager.setAcknowledged(inviteId, this.databaseManager.getDataSource());
    }

    public void denyInvite(String inviteId) throws Exception {
        this.clanDataManager.setDenied(inviteId, this.databaseManager.getDataSource());
    }

    public ClanRequestBean getActiveRequest(String userId) throws Exception {
        return this.clanDataManager.getActiveInvite(userId, this.databaseManager.getDataSource());
    }

    public void updateExpiredInvites() throws Exception {
        this.clanDataManager.updateExpiredInvites(this.getDatabaseManager().getDataSource());
    }

    /*============================================
      Part of interaction manager
    ============================================*/

    public void newInteraction(Interactions interactions) throws Exception {
        this.interactionData.newInteraction(interactions, this.databaseManager.getDataSource());
    }

    public void updateInteraction(Interactions interactions) throws Exception {
        this.interactionData.updateInteraction(interactions, this.databaseManager.getDataSource());
    }


    public Interactions fetchInteraction(String id) throws Exception {
        return this.interactionData.fetchInteraction(id, this.databaseManager.getDataSource());
    }

    public void setAcknowledged(String userId) throws Exception {
        this.interactionData.setAcknowledged(userId, this.databaseManager.getDataSource());
    }

    public void setAttached(Interactions interactions) throws Exception {
        this.interactionData.setAttached(interactions, this.databaseManager.getDataSource());
    }

    public void setUpdated(Interactions interactions) throws Exception {
        this.interactionData.setUpdated(interactions, this.databaseManager.getDataSource());
    }

    public void updateExpiredInteractions() throws Exception {
        this.interactionData.updateExpiredInteractions(this.databaseManager.getDataSource());
    }

    public List<Interactions> getAllInteractions() throws Exception {
        return this.interactionData.getAllInteractions(this.databaseManager.getDataSource());
    }

    // CHANNELS

    public void createChannel(ChannelBean channel) throws Exception {
        this.channelDataManager.createChannel(channel, this.databaseManager.getDataSource());
    }

    public boolean hasChannel(UserSnowflake currentOwner) throws Exception {
        return this.channelDataManager.hasChannel(currentOwner, this.databaseManager.getDataSource());
    }

    public ChannelBean getChannel(UserSnowflake currentOwner) throws Exception {
        return this.channelDataManager.getChannel(currentOwner, this.databaseManager.getDataSource());
    }

    public void transferChannelOwner(UserSnowflake currentOwner, UserSnowflake newOwner) throws Exception {
        this.channelDataManager.transferOwner(currentOwner, newOwner, this.databaseManager.getDataSource());
    }

    public void deleteChannel(UserSnowflake currentOwner) throws Exception {
        this.channelDataManager.deleteChannel(currentOwner, this.databaseManager.getDataSource());
    }

    public List<DummyChannel> deleteNotPaidChannels() {
        return this.channelDataManager.deleteNotPaidChannels(this.databaseManager.getDataSource());
    }

    public List<DummyChannel> getChannelsExpiringSoon() {
        return this.channelDataManager.getChannelsExpiringSoon(this.databaseManager.getDataSource());
    }

    public DummyChannel getChannelsExpiringSoon(String channelId) {
        return this.channelDataManager.getChannelsExpiringSoon(channelId, this.databaseManager.getDataSource());
    }

    public void addChannelRent(ChannelRent rent) {
        this.channelDataManager.addChannelRent(rent, this.databaseManager.getDataSource());
    }

    public ChannelOption getChannelOption(String channelId) {
        return this.channelDataManager.getChannelOption(channelId, this.databaseManager.getDataSource());
    }

    public void updateChannelOption(boolean isAutoRenewal, String channelId) {
        this.channelDataManager.updateChannelOption(isAutoRenewal, channelId, this.databaseManager.getDataSource());
    }

    // statistics manager

    public void insertVoiceActivity(UserSnowflake user, int amount) {
        this.statisticsManager.insertVoiceActivity(user, amount, this.databaseManager.getDataSource());
    }

    public List<PlayerVoiceActivity> getVoiceActivities() {
        return this.statisticsManager.getVoiceActivity(this.databaseManager.getDataSource());
    }

    public void insertGuildEngagement(UserSnowflake user, GuildEngagement.Action action) {
        this.statisticsManager.insertGuildEngagement(user, action, this.databaseManager.getDataSource());
    }

    public List<GuildEngagement> getGuildEngagements()  {
        return this.statisticsManager.getGuildEngagements(this.databaseManager.getDataSource());
    }

    public void insertGuildEngagementReport(ReportNotification.ReportType reportType) {
        this.statisticsManager.insertGuildEngagementReport(reportType, this.databaseManager.getDataSource());
    }

    public Pair<Boolean, ReportNotification> hasActiveReport(ReportNotification.ReportType reportType) {
        return this.statisticsManager.hasActiveReport(reportType, this.databaseManager.getDataSource());
    }

    // Casino game service

    public void createGameSession(CasinoGameBuilder game) {
        this.casinoDataManager.createGameSession(game, this.databaseManager.getDataSource());
    }

    public CasinoGameBuilder getGameSession(String gameId) {
        return this.casinoDataManager.getGameSession(gameId, this.databaseManager.getDataSource());
    }

    public void updateGameSession(CasinoGameBuilder game) {
        this.casinoDataManager.updateGameSession(game, this.databaseManager.getDataSource());
    }

    public List<CasinoGameBuilder> getAllGameSessions(String gameId) {
        return this.casinoDataManager.getAllGameSessions(this.databaseManager.getDataSource());
    }
    public void updateGameStatus(String gameId, CasinoGameBuilder.GameStatus gameStatus) {
        this.casinoDataManager.updateGameStatus(gameId, gameStatus, this.databaseManager.getDataSource());
    }
    public void updateScores(String gameId, int playerScore, int neonScore) {
        this.casinoDataManager.updateScores(gameId, playerScore, neonScore, this.databaseManager.getDataSource());
    }
    public boolean hasGame(UserSnowflake user) {
        return this.casinoDataManager.hasGame(user, this.databaseManager.getDataSource());
    }
    public CasinoGameBuilder fetchGame(UserSnowflake user) {
        return this.casinoDataManager.fetchGame(user, this.databaseManager.getDataSource());
    }

    public boolean hasUpgrade(UserSnowflake user, int upgradeId) {
        return this.itemDataManager.hasUpgrade(user, upgradeId, this.databaseManager.getDataSource());
    }

    public boolean hasItem(UserSnowflake user, String slug) {
        return this.itemDataManager.hasItem(user, slug, this.databaseManager.getDataSource());
    }

    public boolean hasItem(UserSnowflake user, int quantity, String slug) {
        return this.itemDataManager.hasItem(user, slug, quantity, this.databaseManager.getDataSource());
    }

    public boolean isEquipped(UserSnowflake user, String slug) {
        return this.itemDataManager.isEquipped(user, slug, this.databaseManager.getDataSource());
    }

    public List<ItemDescriptionBean> getUnlockedItems(int level) {
        return this.itemDataManager.fetchAllUnlockedItems(level, this.getDatabaseManager().getDataSource());
    }

    public List<ItemDescriptionBean> getAllItems() throws Exception {
        return this.itemDataManager.fetchAllItems(this.getDatabaseManager().getDataSource());
    }

    public List<ItemDescriptionBean> getAllInternalItems() throws Exception {
        return this.itemDataManager.getAllInternalItems(this.getDatabaseManager().getDataSource());
    }

    public List<ItemDescriptionBean> fetchAllItemsPurchasable() throws Exception {
        return this.itemDataManager.fetchAllItemsPurchasable(this.getDatabaseManager().getDataSource());
    }

    public ItemDescriptionBean fetchItem(String slug) throws Exception {
        return this.itemDataManager.fetchItem(slug, this.getDatabaseManager().getDataSource());
    }

    public ItemDescriptionBean fetchInternalItem(String slug) throws Exception {
        return this.itemDataManager.fetchInternalItem(slug, this.getDatabaseManager().getDataSource());
    }

    public void updateDurability(UserSnowflake user, String itemSlug, int durability) throws Exception {
        this.itemDataManager.updateDurability(user, itemSlug, durability, this.databaseManager.getDataSource());
    }

    public void setEquipped(UserSnowflake user, int itemId, boolean isEquipped) {
        this.itemDataManager.setEquipped(user, itemId, isEquipped, this.databaseManager.getDataSource());
    }

    // Role manager

    public List<PurchasableRoles> fetchPurchasableRoles() {
        return this.rolesDataManager.fetchAllPurchasableRoles(this.databaseManager.getDataSource());
    }

    public void addPurchasedRoles(PurchasedRoles role) {
        this.rolesDataManager.addPurchasedRole(role, this.databaseManager.getDataSource());
    }

    public PurchasableRoles fetchRoleById(String roleId) {
        return this.rolesDataManager.fetchRoleById(roleId, this.databaseManager.getDataSource());
    }

    public PurchasedRoles fetchRoleById(UserSnowflake user, String roleId) {
        return this.rolesDataManager.fetchRoleById(user, roleId, this.databaseManager.getDataSource());
    }

    public PurchasedRoles fetchSelectedRole(UserSnowflake user) {
        return this.rolesDataManager.fetchSelectedRole(user, this.databaseManager.getDataSource());
    }

    public List<PurchasedRoles> fetchPurchasedRoles(UserSnowflake user) {
        return this.rolesDataManager.fetchAllPurchasedRoles(user, this.databaseManager.getDataSource());
    }

    public void updatePurchasedRole(PurchasedRoles role) {
        this.rolesDataManager.updatePurchasedRole(role, this.databaseManager.getDataSource());
    }

    public boolean hasPurchasedRole(UserSnowflake user, String roleId) {
        return this.rolesDataManager.hasPurchasedRole(user, roleId, this.databaseManager.getDataSource());
    }

    public void deletePurchasedRole(PurchasedRoles role) {
        this.rolesDataManager.deletePurchasedRole(role, this.databaseManager.getDataSource());
    }

    public RestrictedAccess fetchRestrictedPlayer(UserSnowflake user) {
        return this.restrictionDataManager.fetchPlayerRestriction(user, this.databaseManager.getDataSource());
    }

    public void createPlayerRestriction(RestrictedAccess restriction) {
        this.restrictionDataManager.savePlayerRestriction(restriction, this.databaseManager.getDataSource());
    }

    public void createAutoReaction(String channelId, String type) {
        this.guildDataManager.createChannelAutoReact(channelId, type, this.databaseManager.getDataSource());
    }

    public void deleteAutoReaction(String channelId) {
        this.guildDataManager.deleteAutoReaction(channelId, this.databaseManager.getDataSource());
    }

    public boolean hasAutoReaction(String channelId) {
        return this.guildDataManager.hasChannelReactions(channelId, this.databaseManager.getDataSource());
    }

    public AutoReactionBuilder fetchChannelReaction(String channelId) {
        return this.guildDataManager.fetchChannelReactions(channelId, this.databaseManager.getDataSource());
    }

    public boolean hasUnlockedRole(UserSnowflake user, String roleId) {
        return this.playerManager.hasUnlockedRole(user, roleId, this.databaseManager.getDataSource());
    }

    public void unlockRole(@NotNull UserSnowflake user, @NotNull LevelReward reward) {
        this.playerManager.unlockRole(user, reward, this.databaseManager.getDataSource());
    }

    public void createRewardRole(String roleId, int requiredLevel) {
        this.playerManager.createRewardRole(roleId, requiredLevel, this.databaseManager.getDataSource());
    }

    public List<LevelReward> getAllRewards() {
        return this.playerManager.getAllRewards(this.databaseManager.getDataSource());
    }

    public boolean hasRewardAtLevel(long level) {
        return this.playerManager.hasRewardAtLevel(level, this.databaseManager.getDataSource());
    }

    public LevelReward getRewardAtLevel(long level) {
        return this.playerManager.getRewardAtLevel(level, this.databaseManager.getDataSource());
    }

    public List<LevelReward> getRewardsUpToLevel(long level) {
        return this.playerManager.getRewardsUpToLevel(level, this.databaseManager.getDataSource());
    }

    public void addPermanentRole(PermanentRole role) {
        this.playerManager.addPermanentRole(role, this.databaseManager.getDataSource());
    }

    public void removePermanentRole(PermanentRole role) {
        this.playerManager.removePermanentRole(role, this.databaseManager.getDataSource());
    }

    public List<PermanentRole> fetchPermanentRoles(UserSnowflake user) {
        return this.playerManager.fetchPermanentRoles(user, this.databaseManager.getDataSource());
    }

    public void createTicket(@NotNull TicketBuilder ticket) {
        this.ticketManager.createTicket(ticket, this.databaseManager.getDataSource());
    }

    public TicketBuilder fetchTicket(String channelId) {
        return this.ticketManager.fetchTicket(channelId, this.databaseManager.getDataSource());
    }

    public TicketBuilder fetchTicketById(String ticketId) {
        return this.ticketManager.fetchTicketById(ticketId, this.databaseManager.getDataSource());
    }

    public TicketBuilder fetchTicketByUser(UserSnowflake user) {
        return this.ticketManager.fetchTicketByUser(user, this.databaseManager.getDataSource());
    }

    public void updateTicket(@NotNull TicketBuilder ticket) {
        this.ticketManager.updateTicket(ticket, this.databaseManager.getDataSource());
    }

    public boolean hasTicket(@NotNull UserSnowflake user) {
        return this.ticketManager.hasTicket(user, this.databaseManager.getDataSource());
    }

    public void addTicketMessage(String ticketId, @NotNull net.dv8tion.jda.api.entities.Message message) {
        this.ticketManager.addTicketMessage(ticketId, message, this.databaseManager.getDataSource());
    }

    public List<TicketMessageBuilder> fetchMessages(String ticketId) {
        return this.ticketManager.fetchMessages(ticketId, this.databaseManager.getDataSource());
    }

    public void createVerificationRecord(@NotNull VerificationBuilder verification) {
        this.verificationManager.createVerificationRecord(verification, this.databaseManager.getDataSource());
    }

    public VerificationBuilder getVerificationRecord(String challengeCode) {
        return this.verificationManager.getVerificationRecord(challengeCode, this.databaseManager.getDataSource());
    }

    public boolean hasVerification(@NotNull UserSnowflake user) {
        return this.verificationManager.hasVerification(user, this.databaseManager.getDataSource());
    }

    public void updateVerificationRecord(@NotNull VerificationBuilder verification) {
        this.verificationManager.updateVerificationRecord(verification, this.databaseManager.getDataSource());
    }

    public HashMap<UserSnowflake, Long> fetchAllReminders() {
        return this.verificationManager.fetchAllReminders(this.databaseManager.getDataSource());
    }

    public boolean addReminder(UserSnowflake user) {
        return this.verificationManager.addReminder(user, this.databaseManager.getDataSource());
    }

    public HashMap<UserSnowflake, Timestamp> fetchExpiredReminders() {
        return this.verificationManager.fetchExpiredReminders(this.databaseManager.getDataSource());
    }

    public HashMap<UserSnowflake, Timestamp> fetchExpiredRemindersLocked() {
        return this.verificationManager.fetchExpiredRemindersLocked(this.databaseManager.getDataSource());
    }

    public boolean hasExpiredReminder(UserSnowflake user) {
        return this.verificationManager.hasExpiredReminder(user, this.databaseManager.getDataSource());
    }

    public void lockReminder(UserSnowflake user) {
        this.verificationManager.lockReminder(user, this.databaseManager.getDataSource());
    }

    public void notifyReminder(UserSnowflake user) {
        this.verificationManager.notifyReminder(user, this.databaseManager.getDataSource());
    }

    public void removeReminder(UserSnowflake user) {
        this.verificationManager.deleteReminder(user, this.databaseManager.getDataSource());
    }

    public boolean isQuarantined(UserSnowflake user) {
        return this.playerManager.isQuarantined(user, this.databaseManager.getDataSource());
    }

    public void quarantineUser(UserSnowflake user) {
        this.playerManager.quarantineUser(user, this.databaseManager.getDataSource());
    }

    public void quarantineFUser(Guild guild, UserSnowflake user) {
        this.furRaidManager.quarantineUser(guild, user, this.databaseManager.getDataSource());
    }

    public boolean isFQuarantined(Guild guild, UserSnowflake user) {
        return this.furRaidManager.isFQuarantined(guild, user, this.databaseManager.getDataSource());
    }

    public void unquarantineFUser(Guild guild, UserSnowflake user) {
        this.furRaidManager.unquarantineUser(guild, user, this.databaseManager.getDataSource());
    }

    public void unquarantineUser(UserSnowflake user) {
        this.playerManager.unquarantineUser(user, this.databaseManager.getDataSource());
    }

    public void addPlayerDeck(@NotNull UserSnowflake user, String slug, boolean isSelected) {
        this.playerManager.addPlayerDeck(user, slug, isSelected, this.databaseManager.getDataSource());
    }

    public String getSelectedDeck(@NotNull UserSnowflake user) {
        return this.playerManager.getSelectedDeck(user, this.databaseManager.getDataSource());
    }

    public void switchDeck(@NotNull UserSnowflake user, String slug) {
        this.playerManager.switchDeck(user, slug, this.databaseManager.getDataSource());
    }

    // FUR RAID DB ONLY

    public void addVote(UserSnowflake user) {
        this.furRaidManager.addVote(user, this.databaseManager.getDataSource());
    }

    public Permissions getApiUserFromToken(String token) {
        return this.furRaidManager.getApiUserFromToken(token, this.databaseManager.getDataSource());
    }

    public void addGlobalBlacklist(BlacklistBuilder blacklist) {
        this.furRaidManager.addGlobalBlacklist(blacklist, this.databaseManager.getDataSource());
    }

    public void removeGlobalBlacklist(UserSnowflake blacklist) {
        this.furRaidManager.removeGlobalBlacklist(blacklist, this.databaseManager.getDataSource());
    }

    public boolean isGloballyBlacklisted(UserSnowflake user) {
        return this.furRaidManager.isGloballyBlacklisted(user, this.databaseManager.getDataSource());
    }

    public void addLocalBlacklist(BlacklistBuilder blacklist) {
        this.furRaidManager.addLocalBlacklist(blacklist, this.databaseManager.getDataSource());
    }

    public void removeLocalBlacklist(Guild guild, UserSnowflake blacklist) {
        this.furRaidManager.removeLocalBlacklist(guild, blacklist, this.databaseManager.getDataSource());
    }

    public boolean isLocallyBlacklisted(Guild guild, UserSnowflake blacklist) {
        return this.furRaidManager.isLocallyBlacklisted(guild, blacklist, this.databaseManager.getDataSource());
    }

    public LocalBlacklist fetchLocalBlacklist(Guild guild, UserSnowflake user) {
        return this.furRaidManager.fetchLocalBlacklist(guild, user, this.databaseManager.getDataSource());
    }

    public List<LocalBlacklist> fetchLocalBlacklists(Guild guild) {
        return this.furRaidManager.fetchLocalBlacklists(guild, this.databaseManager.getDataSource());
    }

    public void addWhitelist(Guild guild, UserSnowflake user) {
        this.furRaidManager.addWhitelist(guild, user, this.databaseManager.getDataSource());
    }

    public List<Whitelist> fetchWhitelist(Guild guild) {
        return this.furRaidManager.fetchWhitelist(guild, this.databaseManager.getDataSource());
    }

    public void removeWhitelist(Guild guild, UserSnowflake user) {
        this.furRaidManager.removeWhitelist(guild, user, this.databaseManager.getDataSource());
    }

    public boolean isWhitelisted(Guild guild, UserSnowflake user) {
        return this.furRaidManager.isWhitelisted(guild, user, this.databaseManager.getDataSource());
    }

    public Blacklist fetchGlobalBlacklist(UserSnowflake user) {
        return this.furRaidManager.fetchGlobalBlacklist(user, this.databaseManager.getDataSource());
    }

    public int globalBlacklistCount() {
        return this.furRaidManager.globalBlacklistCount(this.databaseManager.getDataSource());
    }

    public int localBlacklistCount(long guildId) {
        return this.furRaidManager.localBlacklistCount(guildId, this.databaseManager.getDataSource());
    }

    public int whitelistCount(long guildId) {
        return this.furRaidManager.whitelistCount(guildId, this.databaseManager.getDataSource());
    }

    public String getOfferNameById(long offerId) {
        return this.furRaidManager.getOfferNameById(offerId, this.databaseManager.getDataSource());
    }

    public FurRaidPremiumOffer getOfferById(long offerId) {
        return this.furRaidManager.getOfferById(offerId, this.databaseManager.getDataSource());
    }

    public List<FurRaidPremiumOffer> getAllOffers() {
        return this.furRaidManager.getAllOffers(this.databaseManager.getDataSource());
    }

    public Changelogs getLatestChangelogs() {
        return this.furRaidManager.getLatestChangelogs(this.databaseManager.getDataSource());
    }

    public void createGuild(GuildSettings guild) throws Exception {
        this.furRaidManager.createGuild(guild, this.databaseManager.getDataSource());
    }

    public GuildSettings fetchGuildSettings(Guild guild) {
        return this.furRaidManager.fetchGuildSettings(guild, this.databaseManager.getDataSource());
    }

    public void deleteGuildSettings(Guild guild) {
        this.furRaidManager.deleteGuildSettings(guild, this.databaseManager.getDataSource());
    }

    public void updateGuildSettings(GuildSettings settings) {
        this.furRaidManager.updateGuildSettings(settings, this.databaseManager.getDataSource());
    }

    public boolean isStaff(UserSnowflake user) {
        return this.furRaidManager.isStaff(user, this.databaseManager.getDataSource());
    }

    public GuildPremiumOffer getGuildPremium(long guildId) {
        return this.furRaidManager.getGuildPremium(guildId, this.databaseManager.getDataSource());
    }

    // Telegram sekce

    public void createTelegramVerification(@NotNull TelegramVerification verification) {
        this.telegramManager.createTelegramVerification(verification, this.databaseManager.getDataSource());
    }

    public TelegramVerification getTelegramVerification(long userId) {
        return this.telegramManager.getTelegramVerification(userId, this.databaseManager.getDataSource());
    }

    public boolean hasVerificationCode(long userId) {
        return this.telegramManager.hasVerificationCode(userId, this.databaseManager.getDataSource());
    }

    public boolean isVerified(long userId) {
        return this.telegramManager.isVerified(userId, this.databaseManager.getDataSource());
    }

    public void unlinkAccount(long userId) {
        this.telegramManager.unlinkAccount(userId, this.databaseManager.getDataSource());
    }

    public void addTelegramMessage(long messageId) {
        this.telegramManager.addMessage(messageId, this.databaseManager.getDataSource());
    }

    public boolean hasTelegramMessage(long messageId) {
        return this.telegramManager.hasMessage(messageId, this.databaseManager.getDataSource());
    }

     /*============================================
      Part of sanction manager
    ============================================*/

    // Apply a sanction to a player
    public void applySanction(Sanction sanction)throws Exception {
        this.furRaidSanctionData.applySanction(sanction, this.databaseManager.getDataSource());
    }

    public void removeSanction(Guild guild, int sanctionType, UserSnowflake player)throws Exception
    {
        // Remove the sanction
        this.furRaidSanctionData.removeSanction(guild, sanctionType, player, this.databaseManager.getDataSource());
    }

    // Check if a player is banned
    @SneakyThrows
    public Sanction getPlayerBanned(String player, String guildId) {
        return this.furRaidSanctionData.getPlayerBanned(player, guildId, this.databaseManager.getDataSource());
    }

    // Get all sanctions for a player and type
    public List<Sanction> getAllSanctions(String guildId) throws Exception
    {
        // Get sanctions
        return this.furRaidSanctionData.getAllSanctions(guildId, this.databaseManager.getDataSource());
    }

    public int getAllWarns(String userId, String guildId) throws Exception
    {
        // Get sanctions
        return this.furRaidSanctionData.getAllWarns(userId, guildId, this.databaseManager.getDataSource());
    }


    // Get all actives sanctions for a player and type
    public List<Sanction> getAllActiveSanctions(String userId, int sanctionType, String guildId) throws Exception
    {
        // Get sanctions
        return this.furRaidSanctionData.getAllActiveSanctions(userId, guildId, sanctionType, this.databaseManager.getDataSource());
    }

    public void updateSanction(long sanctionId, String guildId, boolean status) throws Exception {
        this.furRaidSanctionData.updateSanctionStatus(sanctionId, guildId, status, this.getDatabaseManager().dataSource);
    }

    public List<Sanction> updateAllSanctionsFrdb() throws Exception {
        return this.furRaidSanctionData.updateExpiredSanctions(this.databaseManager.dataSource);
    }

    public void createVerificationRecord(@NotNull Verification verification) {
        this.furRaidVerificationManager.createVerificationRecord(verification, this.databaseManager.getDataSource());
    }


    public Verification getVerificationRecord(String guildId, String challengeCode) {
        return this.furRaidVerificationManager.getVerificationRecord(guildId, challengeCode, this.databaseManager.getDataSource());
    }

    public Verification getVerificationRecord(String guildId, UserSnowflake user) {
        return this.furRaidVerificationManager.getVerificationRecord(guildId, user, this.databaseManager.getDataSource());
    }

    public Verification getVerificationRecord(String guildId, int id) {
        return this.furRaidVerificationManager.getVerificationRecord(guildId, id, this.databaseManager.getDataSource());
    }

    public List<Verification> getVerificationRecords(String guildId) {
        return this.furRaidVerificationManager.getVerificationRecords(guildId, this.databaseManager.getDataSource());
    }

    public List<Verification> getAllVerificationRecords(String guildId) {
        return this.furRaidVerificationManager.getAllVerificationRecords(guildId, this.databaseManager.getDataSource());
    }

    public boolean hasFVerification(@NotNull UserSnowflake user) {
        return this.furRaidVerificationManager.hasVerification(user, this.databaseManager.getDataSource());
    }

    public void updateVerificationRecord(@NotNull Verification verification) {
        this.furRaidVerificationManager.updateVerificationRecord(verification, this.databaseManager.getDataSource());
    }

    // FurRaid Ticket

    public void createFTicket(@NotNull eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder ticket) {
        this.furRaidTicketManager.createTicket(ticket, this.databaseManager.getDataSource());
    }

    public eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder fetchFTicket(String channelId, String guildId) {
        return this.furRaidTicketManager.fetchTicket(channelId, guildId, this.databaseManager.getDataSource());
    }

    public eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder fetchFTicketById(String ticketId) {
        return this.furRaidTicketManager.fetchTicketById(ticketId, this.databaseManager.getDataSource());
    }

    public List<eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder> fetchAllTickets(Guild guild) {
        return this.furRaidTicketManager.fetchAllTickets(guild.getId(), this.databaseManager.getDataSource());
    }

    public eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder fetchFTicketByUser(UserSnowflake user, String guildId) {
        return this.furRaidTicketManager.fetchTicketByUser(user, guildId, this.databaseManager.getDataSource());
    }

    public void updateFTicket(@NotNull eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder ticket) {
        this.furRaidTicketManager.updateTicket(ticket, this.databaseManager.getDataSource());
    }

    public boolean hasFTicket(@NotNull UserSnowflake user, String guildId) {
        return this.furRaidTicketManager.hasTicket(user, guildId, this.databaseManager.getDataSource());
    }

    public void addFTicketMessage(String ticketId, @NotNull net.dv8tion.jda.api.entities.Message message) {
        this.furRaidTicketManager.addTicketMessage(ticketId, message, this.databaseManager.getDataSource());
    }

    public List<eu.fluffici.bot.api.beans.furraid.ticket.TicketMessageBuilder> fetchFTicketMessages(String ticketId) {
        return this.furRaidTicketManager.fetchMessages(ticketId, this.databaseManager.getDataSource());
    }

    public eu.fluffici.bot.api.beans.furraid.ticket.TicketMessageBuilder fetchFTicketMessageById(String ticketId, String messageId) {
        return this.furRaidTicketManager.fetchMessageById(ticketId, messageId, this.databaseManager.getDataSource());
    }

    // FurSona manager

    public void createCharacter(@NotNull FurSonaBuilder builder) {
        this.furSonaDataManager.createCharacter(builder, this.databaseManager.getDataSource());
    }

    public void updateCharacter(@NotNull FurSonaBuilder builder) {
        this.furSonaDataManager.updateCharacter(builder, this.databaseManager.getDataSource());
    }

    public boolean hasCharacter(@NotNull UserSnowflake user) {
        return this.furSonaDataManager.hasCharacter(user, this.databaseManager.getDataSource());
    }

    public void deleteCharacter(@NotNull FurSonaBuilder builder) {
        this.furSonaDataManager.deleteCharacter(builder, this.databaseManager.getDataSource());
    }

    public FurSonaBuilder getCharacterByOwner(@NotNull UserSnowflake owner) throws SQLException {
        return this.furSonaDataManager.getCharacterByOwner(owner, this.databaseManager.getDataSource());
    }

    public boolean hasMaintenance(String id) {
        return this.applicationManager.hasMaintenanceRecord(Integer.parseInt(id), this.databaseManager.getDataSource());
    }

    public void addMaintenance(String id) {
        this.applicationManager.addMaintenanceRecord(Integer.parseInt(id), this.databaseManager.getDataSource());
    }

    // Staff paycheck

    public boolean hasStaffPaycheck(UserSnowflake user) {
        return this.playerManager.hasStaffPaycheck(user, this.databaseManager.getDataSource());
    }

    public void updateStaffPaycheck(UserSnowflake user, int points) {
        this.playerManager.updateStaffPaycheck(user, points, this.databaseManager.getDataSource());
    }

    public void staffPaycheckWithdraw(UserSnowflake user) {
        this.playerManager.staffPaycheckWithdraw(user, this.databaseManager.getDataSource());
    }

    public List<StaffPaycheck> getAllStaff() {
        return this.playerManager.getAllStaff(this.databaseManager.getDataSource());
    }
}
