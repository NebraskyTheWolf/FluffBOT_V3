/*
---------------------------------------------------------------------------------
File Name : ButtonVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.button.verification;


import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.OfferQuota;
import eu.fluffici.bot.api.beans.furraid.verification.Verification;
import eu.fluffici.bot.api.beans.furraid.verification.VerificationParser;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.server.users.FGetUserRoute;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_FOLDER;
import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;

public class ButtonVerification extends ButtonBuilder {
    /**
     * The SPAMMER_MASK variable represents a bit mask for identifying spammers.
     * It is used in the class ButtonVerification, which is a superclass of ButtonBuilder.
     * <p>
     * The SPAMMER_MASK variable is defined as a constant integer with the value of 1 shifted to the left by 20 positions.
     * This creates a bitmask with the 21st bit set to 1 and all other bits set to 0.
     * <p>
     * The SPAMMER_MASK is used to perform bitwise operations with other variables or constants to check if a bit is set or to set a bit in a bitmask.
     * It is commonly used for spam detection or filtering purposes.
     * <p>
     * Example usage:
     * <p>
     * // Check if the 21st bit is set in a bitmask
     * if ((bitmask & SPAMMER_MASK) != 0) {
     *     // Bit is set, indicating a potential spammer
     *     // Perform spam detection logic here
     * }
     * <p>
     * // Set the 21st bit in a bitmask
     * bitmask |= SPAMMER_MASK;
     * <p>
     * Note: The above code is just an example. Actual usage may vary depending on the context and requirements.
     */
    public final int SPAMMER_MASK = 1 << 20;

    public ButtonVerification() {
        super("row:verify", "Verify", ButtonStyle.PRIMARY);
    }

    /**
     * Executes the button interaction by creating a custom modal with action rows and a title.
     * The modal callback is not implemented in this method.
     *
     * @param interaction The button interaction triggering the execution.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull ButtonInteraction interaction) {
        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(interaction.getGuild());
        this.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        String challengeId = GameId.generateId();

        if (FurRaidDB.getInstance().getGameServiceManager().hasFVerification(interaction.getUser())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.pending"))).setEphemeral(true).queue();
            return;
        }

        OfferQuota offerQuota = FurRaidDB.getInstance().getOfferManager().getByGuild(interaction.getGuild().getIdLong());

        if (!guildSettings.getConfig().getFeatures().getVerification().isEnabled()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.disabled"))).setEphemeral(true).queue();
            return;
        }

        String gateChannel = guildSettings.getConfig().getFeatures().getVerification().getSettings().getVerificationGate();
        String loggingChannel = guildSettings.getConfig().getFeatures().getVerification().getSettings().getVerificationLoggingChannel();
        List<FurRaidConfig.Question> questions = guildSettings.getConfig().getFeatures().getVerification().getSettings().getQuestions();

        if (questions.isEmpty()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.no_questions"))).setEphemeral(true).queue();
            return;
        }

        List<ActionRow> actionRows = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            FurRaidConfig.Question question = questions.get(i);

            actionRows.add(ActionRow.of(TextInput
                    .create("row:verification:".concat(this.getNumber(i)), question.getTitle(), TextInputStyle.PARAGRAPH)
                    .setMaxLength(question.getMax())
                    .setMinLength(question.getMin())
                    .setPlaceholder(question.getPlaceholder())
                    .setRequired(true)
                    .build())
            );
        }

        Guild guild = interaction.getGuild();
        TextChannel verificationChannel = guild.getTextChannelById(loggingChannel);

        handleCustomModal(interaction, "Verification form", actionRows, challengeId, (modalInteraction, callbackId) -> {
            String answerOne = modalInteraction.getValue("row:verification:one").getAsString();

            // Preparing the form to be sent
            EmbedBuilder verificationForm = getEmbed().simpleAuthoredEmbed();
            verificationForm.setAuthor(getLanguageManager().get("button.verification.form.title", modalInteraction.getUser().getGlobalName()), "https://bot.fluffici.eu", ICON_QUESTION_MARK);

            List<VerificationParser.Question> answers = new ArrayList<>();

            for (int i = 0; i < questions.size(); i++) {
                FurRaidConfig.Question question = questions.get(i);

                String answer =  modalInteraction.getValue("row:verification:".concat(this.getNumber(i))).getAsString();
                answers.add(new VerificationParser.Question(question.getTitle(), answer));

                verificationForm.appendDescription(this.questionFormatter(question.getTitle(), answer.concat("\n")));
            }

            verificationForm.setColor(Color.ORANGE);
            verificationForm.setTimestamp(Instant.now());
            verificationForm.setThumbnail(modalInteraction.getUser().getAvatarUrl());

            // Adding some user information
            verificationForm.addField(this.getLanguageManager().get("common.username"), modalInteraction.getUser().getGlobalName(), true);
            verificationForm.addField("ID", modalInteraction.getUser().getId(), true);
            verificationForm.addField("Mention", modalInteraction.getUser().getAsMention(), false);

            // Converting the discord user creation date to days using ChronoUnit
            OffsetDateTime createdDate = modalInteraction.getMember().getTimeCreated();
            LocalDate dateCreated = createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Long daysPassed = ChronoUnit.DAYS.between(dateCreated, LocalDate.now(ZoneId.of("Europe/Prague")));

            verificationForm.addField(this.getLanguageManager().get("common.joined"), NumberFormat.getNumberInstance().format(daysPassed) + " dny", true);

            String yes = this.getLanguageManager().get("common.yes");
            String no = this.getLanguageManager().get("common.no");

            boolean isGloballyBlacklisted = FurRaidDB.getInstance().getGameServiceManager().isGloballyBlacklisted(modalInteraction.getUser());
            boolean isLocallyBlacklisted = FurRaidDB.getInstance().getGameServiceManager().isLocallyBlacklisted(modalInteraction.getGuild(), modalInteraction.getUser());
            boolean isSpammer = FGetUserRoute.isSpammer(interaction.getUser());

            verificationForm.addField(this.getLanguageManager().get("common.globally_blacklisted"), (isGloballyBlacklisted ? yes : no), false);
            verificationForm.addField(this.getLanguageManager().get("common.locally_blacklisted"), (isLocallyBlacklisted ? yes : no), false);
            verificationForm.addField(this.getLanguageManager().get("common.is_flagged_spam"), (isSpammer ? yes : no), false);

            // Staff verification controller
            StringSelectMenu.Builder actions = StringSelectMenu.create("select:verification-action");
            actions.addOption("Grant", "ACCEPT:".concat(challengeId), Emoji.fromCustom("clipboardcheck", 1216864649396486374L, true));
            actions.addOption("Deny", "DENY:".concat(challengeId), Emoji.fromCustom("clipboardx", 1216864650914824242L, false));

            // Sending the form to the staff channel
            MessageCreateAction messageCreateAction = verificationChannel
                    .sendMessageEmbeds(verificationForm.build());

            if (isGloballyBlacklisted || isLocallyBlacklisted) {
                messageCreateAction.setComponents(
                        ActionRow.of(Button.danger("button:none", this.getLanguageManager().get("command.verification.not_eligible"))
                                .withDisabled(true)
                                .withEmoji(Emoji.fromCustom("userx", 1252946639933673483L, false))
                        )
                );
            } else {
                messageCreateAction.addActionRow(actions.build());
            }

            Message message = messageCreateAction.complete();

            FurRaidDB.getInstance()
                    .getGameServiceManager()
                    .createVerificationRecord(new Verification(
                            0L,
                            guild.getId(),
                            modalInteraction.getUser().getId(),
                            "PENDING",
                            null,
                            callbackId,
                            message.getId(),
                            answers,
                            null,
                            null
                    ));

            modalInteraction.replyEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor("Successfully Sent", "https://frdb.fluffici.eu", ICON_FOLDER)
                    .setDescription("Your verification request has been sent, please be patient.")
                    .setTimestamp(Instant.now())
                    .setColor(Color.GREEN)
                    .build()
            ).setEphemeral(true).queue();
        });
    }

    /**
     * Returns the word representation of the given number.
     *
     * @param number The number to be converted. Must be between 1 and 5 (inclusive).
     * @return The word representation of the number.
     * @throws IllegalStateException If the number is not between 1 and 5.
     */
    private String getNumber(int number) {
        return switch (number) {
            case 0 -> "one";
            case 1 -> "two";
            case 2 -> "three";
            case 3 -> "four";
            case 4 -> "five";
            default -> throw new IllegalStateException("Unexpected value: " + number);
        };
    }

    /**
     * Formats a question with its answer.
     *
     * @param title  The title of the question.
     * @param answer The answer to the question.
     * @return The formatted question and answer string.
     */
    private String questionFormatter(String title, String answer) {
        return """
               **%s:**
               ```
                %s
               ```
               """.formatted(title, answer);
    }
}