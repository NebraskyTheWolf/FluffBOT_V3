/*
---------------------------------------------------------------------------------
File Name : SanctionManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.Sanction;
import eu.fluffici.bot.api.beans.furraid.SanctionBuilder;
import eu.fluffici.bot.api.furraid.SanctionType;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.*;

/**
 * The SanctionManager class is responsible for managing sanctions.
 * It provides methods for adding, removing, and checking sanctions
 * for a given user.
 */
public class SanctionManager {

    private final FurRaidDB instance;

    public SanctionManager(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * Adds a sanction to the system.
     *
     * @param sanctionBuilder the builder object containing the details of the sanction
     */
    public boolean addSanction(@NotNull SanctionBuilder sanctionBuilder) {
        return switch (sanctionBuilder.getSanctionType()) {
            case BAN -> this.handleBan(sanctionBuilder);
            case MUTE -> this.handleMute(sanctionBuilder);
            case KICK -> this.handleKick(sanctionBuilder);
            case WARN -> this.handleWarn(sanctionBuilder);
        };
    }

    /**
     * Fetches a sanction for a given user and type.
     *
     * @param user The Member object representing the user.
     * @param type The type of sanction to fetch.
     * @return The Sanction object that matches the user and type.
     */
    @SneakyThrows
    public Sanction fetchSanction(@NotNull Member user, int type) {
        return this.instance.getGameServiceManager().getAllSanctions(user.getGuild().getId())
                .stream()
                .filter(sanction -> sanction.getUserId().equals(user.getId()))
                .filter(sanction -> sanction.getTypeId() == type)
                .filter(sanction -> !sanction.isDeleted())
                .limit(1)
                .distinct().toList()
                .getFirst();
    }

    /**
     * Handles the ban sanction for a user.
     *
     * @param sanctionBuilder the builder object containing the details of the sanction
     */
    @SneakyThrows
    private boolean handleBan(@NotNull SanctionBuilder sanctionBuilder) {
        if (sanctionBuilder.getMember().getId().equals(sanctionBuilder.getAuthor().getId()))
            return false;

        this.instance.getGameServiceManager().applySanction(new Sanction(
                0L,
                Sanction.BAN,
                sanctionBuilder.getGuild().getId(),
                sanctionBuilder.getMember().getId(),
                sanctionBuilder.getReason(),
                sanctionBuilder.getAuthor().getId(),
                sanctionBuilder.getExpiration(),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                null
        ));

        try {
            sanctionBuilder.getMember().ban(7, TimeUnit.DAYS).reason(sanctionBuilder.getReason())
                    .queue();
        } catch (Exception e) {
            return false;
        }

        this.handleLogging(sanctionBuilder);
        return true;
    }

    /**
     * Handles the mute sanction.
     *
     * @param sanctionBuilder the builder object containing the details of the sanction
     */
    @SneakyThrows
    private boolean handleMute(@NotNull SanctionBuilder sanctionBuilder) {
        if (sanctionBuilder.getMember().getId().equals(sanctionBuilder.getAuthor().getId()))
            return false;

        Map.Entry<Long, TimeUnit> result = this.timestampToPair(sanctionBuilder.getExpiration());

        this.instance.getGameServiceManager().applySanction(new Sanction(
                0L,
                Sanction.MUTE,
                sanctionBuilder.getGuild().getId(),
                sanctionBuilder.getMember().getId(),
                sanctionBuilder.getReason(),
                sanctionBuilder.getAuthor().getId(),
                sanctionBuilder.getExpiration(),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                null
        ));

        try {
            sanctionBuilder.getMember().timeoutFor(result.getKey(), result.getValue()).reason(sanctionBuilder.getReason())
                    .queue();
        } catch (Exception e) {
            return false;
        }

        this.handleLogging(sanctionBuilder);
        return true;
    }


    /**
     * Handles the kick sanction for a user.
     *
     * @param sanctionBuilder the builder object containing the details of the sanction
     */
    @SneakyThrows
    private boolean handleKick(@NotNull SanctionBuilder sanctionBuilder) {
        if (sanctionBuilder.getMember().getId().equals(sanctionBuilder.getAuthor().getId()))
            return false;

        this.instance.getGameServiceManager().applySanction(new Sanction(
                0L,
                Sanction.KICK,
                sanctionBuilder.getGuild().getId(),
                sanctionBuilder.getMember().getId(),
                sanctionBuilder.getReason(),
                sanctionBuilder.getAuthor().getId(),
                sanctionBuilder.getExpiration(),
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                null
        ));

        try {
            sanctionBuilder.getMember().kick().reason(sanctionBuilder.getReason()).queue();
        } catch (Exception e) {
            return false;
        }

        this.handleLogging(sanctionBuilder);
        return true;
    }

    /**
     * Handles a warning sanction.
     *
     * @param sanctionBuilder the builder object containing the details of the sanction
     */
    @SneakyThrows
    private boolean handleWarn(SanctionBuilder sanctionBuilder) {
        if (sanctionBuilder.getMember().getId().equals(sanctionBuilder.getAuthor().getId()))
            return false;

        this.instance.getGameServiceManager().applySanction(new Sanction(
                0L,
                Sanction.WARN,
                sanctionBuilder.getGuild().getId(),
                sanctionBuilder.getMember().getId(),
                sanctionBuilder.getReason(),
                sanctionBuilder.getAuthor().getId(),
                null,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                false,
                null
        ));

        this.handleLogging(sanctionBuilder);
        return true;
    }

    /**
     * Handles logging for a sanction.
     *
     * @param sanction The SanctionBuilder object containing the details of the sanction.
     */
    private void handleLogging(@NotNull SanctionBuilder sanction) {
        Guild guild = sanction.getGuild();
        GuildSettings guildSettings = this.instance.getBlacklistManager().fetchGuildSettings(guild);
        this.instance.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());


        if (guildSettings.getLoggingChannel() != null) {
            TextChannel loggingChannel = guild.getTextChannelById(guildSettings.getLoggingChannel());
            if (loggingChannel != null && loggingChannel.canTalk(guild.getSelfMember())) {
                EmbedBuilder sanctionLog = this.instance.getEmbed().simpleAuthoredEmbed();
                sanctionLog.setAuthor(this.getTitleByType(sanction.getSanctionType(), sanction.getMember().getEffectiveName()), "https://frdb.fluffici.eu", this.getIconByType(sanction.getSanctionType()));
                if (sanction.getMember().getAvatarUrl() != null)
                    sanctionLog.setThumbnail(sanction.getMember().getAvatarUrl());
                sanctionLog.addField(this.instance.getLanguageManager().get("common.user.name"), sanction.getMember().getAsMention(), false);
                sanctionLog.addField(this.instance.getLanguageManager().get("common.user.id"), sanction.getMember().getId(), false);
                if (sanction.getReason() != null)
                    sanctionLog.addField(this.instance.getLanguageManager().get("common.reason"), sanction.getReason(), false);
                sanctionLog.addField(this.instance.getLanguageManager().get("common.author"), sanction.getAuthor().getAsMention(), false);
                if (sanction.getExpiration() != null)
                    sanctionLog.addField(this.instance.getLanguageManager().get("common.duration"), sanction.getExpiration().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false);
                sanctionLog.setTimestamp(Instant.now());

                loggingChannel.sendMessageEmbeds(sanctionLog.build()).queue();
            }
        }
    }

