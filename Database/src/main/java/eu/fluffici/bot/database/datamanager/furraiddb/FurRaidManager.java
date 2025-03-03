/*
---------------------------------------------------------------------------------
File Name : FurRaidManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.database.datamanager.furraiddb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import eu.fluffici.bot.api.beans.furraid.*;
import eu.fluffici.bot.api.furraid.permissive.Permissions;
import eu.fluffici.bot.api.furraid.permissive.UserEntity;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

public class FurRaidManager {

    public void addVote(@NotNull UserSnowflake user, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `furraid_votes` (user_id) VALUES(?)")) {
                statement.setString(1, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Permissions getApiUserFromToken(String token, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `furraid_users` WHERE `token` = ?")) {
                statement.setString(1, token);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return new Permissions(new UserEntity(token, resultSet.getInt("permissions")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addGlobalBlacklist(@NotNull BlacklistBuilder blacklist, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO furraid_blacklist (user_id, author_id, reason, attachment_url, created_at) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, blacklist.getUser().getId());
            statement.setString(2, blacklist.getAuthor().getId());
            statement.setString(3, blacklist.getReason());
            statement.setString(4, blacklist.getAttachmentUrl());
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeGlobalBlacklist(@NotNull UserSnowflake user, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM furraid_blacklist WHERE user_id = ?")) {
            statement.setString(1, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isGloballyBlacklisted(@NotNull UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_blacklist WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Blacklist fetchGlobalBlacklist(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_blacklist WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String userId = resultSet.getString("user_id");
                        String authorId = resultSet.getString("author_id");
                        String reason = resultSet.getString("reason");
                        String attachmentUrl = resultSet.getString("attachment_url");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");
                        return new Blacklist(UserSnowflake.fromId(userId), UserSnowflake.fromId(authorId), reason, attachmentUrl, createdAt);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Local blacklist

    public void addLocalBlacklist(BlacklistBuilder blacklist, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO furraid_local_blacklist (guild_id, user_id, author_id, reason, created_at) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, blacklist.getGuild().getId());
            statement.setString(2, blacklist.getUser().getId());
            statement.setString(3, blacklist.getAuthor().getId());
            statement.setString(4, blacklist.getReason());
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeLocalBlacklist(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM furraid_local_blacklist WHERE guild_id = ? AND user_id = ?")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLocallyBlacklisted(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_local_blacklist WHERE guild_id = ? AND user_id = ?")) {
                statement.setString(1, guild.getId());
                statement.setString(2, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public LocalBlacklist fetchLocalBlacklist(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_local_blacklist WHERE user_id = ? AND guild_id = ?")) {
                statement.setString(1, user.getId());
                statement.setString(2, guild.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String guildId = resultSet.getString("guild_id");
                        String userId = resultSet.getString("user_id");
                        String authorId = resultSet.getString("author_id");
                        String reason = resultSet.getString("reason");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");
                        return new LocalBlacklist(guildId, UserSnowflake.fromId(userId), UserSnowflake.fromId(authorId), reason, createdAt);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<LocalBlacklist> fetchLocalBlacklists(Guild guild, DataSource dataSource) {
        List<LocalBlacklist> localBlacklists = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_local_blacklist WHERE guild_id = ?")) {
                statement.setString(1, guild.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String guildId = resultSet.getString("guild_id");
                        String userId = resultSet.getString("user_id");
                        String authorId = resultSet.getString("author_id");
                        String reason = resultSet.getString("reason");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");

                        localBlacklists.add(new LocalBlacklist(guildId, UserSnowflake.fromId(userId), UserSnowflake.fromId(authorId), reason, createdAt));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return localBlacklists;
    }

    // Whitelist

    public void addWhitelist(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO furraid_whitelist (guild_id, user_id) VALUES (?, ?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Whitelist> fetchWhitelist(Guild guild, DataSource dataSource) {
        List<Whitelist> whitelists = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_whitelist WHERE guild_id = ?")) {
                statement.setString(1, guild.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String guildId = resultSet.getString("guild_id");
                        String userId = resultSet.getString("user_id");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");

                        whitelists.add(new Whitelist(guildId, UserSnowflake.fromId(userId), createdAt));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return whitelists;
    }

    public void removeWhitelist(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM furraid_whitelist WHERE guild_id = ? AND user_id = ?")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isWhitelisted(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_whitelist WHERE guild_id = ? AND user_id = ?")) {
                statement.setString(1, guild.getId());
                statement.setString(2, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int globalBlacklistCount(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM furraid_blacklist")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> user = new ArrayList<>();
                while (resultSet.next()) {
                    user.add(resultSet.getString("user_id"));
                }

                return user.size();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Guild settings

    public void createGuild(@NotNull GuildSettings guild, @NotNull DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO furraid_guild_settings (guild_id, settings, logging_channel, created_at, updated_at) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, guild.getGuildId());
            statement.setString(2, buildDefaultConfig().toString());
            statement.setString(3, guild.getLoggingChannel());
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GuildSettings fetchGuildSettings(@NotNull Guild guild, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_guild_settings WHERE guild_id = ?")) {
                statement.setString(1, guild.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String guildId = resultSet.getString("guild_id");

                        FurRaidConfig config = new Gson().fromJson(resultSet.getString("settings"), FurRaidConfig.class);

                        String loggingChannel = resultSet.getString("logging_channel");
                        boolean isBlacklisted = resultSet.getBoolean("is_blacklisted");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");
                        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

                        return new GuildSettings(guildId, config, loggingChannel, isBlacklisted, createdAt, updatedAt);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteGuildSettings(Guild guild, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM furraid_guild_settings WHERE guild_id = ?")) {
            statement.setString(1, guild.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGuildSettings(GuildSettings settings, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE furraid_guild_settings SET settings = ?, logging_channel = ?, updated_at = ? WHERE guild_id = ?")) {
            statement.setString(1, new Gson().toJson(settings.getConfig()));
            statement.setString(2, settings.getLoggingChannel());
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.setString(4, settings.getGuildId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Staff

    public boolean isStaff(UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_staff WHERE user_id = ?")) {
                statement.setString(1, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Offers

    public GuildPremiumOffer getGuildPremium(long guildId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_premium_guilds WHERE guild_id = ?")) {
                statement.setLong(1, guildId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        UserSnowflake customer = UserSnowflake.fromId(resultSet.getString("customer_id"));
                        int offerId = resultSet.getInt("offer_id");
                        Timestamp createdAt = resultSet.getTimestamp("created_at");
                        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                        Timestamp expirationAt = resultSet.getTimestamp("expiration_at");
                        boolean isActive = resultSet.getBoolean("is_active");

                        return new GuildPremiumOffer(customer, offerId, createdAt, updatedAt, expirationAt, isActive);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new GuildPremiumOffer(null, 1, null, null, null, false);
    }

    // End offers

    public int localBlacklistCount(long guildId, DataSource dataSource) {
        List<String> user = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM furraid_local_blacklist WHERE guild_id = ?")) {
            statement.setString(1, String.valueOf(guildId));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    user.add(resultSet.getString("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user.size();
    }

    public int whitelistCount(long guildId, DataSource dataSource) {
        List<String> user = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM furraid_whitelist WHERE guild_id = ?")) {
            statement.setString(1, String.valueOf(guildId));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    user.add(resultSet.getString("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user.size();
    }

    public String getOfferNameById(long offerId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT offer_name FROM furraid_premium_offers WHERE id = ?")) {
            statement.setLong(1, offerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("offer_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "FurRaid Classic";
    }

    private static final Gson gson = new Gson();

    public FurRaidPremiumOffer getOfferById(long offerId, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_premium_offers WHERE id = ?")) {
            statement.setLong(1, offerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFurRaidPremiumOffer(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FurRaidPremiumOffer> getAllOffers( DataSource dataSource) {
        List<FurRaidPremiumOffer> offers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_premium_offers")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    offers.add(mapResultSetToFurRaidPremiumOffer(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offers;
    }

    public Changelogs getLatestChangelogs( DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT title, description, bannerURL, version, build, features, bugs, note FROM furraid_changelogs ORDER BY createdAt DESC LIMIT 1")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                   return new Changelogs(
                           resultSet.getString("title"),
                           resultSet.getString("description"),
                           resultSet.getString("bannerURL"),
                           resultSet.getString("version"),
                           resultSet.getString("build"),
                           new Gson().fromJson(resultSet.getString("features"), JsonArray.class),
                           new Gson().fromJson(resultSet.getString("bugs"), JsonArray.class),
                           resultSet.getString("note")
                   );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private FurRaidPremiumOffer mapResultSetToFurRaidPremiumOffer(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String offerSlug = resultSet.getString("offer_slug");
        String offerName = resultSet.getString("offer_name");
        String offerDescription = resultSet.getString("offer_description");
        long offerPrice = resultSet.getLong("offer_price");
        double offerDiscount = resultSet.getDouble("offer_discount");
        double offerVat = resultSet.getDouble("offer_vat");
        String featuresJson = resultSet.getString("offer_features");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        List<FurRaidPremiumOffer.OfferFeatures> offerFeatures = parseFeatures(featuresJson);

        return new FurRaidPremiumOffer(id, offerSlug, offerName, offerDescription, offerPrice,
                offerDiscount, offerVat, offerFeatures, createdAt, updatedAt);
    }

    private List<FurRaidPremiumOffer.OfferFeatures> parseFeatures(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<ArrayList<FurRaidPremiumOffer.OfferFeatures>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static JsonObject buildDefaultConfig() {
        FurRaidConfig config = new FurRaidConfig();
        FurRaidConfig.Settings settings = new FurRaidConfig.Settings();
        settings.setLanguage("en");
        settings.setDisabledCommands(listOf());
        settings.setExemptedChannels(listOf());
        settings.setExemptedRoles(listOf());
        settings.setStaffRoles(listOf());
        settings.setUsingGlobalBlacklist(true);
        settings.setUsingLocalBlacklist(true);
        settings.setUsingJoinLeaveInformation(true);
        settings.setWhitelistOverride(true);

        config.setSettings(settings);

        FurRaidConfig.Features features = new FurRaidConfig.Features();

        FurRaidConfig.WelcomingFeature welcomingFeature = new FurRaidConfig.WelcomingFeature();
        welcomingFeature.setSettings(new FurRaidConfig.WelcomingSettings(
                "",
                "",
                "",
                ""
        ));

        FurRaidConfig.AntiScamFeature antiScamFeature = new FurRaidConfig.AntiScamFeature();
        antiScamFeature.setSettings(new FurRaidConfig.AntiScamSettings(
                "",
                ""
        ));

        FurRaidConfig.TicketFeature ticketFeature = new FurRaidConfig.TicketFeature();
        ticketFeature.setSettings(new FurRaidConfig.TicketSettings(
                "",
                "",
                "",
                "",
                "",
                true,
                true
        ));

        FurRaidConfig.AntiRaidFeature antiRaidFeature = new FurRaidConfig.AntiRaidFeature();
        antiRaidFeature.setSettings(new FurRaidConfig.AntiRaidSettings(
                "",
                "",
                0,
                0
        ));

        FurRaidConfig.BlacklistFeature blacklistFeature = new FurRaidConfig.BlacklistFeature();
        blacklistFeature.setSettings(new FurRaidConfig.BlacklistSettings(""));

        FurRaidConfig.AutoModerationFeature autoModerationFeature = new FurRaidConfig.AutoModerationFeature();
        autoModerationFeature.setSettings(new FurRaidConfig.AutoModerationSettings(
                "",
                listOf()
        ));

        FurRaidConfig.VerificationFeature verificationFeature = new FurRaidConfig.VerificationFeature();
        verificationFeature.setSettings(new FurRaidConfig.VerificationSettings(
                "",
                "",
                "",
                "",
                "",
                listOf()
        ));

        FurRaidConfig.InviteTrackerFeature inviteTrackerFeature = new FurRaidConfig.InviteTrackerFeature();
        inviteTrackerFeature.setSettings(new FurRaidConfig.InviteTrackerSettings(
                ""
        ));

        features.setWelcoming(welcomingFeature);
        features.setAntiScamFeature(antiScamFeature);
        features.setTicket(ticketFeature);
        features.setAntiRaid(antiRaidFeature);
        features.setGlobalBlacklist(blacklistFeature);
        features.setAutoModeration(autoModerationFeature);
        features.setVerification(verificationFeature);
        features.setInviteTracker(inviteTrackerFeature);
        features.setLocalBlacklist(blacklistFeature);

        config.setFeatures(features);

        return gson.fromJson(gson.toJson(config), JsonObject.class);
    }
    
    public void quarantineUser(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO furraid_quarantined_users (guild_id, user_id) VALUES (?, ?)")) {
                statement.setString(1, guild.getId());
                statement.setString(2, user.getId());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isFQuarantined(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM furraid_quarantined_users WHERE guild_id = ? AND user_id = ?")) {
                statement.setString(1, guild.getId());
                statement.setString(2, user.getId());
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void unquarantineUser(Guild guild, UserSnowflake user, DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM furraid_quarantined_users WHERE guild_id = ? AND user_id = ?")) {
            statement.setString(1, String.valueOf(guild.getIdLong()));
            statement.setString(2, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}