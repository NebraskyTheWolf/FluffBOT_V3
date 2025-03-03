package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandKick.java

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
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@CommandHandle
@SuppressWarnings("All")
public class CommandKick extends Command {

    private final FluffBOT instance;

    public CommandKick(FluffBOT instance) {
        super("kick", "This command allows you to kick a specific user.", CommandCategory.MODERATOR);

        this.instance = instance;
        this.getOptionData().add(new OptionData(OptionType.USER, "user", "The user to kick", true));
        this.getOptionData().add(new OptionData(OptionType.STRING, "reason", "The reason for the ban", true));
        this.getOptionData().add(new OptionData(OptionType.ATTACHMENT, "attachment", "The screenshot for proof", true));

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        String reason = interaction.getOption("reason").getAsString();
        Message.Attachment attachment = interaction.getOption("attachment").getAsAttachment();

        this.instance.getSanctionManager().kick(
                interaction.getGuild().getMember(user),
                interaction.getUser(),
                reason,
                attachment
        );

        this.getUserManager().addPointToStaff(interaction.getUser(), 5);
        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.kick.success", user.getGlobalName(), reason))).setEphemeral(true).queue();
    }
}