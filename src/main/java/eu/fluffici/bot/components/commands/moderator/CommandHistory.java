package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandHistory.java

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
import eu.fluffici.bot.api.DurationUtil;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

@CommandHandle
@SuppressWarnings("All")
public class CommandHistory extends Command {
    private final FluffBOT instance;

    public CommandHistory(FluffBOT instance) {
        super("history", "Displays a user's moderation history.", CommandCategory.MODERATOR);

        this.instance = instance;

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Input the userId to view their moderation history.", true));
        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();

        try {
            List<SanctionBean> sanctions = this.instance.getGameServiceManager().getAllSanctions();
            List<SanctionBean> userSanctions = sanctions.stream()
                    .filter(sanction -> sanction.getUserId().equals(user.getId()))
                    .sorted(Comparator.comparing(SanctionBean::getCreatedAt).reversed())
                    .limit(25)
                    .toList();

            if (userSanctions.isEmpty()) {
                interaction.replyEmbeds(
                        this.getEmbed().simpleAuthoredEmbed(user, this.getLanguageManager().get("command.history.not_found.title"), this.getLanguageManager().get("command.history.not_found.desc"), Color.GREEN)
                                .build()
                ).setEphemeral(true).queue();
            } else {
                List<MessageEmbed.Field> fields = userSanctions.stream()
                        .map(sanction -> new MessageEmbed.Field(
                                this.getLanguageManager().get("command.history.field.title", this.getTypeFromId(sanction.getTypeId())),
                                this.getLanguageManager().get("command.history.field.value", sanction.getReason(), DurationUtil.getDuration(sanction.getCreatedAt().getTime()).toString(), interaction.getGuild().getMemberById(sanction.getAuthorId()).getUser().getEffectiveName()), true))
                        .toList();

                EmbedBuilder embedBuilder = this.getEmbed().simpleAuthoredEmbed(user,
                        this.getLanguageManager().get("command.history.found.title"),
                        this.getLanguageManager().get("command.history.found.desc", userSanctions.size()),
                        Color.RED
                );
                embedBuilder.getFields().addAll(fields);

                interaction.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            interaction.reply(this.getLanguageManager().get("command.history.error")).setEphemeral(true).queue();
        }
    }

    private String getTypeFromId(int id) {
        return switch (id) {
            case 1 -> "Warning";
            case 2 -> "Banishment";
            case 3 -> "Expulsion";
            case 4 -> "Silencing";
            default -> "Unknown Moderation Type";
        };
    }
}