package eu.fluffici.bot.components.commands.games;

/*
---------------------------------------------------------------------------------
File Name : CommandCasino.java

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
import eu.fluffici.bot.api.beans.game.CasinoGameBuilder;
import eu.fluffici.bot.api.game.blackjack.PlayerResultBuilder;
import eu.fluffici.bot.api.game.blackjack.PlayerResultGenerator;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;

/**
 * Represents a command that allows users to play various games in the casino.
 * Inherits from the Command class.
 */
@SuppressWarnings("ALL")
public class CommandCasino extends Command {

    /**
     * Represents a command that allows users to play various games in the casino.
     * Inherits from the Command class.
     */
    public CommandCasino() {
        super("casino", "Welcome to the casino! Enjoy roulette, blackjack, slots, and more.", CommandCategory.GAMES);

        this.getOptions().put("noSelfUsage", true);

        this.getSubcommandData().add(new SubcommandData("slots", "Play the slot machine and try your luck to win fluff-tokens")
                .addOption(OptionType.INTEGER, "amount", "The amount of fluff-tokens to bet", true)
        );

        this.getSubcommandData().add(new SubcommandData("blackjack", "Challenge the dealer in a game of blackjack to win fluff-tokens")
                .addOption(OptionType.INTEGER, "amount", "The amount of fluff-tokens to bet", true)
        );

        this.getSubcommandData().add(new SubcommandData("roulette", "Place your bets on the roulette wheel and win fluff-tokens")
                .addOption(OptionType.INTEGER, "amount", "The amount of fluff-tokens to bet", true)
                .addOption(OptionType.INTEGER, "bet", "Your predicted bet number", true)
        );
    }

    /**
     * Executes the casino command based on the given command interaction.
     *
     * @param interaction The CommandInteraction object representing the command interaction.
     */
    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        String command = interaction.getSubcommandName();
        PlayerBean player = this.getUserManager().fetchUser(interaction.getMember());

