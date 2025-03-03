package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : ButtonBuilder.java

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


import eu.fluffici.bot.api.hooks.IEmbed;
import eu.fluffici.bot.api.hooks.IInteractionManager;
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.hooks.IUserManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static eu.fluffici.bot.api.IconRegistry.*;

@Getter
public abstract class ButtonBuilder {
    private final String customId;
    private final String label;
    private final ButtonStyle style;
    private final Map<String, Boolean> options = new LinkedHashMap<>();
    private final Map<String, String> arguments = new LinkedHashMap<>();

    private boolean isSelectMenu;

    @Setter
    private IEmbed embed;
    @Setter
    private ILanguageManager languageManager;

    @Setter
    private IUserManager userManager;
    @Setter
    private IInteractionManager interactionManager;

    public ButtonBuilder(String customId, String label, ButtonStyle style) {
        this.customId = customId;
        this.label = label;
        this.style = style;
    }

    public abstract void execute(ButtonInteraction interaction);

    public Pair<Boolean, Pair<String, Interactions>> handleInteraction(String userId) {
        if (this.getArguments().containsKey("personal")) {
            Interactions interactions = this.interactionManager.fetchInteraction(this.getArguments().get("interactionId"));
            if (interactions.isExpired())
                return Pair.of(false, Pair.of(this.getLanguageManager().get("button.error.expired"), null));
            else if (!interactions.getUserId().equals(userId))
                return Pair.of(false, Pair.of(this.getLanguageManager().get("button.error.not_owned"), null));
            else if (interactions.isAcknowledged())
                return Pair.of(false, Pair.of(this.getLanguageManager().get("button.error.already_acknowledged"), null));
            else
                return Pair.of(true, Pair.of(null, interactions));
        } else
            return Pair.of(false, Pair.of(this.getLanguageManager().get("button.error.wrong_type"), null));
    }

    public net.dv8tion.jda.api.interactions.components.buttons.Button build(Emoji emoji) {
        if (emoji != null) {
            return net.dv8tion.jda.api.interactions.components.buttons.Button.of(this.style, this.customId, this.label).withEmoji(emoji);
        }
        return net.dv8tion.jda.api.interactions.components.buttons.Button.of(this.style, this.customId, this.label);
    }

    /**
     * Builds an error message embed with the given description.
     *
     * @param description The description of the error message.
     * @return The built MessageEmbed object representing the error message.
     */
    public MessageEmbed buildError(String description) {
        return this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.error"), "https://fluffici.eu", ICON_ALERT_CIRCLE)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .setFooter(this.getLanguageManager().get("common.error.footer"), ICON_QUESTION_MARK)
                .build();
    }

    /**
     * Builds a success message embed with the given description.
     *
     * @param description The description of the success message.
     * @return The built MessageEmbed object representing the success message.
     */
    public MessageEmbed buildSuccess(String description) {
        return this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }
}
