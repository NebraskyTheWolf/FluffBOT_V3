package eu.fluffici.bot.components.commands.games;

/*
---------------------------------------------------------------------------------
File Name : CommandFarm.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.crafting.ItemCraftingMaterials;
import eu.fluffici.bot.api.game.farm.FarmingType;
import eu.fluffici.bot.api.game.farm.UserMiningInventory;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.inventory.InventoryBuilder;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandFarm extends Command {

    public CommandFarm() {
        super("farm", "This command allow you to farm different resources!", CommandCategory.GAMES);

        OptionData optionData = new OptionData(OptionType.STRING, "farming-group", "The resource you want to farm", true);

        for (FarmingType group : FarmingType.values())
                optionData.addChoice(FurRaidDB.getInstance().getLanguageManager().get("command.farm.type." + group.name()+ ".name"), group.name());

        this.getOptionData().add(optionData);
        this.getOptions().put("channelRestricted", true);
    }

    /**
     * Executes the command for farming.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     */
    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        FarmingType farmingGroup = FarmingType.valueOf(interaction.getOption("farming-group").getAsString().toUpperCase());

        if (farmingGroup.isRestricted()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.farm.restricted")))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        InventoryBuilder inventory = this.getUserManager().fetchInventory(interaction.getUser());
        Pair<Boolean, Duration> limited = this.isRateLimited(interaction.getUser(), 260);
        if (limited.getLeft()) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.farm.limited", "%s:h %s:min".formatted(limited.getRight().toHours(), limited.getRight().toMinutesPart()))
            )).setEphemeral(true).queue();
            return;
        }

        if (inventory.isSlotOccupied(farmingGroup.getEquipmentType())) {
            UserMiningInventory miningInventory = new UserMiningInventory(inventory.getItemOnType(farmingGroup.getEquipmentType()), farmingGroup);

            interaction.deferReply(false).queue();

            boolean toolDurabilityExhausted = false;

            while (!miningInventory.getMiningChunk().isFullyMined()) {
                if (!miningInventory.mineNextBlock(miningInventory.getMiningChunk())) {
                    toolDurabilityExhausted = true;
                    break;
                }
            }

            if (toolDurabilityExhausted) {
                this.handleEnd(interaction, miningInventory, FarmingErrorType.TOOL_DURABILITY);
            } else {
                this.handleEnd(interaction, miningInventory, FarmingErrorType.NONE);
            }
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.craft.invalid_equipment_type")))
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Handles the end of a farming operation.
     *
     * This method is responsible for updating the player's tool durability, collecting farmed items,
     * and sending the appropriate response based on the error type.
     *
     * @param interaction The CommandInteraction object representing the interaction with the command.
     * @param miningInventory The UserMiningInventory object representing the mining inventory of the user.
     * @param errorType The FarmingErrorType representing the type of error encountered during farming. It can be TOOL_DURABILITY or NONE.
     */
    private void handleEnd(@NotNull CommandInteraction interaction, @NotNull UserMiningInventory miningInventory, FarmingErrorType errorType) {
       try {
           int totalItems = miningInventory.getItems().values().stream().mapToInt(Integer::intValue).sum();
           FluffBOT.getInstance()
                   .getGameServiceManager()
                   .updateDurability(interaction.getUser(), miningInventory.getPlayerTool().getItemSlug(), totalItems);

           List<ItemCraftingMaterials> farmedItems = new ArrayList<>();
           for (Map.Entry<String, Integer> items : miningInventory.getItems().entrySet()) {
               ItemDescriptionBean item = FluffBOT.getInstance().getGameServiceManager().fetchItem(items.getKey());

               this.getUserManager().addItem(interaction.getUser(), item, items.getValue());

               farmedItems.add(ItemCraftingMaterials.builder()
                       .materialSlug(item.getItemName())
                       .quantity(items.getValue())
                       .build());
           }

           StringBuilder items = new StringBuilder();
           for (ItemCraftingMaterials item : farmedItems) {
               items.append("**")
                       .append(item.getMaterialSlug())
                       .append("**")
                       .append(" x ")
                       .append("**")
                       .append(NumberFormat.getNumberInstance().format(item.getQuantity()))
                       .append("**\n");
           }

           if (farmedItems.isEmpty()) {
               items.append(this.getLanguageManager().get("command.farm.no_items"));
           }

           switch (errorType) {
               case NONE -> interaction.getHook().sendMessageEmbeds(this.buildSuccess(this.getLanguageManager().get("command.farm.success", items)))
                       .queue();
               case TOOL_DURABILITY -> interaction.getHook().sendMessageEmbeds(this.buildSuccess(this.getLanguageManager().get("command.farm.tool_durability_exhausted", items)))
                       .setEphemeral(true)
                       .queue();
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    private enum FarmingErrorType {
        TOOL_DURABILITY,
        NONE
    }
}
