package eu.fluffici.bot.components.commands.games;

/*
---------------------------------------------------------------------------------
File Name : CommandGamble.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.gamble.Gamble;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@CommandHandle
public class CommandGamble extends Command {

    // Loading the gamble algorithm
    private final Gamble gamble = new Gamble(
            Integer.parseInt(FluffBOT.getInstance().getDefaultConfig().getProperty("command.gamble.matrix.x", "2")),
            Integer.parseInt(FluffBOT.getInstance().getDefaultConfig().getProperty("command.gamble.matrix.z", "2")),
            Integer.parseInt(FluffBOT.getInstance().getDefaultConfig().getProperty("command.gamble.matrix.gap", "500"))
    );

    public CommandGamble() {
        super("gamble", "Gamble your money and try to get your deposit multiplied.", CommandCategory.GAMES);

        this.getOptions().put("channelRestricted", true);
        this.getOptionData().add(new OptionData(OptionType.INTEGER, "amount", "Amount to gamble.", true));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        PlayerBean player = this.getUserManager().fetchUser(interaction.getUser());
        int amount = Math.abs(interaction.getOption("amount").getAsInt());

        if (!this.getUserManager().hasEnoughTokens(player, amount)) {
            interaction.replyEmbeds(this.buildError(
                    this.getLanguageManager().get("command.gamble.error.not_enough")
            )).queue();
        } else {
            boolean result = gamble.isDrawn(amount);

            if (result) {
                interaction.replyEmbeds(this.buildSuccess(
                        this.getLanguageManager().get("command.gamble.error.success_oh_no_we_lost_uwu", (amount * 2))
                )).queue();
                this.getUserManager().addTokens(player, (amount * 2));
            } else {
                interaction.replyEmbeds(this.buildError(
                        this.getLanguageManager().get("command.gamble.error.baited_hehehe", amount)
                )).queue();

                this.getUserManager().removeTokens(player, amount);
            }
        }
    }
}
