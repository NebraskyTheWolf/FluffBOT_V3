package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandToken.java

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


import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

@CommandHandle
public class CommandToken extends Command {
    public CommandToken() {
        super("token", "Manage tokens for a specific user.", CommandCategory.ADMINISTRATOR);

        this.getSubcommandData().add(
                new SubcommandData("increment", "Increase the amount of token")
                .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the token for.").setRequired(true))
                .addOptions(new OptionData(OptionType.NUMBER, "token", "The amount of token to add for the user.").setRequired(true))
        );

        this.getSubcommandData().add(
                new SubcommandData("decrement", "Decrease the amount of token")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the token for.").setRequired(true))
                        .addOptions(new OptionData(OptionType.NUMBER, "token", "The amount of token to add for the user.").setRequired(true))
        );

        this.getSubcommandData().add(
                new SubcommandData("check", "Look at the amount of token for the user.")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the token for.").setRequired(true))
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        PlayerBean handle = this.getUserManager().fetchUser(user);

        if (handle == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.token.user_not_found"))).setEphemeral(true).queue();
            return;
        }

        String commandName = interaction.getSubcommandName();


        switch (Objects.requireNonNull(commandName)) {
            case "increment":
                handleIncrement(handle, interaction);
                break;
            case "decrement":
                handleDecrement(handle, interaction);
                break;
            case "check":
                handleCheck(handle, interaction);
                break;
        }
    }

    private void handleIncrement(PlayerBean player, CommandInteraction interaction) {
        int tokenAmount = Math.abs(interaction.getOption("token").getAsInt());
        this.getUserManager().addTokens(player, tokenAmount);

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.token.add.success", tokenAmount, player.getUserId()))).setEphemeral(true).queue();
    }
    private void handleDecrement(PlayerBean player, CommandInteraction interaction) {
        int tokenAmount = Math.abs(interaction.getOption("token").getAsInt());

        this.getUserManager().removeTokens(player, tokenAmount);
        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.token.remove.success", tokenAmount, player.getUserId()))).setEphemeral(true).queue();
    }
    private void handleCheck(PlayerBean player, CommandInteraction interaction) {
        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.token.check.success", player.getTokens()))).setEphemeral(true).queue();
    }
}
