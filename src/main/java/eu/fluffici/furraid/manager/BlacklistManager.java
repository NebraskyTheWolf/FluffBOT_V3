/*
---------------------------------------------------------------------------------
File Name : BlacklistManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import com.google.gson.Gson;
import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.Blacklist;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.LocalBlacklist;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import eu.fluffici.bot.api.hooks.furraid.IBlacklistManager;
import eu.fluffici.bot.api.hooks.furraid.WhitelistBuilder;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;

/**
 * The BlacklistManager class is responsible for managing the blacklists and whitelists for users and guilds.
 */
public class BlacklistManager implements IBlacklistManager {

    private final FurRaidDB instance;

    public BlacklistManager(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * Adds a global blacklist to the blacklist manager.
     *
     * @param blacklist The blacklist to be added. Must be an instance of BlacklistBuilder.
     */
    @Override
    public void addGlobalBlacklist(BlacklistBuilder blacklist) {
        this.instance.getGameServiceManager()
                .addGlobalBlacklist(blacklist);
    }

    /**
     * Adds a local blacklist to the blacklist manager.
     *
     * @param blacklist The local blacklist to be added. Must be an instance of BlacklistBuilder.
     */
    @Override
    public void addLocalBlacklist(BlacklistBuilder blacklist) {
        this.instance.getGameServiceManager()
                .addLocalBlacklist(blacklist);
        this.handleLogging(blacklist);
    }

    /**
     * Removes a global blacklist from the blacklist manager for the specified user.
     *
     * @param user The user for whom the global blacklist should be removed.
     */
    @Override
    public void removeGlobalBlacklist(UserSnowflake user) {
        this.instance.getGameServiceManager()
                .removeGlobalBlacklist(user);
    }

    /**
     * Removes a local blacklist from the blacklist manager for the specified user.
     *
     * @param user The user for whom the local blacklist should be removed. Must be a valid UserSnowflake object.
     */
    @Override
    public void removeLocalBlacklist(Guild guild, UserSnowflake user) {
        this.instance.getGameServiceManager()
                .removeLocalBlacklist(guild, user);
    }

    /**
     * Checks if a user is globally blacklisted.
     *
     * @param user The user to be checked. Must be a valid UserSnowflake object.
     * @return True if the user is globally blacklisted, false otherwise.
     */
    @Override
    public boolean isGloballyBlacklisted(UserSnowflake user) {
        return this.instance.getGameServiceManager()
                .isGloballyBlacklisted(user);
    }

    /**
     * Checks if a user is locally blacklisted.
     *
     * @param user The user to be checked. Must be a valid UserSnowflake object.
     * @return True if the user is locally blacklisted, false otherwise.
     */
    @Override
    public boolean isLocallyBlacklisted(Guild guild, UserSnowflake user) {
        return this.instance.getGameServiceManager()
                .isLocallyBlacklisted(guild, user);
    }

    /**
     * Fetches the global blacklist for the specified user.
     *
     * @param user The user for whom the global blacklist should be fetched. Must be a valid UserSnowflake object.
     * @return The BlacklistBuilder object representing the global blacklist for the specified user.
     */
    @Override
    public Blacklist fetchGlobalBlacklist(UserSnowflake user) {
        return this.instance.getGameServiceManager()
                .fetchGlobalBlacklist(user);
    }

    /**
     * Fetches the local blacklist for the specified user.
     *
     * @param user The user for whom the local blacklist should be fetched. Must be a valid UserSnowflake object.
     * @return The BlacklistBuilder object representing the local blacklist for the specified user.
     */
    @Override
    public LocalBlacklist fetchLocalBlacklist(Guild guild, UserSnowflake user) {
        return this.instance.getGameServiceManager()
                .fetchLocalBlacklist(guild, user);
    }

    /**
     * Adds a whitelist to the whitelist manager.
     *
     * @param whitelist The whitelist to be added. Must be an instance of WhitelistBuilder.
     */
    @Override
    public void addWhitelist(WhitelistBuilder whitelist) {
        this.instance.getGameServiceManager()
                .addWhitelist(whitelist.getGuild(), whitelist.getUser());
    }

    /**
     * Checks if a user is whitelisted.
     *
     * @param whitelist The WhitelistBuilder object representing the whitelist to be checked. Must not be null.
     * @return True if the user is whitelisted, false otherwise.
     */
    @Override
    public boolean isWhitelisted(WhitelistBuilder whitelist) {
        return this.instance.getGameServiceManager()
                    .isWhitelisted(whitelist.getGuild(), whitelist.getUser());
    }

    /**
     * Removes a whitelist from the whitelist manager.
     *
     * @param whitelist The whitelist to be removed. Must be an instance of WhitelistBuilder.
     */
    @Override
    public void removeWhitelist(WhitelistBuilder whitelist) {
        this.instance.getGameServiceManager()
                .removeWhitelist(whitelist.getGuild(), whitelist.getUser());
    }

    /**
     * Returns the number of blacklists in the blacklist manager.
     * A blacklist represents a set of users who are prohibited or restricted from certain actions.
     *
     * @return The number of blacklists in the blacklist manager.
     */
    @Override
    public int blacklistCount() {
        return this.instance.getGameServiceManager().globalBlacklistCount();
    }

    /**
     * Creates a guild with the specified settings.
     *
     * @param settings The settings for the guild. Must be an instance of GuildSettings.
     */
    @Override
    public void createGuild(GuildSettings settings) {
        try {
            this.instance.getGameServiceManager().createGuild(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a guild from the blacklist and whitelist manager.
     * This method removes all associated blacklists, whitelists, and guild settings for the specified guild.
     *
     * @param guild The guild to be deleted. Must be a valid Guild object.
     */
    @Override
    public void deleteGuild(Guild guild) {
        this.instance.getGameServiceManager().deleteGuildSettings(guild);
    }

    /**
     * Updates the guild settings with the provided settings.
     *
     * @param settings The new settings for the guild. Must be an instance of GuildSettings.
     */
    @Override
    public void updateGuild(GuildSettings settings) {
        this.instance.getGameServiceManager().updateGuildSettings(settings);
    }

    /**
     * Fetches the guild settings for the specified guild.
     *
     * @param guild The guild for which the settings should be fetched.
     * @return The GuildSettings object representing the settings for the specified guild.
     */
    @Override
    public GuildSettings fetchGuildSettings(Guild guild) {
        return this.instance.getGameServiceManager().fetchGuildSettings(guild);
    }

    /**
     * Checks if a user is a staff member.
     *
     * @param user The user to be checked. Must be a valid UserSnowflake object.
     * @return True if the user is a staff member, false otherwise.
     */
    @Override
    public boolean isStaff(UserSnowflake user) {
        return this.instance.getGameServiceManager().isStaff(user);
    }

    /**
     * Handles logging of a blacklisted user.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param blacklist   The BlacklistBuilder object containing information about the blacklisted user.
     */
    @SuppressWarnings("All")
    private void handleLogging(@NotNull BlacklistBuilder blacklist) {
        GuildSettings guildSettings = FurRaidDB.getInstance()
                .getBlacklistManager()
                .fetchGuildSettings(blacklist.getGuild());

        this.instance.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        User user = this.instance.getJda().getUserById(blacklist.getUser().getId());

        String loggingChannelId = guildSettings.getLoggingChannel();

        if (guildSettings != null && loggingChannelId != null) {
            TextChannel loggingChannel = blacklist.getGuild().getTextChannelById(loggingChannelId);

            if (loggingChannel != null && loggingChannel.canTalk(blacklist.getGuild().getSelfMember())) {
                EmbedBuilder blacklistedMessage = this.instance.getEmbed().simpleAuthoredEmbed();
                blacklistedMessage.setColor(Color.decode("#9412d5"));
                blacklistedMessage.setAuthor(this.instance.getLanguageManager().get("common.locally_blacklisted.title.added", user.getGlobalName()), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED);

                blacklistedMessage.setThumbnail(user.getAvatarUrl());
                blacklistedMessage.setTimestamp(Instant.now());

                blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.id"), blacklist.getUser().getId(), false);
                blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.name"), user.getEffectiveName(), true);
                blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_by"), blacklist.getAuthor().getAsMention(), true);
                blacklistedMessage.addField(this.instance.getLanguageManager().get("common.reason"), blacklist.getReason(), false);
                blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_at"), new Timestamp(System.currentTimeMillis()).toLocalDateTime().format(FurRaidDB.getInstance().getDateTimeFormatter()), true);

                loggingChannel.sendMessageEmbeds(blacklistedMessage.build()).queue();
            }

            Member member = blacklist.getGuild().getMember(user);

            if (member.canInteract(blacklist.getGuild().getSelfMember())) {
                blacklist.getGuild().getMember(user).ban(7, TimeUnit.DAYS).reason(blacklist.getReason()).queue();
            }
        }
    }

    public DiscordUser getCachedUser(@NotNull Guild guild, UserSnowflake user) {
        String memberKey = "guild:" + guild.getId() + "/members";
        try (Jedis redis = this.instance.getRedisResource()) {
            if (redis.hexists(memberKey, user.getId())) {
                return new Gson().fromJson(redis.hget(memberKey, user.getId()), DiscordUser.class);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean checkIsSpammer(@NotNull Guild guild, UserSnowflake user) {
        DiscordUser cachedUser = getCachedUser(guild, user);
        if (cachedUser == null)
            return false;

        int SPAMMER_MASK = 1 << 20;
        return ((cachedUser.getFlags() & SPAMMER_MASK) != 0);
    }
}