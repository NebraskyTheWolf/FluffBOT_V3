package eu.fluffici.bot.events.interaction;

/*
---------------------------------------------------------------------------------
File Name : ContextMenuListeners.java

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
import eu.fluffici.bot.api.beans.players.RestrictedAccess;
import eu.fluffici.bot.api.interactions.Context;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_MINUS;

public class ContextMenuListeners extends ListenerAdapter {

    private final FluffBOT instance;

    public ContextMenuListeners(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;

        Context<UserContextInteraction> interactionContext = this.instance.getContextManager().findByName(event.getName());
        if (interactionContext != null) {
            CompletableFuture.runAsync(() -> {
                interactionContext.execute(event.getInteraction());
            });
        } else {
            event.reply(this.instance.getLanguageManager().get("command.unknown")).setEphemeral(true).queue();
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        if (this.instance.getIsLoading()) {
            event.reply(this.instance.getLanguageManager().get("system.loading")).setEphemeral(true).queue();
            return;
        }

        // Handling terminated user(s).
        RestrictedAccess restrictedAccess = this.instance.getGameServiceManager().fetchRestrictedPlayer(event.getUser());
        boolean isRestricted = restrictedAccess != null;

        if (isRestricted) {
            User author = this.instance.getJda().getUserById(restrictedAccess.getAuthor().getId());

            event.getInteraction().replyEmbeds(this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.interaction.restricted"), "https://fluffici.eu", ICON_CIRCLE_MINUS)
                    .setDescription(this.instance.getLanguageManager().get("common.interaction.restricted.desc", restrictedAccess.getReason()))
                    .setFooter(author.getGlobalName(), author.getAvatarUrl())
                    .setColor(Color.RED)
                    .setTimestamp(restrictedAccess.getCreatedAt().toInstant())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        Context<MessageContextInteraction> interactionContext = this.instance.getContextManager().findByName(event.getName());
        if (interactionContext != null) {
            interactionContext.execute(event.getInteraction());
        } else {
            event.reply(this.instance.getLanguageManager().get("command.unknown")).setEphemeral(true).queue();
        }
    }
}
