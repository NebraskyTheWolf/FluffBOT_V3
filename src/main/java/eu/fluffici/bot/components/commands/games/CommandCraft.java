/*
---------------------------------------------------------------------------------
File Name : CommandCraft

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.games;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.crafting.ItemCraftBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.item.ItemFoodBuilder;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_NOTE;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.commands.profile.CommandInventory.getTexture;

public class CommandCraft extends Command {
    public CommandCraft() {
        super("craft", "Craft materials or item(s) with your resources", CommandCategory.GAMES);

        this.getOptions().put("channelRestricted", true);
        this.getOptionData().add(new OptionData(OptionType.STRING, "item-name", "Select the item to craft.", true, true));
    }

    @Override
    @SneakyThrows
    public void execute(@NotNull CommandInteraction interaction) {
        String itemName = interaction.getOption("item-name").getAsString();
        ItemCraftBuilder craft = FluffBOT.getInstance().getItemCraftManager().getCraft(itemName);
        if (craft == null) {
            interaction.replyEmbeds(buildError("Invalid item name.")).setEphemeral(true).queue();
            return;
        }

        UserSnowflake user = interaction.getUser();

        String missingItems = FluffBOT.getInstance().getItemCraftManager().getMissingItems(user, itemName);
        if (!missingItems.isEmpty()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.craft.missing_item", missingItems))).queue();
            return;
        }

        String item = FluffBOT.getInstance().getGameServiceManager().fetchItem(craft.getSlug()).getItemName();

        handleConfirmation(interaction,
                this.getLanguageManager().get("command.craft.confirm", item),
                this.getLanguageManager().get("command.craft.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        CompletableFuture.supplyAsync(() -> FluffBOT.getInstance().getItemCraftManager().craftItem(user, itemName))
                                .whenComplete(((result, throwable) -> {
                                    if (result.getLeft()) {
                                        try {
                                            ItemDescriptionBean craftedItem = FluffBOT.getInstance().getGameServiceManager().fetchItem(craft.getSlug());
                                            String generatedId = GameId.generateId();

                                            interaction.getHook().sendMessageEmbeds(buildCraftSuccess(craftedItem, craft, generatedId))
                                                    .addFiles(FileUpload.fromData(getTexture(craftedItem.getItemSlug()), generatedId.concat(".png")))
                                                    .setEphemeral(true)
                                                    .queue();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        interaction.getHook().sendMessageEmbeds(buildError(result.getRight())).setEphemeral(true).queue();
                                    }
                                }));
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.getHook().sendMessageEmbeds(buildError(getLanguageManager().get("command.craft.canceled", item))).setEphemeral(true).queue();
                    }
                }, true, true);
    }

    @NotNull
    @SuppressWarnings("all")
    private MessageEmbed buildCraftSuccess(@NotNull ItemDescriptionBean item, @NotNull ItemCraftBuilder craftBuilder, @NotNull String generatedId) {
        EmbedBuilder message = this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("command.craft.item.info", item.getItemName()), "https://fluffici.eu", ICON_NOTE.getUrl())
                .setDescription(this.getLanguageManager().get(item.getItemDesc()))
                .setThumbnail("attachment://".concat(generatedId.concat(".png")))
                .addField(this.getLanguageManager().get("common.rarity"), this.getLanguageManager().get("rarity.".concat(item.getItemRarity().name().toLowerCase())), true)
                .addField(this.getLanguageManager().get("common.quantity"), NumberFormat.getNumberInstance().format(craftBuilder.getQuantity()), true);

        if (item.isEquipment()) {
            message.addField(this.getLanguageManager().get("command.inventory.item.durability"), String.valueOf(item.getDurability()), true);
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
        }

        return message.build();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        try {
            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getAllItems()
                    .stream()
                    .filter(ItemDescriptionBean::isCraftable)
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