package eu.fluffici.furraid.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildAddListener.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

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

import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.Blacklist;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.LocalBlacklist;
import eu.fluffici.bot.api.hooks.furraid.WhitelistBuilder;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.server.users.FGetUserRoute;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.*;

/**
 * The GuildUserJoinListener class handles the events triggered when a member joins a guild.
 */
public class GuildUserJoinListener extends ListenerAdapter {
    public static final int SPAMMER_MASK = 1 << 20;
    private final FurRaidDB instance;

    public GuildUserJoinListener(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * Handles the event when a member joins a guild.
     *
     * @param event The GuildMemberJoinEvent triggered when a member joins a guild.
     */
    @Override
    @SuppressWarnings("All")
    public void onGuildMemberJoin(@NonNull GuildMemberJoinEvent event) {
        GuildSettings guildSettings = this.instance
                .getBlacklistManager()
                .fetchGuildSettings(event.getGuild());

        // Loading the guild language
        this.instance.getLanguageManager()
                .loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        boolean isGloballyBlacklisted = this.instance.getBlacklistManager().isGloballyBlacklisted(event.getUser());
        boolean isLocallyBlacklisted = this.instance.getBlacklistManager().isLocallyBlacklisted(event.getGuild(), event.getUser());
        boolean isWhitelisted = this.instance.getBlacklistManager().isWhitelisted(WhitelistBuilder
                .builder()
                        .guild(event.getGuild())
                        .user(event.getUser())
                .build()
        );
        boolean isSpammer = FGetUserRoute.isSpammer(event.getUser());

        if (guildSettings != null && guildSettings.getLoggingChannel() != null) {
            TextChannel loggingChannel = event.getGuild().getTextChannelById(guildSettings.getLoggingChannel());

            if (guildSettings.getConfig().getSettings().isUsingJoinLeaveInformation()) {
                String yes = this.instance.getLanguageManager().get("common.yes");
                String no = this.instance.getLanguageManager().get("common.no");

                EmbedBuilder leftMessage = this.instance.getEmbed().simpleAuthoredEmbed();
                leftMessage.setAuthor(event.getUser().getEffectiveName());
                leftMessage.setThumbnail((event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getDefaultAvatarUrl()));
                leftMessage.setTitle(this.instance.getLanguageManager().get("common.user.join.title", event.getUser().getGlobalName()));
                leftMessage.setDescription(this.instance.getLanguageManager().get("common.user.join.description"));
                leftMessage.setColor(Color.GREEN);

                leftMessage.addField(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId(), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.account_age"), event.getUser().getTimeCreated().format(this.instance.getDateTimeFormatter()), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.globally_blacklisted"), (isGloballyBlacklisted ? yes : no), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.locally_blacklisted"), (isLocallyBlacklisted ? yes : no), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.is_flagged_spam"), (isSpammer ? yes : no), false);

                loggingChannel.sendMessageEmbeds(leftMessage.build()).queue();
            }

            if (isGloballyBlacklisted && !isWhitelisted) {
                if (guildSettings.getConfig().getSettings().isUsingGlobalBlacklist()) {
                    this.handleGloballyBlacklistedUser(event, loggingChannel);
                }
            } else if (isLocallyBlacklisted && !isWhitelisted) {
                if (guildSettings.getConfig().getSettings().isUsingLocalBlacklist()) {
                    this.handleLocallyBlacklistedUser(event, loggingChannel);
                }
            }
        }



