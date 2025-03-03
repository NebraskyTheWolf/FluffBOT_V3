package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandCoins.java

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
import eu.fluffici.bot.api.item.EquipmentSlot;
import eu.fluffici.bot.api.item.EquipmentType;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@CommandHandle
public class CommandGive extends Command {
    public CommandGive() {
        super("give", "Give a item to a member", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select a user", true));
        this.getOptionData().add(new OptionData(OptionType.STRING, "itemname", "Select the item slug.", true, true));
        this.getOptionData().add(new OptionData(OptionType.INTEGER, "quantity", "Quantity to give", true));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        String itemSlug = interaction.getOption("itemname").getAsString();
        int quantity = Math.abs(interaction.getOption("quantity").getAsInt());

       try {
           ItemDescriptionBean item = FluffBOT.getInstance()
                   .getGameServiceManager()
                   .fetchItem(itemSlug);

           if (item != null) {
               if (!(item.isStackable() || item.isEquipment() || item.getEquipmentSlug() != EquipmentSlot.NONE
                       || item.getEquipmentType() != EquipmentType.ITEM)
                       && this.getUserManager().hasItem(user, itemSlug)) {
                   interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.give.item.not_given"))).setEphemeral(true).queue();
                   return;
               }
               this.getUserManager().addItem(user, item, quantity);
               interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.give.item.given"))).setEphemeral(true).queue();
           } else {
               interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.give.item.not_found"))).setEphemeral(true).queue();
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        try {
            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getAllItems()
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
