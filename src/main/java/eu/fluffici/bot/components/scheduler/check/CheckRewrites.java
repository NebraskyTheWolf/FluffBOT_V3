package eu.fluffici.bot.components.scheduler.check;

/*
---------------------------------------------------------------------------------
File Name : CheckRewrites.java

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
import eu.fluffici.bot.api.beans.players.EconomyHistory;
import eu.fluffici.bot.api.events.UserTransferEvent;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.Task;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.fluffici.bot.api.IconRegistry.ICON_REPORT_SEARCH;
import static eu.fluffici.bot.api.IconRegistry.ICON_SHIELD_X;

@SuppressWarnings("All")
public class CheckRewrites extends Task {

    private final FluffBOT instance;

    public CheckRewrites(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading CheckRewrites scheduler.");
    }

    /**
     * Executes the anti-cheat checks.
     * <p>
     * 1. The method first checks all balances by calling the checkBalances() method, which retrieves all the transaction records
     *    and calculates the balance for each user in each currency.
     * 2. Then it iterates through all users and their balances. If the calculated balance is greater than the current user balance
     *    for the given currency, it recognizes this as a potential rewrite and set the user's balance to the correct amount.
     * 3. If the potential rewrite is detected, a warning message is logged, the user balance is updated in the database,
     *    and a message about the incident is sent to the specified text channel.
     * 4. The method continues this check every 10 minutes.
     * <p>
     * Note: This method is scheduled to run at a fixed rate, therefore it will wait for 10 minutes after the end of each execution
     *       before starting the next one.
     */
    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            this.instance.getLogger().debug("Running Anti-Cheat checks.");
            this.instance.getLogger().debug(" -> Looking for potential un-allowed database rewrite(s).");
            this.instance.getLogger().debug(" -> Checking for rewrites in user profiles.");

            CompletableFuture.runAsync(() -> {
                AtomicInteger count = new AtomicInteger(0);

                Map<String, Map<EconomyHistory.Currency, Integer>> balances = this.checkBalances();

                for (Map.Entry<String, Map<EconomyHistory.Currency, Integer>> entry : balances.entrySet()) {
                    String userId = entry.getKey();
                    Map<EconomyHistory.Currency, Integer> userBalance = entry.getValue();

                    for (Map.Entry<EconomyHistory.Currency, Integer> balanceEntry : userBalance.entrySet()) {
                        EconomyHistory.Currency currency = balanceEntry.getKey();
                        int balance = balanceEntry.getValue();

                        try {
                            PlayerBean playerBean = this.instance.getGameServiceManager().getPlayerAsync(userId).join();
                            User user = this.instance.getJda().getUserById(userId);
                            if (user == null) {
                                this.instance.getLogger().warn("User not found for user %s", userId);
                                continue;
                            }
                            if (playerBean == null) {
                                this.instance.getLogger().warn("Player not found for user %s", userId);
                                continue;
                            }

                            long playerCurrencyBalance = switch (currency) {
                                case COINS -> playerBean.getCoins();
                                case UPVOTE -> playerBean.getUpvote();
                                case TOKEN -> playerBean.getTokens();
                                case KARMA -> playerBean.getKarma();
                            };

                            if (playerCurrencyBalance > balance) {
                                long amountToDeduct = Math.abs(balance - playerCurrencyBalance);

                                this.instance.getLogger().warn("  -> Potential rewrite detected for user %s. Currency: %s, Balance: %d, Amount : %d",
                                        userId, currency, balance, amountToDeduct);

                                switch (currency) {
                                    case COINS -> playerBean.setCoins(playerBean.getCoins() - amountToDeduct);
                                    case UPVOTE -> playerBean.setUpvote(playerBean.getUpvote() - amountToDeduct);
                                    case TOKEN -> playerBean.setTokens(playerBean.getTokens() - amountToDeduct);
                                    case KARMA -> playerBean.setKarma(playerBean.getKarma() - amountToDeduct);
                                }
                                ;

                                List<UserTransferEvent> transferEvents = this.fetchTransfers(user);
                                if (transferEvents.isEmpty()) {
                                    this.instance.getLogger().debug("No transfers was initiated.");
                                } else {
                                    transferEvents.forEach(event -> {
                                        switch (event.getCurrencyType()) {
                                            case COINS -> event.getTargetPlayer().setCoins(event.getTargetPlayer().getCoins() - event.getAmount());
                                            case UPVOTE -> event.getTargetPlayer().setUpvote(event.getTargetPlayer().getUpvote() - event.getAmount());
                                            case TOKENS -> event.getTargetPlayer().setTokens(event.getTargetPlayer().getTokens() - event.getAmount());
                                            case KARMA -> event.getTargetPlayer().setKarma(event.getTargetPlayer().getKarma() - event.getAmount());
                                        }
                                        ;

                                        try {
                                            this.instance.getGameServiceManager().updatePlayer(playerBean);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        this.instance.getLogger().debug("%s transfer rolled-back due to cheating for %s issuer %s.",
                                                event.getTransferId(), event.getTargetPlayer().getUserId(), event.getPlayer().getUserId()
                                        );
                                    });
                                }

                                List<String> rolledBack = transferEvents.stream()
                                        .filter(event -> event.getAmount() > 0)
                                        .map(UserTransferEvent::getTransferId)
                                        .toList();
                                String joinedString = String.join("\n", rolledBack);

                                this.instance.getGameServiceManager().updatePlayer(playerBean);
                                this.instance.getTransferEventMap().remove(user);

                                EmbedBuilder embedBuilder = this.instance.getEmbed()
                                        .simpleAuthoredEmbed()
                                        .setAuthor("Anti-Cheat", "https://fluffici.eu", ICON_SHIELD_X)
                                        .setTitle("Byly zjištěny přepisové anomálie.")
                                        .setDescription(String.format("Zjistili jsme anomálii v zůstatcích <@%s>.", userId))
                                        .addField("Měna", currency.name(), true)
                                        .addField("Zůstatek", String.valueOf(balance), true)
                                        .addField("Problémová váha", String.valueOf(Math.abs(amountToDeduct - balance)), true)
                                        .setTimestamp(Instant.now())
                                        .setFooter(String.format("Zůstatek automaticky snížen o %s", Math.abs(amountToDeduct - balance)), ICON_REPORT_SEARCH);

                                if (!rolledBack.isEmpty())
                                    embedBuilder.addField("Vrácené transakce", joinedString, false);

                                this.instance.getJda()
                                        .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.logging"))
                                        .sendMessageEmbeds(embedBuilder.build())
                                        .queue();

                                count.incrementAndGet();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (count.get() <= 0) {
                    this.instance.getLogger().debug("No potential rewrites detected.");
                }
            }).exceptionally(ex -> {
                this.instance.getLogger().error("A error occurred while running the anti-cheat checks.", ex);
                return null;
            }).whenCompleteAsync((unused, throwable) -> {
                this.instance.getLogger().debug("The anti-cheat was running successfully. Next run in 60 minutes.");
            });
        }, 5, 60, TimeUnit.MINUTES);
    }

    /**
     * Retrieves the balances of all users in the economy system.
     *
     * @return A map containing user balances. The keys are user IDs and the values are maps of currency to balance.
     */
    @SneakyThrows
    public Map<String, Map<EconomyHistory.Currency, Integer>> checkBalances() {
        Map<String, Map<EconomyHistory.Currency, Integer>> userBalances = new HashMap<>();

        CompletableFuture.runAsync(() -> {
            try {
                for (EconomyHistory transaction : this.instance.getGameServiceManager().getAllEconomyRecord()) {
                    String userId = transaction.getUserId();
                    EconomyHistory.Currency currency = transaction.getCurrency();

                    int amount = transaction.getAmount();
                    EconomyHistory.Operation operation = transaction.getOperation();

                    // If user doesn't exist in map, add them
                    userBalances.putIfAbsent(userId, new HashMap<>());

                    int currentBalance = userBalances.get(userId).getOrDefault(currency, 0);

                    if (operation == EconomyHistory.Operation.CREDIT) {
                        currentBalance += amount;
                    } else if (operation == EconomyHistory.Operation.DEBIT) {
                        currentBalance -= amount;
                    }

                    userBalances.get(userId).put(currency, currentBalance);
                }
            } catch (Exception e) {
                this.instance.getLogger().error("A error occurred while fetching the balances.", e);
                e.printStackTrace();
            }
        });

        return userBalances;
    }

    public List<UserTransferEvent> fetchTransfers(UserSnowflake user) {
        List<UserTransferEvent> transferEvents = new ArrayList<>();

        CompletableFuture.runAsync(() -> this.instance.getTransferEventMap().forEach(((userSnowflake, event) -> {
            if (userSnowflake.getId().equals(user.getId()))
                transferEvents.add(event);
        })));

        return transferEvents;
    }
}