        switch (command) {
            case "slots" -> this.handleSlots(interaction, player);
            case "blackjack" -> this.handleBlackjack(interaction, player);
            case "roulette" -> this.handleRoulette(interaction, player);
        }
    }

    /**
     * Handles the slots subcommand of the casino command.
     *
     * @param interaction The interaction object representing the command interaction.
     * @param player      The player object representing the player using the command.
     */
    private void handleSlots(@NotNull CommandInteraction interaction, PlayerBean player) {
        int amount = Math.abs(interaction.getOption("amount").getAsInt());

        if (!this.getUserManager().hasEnoughTokens(player, amount)) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.casino.insufficient_tokens")
            )).setEphemeral(true).queue();
            return;
        }

        String[] symbols = {"🍒", "🍋", "🍊", "🍇", "🍉", "💎"};
        Random random = new Random();
        String[] slots = new String[5];

        for (int i = 0; i < 5; i++) {
            slots[i] = symbols[random.nextInt(symbols.length)];
        }

        // Display the result
        String slotResult = String.join(" | ", slots);

        // Calculate the winnings based on the result
        int winnings = calculateWinningsSlots(slots);
        if (winnings > 0) {
            this.getUserManager().addTokens(player, (amount * 2) + (winnings / 2));

            interaction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("command.casino.slot.won"), "https://fluffici.eu", ICON_MEDAL.getUrl())
                            .setDescription(this.getLanguageManager().get("command.casino.slot.result", slotResult))
                            .addField(this.getLanguageManager().get("command.casino.slot.winning"), NumberFormat.getNumberInstance().format(winnings), true)
                    .build()
            ).queue();
        } else {
            interaction.replyEmbeds(this.buildLost(this.getLanguageManager().get("command.casino.slot.no_winning"))).queue();
            this.getUserManager().removeTokens(player, amount);
        }
    }

    /**
     * Handles the blackjack subcommand of the casino command.
     *
     * @param interaction The interaction object representing the command interaction.
     * @param player      The player object representing the player using the command.
     */
    private void handleBlackjack(@NotNull CommandInteraction interaction, PlayerBean player) {
        int amount = Math.abs(interaction.getOption("amount").getAsInt());

        if (!this.getUserManager().hasEnoughTokens(player, amount)) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.casino.insufficient_tokens")
            )).setEphemeral(true).queue();
            return;
        }

        try {
            String gameId = UUID.randomUUID().toString();

            FluffBOT.getInstance().getGameServiceManager().createGameSession(new CasinoGameBuilder(
                    gameId,
                    CasinoGameBuilder.GameStatus.STARTED,
                    interaction.getUser(),
                    0,
                    0
            ));

            List<String> deck = initializeDeck();

            // Shuffle the deck
            shuffleDeck(deck);

            // Deal cards to the player and the dealer
            List<String> playerHand = new ArrayList<>();
            List<String> neonHand = new ArrayList<>();

            dealCard(playerHand, deck);
            dealCard(playerHand, deck);

            dealCard(neonHand, deck);
            dealCard(neonHand, deck);

            // Calculate the initial score for the player and the dealer
            int playerScore = calculateScore(playerHand);
            int dealerScore = calculateScore(neonHand);

            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .updateGameStatus(gameId, CasinoGameBuilder.GameStatus.ONGOING);
            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .updateScores(gameId, playerScore, dealerScore);

            BlackjackButtonListener buttonListener = new BlackjackButtonListener(interaction, gameId, deck, player, playerHand, neonHand, playerScore, dealerScore);
            interaction.getJDA().addEventListener(buttonListener);

            interaction.replyEmbeds(
                    this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("command.casino.blackjack.hit_or_stand"), "https://fluffici.eu", ICON_QUESTION_MARK.getUrl())
                            .setDescription(this.getLanguageManager().get("command.casino.blackjack.hit_or_stand.desc"))
                            .build()
            ).addActionRow(
                    Button.primary("button:hit_".concat(interaction.getUser().getId()), this.getLanguageManager().get("command.casino.blackjack.hit")),
                    Button.secondary("button:stand_".concat(interaction.getUser().getId()), this.getLanguageManager().get("command.casino.blackjack.stand")),
                    Button.success("button:rules", this.getLanguageManager().get("command.casino.blackjack.rules"))
            ).setEphemeral(true).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the roulette subcommand of the casino command.
     *
     * @param interaction The interaction object representing the command interaction.
     * @param player      The player object representing the player using the command.
     */
    private void handleRoulette(@NotNull CommandInteraction interaction, PlayerBean player) {
        int betAmount = Math.abs(interaction.getOption("amount").getAsInt());
        int bet = Math.abs(interaction.getOption("bet").getAsInt());

        if (betAmount <= 0 || bet <= 0) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.casino.roulette.invalid_bet"))).setEphemeral(true).queue();
            return;
        }

        if (!this.getUserManager().hasEnoughTokens(player, betAmount)) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.casino.insufficient_tokens")
            )).setEphemeral(true).queue();
            return;
        }

        int winningNumber = new Random().nextInt(37);
        boolean isRed = isRedNumber(winningNumber);

        int payout;
        if (isRed && isRedNumber(bet)) {
            payout = betAmount * 2;
        } else if (!isRed && isBlackNumber(bet)) {
            payout = betAmount * 2;
        } else if (bet == winningNumber) {
            payout = betAmount * 36; // Payout for exact number match is 36 times the bet
        } else {
            payout = 0;
        }

        if (payout > 0) {
            this.getUserManager().addTokens(player, payout);
        } else {
            this.getUserManager().removeTokens(player, betAmount);
        }

        interaction.replyEmbeds(this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("command.casino.roulette.result"), "https://fluffici.eu", isRed ? ICON_MEDAL.getUrl() : ICON_ALERT.getUrl())
                .setDescription(this.getLanguageManager().get("command.casino.roulette.result.desc", winningNumber, isRed ? "Red" : "Black"))
                .addField(this.getLanguageManager().get("command.casino.roulette.payout"), NumberFormat.getNumberInstance().format(payout), true)
                .build()
        ).queue();

    }

    /**
     * Calculates the winnings for a given set of slots.
     *
     * @param slots An array of strings representing the slots. Each element corresponds to a slot position.
     * @return The calculated winnings based on the slots configuration. Returns 0 if there is no winning combination.
     */
    private int calculateWinningsSlots(@NotNull String[] slots) {
        Map<String, Integer> payouts = new HashMap<>();
        payouts.put("🍒", 10);
        payouts.put("🍋", 20);
        payouts.put("🍊", 30);
        payouts.put("🍇", 40);
        payouts.put("🍉", 50);
        payouts.put("💎", 100);

        if (slots[0].equals(slots[1]) && slots[1].equals(slots[2])) {
            return payouts.get(slots[0]);
        }

        if (slots[0].equals(slots[1]) || slots[1].equals(slots[2]) || slots[0].equals(slots[2])) {
            String matchingSymbol = slots[0].equals(slots[1]) ? slots[0] : slots[2];
            return payouts.get(matchingSymbol) / 2;
        }

        if (slots[0].equals("🍒") && slots[1].equals("💎") && slots[2].equals("🍒") && slots[3].equals("💎")) {
            return 1000;
        }

        return 0;
    }

    /**
     * Initializes a new deck of cards for the game.
     *
     * @return A List of Strings representing the deck of cards.
     */
    // BLACKJACK
    @NotNull
    private List<String> initializeDeck() {
        List<String> deck = new ArrayList<>();
        String[] suits = {"♠", "♣", "♥", "♦"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + suit);
            }
        }
        return deck;
    }

    /**
     * Shuffles the given deck of cards using the Fisher-Yates algorithm.
     *
     * @param deck The List of Strings representing the deck of cards to be shuffled.
     */
    private void shuffleDeck(@NotNull List<String> deck) {
        Random random = new Random();
        for (int i = deck.size() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            String temp = deck.get(index);
            deck.set(index, deck.get(i));
            deck.set(i, temp);
        }
    }

    /**
     * Method to deal a card from the deck to a player's hand.
     *
     * @param hand The List of Strings representing the player's hand.
     * @param deck The List of Strings representing the deck of cards.
     */
    // Method to deal a card from the deck to a player's hand
    private static void dealCard(@NotNull List<String> hand, @NotNull List<String> deck) {
        hand.add(deck.remove(0));
    }

    /**
     * Method to calculate the score of a hand in blackjack.
     *
     * @param hand The List of Strings representing the player's hand.
     * @return The calculated score of the hand.
     */
    // Method to calculate the score of a hand in blackjack
    private static int calculateScore(@NotNull List<String> hand) {
        int score = 0;
        int numAces = 0;

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);
            switch (rank) {
                case "J":
                case "Q":
                case "K":
                    score += 10;
                    break;
                case "A":
                    score += 11; // Assume initially Aces count as 11
                    numAces++;
                    break;
                default:
                    score += Integer.parseInt(rank);
            }
        }

        // Adjust score for Aces if needed
        while (score > 21 && numAces > 0) {
            score -= 10; // Change value of Ace from 11 to 1
            numAces--;
        }

        return score;
    }

    public static class BlackjackButtonListener extends ListenerAdapter {
        private final CommandInteraction interaction;
        private final PlayerBean player;
        private final List<String> deck;
        private final List<String> playerHand;
        private final List<String> dealerHand;
        private final int playerScore;
        private final int dealerScore;
        private final String gameId;

        public BlackjackButtonListener(CommandInteraction interaction, String gameId, List<String> deck, PlayerBean player, List<String> playerHand, List<String> dealerHand, int playerScore, int dealerScore) {
            this.interaction = interaction;
            this.player = player;
            this.deck = deck;
            this.playerHand = playerHand;
            this.dealerHand = dealerHand;
            this.playerScore = playerScore;
            this.dealerScore = dealerScore;
            this.gameId = gameId;
        }

        /**
         * Handles a button interaction event.
         *
         * @param event The ButtonInteractionEvent object representing the button interaction event.
         */
        @Override
        public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
            if (!event.getUser().getId().equals(interaction.getUser().getId())) {
                return;
            }

            String buttonid = event.getButton().getId();

            if (buttonid.equals("button:rules")) {
                event.getInteraction().replyEmbeds(FluffBOT.getInstance().getEmbed()
                        .simpleAuthoredEmbed()
                                .setAuthor("Blackjack Rules", "https://fluffici.eu", ICON_NOTE.getUrl())
                                .setDescription(
                                        """     
                                        - **Objective**: The goal is to beat the dealer's hand without going over 21.
                                             - **Card Values**: Number cards are worth their face value, face cards (Jacks, Queens, and Kings) are worth 10, and Aces can be worth 1 or 11.
                                        - **Gameplay**:\s
                                             - The player and Neon are each dealt two cards.
                                             - The player's cards are dealt face up, while one of the Neon's cards is face up and the other is face down.
                                             - The player can choose to Hit (receive another card) or Stand (keep their current hand).
                                             - If the player's total exceeds 21, they bust and lose the round.
                                        - **Neon's Turn**:\s
                                             - Once the player stands, Neon reveals their face-down card.
                                             - The dealer must Hit until their hand reaches at least 17.
                                        - **Winning**:\s
                                             - If the player's hand is closer to 21 than the dealer's without going over, the player wins.
                                             - If the Neon busts or if the player has a natural blackjack (an Ace and a 10-value card), the player wins.
                                             - If the player and Neon have the same total, it's a push (tie), and the player's bet is returned.
                                        - **Payout**:\s
                                             - A winning hand pays out at 1:1, except for a natural blackjack, which typically pays out at 3:2.
                                             - When you win, your bet will be doubled, rewarding you with twice the amount you wagered.
                                        Good luck and enjoy playing blackjack!                                   
                                        """)
                        .build()
                ).setEphemeral(true).queue();
                return;
            }

            int amount = Math.abs(interaction.getOption("amount").getAsInt());

            if (buttonid.equals("button:hit_".concat(interaction.getUser().getId()))) {
                dealCard(playerHand, deck);
                int updatedScore = calculateScore(playerHand);
                resolveBlackjack(event, amount, deck, player, updatedScore, dealerHand, dealerScore);
            } else if (buttonid.equals("button:stand_".concat(interaction.getUser().getId()))) {
                resolveBlackjack(event, amount, deck, player, playerScore, dealerHand, dealerScore);
            }

            Message message = event.getMessage();
            List<ItemComponent> components = message.getActionRows().stream()
                    .map(ActionRow::asDisabled)
                    .flatMap(actionRow -> actionRow.getComponents().stream())
                    .collect(Collectors.toList());

            message.editMessageEmbeds(message.getEmbeds())
                    .setActionRow(components).queue();

            event.getJDA().removeEventListener(this);
        }

        /**
         * Resolve the outcome of a blackjack game.
         *
         * @param interaction The ButtonInteraction that triggered the game.
         * @param amount The amount of tokens bet by the player.
         * @param deck The List of Strings representing the deck of cards.
         * @param player The PlayerBean representing the player.
         * @param playerScore The current score of the player's hand.
         * @param neonHand The List of Strings representing the dealer's hand.
         * @param dealerScore The current score of the dealer's hand.
         */
        private void resolveBlackjack(ButtonInteraction interaction, int amount, List<String> deck, PlayerBean player, int playerScore, List<String> neonHand, int dealerScore) {
            while (dealerScore < 17) {
                dealCard(neonHand, deck);
                dealerScore = calculateScore(neonHand);
            }

            EmbedBuilder embedBuilder = FluffBOT
                    .getInstance()
                    .getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(FluffBOT.getInstance().getLanguageManager().get("command.casino.blackjack.result"), "https://fluffici.eu", ICON_MEDAL.getUrl())
                    .setImage("attachment://".concat(gameId).concat("_result.png"));

            CasinoGameBuilder currentGame = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getGameSession(gameId);

            PlayerResultBuilder playerResultBuilder = new PlayerResultBuilder();
            playerResultBuilder.setCurrentGame(currentGame);
            playerResultBuilder.setPlayer(player);
            playerResultBuilder.setUser(interaction.getUser());
            playerResultBuilder.setPlayerScore(playerScore);
            playerResultBuilder.setNeonScore(dealerScore);
            playerResultBuilder.setPlayerHand(playerHand);
            playerResultBuilder.setNeonHand(neonHand);

            String playerDeck = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getSelectedDeck(UserSnowflake.fromId(player.getUserId()));

            // Selecting the player's deck based on their selection.
            if (playerDeck == null)
                playerResultBuilder.setDeckSlug("classic");
            else
                playerResultBuilder.setDeckSlug(playerDeck);

            if (playerScore > 21) {
                playerResultBuilder.setGameStatus(PlayerResultBuilder.GameStatus.BUSTED);
                FluffBOT.getInstance().getUserManager().removeTokens(player, amount);
                embedBuilder.addField(FluffBOT.getInstance().getLanguageManager().get("command.casino.lost"),
                        FluffBOT.getInstance().getLanguageManager().get("common.value.field", NumberFormat.getNumberInstance().format(-amount)) ,true);
            } else if (playerScore == dealerScore) {
                playerResultBuilder.setGameStatus(PlayerResultBuilder.GameStatus.TIE);
            } else if (dealerScore > 21 || playerScore > dealerScore) {
                int winnings = (int) (amount * 2.5);
                FluffBOT.getInstance().getUserManager().addTokens(player, winnings);

                playerResultBuilder.setGameStatus(PlayerResultBuilder.GameStatus.WIN);
                embedBuilder.addField(FluffBOT.getInstance().getLanguageManager().get("command.casino.win"),
                        FluffBOT.getInstance().getLanguageManager().get("common.value.field", NumberFormat.getNumberInstance().format(winnings)) ,true);
            } else if (dealerScore == 21) {
                playerResultBuilder.setGameStatus(PlayerResultBuilder.GameStatus.LOST);
                FluffBOT.getInstance().getUserManager().removeTokens(player, amount);

                embedBuilder.addField(FluffBOT.getInstance().getLanguageManager().get("command.casino.lost"),
                        FluffBOT.getInstance().getLanguageManager().get("common.value.field", NumberFormat.getNumberInstance().format(-amount)) ,true);
            } else {
                playerResultBuilder.setGameStatus(PlayerResultBuilder.GameStatus.LOST);
                FluffBOT.getInstance().getUserManager().removeTokens(player, amount);

                embedBuilder.addField(FluffBOT.getInstance().getLanguageManager().get("command.casino.lost"),
                        FluffBOT.getInstance().getLanguageManager().get("common.value.field", NumberFormat.getNumberInstance().format(-amount)) ,true);
            }

            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .updateScores(gameId, playerScore, dealerScore);
            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .updateGameStatus(gameId, CasinoGameBuilder.GameStatus.FINISHED);

            PlayerResultGenerator result = new PlayerResultGenerator(FluffBOT.getInstance().getLanguageManager(), playerResultBuilder);

            CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> {
                try {
                    return result.generateResultCard();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            generatedProfile.whenComplete((fileUpload, throwable) -> {
                interaction.replyEmbeds(embedBuilder.build()).addFiles(fileUpload).queue();
            }).exceptionally(e -> {
                interaction.replyEmbeds(this.buildError(FluffBOT.getInstance().getLanguageManager().get("command.blackjack.completable.failed"))).queue();
                FluffBOT.getInstance().getLogger().error("A error occurred while generating a profile.", e);
                e.printStackTrace();
                return null;
            });

            if (generatedProfile.isCancelled()
                    || generatedProfile.state() == Future.State.FAILED
                    || generatedProfile.state() == Future.State.CANCELLED) {
                interaction.replyEmbeds(this.buildError(FluffBOT.getInstance().getLanguageManager().get("command.blackjack.completable.failed"))).queue();
            }
        }

        public MessageEmbed buildError(String description) {
            return FluffBOT.getInstance()
                    .getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.casino.fail"), "https://fluffici.eu", ICON_WARNING.getUrl())
                    .setDescription(description)
                    .setTimestamp(Instant.now())
                    .build();
        }
    }

    /**
     * Determines if a given number is a red number.
     *
     * @param number The number to check.
     * @return true if the number is a red number, false otherwise.
     */
    private boolean isRedNumber(int number) {
        return (number != 0 && number % 2 == 1) || (number == 0);
    }

    /**
     * Determines if a given number is a black number.
     *
     * @param number The number to check.
     * @return {@code true} if the number is a black number, {@code false} otherwise.
     */
    private boolean isBlackNumber(int number) {
        return number != 0 && number % 2 == 0;
    }
    /**
     * Builds a MessageEmbed for a lost casino result.
     *
     * @param description The description to set for the embed.
     * @return A MessageEmbed representing the lost casino result.
     */
    public MessageEmbed buildLost(String description) {
        return FluffBOT.getInstance()
                .getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(FluffBOT.getInstance().getLanguageManager().get("common.casino.fail"), "https://fluffici.eu", ICON_WARNING.getUrl())
                .setDescription(description)
                .setTimestamp(Instant.now())
                .build();
    }
}
