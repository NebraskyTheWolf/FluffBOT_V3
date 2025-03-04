package eu.fluffici.bot.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildUserJoinListener.java

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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.level.LevelReward;
import eu.fluffici.bot.api.beans.players.PermanentRole;
import eu.fluffici.bot.api.beans.statistics.GuildEngagement;
import eu.fluffici.bot.api.hooks.PlayerBean;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.bot.api.IconRegistry.ICON_USER_PLUS;

@SuppressWarnings("All")
public class GuildUserJoinListener extends ListenerAdapter {

    private final FluffBOT instance;

    public GuildUserJoinListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    @SneakyThrows
    public void onGuildMemberJoin(@NonNull GuildMemberJoinEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;

        this.handleInviteTracker(event);
        this.handlePermanentRoles(event);

        event.getGuild().addRoleToMember(event.getUser(), event.getGuild().getRoleById(this.instance.getDefaultConfig().getProperty("roles.unverified")));

        event.getGuild().getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.welcome")).sendMessageEmbeds(this.instance.getEmbed().simpleAuthoredEmbed()
                .setAuthor(String.format("%s se připojil/a", event.getMember().getUser().getEffectiveName()), "https://fluffici.eu", ICON_USER_PLUS.getUrl())
                .setColor(Color.GREEN)
                .setFooter(event.getMember().getId())
                .setThumbnail(event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build()).queue();

        if (!event.getUser().isBot() || !event.getUser().isSystem()) {
            this.instance.getAchievementManager().unlock(event.getUser(), 33);

            PlayerBean player = this.instance.getUserManager().fetchUser(UserSnowflake.fromId(event.getUser().getId()));
            if (player == null) {
                PlayerBean playerBean = new PlayerBean(
                        event.getUser().getId(),
                        null,
                        null,
                        false,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        null,
                        100,
                        100,
                        100,
                        100,
                        15,
                        "",
                        0
                );

                this.instance.getUserManager().createPlayer(playerBean);
                this.instance.getUserManager().addTokens(playerBean, 50);
                this.instance.getLogger().info("Creating new user profile for " + event.getUser().getEffectiveName());
            } else {
                List<LevelReward> levelReward = this.instance.getGameServiceManager().getRewardsUpToLevel(player.getLevel());
                for (LevelReward levelRole : levelReward) {
                    Role role = event.getGuild().getRoleById(levelRole.getRoleId());
                    event.getGuild().addRoleToMember(event.getUser(), role);
                }
            }
        } else {
            this.instance.getLogger().info("Profile creation ignored for " + event.getUser().getEffectiveName() + " because it's flagged as bot or system user.");
        }

        this.instance.getGameServiceManager().insertGuildEngagement(event.getMember(), GuildEngagement.Action.GUILD_JOIN);
        FluffBOT.getInstance().getGameServiceManager().addReminder(event.getUser());


    }

    /**
     * Handle assignment of permanent roles for a guild member who rejoined the guild.
     *
     * @param event The join event for a guild member.
     */
    private void handlePermanentRoles(@NotNull GuildMemberJoinEvent event) {
        List<PermanentRole> permanentRoles = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchPermanentRoles(event.getUser());

        if (!permanentRoles.isEmpty()) {
            for (PermanentRole role : permanentRoles) {
                Role guildRole = event.getGuild().getRoleById(role.getRoleId());

                if (guildRole != null) {
                    event.getGuild().addRoleToMember(event.getMember(), guildRole).queue();
                }
            }
        }
    }

    /**
     * Handle tracking of invites for a guild member join event.
     *
     * @param event The join event for a guild member.
     */
    private void handleInviteTracker(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        guild.retrieveInvites().queue(currentInvites -> {
            Invite usedInvite = null;
            for (Invite invite : currentInvites) {
                String url = invite.getUrl();
                int currentUses = invite.getUses();
                int cachedUses = this.instance.getInviteUses().getOrDefault(url, 0);

                if (currentUses > cachedUses) {
                    usedInvite = invite;
                    this.instance.getInviteUses().put(url, currentUses);
                    break;
                }
            }

            if (usedInvite != null) {
                event.getJDA().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild")).getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.invite"))
                        .sendMessageEmbeds(this.instance.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.instance.getLanguageManager().get("common.invite.used", event.getUser().getGlobalName()), "https://fluffici.eu", ICON_QUESTION_MARK.getUrl())
                                .setThumbnail(event.getUser().getAvatarUrl())
                                .setDescription(this.instance.getLanguageManager().get("common.invite.used.desc",
                                        event.getUser().getAsMention(), event.getUser().getGlobalName(),
                                        usedInvite.getInviter().getAsMention(), usedInvite.getInviter().getGlobalName(),
                                        usedInvite.getCode(), usedInvite.getUses(),
                                        guild.getMemberCount()
                                ))
                                .setTimestamp(Instant.now())
                                .build()
                        ).queue();
            }
        }, error -> this.instance.getLogger().warn("Failed to retrieve invites for guild: %s", guild.getName()));
    }

    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
        super.onGuildInviteCreate(event);

        this.instance.getInviteUses().put(event.getInvite().getUrl(), event.getInvite().getUses());
    }

    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
        super.onGuildInviteDelete(event);

        this.instance.getInviteUses().remove(event.getUrl());
    }
}
