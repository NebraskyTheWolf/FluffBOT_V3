package eu.fluffici.bot.events;

/*
---------------------------------------------------------------------------------
File Name : EventManager.java

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
import eu.fluffici.bot.events.application.ErrorStateListener;
import eu.fluffici.bot.events.application.ReadyStateListener;
import eu.fluffici.bot.events.guild.GuildAddListener;
import eu.fluffici.bot.events.guild.GuildLoggingListener;
import eu.fluffici.bot.events.guild.GuildUserJoinListener;
import eu.fluffici.bot.events.guild.GuildUserLeftListener;
import eu.fluffici.bot.events.interaction.ButtonInteractionListener;
import eu.fluffici.bot.events.interaction.ContextMenuListeners;
import eu.fluffici.bot.events.interaction.ModalInteractionListener;
import eu.fluffici.bot.events.interaction.SlashCommandListener;
import eu.fluffici.bot.events.user.*;

public class EventManager {
    public void register(FluffBOT instance) {
        // Application
        instance.getJda().addEventListener(new ErrorStateListener());

        ReadyStateListener readyStateListener = new ReadyStateListener(instance);
        instance.getJda().addEventListener(readyStateListener);
        instance.getEventBus().register(readyStateListener);

        // Guild
        instance.getJda().addEventListener(new GuildUserJoinListener(instance));
        instance.getJda().addEventListener(new GuildUserLeftListener(instance));
        instance.getJda().addEventListener(new MemberMessageListener(instance));
        instance.getJda().addEventListener(new MemberReactionListener(instance));
        instance.getJda().addEventListener(new MemberBoostListener(instance));
        instance.getJda().addEventListener(new GuildAddListener(instance));
        instance.getJda().addEventListener(new MemberVoiceChatListener());

        // Interactions
        instance.getJda().addEventListener(new ContextMenuListeners(instance));

        ModalInteractionListener modalInteractionListener = new ModalInteractionListener(instance);
        instance.getJda().addEventListener(modalInteractionListener);
        instance.getEventBus().register(modalInteractionListener);

        ButtonInteractionListener buttonInteractionListener = new ButtonInteractionListener(instance);
        instance.getJda().addEventListener(buttonInteractionListener);
        instance.getEventBus().register(buttonInteractionListener);

        SlashCommandListener slashCommandListener = new SlashCommandListener(instance);
        instance.getJda().addEventListener(slashCommandListener);
        instance.getEventBus().register(slashCommandListener);


        // Logging

        GuildLoggingListener guildLoggingListener = new GuildLoggingListener(instance);
        instance.getJda().addEventListener(guildLoggingListener);
        instance.getEventBus().register(guildLoggingListener);

        instance.getEventBus().register(new FetchedMessageEvent());
    }
}
