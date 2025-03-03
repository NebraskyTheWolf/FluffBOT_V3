/*
---------------------------------------------------------------------------------
File Name : CommandModeration

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.moderator;

import eu.fluffici.bot.api.DurationUtil;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.Sanction;
import eu.fluffici.bot.api.beans.furraid.SanctionBuilder;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.furraid.SanctionType;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.netlib.blas.Srot;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Represents a command for moderation purposes.
 */
@SuppressWarnings("All")
public class CommandModeration extends FCommand {
    public CommandModeration() {
        super("moderation", "This command will help you using the moderation tools", CommandCategory.MODERATOR);

        this.getSubcommandData().add(new SubcommandData("ban", "Ban a user from the server")
                .addOption(OptionType.USER, "user", "The user to ban", true)
                .addOption(OptionType.STRING, "reason", "The reason for banning the user", true)
                .addOption(OptionType.STRING, "duration", "The duration of the ban", true)
        );
        this.getSubcommandData().add(new SubcommandData("kick", "Kick a user from the server")
                .addOption(OptionType.USER, "user", "The user to kick", true)
                .addOption(OptionType.STRING, "reason", "The reason for kicking the user", true)
        );
        this.getSubcommandData().add(new SubcommandData("mute", "Mute a user for a specified duration")
                .addOption(OptionType.USER, "user", "The user to mute", true)
                .addOption(OptionType.STRING, "reason", "The reason for muting the user", true)
                .addOption(OptionType.STRING, "duration", "The duration of the ban", true)
        );
        this.getSubcommandData().add(new SubcommandData("warn", "Warn a user")
                .addOption(OptionType.USER, "user", "The user to warn", true)
                .addOption(OptionType.STRING, "reason", "The reason for warning the user", true)
        );
        this.getSubcommandData().add(new SubcommandData("history", "View the moderation history of a user")
                .addOption(OptionType.USER, "user", "The user to lookup", true)
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(
                Permission.MODERATE_MEMBERS,
                Permission.ADMINISTRATOR
        ));
        this.getOptions().put("noSelfUser", true);
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    public void execute(@NotNull CommandInteraction interaction, GuildSettings settings) {
        String command = interaction.getSubcommandName();

        switch (command) {
            case "ban" -> this.handleBan(interaction);
            case "mute" -> this.handleMute(interaction);
            case "kick" -> this.handleKick(interaction);
            case "warn" -> this.handleWarn(interaction);
            case "history" -> this.handleHistory(interaction);
        }
    }

    /**
     * Handles the ban command for moderation.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleBan(@NotNull CommandInteraction interaction) {
        if (!interaction.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            interaction.replyEmbeds(this.buildError("You are not permitted to perform this operation.")).queue();
            return;
        }

        Member member = interaction.getOption("user").getAsMember();
        String reason = interaction.getOption("reason").getAsString();
        String duration = interaction.getOption("duration").getAsString();

        Timestamp parsedDuration = this.parseDuration(interaction, duration);
        if (parsedDuration != null) {
            boolean result = FurRaidDB.getInstance().getSanctionManager().addSanction(SanctionBuilder
                    .builder()
                        .guild(interaction.getGuild())
                        .sanctionType(SanctionType.BAN)
                        .member(member)
                        .author(interaction.getMember())
                        .expiration(parsedDuration)
                        .reason(reason)
                    .build()
            );

            if (result) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.ban.succeed", member.getAsMention(), reason))).queue();
            } else {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.ban.failed"))).queue();
            }
        }
    }

    /**
     * Handles the kick command for moderation.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleKick(@NotNull CommandInteraction interaction) {
        if (!interaction.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            interaction.replyEmbeds(this.buildError("You are not permitted to perform this operation.")).queue();
            return;
        }

        Member member = interaction.getOption("user").getAsMember();
        String reason = interaction.getOption("reason").getAsString();

        boolean result = FurRaidDB.getInstance().getSanctionManager().addSanction(SanctionBuilder
                .builder()
                .guild(interaction.getGuild())
                .sanctionType(SanctionType.KICK)
                .member(member)
                .author(interaction.getMember())
                .reason(reason)
                .build()
        );

        if (result) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.kick.succeed", member.getAsMention(), reason))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.kick.failed"))).queue();
        }
    }

    /**
     * Handles muting a user in a command interaction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleMute(@NotNull CommandInteraction interaction) {
        Member member = interaction.getOption("user").getAsMember();
        String reason = interaction.getOption("reason").getAsString();
        String duration = interaction.getOption("duration").getAsString();

        Timestamp parsedDuration = this.parseDuration(interaction, duration);
        if (parsedDuration != null) {
            boolean result = FurRaidDB.getInstance().getSanctionManager().addSanction(SanctionBuilder
                    .builder()
                    .guild(interaction.getGuild())
                    .sanctionType(SanctionType.MUTE)
                    .member(member)
                    .author(interaction.getMember())
                    .expiration(parsedDuration)
                    .reason(reason)
                    .build()
            );

            if (result) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.mute.succeed", member.getAsMention()))).queue();
            } else {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.mute.failed"))).queue();
            }
        }
    }

    /**
     * Handles a warning for the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleWarn(@NotNull CommandInteraction interaction) {
        Member member = interaction.getOption("user").getAsMember();
        String reason = interaction.getOption("reason").getAsString();

        boolean result = FurRaidDB.getInstance().getSanctionManager().addSanction(SanctionBuilder
                .builder()
                .guild(interaction.getGuild())
                .sanctionType(SanctionType.WARN)
                .member(member)
                .author(interaction.getMember())
                .reason(reason)
                .build()
        );

        if (result) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.warn.succeed", member.getAsMention()))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.moderation.warn.failed"))).queue();
        }
    }

    /**
     * Handles the history command for moderation.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleHistory(@NotNull CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();

        try {
            List<Sanction> sanctions = FurRaidDB.getInstance().getGameServiceManager().getAllSanctions(interaction.getGuild().getId());
            List<Sanction> userSanctions = sanctions.stream()
                    .filter(sanction -> sanction.getUserId().equals(user.getId()))
                    .sorted(Comparator.comparing(Sanction::getCreatedAt).reversed())
                    .limit(25)
                    .toList();

            if (userSanctions.isEmpty()) {
                interaction.replyEmbeds(
                        this.getEmbed().simpleAuthoredEmbed(user, this.getLanguageManager().get("command.history.not_found.title"), this.getLanguageManager().get("command.history.not_found.desc"), Color.GREEN)
                                .build()
                ).setEphemeral(true).queue();
            } else {
                List<MessageEmbed.Field> fields = userSanctions.stream()
                        .map(sanction -> new MessageEmbed.Field(
                                this.getLanguageManager().get("command.history.field.title", this.getTypeFromId(sanction.getTypeId())),
                                this.getLanguageManager().get("command.history.field.value", sanction.getReason(), DurationUtil.getDuration(sanction.getCreatedAt().getTime()).toString(), interaction.getGuild().getMemberById(sanction.getAuthorId()).getUser().getEffectiveName()), true))
                        .toList();

                EmbedBuilder embedBuilder = this.getEmbed().simpleAuthoredEmbed(user,
                        this.getLanguageManager().get("command.history.found.title"),
                        this.getLanguageManager().get("command.history.found.desc", userSanctions.size()),
                        Color.RED
                );
                embedBuilder.getFields().addAll(fields);

                interaction.replyEmbeds(embedBuilder.build()).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            interaction.reply(this.getLanguageManager().get("command.history.error")).queue();
        }
    }

    /**
     * Validates the duration string.
     *
     * @param duration The duration string to validate.
     * @return {@code true} if the duration string contains a colon (:) symbol, {@code false} otherwise.
     */
    private boolean validateDuration(@NotNull String duration) {
        return duration.indexOf(':') != -1;
    }

    /**
     * Parses a duration string and returns a Timestamp object representing the calculated time in the future.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param duration    The duration string to parse.
     * @return A Timestamp object representing the calculated time in the future.
     * @throws IllegalArgumentException if the duration string has an invalid format.
     */
    @Nullable
    private Timestamp parseDuration(CommandInteraction interaction, @NotNull String duration) {
        if (!this.validateDuration(duration)) {
            interaction.replyEmbeds(this.buildError("Invalid duration format, please use for example: 10:d or 10:days, 1:m or 1:months, for more information, please read the documentation."))
                    .addActionRow(Button.link("https://frdbdocs.fluffici.eu", "Documentation"))
                    .queue();
            return null;
        }

        String[] formatted = duration.split(":");
        int amount = Integer.parseInt(formatted[0]);

        return switch (formatted[1]) {
            case "d":
            case "days":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.DAYS));
            case "m":
            case "months":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.MONTHS));
            case "min":
            case "minutes":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.MINUTES));
            case "h":
            case "hours":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.HOURS));
            case "sec":
            case "seconds":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.SECONDS));
            case "y":
            case "years":
                yield Timestamp.from(Instant.now().plus(amount, ChronoUnit.YEARS));
            default: {
                interaction.replyEmbeds(this.buildError("Invalid duration format, please use for example: 10:d or 10:days, 1:m or 1:months, for more information, please read the documentation."))
                        .addActionRow(Button.link("https://frdbdocs.fluffici.eu", "Documentation"))
                        .queue();
                yield null;
            }
        };
    }

    /**
     * Retrieves the type of moderation based on the given ID.
     *
     * @param id The ID representing the type of moderation.
     * @return A String representing the type of moderation.
     */
    private String getTypeFromId(int id) {
        return switch (id) {
            case 1 -> "Warning";
            case 2 -> "Banishment";
            case 3 -> "Expulsion";
            case 4 -> "Silencing";
            default -> "Unknown Moderation Type";
        };
    }
}