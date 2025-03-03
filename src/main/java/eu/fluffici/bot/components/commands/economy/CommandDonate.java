package eu.fluffici.bot.components.commands.economy;

/*
---------------------------------------------------------------------------------
File Name : CommandDonate.java

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
import eu.fluffici.bot.api.CurrencyType;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.awt.*;
import java.text.NumberFormat;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
public class CommandDonate extends Command {
    public CommandDonate() {
        super("donate", "Donate your fluff tokens to another user.", CommandCategory.ECONOMY);
        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Choose the user recipient of your donation.", true));
        this.getOptionData().add(new OptionData(OptionType.INTEGER, "amount", "The number of tokens you wish to donate.", true));

        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        PlayerBean handle = this.getUserManager().fetchUser(user);

        if (handle == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.donate.user_not_found"))).queue();
            return;
        }

        if (user.getId().equals(interaction.getUser().getId())) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.donate.self_donation"))).queue();
            return;
        }

        // Adding a ABS to avoid minuses haxx.
        // Bap the stealing foxes!
        int amount = Math.abs(interaction.getOption("amount").getAsInt());

        PlayerBean issuer = this.getUserManager().fetchUser(interaction.getUser());

        // Anti-dumbass safeguard :)
        handleConfirmation(interaction, this.getLanguageManager().get("command.donate.confirm", NumberFormat.getNumberInstance().format(amount), user.getAsMention()), new ConfirmCallback() {
            @Override
            public void confirm(ButtonInteraction interaction) throws Exception {
                Pair<Boolean, String> result = getUserManager().transferTokens(issuer, CurrencyType.TOKENS, amount, handle);

                if (result.getLeft()) {
                    user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(
                            getEmbed().simpleAuthoredEmbed(interaction.getUser(),
                                            getLanguageManager().get("command.donate.success.title"),
                                            getLanguageManager().get("command.donate.success.desc", interaction.getUser().getEffectiveName()),
                                            Color.ORANGE)
                                    .addField(getLanguageManager().get("command.donate.success.field.old_balance"), String.valueOf(handle.getTokens()), true)
                                    .addField(getLanguageManager().get("command.donate.success.field.new_balance"), String.valueOf(Math.abs(handle.getTokens() + amount)), true)
                                    .build()
                    )).queue();

                    FluffBOT.getInstance().getAchievementManager().unlock(interaction.getUser(), 37);

                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.donate.success", amount, user.getEffectiveName()))).queue();
                } else {
                    interaction.replyEmbeds(buildError(getLanguageManager().get("command.donate.insufficient_tokens"))).queue();
                }
            }

            @Override
            public void cancel(ButtonInteraction interaction) throws Exception {
                interaction.replyEmbeds(buildError(getLanguageManager().get("command.donate.cancelled", user.getAsMention()))).queue();
            }
        });
    }
}