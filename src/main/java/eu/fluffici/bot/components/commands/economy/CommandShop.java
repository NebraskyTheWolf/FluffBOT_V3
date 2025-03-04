package eu.fluffici.bot.components.commands.economy;

/*
---------------------------------------------------------------------------------
File Name : CommandShop.java

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
import eu.fluffici.bot.api.beans.players.PlayerProfile;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.item.InventoryStatus;
import eu.fluffici.bot.components.button.shop.PurchaseHandler;
import eu.fluffici.bot.components.button.shop.impl.OperationType;
import eu.fluffici.bot.components.button.shop.impl.Purchase;
import eu.fluffici.bot.components.button.shop.impl.PurchaseCallback;
import eu.fluffici.bot.components.commands.profile.CommandProfile;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_BOOK;

@CommandHandle
public class CommandShop extends Command {
    public CommandShop() {
        super("shop", "Shop command", CommandCategory.ECONOMY);

        this.getOptions().put("guildRestricted", true);
        this.getOptions().put("channelRestricted", true);

        this.getSubcommandData().add(new SubcommandData("sell", "Sell your items")
                .addOption(OptionType.STRING, "item-name", "Select a item to sell from your inventory.", true, true)
                .addOption(OptionType.INTEGER, "quantity", "Enter the quantity to sell", true)
        );
        this.getSubcommandData().add(new SubcommandData("purchase", "Purchase items from the shop")
                .addOption(OptionType.STRING, "item-name", "Select a item to buy from the shop.", true, true)
                .addOption(OptionType.INTEGER, "quantity", "Enter the quantity your wish to purchase.", true)
        );

        this.getSubcommandGroupData().add(new SubcommandGroupData("profile", "Commands for browsing and managing skins in the profile")
                .addSubcommands(new SubcommandData("preview", "Preview a skin before purchasing")
                        .addOption(OptionType.STRING, "skin-name", "The identifier for the skin to preview", true, true)
                )
                .addSubcommands(new SubcommandData("purchase", "Purchase a skin")
                        .addOption(OptionType.STRING, "skin-name", "The identifier for the skin to purchase", true, true)
                )
                .addSubcommands(new SubcommandData("select", "Select a purchased skin for use")
                        .addOption(OptionType.STRING, "skin-name", "The identifier for the skin to select", true, true)
                )
        );

        this.getSubcommandData().add(new SubcommandData("home", "Display the main page of the shop."));
    }

    @Override
    @SuppressWarnings("ALL")
    public void execute(CommandInteraction interaction) {
        PlayerBean player = this.getUserManager().fetchUser(interaction.getMember());

        switch (interaction.getSubcommandGroup()) {
            case "profile" -> {
                switch (interaction.getSubcommandName()) {
                    case "select" -> this.handleProfileSelection(interaction, player);
                    case "preview" -> this.handleProfilePreview(interaction, player);
                    case "purchase" -> this.handleProfilePurchase(interaction, player);
                }
            }
            default -> {
                switch (interaction.getSubcommandName()) {
                    case "sell" -> this.handleSell(interaction, player);
                    case "purchase" -> this.handlePurchase(interaction, player);
                    case "home" -> this.handleShop(interaction, player);
                }
            }
        }
    }

    private void handleProfileSelection(CommandInteraction interaction, PlayerBean player) {

    }

    private void handleProfilePurchase(CommandInteraction interaction, PlayerBean player) {

    }

    @SneakyThrows
    private void handleProfilePreview(@NotNull CommandInteraction interaction, PlayerBean currentPlayer) {
        User currentUser = interaction.getUser();

        String slug = interaction.getOption("skin-name").getAsString();

        ItemDescriptionBean skin = FluffBOT.getInstance().getGameServiceManager().fetchInternalItem(slug);

        if (skin == null) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.shop.profile.preview.error.skin-not-found")
            )).setEphemeral(true).queue();
            return;
        }

        if (currentPlayer == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.unknown"))).queue();
            return;
        }

        currentPlayer.setPlayerProfile(new PlayerProfile(
                skin.getItemId(),
                skin,
                "",
                true
        ));

        if (FluffBOT.getInstance().getCommandManager().findByName("profile") instanceof CommandProfile commandProfile) {
            String requestId = GameId.generateId();

            CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> commandProfile.handleProfile(currentPlayer, currentUser, requestId, true));
            generatedProfile.whenComplete((fileUpload, throwable) -> interaction.replyEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(getLanguageManager().get("command.shop.profile.preview.title", currentUser.getEffectiveName()), "https://fluffici.eu", ICON_BOOK.getUrl())
                    .setImage("attachment://".concat(requestId.concat("_profile.png")))
                    .build()
            ).setContent("-# This profile is a preview of the skin ".concat(skin.getItemName()).concat(" • [Learn More](https://wiki.fluffici.eu)")).addFiles(fileUpload).setEphemeral(true).queue()).exceptionally(e -> {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.failed"))).queue();
                FluffBOT.getInstance().getLogger().error("A error occurred while generating a profile.", e);
                e.printStackTrace();
                return null;
            });

            if (generatedProfile.isCancelled()
                    || generatedProfile.state() == Future.State.FAILED
                    || generatedProfile.state() == Future.State.CANCELLED) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.failed"))).queue();
            }
        }
    }

    /**
     * Handles the sell of an item by a player.
     *
     * @param interaction The command interaction.
     * @param player      The player information.
     */
    @SneakyThrows
    private void handleSell(CommandInteraction interaction, PlayerBean player) {
        ItemDescriptionBean item = FluffBOT.getInstance().getGameServiceManager().fetchItem(interaction.getOption("item-name").getAsString());
        int quantity = Math.abs(interaction.getOption("quantity").getAsInt());

        if (item != null) {
            PurchaseHandler.handlePurchase(interaction, OperationType.SELL, item, quantity, new PurchaseCallback() {
                @Override
                public void cancelled(ButtonInteraction interaction) {
                    interaction.replyEmbeds(buildError(getLanguageManager().get("command.shop.sell.cancelled", item.getItemName()))).setEphemeral(true).queue();
                }
                @Override
                public void execute(ButtonInteraction interaction, Purchase purchase) {
                    Pair<Boolean, String> result = FluffBOT.getInstance().getGameServiceManager().decrementQuantity(
                            interaction.getUser(),
                            item,
                            quantity
                    );

                    if (result.getLeft()) {
                        getUserManager().addTokens(player, purchase.getPrice());
                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.shop.sell.success", item.getItemName(), NumberFormat.getNumberInstance().format(quantity), NumberFormat.getNumberInstance().format(purchase.getPrice())))).setEphemeral(true).queue();
                    } else {
                        interaction.replyEmbeds(buildError(result.getRight())).queue();
                    }
                }

                @Override
                public void error(String message) {
                   Message messages = interaction.getHook().sendMessageEmbeds(buildError(message)).setContent(interaction.getUser().getAsMention()).complete();
                   FluffBOT.getInstance().getScheduledExecutorService().schedule(() -> messages.delete().queue(), 10, TimeUnit.SECONDS);
                }
            });
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.shop.sell.not_found"))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the purchase of an item by a player.
     *
     * @param interaction The command interaction.
     * @param player      The player information.
     */
    @SneakyThrows
    private void handlePurchase(CommandInteraction interaction, PlayerBean player) {
        ItemDescriptionBean item = FluffBOT.getInstance().getGameServiceManager().fetchItem(interaction.getOption("item-name").getAsString());
        int quantity = Math.abs(interaction.getOption("quantity").getAsInt());

        PurchaseHandler.handlePurchase(interaction, OperationType.PURCHASE, item, quantity, new PurchaseCallback() {
            @Override
            public void cancelled(ButtonInteraction interaction) {
                interaction.replyEmbeds(buildError(getLanguageManager().get("command.shop.purchase.cancelled", item.getItemName()))).setEphemeral(true).queue();
            }
            @Override
            public void execute(ButtonInteraction interaction, Purchase purchase) {
               try {
                   Pair<Boolean, InventoryStatus> result = getUserManager().addItem(interaction.getUser(), purchase.getItem(), purchase.getQuantity());

                   if (result.getLeft()) {
                       getUserManager().removeTokens(player, purchase.getPrice());
                       interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.shop.purchase.success", item.getItemName(), NumberFormat.getNumberInstance().format(quantity), NumberFormat.getNumberInstance().format(purchase.getPrice())))).setEphemeral(true).queue();
                   } else {
                       if (result.getRight() == InventoryStatus.FULL) {
                           interaction.replyEmbeds(buildError("command.shop.purchase.inventory_full")).setEphemeral(true).queue();
                       }
                   }
               } catch (Exception e) {
                   e.printStackTrace();
                   throw e;
               }
            }

            @Override
            public void error(String message) {
                Message messages = ((TextChannel)interaction.getChannel()).sendMessageEmbeds(buildError(message)).setContent(interaction.getUser().getAsMention()).complete();
                FluffBOT.getInstance().getScheduledExecutorService().schedule(() -> messages.delete().queue(), 10, TimeUnit.SECONDS);
            }
        });

    }

    /**
     * Handles the execution of the shop command.
     *
     * @param interaction The command interaction.
     * @param player The player information.
     */
    private void handleShop(CommandInteraction interaction, PlayerBean player) {}

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.getSubcommandName() != null) {
            switch (event.getSubcommandName()) {
                case "sell" -> {
                    try {
                        List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance().getGameServiceManager()
                                .fetchInformation(event.getUser())
                                .getItems()
                                .stream()
                                .filter(item -> item.getEquipmentType() != EquipmentType.PROFILE)
                                .filter(item -> item.getName().toLowerCase().startsWith(userInput))
                                .limit(25)
                                .map(item -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getName(), item.getItemSlug()))
                                .collect(Collectors.toList());

                        event.replyChoices(choices).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case "purchase" -> {
                    try {
                        List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                                .getGameServiceManager()
                                .fetchAllItemsPurchasable()
                                .stream()
                                .filter(item -> item.getItemName().toLowerCase().startsWith(userInput))
                                .limit(25)
                                .map(item -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getItemName(), item.getItemSlug()))
                                .collect(Collectors.toList());

                        event.replyChoices(choices).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (event.getSubcommandGroup() != null) {
            if (event.getSubcommandGroup().equals("profile")) {
                try {
                    List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                            .getGameServiceManager()
                            .fetchAllItemsPurchasable()
                            .stream()
                            .filter((ItemDescriptionBean item) -> item.getEquipmentType() == EquipmentType.PROFILE)
                            .filter((ItemDescriptionBean item) -> item.getItemName().toLowerCase().startsWith(userInput))
                            .limit(25)
                            .map((ItemDescriptionBean item) -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getItemName(), item.getItemSlug()))
                            .collect(Collectors.toList());

                    event.replyChoices(choices).queue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
