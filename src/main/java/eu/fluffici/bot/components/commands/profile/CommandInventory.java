package eu.fluffici.bot.components.commands.profile;

/*
---------------------------------------------------------------------------------
File Name : CommandInventory.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/06/2024
Last Modified : 07/06/2024

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
import eu.fluffici.bot.api.beans.players.DropItemBuilder;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.item.InventoryStatus;
import eu.fluffici.bot.api.item.ItemFoodBuilder;
import eu.fluffici.bot.api.upgrade.UserUpgradeBuilder;
import eu.fluffici.bot.components.button.accept.AcceptCallback;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.bot.components.button.paginate.PageBuilder;
import eu.fluffici.bot.components.button.paginate.PaginationBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;
import static eu.fluffici.bot.api.game.GraphicsAPI.makeRoundedCorner;
import static eu.fluffici.bot.components.button.accept.AcceptHandler.handleAcceptance;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.button.paginate.PaginationHandler.handlePagination;

@CommandHandle
@SuppressWarnings("ALL")
public class CommandInventory extends Command {

    private static final int SPECIAL_SLOT_WIDTH = 77;
    private static final int SPECIAL_SLOT_HEIGHT = 68;

    // SLOTS COMMON VALUES
    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 3;
    // INVENTORY VALUES
    private static final int SLOT_WIDTH = 77;
    private static final int SLOT_HEIGHT = 71;
    private static final int START_X = 30;
    private static final int START_Y = 188;
    private static final int SLOT_MARGIN = 5;

    // UPGRADES VALUES
    private static final int UPGRADE_SLOT_WIDTH = 77;
    private static final int UPGRADE_SLOT_HEIGHT = 68;
    private static final int UPGRADE_START_X = 30;
    private static final int UPGRADE_START_Y = 478;
    private static final int UPGRADE_HORIZONTAL_PADDING = 10;
    private static final int UPGRADE_VERTICAL_PADDING = 10;

    // HEALTH BARS
    private static final int BAR_WIDTH = 30;
    private static final int BAR_HEIGHT = 274;
    private static final int HEALTH_X = 545;
    private static final int PROTECTION_X = 581;
    private static final int HUNGER_X = 617;
    private static final int MANA_X = 655;
    private static final int BARS_Y = 217;

    private Font customFont;
    private Font neon;

    /**
     * A command that represents the beta version of the inventory test command.
     */
    public CommandInventory() {
        super("inventory", "See and manage your inventory", CommandCategory.PROFILE);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
        this.getOptions().put("noSelfUser", true);

        try {
            InputStream is = FluffBOT.getInstance().getClass().getResourceAsStream("/fonts/lexend.ttf");
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, is);

            InputStream is1 = FluffBOT.getInstance().getClass().getResourceAsStream("/fonts/neon.otf");
            this.neon = Font.createFont(Font.TRUETYPE_FONT, is1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.getSubcommandData().add(new SubcommandData("open", "Open your inventory"));
        this.getSubcommandData().add(new SubcommandData("eat", "Eat one of your food item.")
                .addOption(OptionType.STRING, "item-name", "The item you want to eat.", true, true)
                .addOption(OptionType.INTEGER, "quantity", "The amount of the item to eat.", true)
        );

        this.getSubcommandGroupData().add(new SubcommandGroupData("items", "Manage your equipment")
                .addSubcommands(new SubcommandData("drop-item", "Drop items out of your inventory")
                        .addOption(OptionType.STRING, "item-name", "Select the item you want to drop.", true, true)
                        .addOption(OptionType.INTEGER, "quantity", "The quantity you want to drop.", true)
                )
                .addSubcommands(new SubcommandData("donate-item", "Donate one of your item to someone else.")
                        .addOption(OptionType.STRING, "item-name", "Select the item you want to donate.", true, true)
                        .addOption(OptionType.INTEGER, "quantity", "The quantity you want to donate.", true)
                        .addOption(OptionType.USER, "user", "Select the user you want to donate.", true)
                )
                .addSubcommands(new SubcommandData("item-info", "Get information about one of your item or equipment")
                        .addOption(OptionType.STRING, "item-name", "Select the item you want to look at.", true, true)
                )
        );

        this.getSubcommandGroupData().add(new SubcommandGroupData("equipments", "Manage your equipment")
                .addSubcommands(new SubcommandData("list-equipment", "List all your non-displayed equipment."))
                .addSubcommands(new SubcommandData("select-equipment", "Equip a piece of equipment you have in inventory.")
                        .addOption(OptionType.STRING, "item-name", "Select the equipment to equip.", true, true)
                )
        );

        this.getSubcommandGroupData().add(new SubcommandGroupData("backpack", "Manage your backpack")
                .addSubcommands(new SubcommandData("open-backpack", "Open your backpack"))
                .addSubcommands(new SubcommandData("purchase-upgrade", "Purchase more slots for your backpack!"))
        );
    }

    @Override
    @SneakyThrows
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.isFromGuild() && event.getSubcommandName() != null) {
            switch (event.getSubcommandName()) {
                case "eat" -> {
                    List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                            .getGameServiceManager()
                            .fetchInformation(event.getUser())
                            .getItems()
                            .stream()
                            .filter(InventoryItem::isEatable)
                            .filter(item -> item.getName().toLowerCase().startsWith(userInput))
                            .limit(25)
                            .map(item -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getName(), item.getItemSlug()))
                            .collect(Collectors.toList());

                    event.replyChoices(choices).queue();
                }
            }
        } else if (event.isFromGuild() && event.getSubcommandGroup() != null) {
            switch (event.getSubcommandGroup()) {
                case "items" -> {
                   switch (event.getSubcommandName()) {
                       case "drop-item", "donate-item" -> {
                           List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                                   .getGameServiceManager()
                                   .fetchInformation(event.getUser())
                                   .getItems()
                                   .stream()
                                   .filter(InventoryItem::isEquipped)
                                   .filter(item -> item.getName().toLowerCase().startsWith(userInput))
                                   .limit(25)
                                   .map(item -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getName(), item.getItemSlug()))
                                   .collect(Collectors.toList());

                           event.replyChoices(choices).queue();
                       }
                       case "item-info" -> {
                           List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                                   .getGameServiceManager()
                                   .fetchInformation(event.getUser())
                                   .getItems()
                                   .stream()
                                   .filter(item -> item.getName().toLowerCase().startsWith(userInput))
                                   .limit(25)
                                   .map(item -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getName(), item.getItemSlug()))
                                   .collect(Collectors.toList());

                           event.replyChoices(choices).queue();
                       }
                   }
                }
                case "equipments" -> {
                    switch (event.getSubcommandName()) {
                        case "select-equipment" -> {
                            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                                    .getGameServiceManager()
                                    .fetchInformation(event.getUser())
                                    .getItems()
                                    .stream()
                                    .filter(InventoryItem::isEquipment)
                                    .filter(InventoryItem::isNotEquipped)
                                    .filter((InventoryItem item) -> item.getName().toLowerCase().startsWith(userInput))
                                    .limit(25)
                                    .map((InventoryItem item) -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(item.getName(), item.getItemSlug()))
                                    .collect(Collectors.toList());

                            event.replyChoices(choices).queue();
                        }
                    }
                }
            }
        }
    }

    /**
     * Executes the command to generate and send the user's inventory.
     *
     * @param interaction The CommandInteraction object representing the user's command interaction.
     */
    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        User currentUser = interaction.getUser();
        PlayerBean currentPlayer =  this.getUserManager().fetchUser(currentUser);

        if (currentPlayer == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.unknown"))).queue();
            return;
        }

        switch (interaction.getSubcommandName()) {
            case "open" -> this.handleInventoryOpen(interaction, currentUser, currentPlayer);
            case "open-backpack" -> this.handleBackpackOpen(interaction, currentUser, currentPlayer);
            case "purchase-upgrade" -> this.handlePurchaseSlots(interaction, currentUser, currentPlayer);
            case "eat" -> this.handleEatItem(interaction, currentUser, currentPlayer);
            // items section
            case "drop-item" -> this.handleDropItem(interaction, currentUser, currentPlayer);
            case "donate-item" -> this.handleDonateItem(interaction, currentUser, currentPlayer);
            case "item-info" -> this.handleItemInfo(interaction, currentUser, currentPlayer);
            // equipments section
            case "list-equipment" -> this.handleEquipmentsList(interaction, currentUser, currentPlayer);
            case "select-equipment" -> this.handleSelectEquipment(interaction, currentUser, currentPlayer);
        }
    }

    private final int SLOT_COST = 1900;
    private final int TOTAL_SLOTS = 91;
    private final int SLOTS_PER_PURCHASE = 4;

    /**
     * Handles the purchase of slots.
     *
     * @param interaction  The command interaction object.
     * @param currentUser  The current user object.
     * @param currentPlayer  The current player bean object.
     */
    private void handlePurchaseSlots(CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        InventoryBuilder inventory = this.getUserManager().fetchInventory(currentUser);
        int maxSlots = inventory.getMetadata().getMaximumSlots();

        if (maxSlots == 91) {
            interaction.replyEmbeds(buildError(this.getLanguageManager().get("command.inventory.backpack.already_fully_unlocked"))).setEphemeral(true).queue();
            return;
        }

        if (!this.getUserManager().hasEnoughTokens(currentPlayer, SLOT_COST)) {
            interaction.replyEmbeds(buildError(this.getLanguageManager().get("command.inventory.backpack.not_enough_tokens"))).setEphemeral(true).queue();
            return;
        }

        int currentMaxSlots = inventory.getMetadata().getMaximumSlots();
        int availableSlots = TOTAL_SLOTS - currentMaxSlots;

        int slotsToPurchase;
        if (availableSlots >= SLOTS_PER_PURCHASE) {
            slotsToPurchase = SLOTS_PER_PURCHASE;
        } else {
            slotsToPurchase = availableSlots;
        }

        int totalPrice = slotsToPurchase * SLOT_COST;

        handleConfirmation(interaction,
                this.getLanguageManager().get("command.inventory.backpack.confirm", slotsToPurchase),
                this.getLanguageManager().get("command.inventory.backpack.confirm.button", NumberFormat.getNumberInstance().format(totalPrice)),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        getUserManager().removeTokens(currentPlayer, totalPrice);

                        currentPlayer.setInventorySize(currentMaxSlots + slotsToPurchase);
                        getUserManager().saveUser(currentPlayer);

                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.backpack.purchase.completed", currentPlayer.getInventorySize()))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.backpack.purchase.cancelled"))).setEphemeral(true).queue();
                    }
        }, false, false, true);
    }

    /**
     * Handles the event when the user tries to open their backpack.
     *
     * @param interaction The command interaction that triggered the backpack open event.
     * @param currentUser The user who triggered the event.
     * @param currentPlayer The current player associated with the user.
     */
    @SneakyThrows
    private void handleBackpackOpen(@NotNull CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        interaction.deferReply(false).queue();

        String requestId = GameId.generateId();

        CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> {
            try {
                return this.handleBackpack(currentPlayer, currentUser, requestId);
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException("Error occurred while handling inventory", e);
            }
        });
        generatedProfile.whenComplete((fileUpload, throwable) -> {
            interaction.getHook().sendMessageEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(getLanguageManager().get("command.backpack.title", currentUser.getGlobalName()), "https://fluffici.eu", ICON_BOOK)
                    .setImage("attachment://".concat(requestId.concat("_backpack.png")))
                    .setTimestamp(Instant.now())
                    .build()
            ).addFiles(fileUpload).queue();
        });

        if (generatedProfile.isCancelled()
                || generatedProfile.isCompletedExceptionally()) {
            interaction.getHook().sendMessageEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.completeable.failed"))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the logic for a player eating an item.
     *
     * @param interaction The command interaction object.
     * @param currentUser Information about the current user.
     * @param currentPlayer Information about the current player.
     */
    @SneakyThrows
    private void handleEatItem(@NotNull CommandInteraction interaction, User currentUser, @NotNull PlayerBean currentPlayer) {
        String itemName = interaction.getOption("item-name").getAsString();
        int quantity = interaction.getOption("quantity").getAsInt();

        if (currentPlayer.getHungerPercentage() == 1.0) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.already_full"))).queue();
        } else {
            ItemDescriptionBean foodItem = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .fetchItem(itemName);

            if (!this.getUserManager().hasItem(currentUser, foodItem.getItemSlug())) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.item_not_found", itemName))).setEphemeral(true).queue();
            } else {
                if (foodItem != null && foodItem.isEatable()) {
                    ItemFoodBuilder foodBuilder = this.getFoodManager().getFood(foodItem.getItemSlug());
                    if (foodBuilder != null) {
                        double maxSatiety = ((1.0 - currentPlayer.getHungerPercentage()) / foodBuilder.getSatiety()) * quantity;
                        int maxQuantity = (int) Math.min(maxSatiety, quantity);

                        if (maxQuantity <= 0) {
                            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.not_needed"))).queue();
                        } else {
                            Pair<Boolean, String> result = FluffBOT.getInstance().getGameServiceManager()
                                    .decrementQuantity(currentUser, foodItem, maxQuantity);

                            if (result.getLeft()) {
                                double satiety = foodBuilder.getSatiety() * maxQuantity;
                                double healingFactor = foodBuilder.getHealingFactor() * maxQuantity;
                                double manaFactor = foodBuilder.getManaFactor() * maxQuantity;

                                double newHungerPercentage = currentPlayer.getHungerPercentage() + satiety;
                                double newHealthPercentage = currentPlayer.getHealthPercentage() + healingFactor;
                                double newManaPercentage = currentPlayer.getManaPercentage() + manaFactor;

                                newHungerPercentage = Math.min(newHungerPercentage, 1.0);
                                newHealthPercentage = Math.min(newHealthPercentage, 1.0);
                                newManaPercentage = Math.min(newManaPercentage, 1.0);

                                currentPlayer.setHungerPercentage(newHungerPercentage);
                                currentPlayer.setHealthPercentage(newHealthPercentage);
                                currentPlayer.setManaPercentage(newManaPercentage);

                                this.getUserManager().saveUser(currentPlayer);


                                interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.inventory.eat.success", maxQuantity, foodItem.getItemName()))).queue();
                            } else {
                                interaction.replyEmbeds(this.buildError(result.getRight())).queue();
                            }
                        }
                    } else {
                        interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.unknown_item", itemName))).setEphemeral(true).queue();
                    }
                } else {
                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.not_eatable", itemName))).setEphemeral(true).queue();
                }
            }
        }
    }

    /**
     * Handles the dropping of an item in the inventory.
     *
     * @param interaction The CommandInteraction object representing the user's command interaction.
     * @param currentUser The User object representing the current user.
     * @param currentPlayer The PlayerBean object representing the current player.
     */
    @SneakyThrows
    private void handleDropItem(CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        String itemName = interaction.getOption("item-name").getAsString();
        int quantity = interaction.getOption("quantity").getAsInt();

        ItemDescriptionBean item = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchItem(itemName);

        if (item.isEquipment()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.drop.equipment_not_droppable"))).setEphemeral(true).queue();
            return;
        }

        if (!this.getUserManager().hasItem(currentUser, item.getItemSlug())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.eat.item_not_found"))).setEphemeral(true).queue();
        } else {

            handleConfirmation(interaction, this.getLanguageManager().get("command.inventory.drop.confirm", item.getItemName(), NumberFormat.getNumberInstance().format(quantity)), new ConfirmCallback() {
                @Override
                public void confirm(ButtonInteraction interaction) {
                    Pair<Boolean, String> result = FluffBOT.getInstance().getGameServiceManager()
                            .decrementQuantity(currentUser, item, quantity);
                    if (result.getLeft()) {
                        String dropId = GameId.generateId();
                        FluffBOT.getInstance()
                                .getGameServiceManager()
                                .createItemDrop(new DropItemBuilder(
                                        dropId,
                                        interaction.getUser(),
                                        null,
                                        false,
                                        item.getItemId(),
                                        quantity
                                ));

                        handleAcceptance(interaction,
                                getLanguageManager().get("command.inventory.drop.initiated"),
                                getLanguageManager().get("command.inventory.drop.announcement", interaction.getUser().getGlobalName(), item.getItemName(), quantity),
                                getLanguageManager().get("command.inventory.drop.claim"),
                                dropId,
                                new AcceptCallback() {
                                    @Override
                                    public void execute(ButtonInteraction interaction, String acceptanceId) throws Exception {
                                        DropItemBuilder drop = FluffBOT.getInstance()
                                                .getGameServiceManager().fetchDroppedItem(acceptanceId);
                                        if (drop != null) {
                                            if (!drop.isClaimed()) {
                                                ItemDescriptionBean droppedItem = FluffBOT.getInstance()
                                                        .getGameServiceManager().fetchItem(drop.getItemId());

                                                try {
                                                    FluffBOT.getInstance()
                                                            .getGameServiceManager()
                                                            .claimDrop(interaction.getUser(), acceptanceId);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                Pair<Boolean, InventoryStatus> result = getUserManager().addItem(interaction.getUser(), droppedItem, drop.getQuantity());

                                                if (result.getLeft()) {
                                                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.inventory.drop.claimed", droppedItem.getItemName(), drop.getQuantity()))).setEphemeral(true).queue();
                                                } else {
                                                    interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.drop.failed"))).setEphemeral(true).queue();
                                                    getUserManager().addItem(drop.getDroppedBy(), droppedItem, drop.getQuantity());
                                                }
                                            } else {
                                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.drop_item_already_claimed", drop.getDroppedBy().getAsMention()))).setEphemeral(true).queue();
                                            }
                                        } else {
                                            interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.drop_item_not_found"))).setEphemeral(true).queue();
                                        }
                                    }
                                }
                        );
                    } else {
                        interaction.replyEmbeds(buildError(result.getRight())).queue();
                    }
                }

                @Override
                public void cancel(ButtonInteraction interaction) {
                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.inventory.drop.cancelled", item.getItemName()))).setEphemeral(true).queue();
                }
            });
        }
    }

    /**
     * Handles the donation of an item in the inventory.
     *
     * @param interaction   The CommandInteraction object representing the user's command interaction.
     * @param currentUser   The User object representing the current user.
     * @param currentPlayer The PlayerBean object representing the current player.
     */
    @SneakyThrows
    private void handleDonateItem(@NotNull CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        User target = interaction.getOption("user").getAsUser();
        String itemName = interaction.getOption("item-name").getAsString();
        int quantity = interaction.getOption("quantity").getAsInt();

        ItemDescriptionBean item = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchItem(itemName);

        if (!this.getUserManager().hasItem(currentUser, item.getItemSlug())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.donate.item_not_found"))).setEphemeral(true).queue();
        } else {
            handleConfirmation(interaction, this.getLanguageManager().get("command.inventory.donate.confirm", item.getItemName(), quantity, target.getGlobalName()), new ConfirmCallback() {
                @Override
                public void confirm(ButtonInteraction interaction) throws Exception {
                    Pair<Boolean, String> result = FluffBOT.getInstance()
                            .getGameServiceManager()
                            .decrementQuantity(currentUser, item, quantity);

                    if (result.getLeft()) {
                        Pair<Boolean, InventoryStatus> targetResult = getUserManager().addItem(target, item, quantity);
                        if (targetResult.getLeft()) {
                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.inventory.donate.success"))).setEphemeral(true).queue();
                        } else {
                            Pair<Boolean, InventoryStatus> fallbackResult = getUserManager().addItem(interaction.getUser(), item, quantity);
                            if (targetResult.getLeft()) {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.inventory.eat.fallback_item_returned"))).setEphemeral(true).queue();
                            } else {
                                FluffBOT.getInstance().getLogger().warn("[DonationFailed:FALLBACK] Unable to return the item %s for %s due to a full inventory.", item.getItemSlug(), interaction.getUser().getId());
                            }
                        }
                    } else {
                        interaction.replyEmbeds(buildError(result.getRight())).queue();
                    }
                }

                @Override
                public void cancel(ButtonInteraction interaction) throws Exception {
                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.inventory.donate.cancel"))).setEphemeral(true).queue();
                }
            });
        }
    }

    /**
     * Handle item information for a given command interaction.
     *
     * @param interaction The command interaction.
     * @param currentUser The user requesting the item information.
     * @param currentPlayer The current player.
     */
    private void handleItemInfo(@NotNull CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        try {
            String itemName = interaction.getOption("item-name").getAsString();

            ItemDescriptionBean item = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .fetchItem(itemName);

            if (this.getUserManager().hasItem(currentUser, itemName)) {
                InventoryItem playerItem = FluffBOT.getInstance()
                        .getGameServiceManager()
                        .getPlayerItem(currentUser, item);

                EmbedBuilder message = this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("command.inventory.item.info", item.getItemName()), "https://fluffici.eu", ICON_NOTE)
                        .setDescription(this.getLanguageManager().get(playerItem.getDescription()))
                        .setThumbnail("attachment://item_".concat(itemName.concat(".png")))
                        .addField(this.getLanguageManager().get("common.rarity"), this.getLanguageManager().get("rarity.".concat(playerItem.getRarity().name().toLowerCase())), true)
                        .addField(this.getLanguageManager().get("common.quantity"), NumberFormat.getNumberInstance().format(playerItem.getQuantity()), true);

                if (item.isEquipment()) {
                    message.addField(this.getLanguageManager().get("command.inventory.item.durability"), playerItem.durabilityText(), true);
                    message.addField(this.getLanguageManager().get("common.is_equipped"), (playerItem.isEquipped() ? this.getLanguageManager().get("common.yes") : this.getLanguageManager().get("common.no")), true);
                    if (item.getResistance() > 0)
                        message.addField(this.getLanguageManager().get("common.resistance"), "+".concat(String.valueOf(item.getResistance())), true);
                } else if (item.isEatable()) {
                    ItemFoodBuilder foodBuilder = this.getFoodManager().getFood(item.getItemSlug());
                    if (foodBuilder != null) {
                        if (foodBuilder.getSatiety() > 0)
                            message.addField(this.getLanguageManager().get("command.inventory.item.satiety"), this.getLanguageManager().get("common.per_item", "+".concat(String.valueOf(foodBuilder.getSatiety()))), false);
                        if (foodBuilder.getHealingFactor() > 0)
                            message.addField(this.getLanguageManager().get("command.inventory.item.health_factor"), this.getLanguageManager().get("common.per_item", "+".concat(String.valueOf(foodBuilder.getHealingFactor()))), false);
                        if (foodBuilder.getManaFactor() > 0)
                            message.addField(this.getLanguageManager().get("command.inventory.item.mana_factor"), this.getLanguageManager().get("common.per_item", "+".concat(String.valueOf(foodBuilder.getManaFactor()))), false);
                    }
                } else if (item.isDrinkable()) {
                    //TODO: Drinkable abilities.
                } else if (item.isEnchantable()) {
                    // TODO: Display the user-enchantment
                } else if (item.isCraftable()) {
                    //TODO: Display the crafting material(s)
                } else if (item.isPurchasable()) {
                    message.appendDescription("\n * Cena položky (koupě): **".concat(NumberFormat.getNumberInstance().format(item.getPriceTokens())).concat(" FT/ks**"));
                }

                message.appendDescription("\n * Cena položky (prodej): **".concat(NumberFormat.getNumberInstance().format(item.getSalePrice())).concat(" FT/ks**"));

                interaction.replyEmbeds(message.build()).addFiles(FileUpload.fromData(getTexture(itemName), "item_".concat(itemName.concat(".png")))).queue();
            } else {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.select.no_item"))).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static InputStream getTexture(String itemName) {
        InputStream is = null;
        try {
            is = FluffBOT.getInstance().getClass().getResourceAsStream(String.format("/assets/items/%s.png", itemName));
            if (is == null) {
                is = FluffBOT.getInstance().getClass().getResourceAsStream("/assets/items/missingno.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }


    /**
     * Handles the equipment list in the inventory.
     *
     * @param interaction   The CommandInteraction object representing the user's command interaction.
     * @param currentUser   The User object representing the current user.
     * @param currentPlayer The PlayerBean object representing the current player.
     */
    private void handleEquipmentsList(@NotNull CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        InventoryBuilder inventory = this.getUserManager().fetchInventory(currentUser);

        if (inventory.hasNoItems()) {
            interaction.replyEmbeds(buildError("Inventory is empty.")).queue();
        } else {
            PaginationBuilder pagination = PaginationBuilder
                    .builder()
                    .pages(new ArrayList<>())
                    .paginationOwner(interaction.getUser())
                    .paginationUniqueId(GameId.generateId())
                    .build();

            for (InventoryItem playerItem : this.getUserManager().fetchInventory(interaction.getUser()).getItems()) {
                if (playerItem.isEquipment() && playerItem.getEquipmentType() != EquipmentType.ITEM) {
                    EmbedBuilder message = this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("command.inventory.item.info", playerItem.getName()), "https://fluffici.eu", ICON_NOTE)
                            .setDescription(this.getLanguageManager().get(playerItem.getDescription()))
                            .setThumbnail("attachment://item.png")
                            .addField(this.getLanguageManager().get("common.rarity"), this.getLanguageManager().get("rarity.".concat(playerItem.getRarity().name().toLowerCase())), true)
                            .addField(this.getLanguageManager().get("common.quantity"), NumberFormat.getNumberInstance().format(playerItem.getQuantity()), true);

                    message.addField(this.getLanguageManager().get("command.inventory.item.durability"), playerItem.durabilityText(), true);
                    message.addField(this.getLanguageManager().get("common.is_equipped"), (playerItem.isEquipped() ? this.getLanguageManager().get("common.yes") : this.getLanguageManager().get("common.no")), true);
                    if (playerItem.getResistance() > 0)
                        message.addField(this.getLanguageManager().get("common.resistance"), "+".concat(String.valueOf(playerItem.getResistance())), true);

                    pagination.addPage(PageBuilder
                            .builder()
                            .message(message.build())
                            .texture(playerItem.getItemSlug())
                            .isTextured(true)
                            .build());
                }
            }

            handlePagination(interaction, pagination);
        }
    }

    /**
     * Handles the selection of equipment in the inventory.
     *
     * @param interaction   The CommandInteraction object representing the user's command interaction.
     * @param currentUser   The User object representing the current user.
     * @param currentPlayer The PlayerBean object representing the current player.
     */
    @SneakyThrows
    private void handleSelectEquipment(CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        String itemName = interaction.getOption("item-name").getAsString();

        ItemDescriptionBean item = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchItem(itemName);

        if (this.getUserManager().hasItem(currentUser, itemName)) {
            if (!item.isEquipment()) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.select.not_a_equipment"))).setEphemeral(true).queue();
                return;
            }

            InventoryBuilder inventory = this.getUserManager().fetchInventory(currentUser);

            if (inventory.isSlotOccupied(item.getEquipmentSlug())) {
                InventoryItem occupied = inventory.getItemOnSlot(item.getEquipmentSlug());
                FluffBOT.getInstance()
                        .getGameServiceManager()
                        .setEquipped(currentUser, occupied.getItemId(), false);
            }

            FluffBOT.getInstance()
                    .getGameServiceManager()
                    .setEquipped(currentUser, item.getItemId(), true);

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.inventory.selected", item.getItemName(), item.getEquipmentSlug().name().toLowerCase()))).setEphemeral(true).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.select.no_item"))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the opening of the inventory for a player.
     *
     * @param interaction  The CommandInteraction object representing the user's command interaction.
     * @param currentUser  The User object representing the current user.
     * @param currentPlayer  The PlayerBean object representing the current player.
     */
    private void handleInventoryOpen(CommandInteraction interaction, User currentUser, PlayerBean currentPlayer) {
        interaction.deferReply(false).queue();

        String requestId = GameId.generateId();

        CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> {
            try {
                return this.handleInventory(currentPlayer, currentUser, requestId);
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException("Error occurred while handling inventory", e);
            }
        });
        generatedProfile.whenComplete((fileUpload, throwable) -> {
            interaction.getHook().sendMessageEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(getLanguageManager().get("command.inventory.title", currentUser.getGlobalName()), "https://fluffici.eu", ICON_BOOK)
                    .setImage("attachment://".concat(requestId.concat("_inventory.png")))
                    .setTimestamp(Instant.now())
                    .build()
            ).addFiles(fileUpload).queue();
        });

        if (generatedProfile.isCancelled()
                || generatedProfile.isCompletedExceptionally()) {
            interaction.getHook().sendMessageEmbeds(this.buildError(this.getLanguageManager().get("command.inventory.completeable.failed"))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the generation and manipulation of the inventory image for a player.
     *
     * @param player The PlayerBean object representing the player's statistics and attributes.
     * @param user The User object representing the player's user information.
     * @param requestId The unique identifier for the inventory image request.
     * @return The FileUpload object containing the generated inventory image.
     */
    @Nullable
    private FileUpload handleInventory(PlayerBean player, User user, String requestId) throws IOException, IllegalAccessException {
        try {
            BufferedImage baseImage = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/inventory/base.png"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g2d = baseImage.createGraphics();
            BufferedImage avatar = ImageIO.read(new URL(user.getAvatarUrl()));
            BufferedImage roundedAvatar = makeRoundedCorner(avatar, avatar.getWidth());

            ge.registerFont(customFont);
            ge.registerFont(neon);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            InventoryBuilder inventoryBuilder = this.getUserManager().fetchInventory(user);
            player.setProtectionPercentage(inventoryBuilder.totalResistance());

            // Main section
            this.handleInventorySlots(g2d, user);
            this.handleUpgradesSlots(g2d, user);

            // Equipments section

            this.handleBars(g2d, player);

            this.handleEquipmentsSlot(g2d, user);
            this.handleSpellsSlot(g2d, user);

            this.handleHotbar(g2d, user);
            this.handleSubHotbar(g2d, user);

            g2d.drawImage(roundedAvatar, 35, 20, 110, 110, null);

            g2d.setColor(Color.white);
            g2d.setFont(customFont.deriveFont(12f));
            g2d.drawString("FluffBOT v3 build: " + FluffBOT.getInstance().getGitProperties().getProperty("git.build.version"), 10, 725);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(baseImage, "png", baos);
            byte[] finalImage = baos.toByteArray();

            return FileUpload.fromData(finalImage, requestId.concat("_inventory.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles the creation of a backpack image for a player.
     *
     * @param player the PlayerBean object representing the player's information
     * @param user the User object representing the user's information
     * @param requestId the unique identifier for the request
     * @return a FileUpload object containing the backpack image in PNG format, or null if there was an error
     * @throws IOException if an I/O error occurs during the image creation process
     * @throws IllegalAccessException if access to a resource or method is denied
     */
    @Nullable
    private FileUpload handleBackpack(PlayerBean player, User user, String requestId) throws IOException, IllegalAccessException {
        try {
            BufferedImage baseImage = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/backpack/base.png"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g2d = baseImage.createGraphics();
            BufferedImage avatar = ImageIO.read(new URL(user.getAvatarUrl()));
            BufferedImage roundedAvatar = makeRoundedCorner(avatar, avatar.getWidth());

            ge.registerFont(customFont);
            ge.registerFont(neon);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Main section
            this.handleBackpackSlots(g2d, user);

            g2d.drawImage(roundedAvatar, 35, 20, 110, 110, null);

            g2d.setColor(Color.white);
            g2d.setFont(customFont.deriveFont(12f));
            g2d.drawString("FluffBOT v3 build: " + FluffBOT.getInstance().getGitProperties().getProperty("git.build.version"), 10, 725);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(baseImage, "png", baos);
            byte[] finalImage = baos.toByteArray();

            return FileUpload.fromData(finalImage, requestId.concat("_backpack.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles the backpack slots for the given user.
     *
     * @param g2d the Graphics2D object used for rendering
     * @param user the User object representing the user
     */
    @SneakyThrows
    private void handleBackpackSlots(Graphics2D g2d, User player) {
        BufferedImage lockIcon = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/backpack/lock.png"));

        final int GRID_HEIGHT = 7;
        final int GRID_WIDTH = 13;
        final int START_X = this.START_X + 1;
        final int START_Y = this.START_Y + 1;
        final int SLOT_WIDTH = this.SLOT_WIDTH + 1;
        final int SLOT_HEIGHT = this.SLOT_HEIGHT + 1;
        final int SLOT_MARGIN = this.SLOT_MARGIN;

        if (FluffBOT.getInstance().getDebug()) {
            g2d.setColor(Color.RED);
            for (int y = 0; y < GRID_HEIGHT; y++) {
                for (int x = 0; x < GRID_WIDTH; x++) {
                    int xPos = START_X + x * SLOT_WIDTH;
                    int yPos = START_Y + y * SLOT_HEIGHT;
                    g2d.drawRect(xPos, yPos, SLOT_WIDTH, SLOT_HEIGHT);
                }
            }
        }

        InventoryBuilder inventory = this.getUserManager().fetchInventory(player);
        int maxSlots = inventory.getMetadata().getMaximumSlots();

        List<InventoryItem> displayableItems = inventory.getItems().stream()
                .filter(InventoryItem::isNotEquipped)
                .filter(InventoryItem::isNotEquipment)
                .limit(91)
                .collect(Collectors.toList());

        for (int i = 0; i < 91; i++) {
            int row = i / GRID_WIDTH;
            int col = i % GRID_WIDTH;
            int slotX = START_X + col * SLOT_WIDTH;
            int slotY = START_Y + row * SLOT_HEIGHT;

            if (i < displayableItems.size() && i < maxSlots) {
                InventoryItem item = displayableItems.get(i);
                BufferedImage image = this.getItemTexture(item.getItemSlug());
                g2d.drawImage(image, slotX + SLOT_MARGIN, slotY + SLOT_MARGIN,
                        SLOT_WIDTH - 2 * SLOT_MARGIN, SLOT_HEIGHT - 2 * SLOT_MARGIN, null);

                g2d.setFont(customFont.deriveFont(Font.BOLD, 6f));
                String itemName = item.getName().toUpperCase();
                FontMetrics fontMetrics = g2d.getFontMetrics();
                int nameWidth = fontMetrics.stringWidth(itemName);
                g2d.setColor(item.getRarity().getColor());
                g2d.drawString(itemName, slotX + (SLOT_WIDTH - nameWidth) / 2,
                        slotY + SLOT_MARGIN + fontMetrics.getAscent());

                g2d.setFont(customFont.deriveFont(Font.BOLD, 12f));
                String quantityText = "x" + item.getQuantity();
                int textWidth = fontMetrics.stringWidth(quantityText);
                g2d.setColor(Color.WHITE);
                g2d.drawString(quantityText, slotX + (SLOT_WIDTH - textWidth) / 2,
                        slotY + SLOT_HEIGHT - SLOT_MARGIN);
            } else if (i >= maxSlots) {
                int lockIconWidth = 38;
                int lockIconHeight = 38;

                int centerX = slotX + (SLOT_WIDTH - lockIconWidth) / 2;
                int centerY = slotY + (SLOT_HEIGHT - lockIconHeight) / 2;

                g2d.drawImage(lockIcon, centerX, centerY, lockIconWidth, lockIconHeight, null);
            }
        }
    }

    /**
     * Handles the rendering of inventory slots.
     *
     * @param g2d     The Graphics2D object used for rendering.
     * @param player  The UserSnowflake object representing the player's inventory.
     *
     * @throws IOException If there is an error reading the item image.
     */
    private void handleInventorySlots(Graphics2D g2d, UserSnowflake player) throws IOException {
        // Draw debug grid
        if (FluffBOT.getInstance().getDebug()) {
            g2d.setColor(Color.RED);
            for (int y = 0; y < GRID_HEIGHT; y++) {
                for (int x = 0; x < GRID_WIDTH; x++) {
                    int xPos = START_X + x * SLOT_WIDTH;
                    int yPos = START_Y + y * SLOT_HEIGHT;
                    g2d.drawRect(xPos, yPos, SLOT_WIDTH, SLOT_HEIGHT);
                }
            }
        }

        // Draw items in inventory
        int row = 0;
        int col = 0;
        for (InventoryItem item : this.getUserManager().fetchInventory(player).getItems()
                .stream()
                .filter(InventoryItem::isNotEquipped)
                .filter(InventoryItem::isNotEquipment)
                .limit(15)
                .toList()
        ) {
            // Avoiding equipement to appear in the main inventory.
            int slotX = START_X + col * (SLOT_WIDTH + 2);
            int slotY = START_Y + row * SLOT_HEIGHT;

            // Draw item image
            BufferedImage image = this.getItemTexture(item.getItemSlug());
            int imageX = slotX + SLOT_MARGIN;
            int imageY = slotY + SLOT_MARGIN;
            int imageWidth = SLOT_WIDTH - 2 * SLOT_MARGIN;
            int imageHeight = SLOT_HEIGHT - 2 * SLOT_MARGIN;
            g2d.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 6f));

            // Draw item name
            String itemName = item.getName().toUpperCase();
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int nameWidth = fontMetrics.stringWidth(itemName);
            int nameX = slotX + (SLOT_WIDTH - nameWidth) / 2;
            int nameY = slotY + SLOT_MARGIN + fontMetrics.getAscent();
            g2d.setColor(item.getRarity().getColor());
            g2d.drawString(itemName, nameX, nameY);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 12f));

            // Draw item quantity
            String quantityText = "x" + item.getQuantity();
            int textWidth = fontMetrics.stringWidth(quantityText);
            int textX = slotX + (SLOT_WIDTH - textWidth) / 2;
            int textY = slotY + SLOT_HEIGHT - SLOT_MARGIN;
            g2d.setColor(Color.white);
            g2d.drawString(quantityText, textX, textY);

            col++;
            if (col >= GRID_WIDTH) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Handles the rendering of upgrade slots in the inventory.
     *
     * @param g2d    The Graphics2D object used for rendering.
     * @param player The UserSnowflake object representing the player's inventory.
     */
    private void handleUpgradesSlots(Graphics2D g2d, UserSnowflake player) throws IOException {
        if (FluffBOT.getInstance().getDebug()) {
            g2d.setColor(Color.RED);
            for (int y = 0; y < GRID_HEIGHT; y++) {
                for (int x = 0; x < GRID_WIDTH; x++) {
                    int xPos = UPGRADE_START_X + x * UPGRADE_SLOT_WIDTH;
                    int yPos = UPGRADE_START_Y + y * UPGRADE_SLOT_HEIGHT;
                    g2d.drawRect(xPos, yPos, UPGRADE_SLOT_WIDTH, UPGRADE_SLOT_HEIGHT);
                }
            }
        }

        g2d.setFont(customFont.deriveFont(Font.BOLD, 8f));

        // Draw upgrades in inventory
        int row = 0;
        int col = 0;
        for (UserUpgradeBuilder upgrade : this.getUserManager().fetchInventory(player).getUpgrades()) {
            int slotX = UPGRADE_START_X + col * (UPGRADE_SLOT_WIDTH + UPGRADE_HORIZONTAL_PADDING);
            int slotY = UPGRADE_START_Y + row * (UPGRADE_SLOT_HEIGHT + UPGRADE_VERTICAL_PADDING);

            // Draw upgrade image
            BufferedImage image = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/upgrades/" + upgrade.getUpgradeId() + ".png"));
            int imageX = slotX + SLOT_MARGIN;
            int imageY = slotY + SLOT_MARGIN;
            int imageWidth = UPGRADE_SLOT_WIDTH - 2 * SLOT_MARGIN;
            int imageHeight = UPGRADE_SLOT_HEIGHT - 2 * SLOT_MARGIN;
            g2d.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);

            // Draw upgrade name
            String upgradeName = upgrade.getName().toUpperCase();
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int nameWidth = fontMetrics.stringWidth(upgradeName);
            int nameX = slotX + (UPGRADE_SLOT_WIDTH - nameWidth) / 2;
            int nameY = slotY + SLOT_MARGIN + fontMetrics.getAscent();
            g2d.setColor(upgrade.getRarity().getColor());
            g2d.drawString(upgradeName, nameX, nameY);

            // Draw upgrade level
            String levelText = "Level: " + upgrade.getLevel() + "/5";
            int textWidth = fontMetrics.stringWidth(levelText);
            int textX = slotX + (UPGRADE_SLOT_WIDTH - textWidth) / 2;
            int textY = slotY + UPGRADE_SLOT_HEIGHT - SLOT_MARGIN;
            g2d.setColor(Color.white);
            g2d.drawString(levelText, textX, textY);

            col++;
            if (col >= GRID_WIDTH) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Handles the rendering of bars on the provided Graphics2D object for the given PlayerBean object.
     *
     * @param g2d    The Graphics2D object used for rendering.
     * @param player The PlayerBean object representing the player's stats.
     */
    private void handleBars(Graphics2D g2d, PlayerBean player) {
        try {
            BufferedImage healthBar = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/inventory/bars/health.png"));
            BufferedImage protectionBar = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/inventory/bars/protection.png"));
            BufferedImage hungerBar = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/inventory/bars/hunger.png"));
            BufferedImage manaBar = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/inventory/bars/mana.png"));

            double healthPercentage = Math.max(0.01, player.getHealthPercentage());
            double protectionPercentage = Math.max(0.01, player.getProtectionPercentage());
            double hungerPercentage = Math.max(0.01, player.getHungerPercentage());
            double manaPercentage = Math.max(0.01, player.getManaPercentage());

            drawFilledBar(g2d, HEALTH_X, BARS_Y, healthPercentage, Color.decode("#FF0A0A"));
            drawFilledBar(g2d, PROTECTION_X, BARS_Y, protectionPercentage, Color.decode("#1AFF0A"));
            drawFilledBar(g2d, HUNGER_X, BARS_Y, hungerPercentage, Color.decode("#FFAD0A"));
            drawFilledBar(g2d, MANA_X, BARS_Y, manaPercentage, Color.decode("#0F50A5"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the rendering of the equipment slot in the inventory.
     *
     * @param g2d  The Graphics2D object used for rendering.
     * @param user The UserSnowflake object representing the player's inventory.
     */
    private void handleEquipmentsSlot(Graphics2D g2d, UserSnowflake user) throws IllegalAccessException {
        this.getUserManager().fetchInventory(user).getItems()
                .stream()
                .filter(InventoryItem::isArmor)
                .filter(InventoryItem::isEquipped)
                .forEach(item -> {
                    try {
                        this.renderSpecialItem(g2d, item);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Handles the rendering of the spells slot on the provided Graphics2D object for the given UserSnowflake object.
     *
     * @param g2d  The Graphics2D object used for rendering.
     * @param user The UserSnowflake object representing the player's inventory.
     */
    private void handleSpellsSlot(Graphics2D g2d, UserSnowflake user) throws IllegalAccessException {
        this.renderSpells(g2d, this.getUserManager().fetchInventory(user).getItems()
                .stream()
                .filter(item -> item.isSpell())
                .filter(item -> item.isEquipped())
                .toList()
        );
    }

    /**
     * Handles the rendering of the hotbar on the provided Graphics2D object for the given UserSnowflake object.
     *
     * @param g2d  The Graphics2D object used for rendering.
     * @param user The UserSnowflake object representing the player's inventory.
     */
    private void handleHotbar(Graphics2D g2d, UserSnowflake user) throws IllegalAccessException {
        for (InventoryItem item : this.getUserManager().fetchInventory(user).getItems())
            if ((item.isSword() || item.isAxe() || item.isStaff() || item.isNecklace()) && item.isEquipped())
                this.renderSpecialItem(g2d, item);
    }

    /**
     * Handles the rendering of a sub-hotbar on the provided Graphics2D object for the given UserSnowflake object.
     *
     * @param g2d  The Graphics2D object used for rendering.
     * @param user The UserSnowflake object representing the player's inventory.
     */
    private void handleSubHotbar(Graphics2D g2d, UserSnowflake user) throws IllegalAccessException {
        for (InventoryItem item : this.getUserManager().fetchInventory(user).getItems())
            if ((item.isPickaxe() || item.isHoe() || item.isBow() || item.isFragment()) && item.isEquipped())
                this.renderSpecialItem(g2d, item);
    }

    /**
     * Renders a special inventory item on the provided Graphics2D object.
     *
     * @param g2d  the Graphics2D object used for rendering
     * @param user the UserSnowflake object representing the player's inventory
     * @param item the InventoryItem to render
     * @param x    the x-coordinate of the item's position
     * @param y    the y-coordinate of the item's position
     * @throws IllegalAccessException if the item is not a special item
     */
    private void renderSpecialItem(Graphics2D g2d, InventoryItem item) throws IllegalAccessException {
        g2d.setFont(this.customFont.deriveFont(8f));

        if (item.getEquipmentSlug() != EquipmentSlot.NONE) {
            try {
                int slotX = (int) item.getEquipmentSlug().getXPosition();
                int slotY = (int) item.getEquipmentSlug().getYPosition();

                if (FluffBOT.getInstance().getDebug()) {
                    if (item.isEquipped()) {
                        g2d.setColor(Color.GREEN);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRect(slotX, slotY, SPECIAL_SLOT_WIDTH, SPECIAL_SLOT_HEIGHT);
                    }
                }

                BufferedImage image = this.getItemTexture(item.getItemSlug());
                int imageX = slotX + SLOT_MARGIN;
                int imageY = slotY + SLOT_MARGIN;
                int imageWidth = SPECIAL_SLOT_WIDTH - 2 * SLOT_MARGIN;
                int imageHeight = SPECIAL_SLOT_HEIGHT - 2 * SLOT_MARGIN;
                g2d.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);

                // Draw upgrade name
                String itemName = item.getName().toUpperCase();
                FontMetrics fontMetrics = g2d.getFontMetrics();
                int nameWidth = fontMetrics.stringWidth(itemName);
                int nameX = slotX + (SPECIAL_SLOT_WIDTH - nameWidth) / 2;
                int nameY = slotY + SLOT_MARGIN + fontMetrics.getAscent();
                g2d.setColor(item.getRarity().getColor());
                g2d.drawString(itemName, nameX, nameY);

                int currentDurability = item.getDurability();
                int maxDurability = item.getMaxDurability();
                int progressBarWidth = (int) ((double) currentDurability / maxDurability * (SPECIAL_SLOT_WIDTH - 10));
                int progressBarHeight = 5;
                int progressBarX = slotX + 2;
                int progressBarY = slotY + SPECIAL_SLOT_HEIGHT - SLOT_MARGIN - progressBarHeight - 5 + 4;
                double durabilityPercentage = (double) currentDurability / maxDurability;

                Color progressBarColor;
                try {
                    if (durabilityPercentage <= 0.5) {
                        int red = 255;
                        int green = (int) (durabilityPercentage * 2 * 255);
                        progressBarColor = new Color(red, green, 0);
                    } else {
                        int red = (int) ((1 - durabilityPercentage) * 2 * 255);
                        int green = 255;
                        progressBarColor = new Color(red, green, 0);
                    }
                } catch (Exception e) {
                    progressBarColor = Color.RED;
                }

                g2d.setColor(Color.GRAY);
                g2d.fillRect(progressBarX, progressBarY, SPECIAL_SLOT_WIDTH - 10, progressBarHeight);
                g2d.setColor(progressBarColor);
                g2d.fillRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(progressBarX, progressBarY, SPECIAL_SLOT_WIDTH - 10, progressBarHeight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalAccessException("Cannot use 'renderSpecialItem' on a non-special item.");
        }
    }

    /**
     * Renders the spells slot on the provided Graphics2D object for the given list of InventoryItem objects.
     *
     * @param g2d    The Graphics2D object used for rendering.
     * @param spells The list of InventoryItem objects representing the spells.
     * @throws IllegalAccessException If there is an error rendering the spells.
     */
    private void renderSpells(Graphics2D g2d, List<InventoryItem> spells) throws IllegalAccessException {
       try {
           double startX = 692.5;
           double startY = 213.48;
           double slotWidth = 50;
           double slotHeight = 50;
           double horizontalSpacing = 10;

           for (int i = 0; i < Math.min(spells.size(), 4); i++) {
               InventoryItem spell = spells.get(i);
               double slotX = startX + i * (slotWidth + horizontalSpacing);
               double slotY = startY;

               BufferedImage image = this.getItemTexture(spell.getItemSlug());
               g2d.drawImage(image, (int)slotX, (int)slotY, (int)slotWidth, (int)slotHeight, null);

               String spellName = spell.getName();
               FontMetrics fontMetrics = g2d.getFontMetrics();
               int nameWidth = fontMetrics.stringWidth(spellName);
               int nameX = (int)(slotX + (slotWidth - nameWidth) / 2);
               int nameY = (int)(slotY + slotHeight + fontMetrics.getHeight());
               g2d.setColor(spell.getRarity().getColor());
               g2d.drawString(spellName, nameX, nameY);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    /**
     * Retrieves the texture for an item based on its slug.
     *
     * @param itemSlug The slug of the item.
     * @return The BufferedImage object representing the item texture.
     * @throws IOException If there is an error reading the item texture.
     */
    private BufferedImage getItemTexture(String itemSlug) throws IOException {
        try {
            InputStream texture = FluffBOT.getInstance().getClass().getResourceAsStream("/assets/items/" + itemSlug + ".png");
            if (texture == null) {
                texture = FluffBOT.getInstance().getClass().getResourceAsStream("/assets/items/missingno.png");
                if (texture == null) {
                    throw new IOException("Missing texture for item: " + itemSlug);
                }
            }
            return ImageIO.read(texture);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Calculates the position of the item image based on the given x and y coordinates,
     * as well as the width and height of the image.
     *
     * @param x           The x-coordinate of the item position.
     * @param y           The y-coordinate of the item position.
     * @param imageWidth  The width of the item image.
     * @param imageHeight The height of the item image.
     * @return The position of the item image as a Point object.
     */
    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    private Point calculateItemImagePosition(int x, int y, int imageWidth, int imageHeight) {
        int centeredX = x + SLOT_WIDTH / 2 - imageWidth / 2;
        int centeredY = y + SLOT_HEIGHT / 2 - imageHeight / 2;
        return new Point(centeredX, centeredY);
    }

    /**
     * Calculates the position of the item quantity text based on the given x and y coordinates and the width of the text.
     *
     * @param x         The x-coordinate of the item position.
     * @param y         The y-coordinate of the item position.
     * @param textWidth The width of the item quantity text.
     * @return The position of the item quantity text as a Point object.
     */
    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    private Point calculateItemQuantityPosition(int x, int y, int textWidth) {
        int centeredX = x + SLOT_WIDTH / 2 - textWidth / 2;
        int centeredY = y + SLOT_HEIGHT - 5;
        return new Point(centeredX, centeredY);
    }

    /**
     * Draws a filled bar on the provided Graphics2D object.
     *
     * @param g2d        The Graphics2D object used for rendering.
     * @param x          The x-coordinate of the top-left corner of the bar.
     * @param y          The y-coordinate of the top-left corner of the bar.
     * @param percentage The percentage of the bar that should be filled. Should be a value between 0 and 1.
     * @param color      The color of the filled portion of the bar.
     */
    private void drawFilledBar(@NotNull Graphics2D g2d, int x, int y, double percentage, Color color) {
        int fillHeight = (int) (BAR_HEIGHT * percentage);
        fillHeight = Math.min(fillHeight, BAR_HEIGHT);
        int fillY = Math.max(y, y + BAR_HEIGHT - fillHeight); // Ensure fillY doesn't exceed BAR_Y

        int innerX = x;
        int innerY = fillY;
        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = fillHeight - 1;

        RoundRectangle2D.Double roundedRect = new RoundRectangle2D.Double(innerX, innerY, innerWidth + 1, innerHeight, 2, 2);

        g2d.setColor(color);
        g2d.fill(roundedRect);
    }
}
