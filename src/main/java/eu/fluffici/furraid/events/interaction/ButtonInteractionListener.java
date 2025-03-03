package eu.fluffici.furraid.events.interaction;

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
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.events.ButtonEvent;
import eu.fluffici.bot.api.interactions.ButtonBuilder;
import eu.fluffici.bot.api.interactions.SelectMenu;
import eu.fluffici.furraid.FurRaidDB;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonInteractionListener extends ListenerAdapter {

    private final FurRaidDB instance;

    public ButtonInteractionListener(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(event.getGuild());
        if (guildSettings.isBlacklisted())
            return;

        if (event.getComponentId().startsWith("button:"))
            return;

        String buttonId = event.getButton().getId();
        ButtonBuilder buttonBuilder = this.instance.getButtonManager().findByName(buttonId);

        this.instance.getEventBus().post(new ButtonEvent(event, buttonBuilder));
    }

    @Override
    public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {
        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(event.getGuild());
        if (guildSettings.isBlacklisted())
            return;

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
    }
}
