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
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("All")
@CommandHandle
public class CommandNickset extends Command {
    public CommandNickset() {
        super("nickset", "Change the nickname of a user", CommandCategory.ADMINISTRATOR);

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Select the user", true));
        this.getOptionData().add(new OptionData(OptionType.STRING, "nickname", "Enter the new nickname, is left empty the nickname will be removed!", false));

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        String newNickname = interaction.getOption("nickname").getAsString();

        PlayerBean player = this.getUserManager().fetchUser(user);

        if  (newNickname.isEmpty()) {
            player.setNickname(null);
            this.getUserManager().saveUser(player);
            interaction.getGuild()
                    .modifyNickname(interaction.getGuild().getMember(user), null)
                    .reason("Changed by ".concat(interaction.getUser().getGlobalName()).concat(" with /nickset"))
                    .queue();
        } else {
            player.setNickname(newNickname);
            this.getUserManager().saveUser(player);
            interaction.getGuild()
                    .modifyNickname(interaction.getGuild().getMember(user), null)
                    .reason("Changed by ".concat(interaction.getUser().getGlobalName()).concat(" with /nickset"))
                    .queue();
        }

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.nickset.success")))
                   .setEphemeral(true)
                   .queue();
    }
}
