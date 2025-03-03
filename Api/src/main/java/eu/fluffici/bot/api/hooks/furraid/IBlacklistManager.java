/*
---------------------------------------------------------------------------------
File Name : IBlacklistManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks.furraid;

import eu.fluffici.bot.api.beans.furraid.Blacklist;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.LocalBlacklist;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

public interface IBlacklistManager {

    /**
     * Adds a global blacklist to the blacklist manager.
     *
     * @param blacklist The blacklist to be added. Must be an instance of BlacklistBuilder.
     */
    void addGlobalBlacklist(BlacklistBuilder blacklist);

    /**
     * Adds a local blacklist to the blacklist manager.
     *
     * @param blacklist The local blacklist to be added. Must be an instance of BlacklistBuilder.
     */
    void addLocalBlacklist(BlacklistBuilder blacklist);

    /**
     * Removes a global blacklist from the blacklist manager for the specified user.
     *
     * @param user The user for whom the global blacklist should be removed.
     */
    void removeGlobalBlacklist(UserSnowflake user);

    /**
     * Removes a local blacklist from the blacklist manager for the specified user.
     *
     * @param user The user for whom the local blacklist should be removed. Must be a valid UserSnowflake object.
     */
    void removeLocalBlacklist(Guild guild, UserSnowflake user);

    /**
     * Checks if a user is globally blacklisted.
     *
     * @param user The user to be checked. Must be a valid UserSnowflake object.
     * @return True if the user is globally blacklisted, false otherwise.
     */
    boolean isGloballyBlacklisted(UserSnowflake user);

    /**
     * Checks if a user is locally blacklisted.
     *
     * @param user The user to be checked. Must be a valid UserSnowflake object.
     * @return True if the user is locally blacklisted, false otherwise.
     */
    boolean isLocallyBlacklisted(Guild guild, UserSnowflake user);

    /**
     * Fetches the global blacklist for the specified user.
     *
     * @param user The user for whom the global blacklist should be fetched. Must be a valid UserSnowflake object.
     * @return The BlacklistBuilder object representing the global blacklist for the specified user.
     */
    Blacklist fetchGlobalBlacklist(UserSnowflake user);

    /**
     * Fetches the local blacklist for the specified user.
     *
     * @param user The user for whom the local blacklist should be fetched. Must be a valid UserSnowflake object.
     * @return The BlacklistBuilder object representing the local blacklist for the specified user.
     */
    LocalBlacklist fetchLocalBlacklist(Guild guild, UserSnowflake user);

    /**
     * Adds a whitelist to the whitelist manager.
     *
     * @param whitelist The whitelist to be added. Must be an instance of WhitelistBuilder.
     */
    void addWhitelist(WhitelistBuilder whitelist);

    /**
     * Checks if a user is whitelisted.
     *
     * @param whitelist The WhitelistBuilder object representing the whitelist to be checked. Must not be null.
     * @return True if the user is whitelisted, false otherwise.
     */

    boolean isWhitelisted(WhitelistBuilder whitelist);

    /**
     * Removes a whitelist from the whitelist manager.
     *
     * @param whitelist The whitelist to be removed. Must be an instance of WhitelistBuilder.
     */
    void removeWhitelist(WhitelistBuilder whitelist);

    /**
     * Returns the number of blacklists in the blacklist manager.
     * A blacklist represents a set of users who are prohibited or restricted from certain actions.
     *
     * @return The number of blacklists in the blacklist manager.
     */
    int blacklistCount();

    /**
     * Creates a guild with the specified settings.
     *
     * @param settings The settings for the guild. Must be an instance of GuildSettings.
     */
    void createGuild(GuildSettings settings);

    /**
     * Deletes a guild from the blacklist and whitelist manager.
     * This method removes all associated blacklists, whitelists, and guild settings for the specified guild.
     *
     * @param guild The guild to be deleted. Must be a valid Guild object.
     */
    void deleteGuild(Guild guild);

    /**
     * Updates the guild settings with the provided settings.
     *
     * @param settings The new settings for the guild. Must be an instance of GuildSettings.
     */
    void updateGuild(GuildSettings settings);

    /**
     * Fetches the guild settings for the specified guild.
     *
     * @param guild The guild for which the settings should be fetched.
     * @return The GuildSettings object representing the settings for the specified guild.
     */
    GuildSettings fetchGuildSettings(Guild guild);

    boolean isStaff(UserSnowflake user);
}
