package eu.fluffici.furraid.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildAddListener.java

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

import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.furraid.verification.Verification;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.server.users.FGetUserRoute;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_USER_X;

public class GuildUserLeftListener extends ListenerAdapter {
    public static final int SPAMMER_MASK = 1 << 20;
    private final FurRaidDB instance;

    public GuildUserLeftListener(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("All")
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        GuildSettings guildSettings = this.instance
                .getBlacklistManager()
                .fetchGuildSettings(event.getGuild());

        // Loading the guild language
        this.instance.getLanguageManager()
                .loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        if (guildSettings != null && guildSettings.getLoggingChannel() != null) {
            if (guildSettings.getConfig().getSettings().isUsingJoinLeaveInformation()) {
                TextChannel loggingChannel = event.getGuild().getTextChannelById(guildSettings.getLoggingChannel());

                String yes = this.instance.getLanguageManager().get("common.yes");
                String no = this.instance.getLanguageManager().get("common.no");

                boolean isGloballyBlacklisted = this.instance.getBlacklistManager().isGloballyBlacklisted(event.getUser());
                boolean isLocallyBlacklisted = this.instance.getBlacklistManager().isLocallyBlacklisted(event.getGuild(), event.getUser());

                boolean isSpammer = FGetUserRoute.isSpammer(event.getUser());

                EmbedBuilder leftMessage = this.instance.getEmbed().simpleAuthoredEmbed();
                leftMessage.setAuthor(event.getUser().getEffectiveName());
                leftMessage.setThumbnail((event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getDefaultAvatarUrl()));
                leftMessage.setTitle(this.instance.getLanguageManager().get("common.user.left.title", event.getUser().getGlobalName()));
                leftMessage.setDescription(this.instance.getLanguageManager().get("common.user.left.description"));
                leftMessage.setColor(Color.RED);

                leftMessage.addField(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId(), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.account_age"), event.getUser().getTimeCreated().format(this.instance.getDateTimeFormatter()), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.globally_blacklisted"), (isGloballyBlacklisted ? yes : no), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.locally_blacklisted"), (isLocallyBlacklisted ? yes : no), false);
                leftMessage.addField(this.instance.getLanguageManager().get("common.is_flagged_spam"), (isSpammer ? yes : no), false);

                loggingChannel.sendMessageEmbeds(leftMessage.build()).queue();
            }
        }

        FurRaidConfig.WelcomingFeature welcomingFeature = guildSettings.getConfig().getFeatures().getWelcoming();
        if (welcomingFeature.isEnabled()) {
            TextChannel goodbyeChannel = event.getGuild().getTextChannelById(welcomingFeature.getSettings().getGoodbyeChannel());
            if (goodbyeChannel != null && goodbyeChannel.canTalk(event.getGuild().getSelfMember())) {
                this.handleGoodbye(event, goodbyeChannel, guildSettings);
            }
        }

        FurRaidConfig.VerificationFeature verificationFeature = guildSettings.getConfig().getFeatures().getVerification();
        if (verificationFeature.isEnabled()) {
            if (FurRaidDB.getInstance().getGameServiceManager().hasFVerification(event.getUser())) {
                Verification verification = FurRaidDB.getInstance().getGameServiceManager().getVerificationRecord(
                        event.getGuild().getId(),
                        event.getUser()
                );

                TextChannel verificationChannel = event.getGuild().getTextChannelById(verificationFeature.getSettings().getVerificationLoggingChannel());
                verificationChannel.editMessageComponentsById(verification.getMessageId())
                                .setComponents(ActionRow.of(
                                        Button.secondary("button:none", "The user left the server").asDisabled()
                                )).queue();

                verification.setStatus("EXPIRED");

                FurRaidDB.getInstance().getGameServiceManager().updateVerificationRecord(verification);
            }
        }

        // Ticket closure on user leave
        FurRaidConfig.TicketFeature ticketFeature = guildSettings.getConfig().getFeatures().getTicket();
        if (ticketFeature.isEnabled()) {
            TicketBuilder ticket = FurRaidDB.getInstance().getGameServiceManager().fetchFTicketByUser(
                    event.getUser(),
                    event.getGuild().getId()
            );
            if (ticketFeature.getSettings().isAutoCloseOnUserLeave()
                    && FurRaidDB.getInstance().getGameServiceManager().hasFTicket(event.getUser(), event.getGuild().getId())) {
                FurRaidDB.getInstance().getTicketManager().closeTicket(ticket, "System");
            } else {
                TextChannel ticketChannel = event.getGuild().getTextChannelById(ticket.getChannelId());
                ticketChannel.sendMessageEmbeds(this.instance.getEmbed().simpleAuthoredEmbed()
                        .setAuthor(this.instance.getLanguageManager().get("event.left.ticket.title", event.getUser().getEffectiveName()), "https://frdb.fluffici.eu", ICON_USER_X.getUrl())
                        .setColor(Color.RED)
                        .setFooter(event.getMember().getId())
                        .setThumbnail((event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getDefaultAvatarUrl()))
                        .setTimestamp(Instant.now())
                        .build()
                ).queue();
            }
        }
    }

    /**
     * Handles the goodbye message when a guild member leaves.
     *
     * @param event The GuildMemberRemoveEvent representing the event of a guild member leaving the server.
     * @param goodbyeChannel The TextChannel to send the goodbye message to.
     */
    @SuppressWarnings("All")
    private void handleGoodbye(@NonNull GuildMemberRemoveEvent event, @NonNull TextChannel goodbyeChannel, @NonNull GuildSettings guildSettings) {
        FurRaidConfig.WelcomingFeature welcomingFeature = guildSettings.getConfig().getFeatures().getWelcoming();

        goodbyeChannel.sendMessageEmbeds(this.instance.getEmbed().simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("event.left.goodbye.title", event.getUser().getEffectiveName()), "https://frdb.fluffici.eu", ICON_USER_X.getUrl())
                .setColor(Color.RED)
                .setDescription(welcomingFeature.getSettings().getLeftMessage())
                .setFooter(event.getMember().getId())
                .setThumbnail((event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getDefaultAvatarUrl()))
                .setTimestamp(Instant.now())
                .build()
        ).queue();
    }
}
