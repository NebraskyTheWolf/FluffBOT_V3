package eu.fluffici.bot.events.interaction;

/*
---------------------------------------------------------------------------------
File Name : ModalInteractionListener.java

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
import eu.fluffici.bot.api.events.ModalEvent;
import eu.fluffici.bot.api.interactions.Modal;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_MINUS;

public class ModalInteractionListener extends ListenerAdapter {

    private final FluffBOT instance;

    public ModalInteractionListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
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

        this.instance.getLogger().debug("Receiving modal %s", event.getModalId());

        if (event.getModalId().startsWith("custom:"))
            return;

        Modal modal = this.instance.getModalManager().findByName(event.getModalId());
        if (modal != null) {
            this.instance.getLogger().debug("Receiving modal %s", event.getModalId());

            this.instance.getEventBus().post(new ModalEvent(
                    event,
                    modal
            ));
        }
    }

    @Subscribe
    public void onModalEvent(ModalEvent event) {
        Preconditions.checkNotNull(event.getInteractionEvent(), "Command interaction cannot be null.");
        Preconditions.checkNotNull(event.getCommandHandle(), "Command handle cannot be null.");

        event.getCommandHandle().execute(event.getInteractionEvent());

        this.instance.getLogger().debug(
                """
                [%s] modal clicked on guild %s: \n
                    -> Member: %s - %s
                    -> Modal:
                        -> Name: %s
                """,
                event.getCommandHandle().getClass().getCanonicalName(),
                event.getInteractionEvent().getGuild().getId(),
                event.getInteractionEvent().getMember().getId(),
                event.getInteractionEvent().getMember().getEffectiveName(),
                event.getCommandHandle().getCustomId()
        );
    }
}
