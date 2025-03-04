package eu.fluffici.bot.events.interaction;

/*
---------------------------------------------------------------------------------
File Name : ButtonInteractionListener.java

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


import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.RestrictedAccess;
import eu.fluffici.bot.api.events.ButtonEvent;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.api.interactions.SelectMenu;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_MINUS;

public class ButtonInteractionListener extends ListenerAdapter {

    private final FluffBOT instance;

    public ButtonInteractionListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("button:"))
            return;
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;

        // Handling terminated user(s).
        RestrictedAccess restrictedAccess = this.instance.getGameServiceManager().fetchRestrictedPlayer(event.getUser());
        boolean isRestricted = restrictedAccess != null;

        if (isRestricted) {
            User author = this.instance.getJda().getUserById(restrictedAccess.getAuthor().getId());

            event.getInteraction().replyEmbeds(this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.interaction.restricted"), "https://fluffici.eu", ICON_CIRCLE_MINUS.getUrl())
                    .setDescription(this.instance.getLanguageManager().get("common.interaction.restricted.desc", restrictedAccess.getReason()))
                    .setFooter(author.getGlobalName(), author.getAvatarUrl())
                    .setColor(Color.RED)
                    .setTimestamp(restrictedAccess.getCreatedAt().toInstant())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        String buttonId = event.getButton().getId();
        ButtonBuilder buttonBuilder = this.instance.getButtonManager().findByName(buttonId);
        if (buttonBuilder != null) {
            if (buttonId.indexOf('#') != -1) {
                buttonBuilder.getArguments().put("personal", "");
                buttonBuilder.getArguments().put("interactionId", buttonId.split("#")[1]);
            }

            this.instance.getEventBus().post(new ButtonEvent(
                    event,
                    buttonBuilder
            ));

        } else {
            event.reply(this.instance.getLanguageManager().get("button.unknown")).setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {

        // Handling terminated user(s).
        RestrictedAccess restrictedAccess = this.instance.getGameServiceManager().fetchRestrictedPlayer(event.getUser());
        boolean isRestricted = restrictedAccess != null;

        if (isRestricted) {
            User author = this.instance.getJda().getUserById(restrictedAccess.getAuthor().getId());

            event.getInteraction().replyEmbeds(this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.interaction.restricted"), "https://fluffici.eu", ICON_CIRCLE_MINUS.getUrl())
                    .setDescription(this.instance.getLanguageManager().get("common.interaction.restricted.desc", restrictedAccess.getReason()))
                    .setFooter(author.getGlobalName(), author.getAvatarUrl())
                    .setColor(Color.RED)
                    .setTimestamp(restrictedAccess.getCreatedAt().toInstant())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        SelectMenu buttonBuilder = this.instance.getButtonManager().findSelectByName(event.getSelectMenu().getId());
        if (buttonBuilder != null) {
            buttonBuilder.execute(event.getInteraction());
        } else {
            event.reply(this.instance.getLanguageManager().get("button.unknown")).setEphemeral(true).queue();
        }
    }

    @Subscribe
    public void onButtonEvent(ButtonEvent event) {
        Preconditions.checkNotNull(event.getInteractionEvent(), "Command interaction cannot be null.");
        Preconditions.checkNotNull(event.getCommandHandle(), "Command handle cannot be null.");

        event.getCommandHandle().execute(event.getInteractionEvent());

        // Displaying the debug information.
        this.instance.getLogger().debug(
                """
                [%s] button clicked on guild %s: \n
                    -> Member: %s - %s
                    -> Button:
                        -> Name: %s
                        -> Label: %s
                """,
                event.getCommandHandle().getClass().getCanonicalName(),
                event.getInteractionEvent().getGuild().getId(),
                event.getInteractionEvent().getMember().getId(),
                event.getInteractionEvent().getMember().getEffectiveName(),
                event.getCommandHandle().getCustomId(),
                event.getCommandHandle().getLabel()
        );
    }
}