    /**
     * Retrieves the icon associated with a given sanction type.
     *
     * @param type the type of sanction
     * @return the icon associated with the given sanction type
     */
    @Contract(pure = true)
    private String getIconByType(@NotNull SanctionType type) {
        return switch (type) {
            case KICK -> ICON_USER_X;
            case WARN -> ICON_WARNING;
            case BAN -> ICON_CIRCLE_SLASHED;
            case MUTE -> ICON_MESSAGE_EXCLAMATION;
        };
    }

    /**
     * Retrieves the title associated with a given sanction type.
     *
     * @param type     the type of sanction
     * @param username the username associated with the sanction
     * @return the title associated with the given sanction type
     */
    @Contract(pure = true)
    private String getTitleByType(@NotNull SanctionType type, String username) {
        return switch (type) {
            case KICK -> this.instance.getLanguageManager().get("common.kick.title", username);
            case WARN -> this.instance.getLanguageManager().get("common.warn.title", username);
            case BAN -> this.instance.getLanguageManager().get("common.ban.title", username);
            case MUTE -> this.instance.getLanguageManager().get("common.mute.title", username);
        };
    }

    /**
     * Converts a timestamp to a pair of time difference and time unit.
     *
     * @param timestamp the timestamp to convert
     * @return a map entry containing the time difference and the time unit
     */
    @NotNull
    private Map.Entry<Long, TimeUnit> timestampToPair(@NotNull Timestamp timestamp) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        long diffInMillies = Math.abs(currentTimestamp.getTime() - timestamp.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return new AbstractMap.SimpleEntry<>(diff, TimeUnit.DAYS);
    }
}