package eu.fluffici.bot.components.scheduler.user;

/*
---------------------------------------------------------------------------------
File Name : UpdateStatistics.java

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
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
@SuppressWarnings("ALL")
public class UpdateStatistics extends Task {

    private final FluffBOT instance;

    public UpdateStatistics(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading UpdateStatistics scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                this.instance.getJda().getUsers()
                        .stream()
                        .filter(user -> !user.isBot())
                        .filter(user -> !user.isSystem())
                        .filter(user -> this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild")).getMember(user) != null)
                        .map(user -> CompletableFuture.runAsync(() -> {
                            PlayerBean userHandle = this.instance.getUserManager().fetchUser(user);
                            if (userHandle != null) {
                                try {
                                    int messageSize = this.instance.getGameServiceManager().sumAll()
                                            .stream()
                                            .filter(message -> message.getUserId().equals(user.getId()))
                                            .toList().size();

                                    boolean hasMessages = this.instance.getGameServiceManager().hasStatistics(user, "messages");
                                    boolean hasTokens = this.instance.getGameServiceManager().hasStatistics(user, "tokens");
                                    boolean hasCoins = this.instance.getGameServiceManager().hasStatistics(user, "coins");
                                    boolean hasKarmas = this.instance.getGameServiceManager().hasStatistics(user, "karmas");
                                    boolean hasUpvotes = this.instance.getGameServiceManager().hasStatistics(user, "upvotes");
                                    boolean hasEvents = this.instance.getGameServiceManager().hasStatistics(user, "events");

                                    if (hasMessages) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "messages", messageSize);
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "messages");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "messages", messageSize);
                                    }

                                    if (hasTokens) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "tokens", userHandle.getTokens());
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "tokens");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "tokens", userHandle.getTokens());
                                    }

                                    if (hasCoins) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "coins", userHandle.getCoins());
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "coins");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "coins", userHandle.getCoins());
                                    }

                                    if (hasKarmas) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "karmas", userHandle.getKarma());
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "karmas");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "karmas", userHandle.getKarma());
                                    }

                                    if (hasUpvotes) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "upvotes", userHandle.getUpvote());
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "upvotes");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "upvotes", userHandle.getUpvote());
                                    }

                                    if (hasEvents) {
                                        this.instance.getGameServiceManager().setUserStatistics(user, "events", userHandle.getEvents());
                                    } else {
                                        this.instance.getGameServiceManager().createUserStatistics(user, "events");
                                        this.instance.getGameServiceManager().setUserStatistics(user, "events", userHandle.getEvents());
                                    }
                                } catch (Exception e) {
                                    this.instance.getLogger().error("Error while updating the statistics.", e);
                                    e.printStackTrace();
                                }
                            }
                        })).forEach(CompletableFuture::join);
            } catch (Exception e) {
                this.instance.getLogger().error("Error while updating the statistics.", e);
                e.printStackTrace();
            }
        }, 10, 190, TimeUnit.SECONDS);
    }
}