        this.handleNewGuildMember(event, guildSettings);
    }

    /**
     * This method handles a new member joining the guild and applies specific features such as AntiRaid, Welcoming and Verification.
     * <p>
     * 1. Starts by retrieving the AntiRaid Feature configuration and checks if it's enabled. If it's enabled and the bot can talk in the AntiRaid channel,
     *    calls handleAntiRaid() to process AntiRaid actions.
     * <p>
     * 2. Next, it retrieves the Welcoming Feature configuration and checks if it's enabled. If it's enabled and the bot can talk in the Welcoming channel,
     *    calls handleWelcome() to process Welcoming actions.
     * <p>
     * 3. Lastly, it retrieves the Verification Feature configuration and checks if it's enabled. If it's enabled and the bot can interact with the configured 'unverified' role,
     *    it assigns this role to the new member. If the bot cannot interact with the 'unverified' role because of hierarchy, it sends a message to the logging channel.
     *
     * Below is a high-level ASCII diagram showing the method flow:
     *
     *<pre>
     * Start
     *  |
     *  V
     * AntiRaid Feature Enabled?
     *  |                 |
     * No                Yes
     *  |                 |
     *  V                 V
     * Proceed       handleAntiRaid
     *  |                 |
     *  V                 V
     * Welcoming Feature Enabled?
     *  |                 |
     * No                Yes
     *  |                 |
     *  V                 V
     * Proceed       handleWelcome
     *  |                 |
     *  V                 V
     * Verification Feature Enabled and Bot can Interact with 'Unverified' Role?
     *  |                 |
     * No                Yes
     *  |                 |
     *  V                 V
     * End           Assign 'Unverified' Role
     *                               |
     *                               V
     *                               End
     *</pre>
     *
     * This ASCII diagram provides a simple visual representation of the method flow, but it might not capture all the nuances of the code.
     * It's mainly used to illustrate the overall flow and decision points in the method.
     *
     * @param event         The GuildMemberJoinEvent, i.e., the event that signifies a new member joining the guild.
     * @param guildSettings The settings of the guild that contains configurations for different features.
     */
    @SneakyThrows
    @SuppressWarnings("All")
    private void handleNewGuildMember(GuildMemberJoinEvent event, @NotNull GuildSettings guildSettings) {
        FurRaidConfig.WelcomingFeature welcomingFeature = guildSettings.getConfig().getFeatures().getWelcoming();
        if (welcomingFeature.isEnabled()) {
            TextChannel welcomeChannel = event.getGuild().getTextChannelById(welcomingFeature.getSettings().getWelcomeChannel());
            if (welcomeChannel != null && welcomeChannel.canTalk(event.getGuild().getSelfMember())) {
                this.handleWelcome(event, welcomeChannel, guildSettings);
            }
        }

        FurRaidConfig.VerificationFeature verificationFeature = guildSettings.getConfig().getFeatures().getVerification();
        if (verificationFeature.isEnabled()) {
            Role unverifiedRole = event.getGuild().getRoleById(verificationFeature.getSettings().getUnverifiedRole());
            if (unverifiedRole != null && unverifiedRole.canInteract(event.getGuild().getRoleByBot(event.getJDA().getSelfUser()))) {
                if (unverifiedRole != null && unverifiedRole.canInteract(event.getGuild().getRoleByBot(event.getJDA().getSelfUser()))) {
                    Member member = event.getMember();
                    if (member != null) {
                        event.getGuild().addRoleToMember(member, unverifiedRole).queue();
                    }
                } else {
                    TextChannel loggingChannel = event.getGuild().getTextChannelById(guildSettings.getLoggingChannel());
                    if (loggingChannel != null && loggingChannel.canTalk(event.getGuild().getSelfMember())) {
                        loggingChannel.sendMessage("I'm unable to assign the role %s to %s because the role is above my hierarchy.".formatted(event.getMember().getAsMention(), unverifiedRole.getAsMention())).queue();
                    }
                }
            }
        }
    }

    /**
     * Handles the welcome event when a member joins a guild.
     *
     * @param event           The GuildMemberJoinEvent triggered when a member joins a guild.
     * @param welcomeChannel  The TextChannel where the welcome message will be sent.
     */
    private void handleWelcome(@NonNull GuildMemberJoinEvent event, @NotNull TextChannel welcomeChannel, @NotNull GuildSettings guildSettings) {
        FurRaidConfig.WelcomingFeature welcomingFeature = guildSettings.getConfig().getFeatures().getWelcoming();

        welcomeChannel.sendMessageEmbeds(this.instance.getEmbed().simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("event.join.welcome.title", event.getMember().getEffectiveName()), "https://frdb.fluffici.eu", ICON_USER_PLUS.getUrl())
                .setColor(Color.GREEN)
                .setDescription(welcomingFeature.getSettings().getJoinMessage())
                .setFooter(event.getMember().getId())
                .setThumbnail(event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build()
        ).queue();
    }

    /**
     * Handles globally blacklisted user by sending an embed message and banning the user.
     *
     * @param event The GuildMemberJoinEvent triggered when a member joins a guild.
     */
    @Contract(pure = true)
    @SuppressWarnings("All")
    private void handleGloballyBlacklistedUser(@NotNull GuildMemberJoinEvent event, @NotNull TextChannel loggingChannel) {
        Blacklist blacklist = this.instance.getBlacklistManager().fetchGlobalBlacklist(event.getUser());

        EmbedBuilder blacklistedMessage = this.instance.getEmbed().simpleAuthoredEmbed();
        blacklistedMessage.setColor(Color.decode("#9412d5"));
        blacklistedMessage.setAuthor(this.instance.getLanguageManager().get("common.globally_blacklisted.title"), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED.getUrl());
        blacklistedMessage.setDescription(this.instance.getLanguageManager().get("common.globally_blacklisted.description"));

        if (blacklist.getAttachmentUrl() != null)
            blacklistedMessage.setImage(blacklist.getAttachmentUrl());

        blacklistedMessage.setThumbnail(event.getUser().getAvatarUrl());
        blacklistedMessage.setTimestamp(Instant.now());

        User author = event.getJDA().getUserById(blacklist.getAuthor().getId());

        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId(), false);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getEffectiveName(), true);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_by"), (author != null ? author.getEffectiveName() : blacklist.getAuthor().getId()), true);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.reason"), blacklist.getReason(), false);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_at"), blacklist.getCreatedAt().toLocalDateTime().format(this.instance.getDateTimeFormatter()), true);

        loggingChannel.sendMessageEmbeds(blacklistedMessage.build()).queue();

        event.getMember().ban(7, TimeUnit.DAYS).reason(blacklist.getReason()).queue();
    }

    /**
     * Handles locally blacklisted user by performing necessary actions based on their status.
     *
     * @param event The GuildMemberJoinEvent triggered when a member joins a guild.
     */
    @Contract(pure = true)
    @SuppressWarnings("All")
    private void handleLocallyBlacklistedUser(@NotNull GuildMemberJoinEvent event, TextChannel loggingChannel) {
        LocalBlacklist localBlacklist = this.instance.getBlacklistManager().fetchLocalBlacklist(event.getGuild(), event.getUser());

        EmbedBuilder blacklistedMessage = this.instance.getEmbed().simpleAuthoredEmbed();
        blacklistedMessage.setColor(Color.decode("#9412d5"));
        blacklistedMessage.setAuthor(this.instance.getLanguageManager().get("common.locally_blacklisted.title"), "https://frdb.fluffici.eu", ICON_CIRCLE_SLASHED.getUrl());
        blacklistedMessage.setDescription(this.instance.getLanguageManager().get("common.locally_blacklisted.description"));
        blacklistedMessage.setThumbnail(event.getUser().getAvatarUrl());
        blacklistedMessage.setTimestamp(Instant.now());

        User author = event.getJDA().getUserById(localBlacklist.getAuthor().getId());

        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId(), false);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getEffectiveName(), true);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_by"), (author != null ? author.getEffectiveName() : localBlacklist.getAuthor().getId()), true);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.reason"), localBlacklist.getReason(), false);
        blacklistedMessage.addField(this.instance.getLanguageManager().get("common.issued_at"), localBlacklist.getCreatedAt().toLocalDateTime().format(this.instance.getDateTimeFormatter()), true);

        loggingChannel.sendMessageEmbeds(blacklistedMessage.build()).queue();

        event.getMember().ban(7, TimeUnit.DAYS).reason(localBlacklist.getReason()).queue();
    }
}
