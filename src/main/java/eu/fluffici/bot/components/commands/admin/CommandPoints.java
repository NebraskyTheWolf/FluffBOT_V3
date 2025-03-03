package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandPoints.java

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
public class CommandPoints extends Command {
    public CommandPoints() {
        super("points", "Manage event points for a specific user.", CommandCategory.ADMINISTRATOR);

        this.getSubcommandData().add(
                new SubcommandData("increment", "Increase the amount of points")
                .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the event points for.").setRequired(true))
                .addOptions(new OptionData(OptionType.INTEGER, "points", "The amount of event points to add for the user.").setRequired(true))
        );

        this.getSubcommandData().add(
                new SubcommandData("decrement", "Decrease the amount of points")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the event points for.").setRequired(true))
                        .addOptions(new OptionData(OptionType.INTEGER, "points", "The amount of event points to add for the user.").setRequired(true))
        );

        this.getSubcommandData().add(
                new SubcommandData("check", "Look at the amount of event points for the user.")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the event points for.").setRequired(true))
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        PlayerBean handle = this.getUserManager().fetchUser(user);

        if (handle == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.points.user_not_found"))).setEphemeral(true).queue();
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
        int points = Math.abs(interaction.getOption("points").getAsInt());
        this.getUserManager().addEvent(player, points);

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.points.add.success", points, player.getUserId()))).setEphemeral(true).queue();
    }
    private void handleDecrement(PlayerBean player, CommandInteraction interaction) {
        int points = Math.abs(interaction.getOption("points").getAsInt());

        this.getUserManager().removeEvent(player, points);
        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.points.remove.success", points, player.getUserId()))).setEphemeral(true).queue();
    }
    private void handleCheck(PlayerBean player, CommandInteraction interaction) {
        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.points.check.success", player.getEvents()))).setEphemeral(true).queue();
    }
}
