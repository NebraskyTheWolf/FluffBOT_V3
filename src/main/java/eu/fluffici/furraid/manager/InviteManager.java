/*
---------------------------------------------------------------------------------
File Name : InviteManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.furraid.FurRaidDB;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;

/**
 * The InviteManager class is responsible for managing and tracking invites
 * for guild members in a Discord server.
 */
@Getter
@SuppressWarnings("All")
public class InviteManager extends ListenerAdapter {
    private final Map<String, Integer> inviteUses = new HashMap<>();

    /**
     * Initializes the invite tracking by retrieving and storing the uses of all invites in all guilds.
     */
    public void init() {
        FurRaidDB.getInstance().getLogger().info("Initializing InviteTracker...");
        Thread inviteLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicLong guildCount = new AtomicLong(0);
                AtomicLong ignoredGuildCount = new AtomicLong(0);
                AtomicLong invitesCount = new AtomicLong(0);

                for (Guild guild : FurRaidDB.getInstance().getJda().getGuilds()) {
                    if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                        guildCount.incrementAndGet();
                        guild.retrieveInvites().complete().forEach(invite -> {
                            invitesCount.incrementAndGet();
                            getInviteUses().put(invite.getUrl(), invite.getUses());
                        });
                    } else {
                        ignoredGuildCount.incrementAndGet();
                    }
                }

                FurRaidDB.getInstance().getLogger().info(guildCount.get() + " guild(s) found with " + invitesCount.get() + " global-invites and "  + ignoredGuildCount.get() + " ignored-guild.");
                FurRaidDB.getInstance().getLogger().info("InviteTracker is now ready.");
            }
        });
        inviteLoader.setName("InviteLoader-Thread-".concat(UUID.randomUUID().toString()));
        inviteLoader.setDaemon(true);
        inviteLoader.setPriority(Thread.MAX_PRIORITY);
        inviteLoader.start();
    }

    /**
     * Handle tracking of invites for a guild member join event.
     *
     * @param event The join event for a guild member.
     */
    private void handleInviteTracker(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        GuildSettings guildSettings = FurRaidDB.getInstance()
                .getBlacklistManager()
                .fetchGuildSettings(guild);
        FurRaidDB.getInstance()
                .getLanguageManager()
                .loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        FurRaidDB.getInstance().getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        if (guildSettings.isBlacklisted())
            return;

        FurRaidConfig.InviteTrackerFeature inviteTrackerFeature = guildSettings.getConfig()
                .getFeatures()
                .getInviteTracker();

        if (inviteTrackerFeature.isEnabled()) {
            guild.retrieveInvites().queue(currentInvites -> {
                Invite usedInvite = null;
                for (Invite invite : currentInvites) {
                    String url = invite.getUrl();
                    int currentUses = invite.getUses();
                    int cachedUses = this.getInviteUses().getOrDefault(url, 0);

                    if (currentUses > cachedUses) {
                        usedInvite = invite;
                        this.getInviteUses().put(url, currentUses);
                        break;
                    }
                }

                if (usedInvite != null) {
                    guild.getTextChannelById(inviteTrackerFeature.getSettings().getTrackingChannel())
                            .sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                                    .simpleAuthoredEmbed()
                                    .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.invite.used", event.getUser().getGlobalName()), "https://frdb.fluffici.eu", ICON_QUESTION_MARK)
                                    .setThumbnail(event.getUser().getAvatarUrl())
                                    .setDescription(FurRaidDB.getInstance().getLanguageManager().get("common.invite.used.desc",
                                            event.getUser().getAsMention(), event.getUser().getGlobalName(),
                                            usedInvite.getInviter().getAsMention(), usedInvite.getInviter().getGlobalName(),
                                            usedInvite.getCode(), usedInvite.getUses(),
                                            guild.getMemberCount()
                                    ))
                                    .setTimestamp(Instant.now())
                                    .build()
                            ).queue();
                }
            }, error -> FurRaidDB.getInstance().getLogger().warn("Failed to retrieve invites for guild: %s", guild.getName()));
        }
    }

    /**
     * Updates the invite uses in the inviteUses map when a guild invite is created.
     *
     * @param event The event triggered when a guild invite is created.
     */
    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
        super.onGuildInviteCreate(event);

        this.getInviteUses().put(event.getInvite().getUrl(), event.getInvite().getUses());
    }

    /**
     * Handles the event when a guild invite is deleted.
     *
     * @param event The GuildInviteDeleteEvent object representing the deleted invite.
     */
    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
        super.onGuildInviteDelete(event);

        this.getInviteUses().remove(event.getUrl());
    }
}