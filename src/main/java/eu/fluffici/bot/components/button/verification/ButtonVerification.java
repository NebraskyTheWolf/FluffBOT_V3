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

package eu.fluffici.bot.components.button.verification;

import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.verification.VerificationBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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
        super("row:verify", "Ověření", ButtonStyle.PRIMARY);
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
        String challengeId = GameId.generateId();
        List<ActionRow> actionRows = new ArrayList<>();

        if (FluffBOT.getInstance().getGameServiceManager().hasVerification(interaction.getUser())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.pending"))).setEphemeral(true).queue();
            return;
        }

        if (FluffBOT.getInstance().getGameServiceManager().isQuarantined(interaction.getUser())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("button.verification.quarantined"))).setEphemeral(true).queue();
            return;
        }

        actionRows.add(ActionRow.of(TextInput
                .create("row:verification:one", "Jste furry? Pokud ano, popište svoji fursonu:", TextInputStyle.PARAGRAPH)
                .setMaxLength(500)
                .setMinLength(2)
                .setPlaceholder("Stručně popište svoji sonu, či uveďte Ne, pokud žádnou nemáte.")
                .setRequired(true)
                .build())
        );
        actionRows.add(ActionRow.of(TextInput
                .create("row:verification:two", "Napište pár slov o sobě:", TextInputStyle.PARAGRAPH)
                .setMaxLength(500)
                .setMinLength(2)
                .setPlaceholder("Jaké jsou vaše zájmy a koníčky? Co děláte ve volném čase? Jaká je vaše povaha?")
                .setRequired(true)
                .build())
        );
        actionRows.add(ActionRow.of(TextInput
                .create("row:verification:three", "Jaký máte vztah k furry komunitě?", TextInputStyle.PARAGRAPH)
                .setMaxLength(500)
                .setMinLength(2)
                .setPlaceholder("Jak dlouho jste součástí komunity, jak jí vnímáte, atd.?")
                .setRequired(true)
                .build())
        );
        actionRows.add(ActionRow.of(TextInput
                .create("row:verification:four", "Jak jste našli náš server?", TextInputStyle.PARAGRAPH)
                .setMaxLength(500)
                .setMinLength(2)
                .setPlaceholder("Pokud od někoho, sdělte nám prosím jeho Discord přezdívku")
                .setRequired(true)
                .build())
        );

        Guild guild = interaction.getGuild();
        TextChannel verificationChannel = guild.getTextChannelById(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.verification"));

        handleCustomModal(interaction, "Ověřte se pro přístup do serveru", actionRows, challengeId, (modalInteraction, callbackId) -> {
            String answerOne = modalInteraction.getValue("row:verification:one").getAsString();
            String answerTwo = modalInteraction.getValue("row:verification:two").getAsString();
            String answerThree = modalInteraction.getValue("row:verification:three").getAsString();
            String answerFour = modalInteraction.getValue("row:verification:four").getAsString();

            // Preparing the form to be sent
            EmbedBuilder verificationForm = getEmbed().simpleAuthoredEmbed();
            verificationForm.setAuthor(getLanguageManager().get("button.verification.form.title", modalInteraction.getUser().getGlobalName()), "https://bot.fluffici.eu", ICON_QUESTION_MARK.getUrl());

            verificationForm.setDescription(
                    String.format(
                    """
                    **Jste furry? Pokud ano, popište nám vaši fursonu (není pravidlem pro připojení):**
                    ```
                    %s
                    ```
                    
                    **Napište pár slov o sobě:**
                    ```
                    %s
                    ```
                    
                    **Jaký máte vztah k furry komunitě?:**
                    ```
                    %s
                    ```
                    
                    **Jak jste našli náš server?:**
                    ```
                    %s
                    ```
                    """, answerOne, answerTwo, answerThree, answerFour)
            );

            verificationForm.setColor(Color.ORANGE);
            verificationForm.setTimestamp(Instant.now());
            verificationForm.setThumbnail(modalInteraction.getUser().getAvatarUrl());

            verificationForm.addField(this.getLanguageManager().get("common.username"), modalInteraction.getUser().getGlobalName(), true);
            verificationForm.addField("ID", modalInteraction.getUser().getId(), true);
            verificationForm.addField("Mention", modalInteraction.getUser().getAsMention(), false);

            OffsetDateTime createdDate = modalInteraction.getMember().getTimeCreated();
            LocalDate dateCreated = createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Long daysPassed = ChronoUnit.DAYS.between(dateCreated, LocalDate.now(ZoneId.of("Europe/Prague")));

            verificationForm.addField(this.getLanguageManager().get("common.joined"), NumberFormat.getNumberInstance().format(daysPassed) + " dny", true);

            String yes = this.getLanguageManager().get("common.yes");
            String no = this.getLanguageManager().get("common.no");

            boolean isGloballyBlacklisted = FluffBOT.getInstance().getGameServiceManager().isGloballyBlacklisted(modalInteraction.getUser());
            boolean isLocallyBlacklisted = FluffBOT.getInstance().getGameServiceManager().isLocallyBlacklisted(modalInteraction.getGuild(), modalInteraction.getUser());
            boolean isSpammer = (interaction.getUser().getFlagsRaw() & SPAMMER_MASK) == SPAMMER_MASK;

            verificationForm.addField(this.getLanguageManager().get("common.globally_blacklisted"), (isGloballyBlacklisted ? yes : no), false);
            verificationForm.addField(this.getLanguageManager().get("common.locally_blacklisted"), (isLocallyBlacklisted ? yes : no), false);
            verificationForm.addField(this.getLanguageManager().get("common.is_flagged_spam"), (isSpammer ? yes : no), false);

            StringSelectMenu.Builder actions = StringSelectMenu.create("select:verification-action");
            actions.addOption("Ověřit", "ACCEPT:".concat(challengeId), Emoji.fromCustom("clipboardcheck", 1216864649396486374L, true));
            actions.addOption("Zamítnout", "DENY:".concat(challengeId), Emoji.fromCustom("clipboardx", 1216864650914824242L, false));
            actions.addOption("Ban", "BAN:".concat(challengeId), Emoji.fromCustom("ban", 1216864274253746268L, true));

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

            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .createVerificationRecord(new VerificationBuilder(
                            modalInteraction.getUser().getId(),
                            null,
                            null,
                            callbackId,
                            new JsonObject()
                    ));

            FluffBOT.getInstance().getGameServiceManager().removeReminder(modalInteraction.getUser());

            modalInteraction.replyEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                            .setAuthor("Úspěšně odesláno", "https://fluffici.eu", ICON_FOLDER.getUrl())
                            .setDescription("Tvoje žádost o ověření byla odeslána, prosíme o ztrpení.")
                            .setTimestamp(Instant.now())
                            .setColor(Color.GREEN)
                    .build()
            ).setEphemeral(true).queue();
        });
    }
}