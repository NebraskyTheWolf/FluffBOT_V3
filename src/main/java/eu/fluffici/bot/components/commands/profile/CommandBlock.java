package eu.fluffici.bot.components.commands.profile;

/*
---------------------------------------------------------------------------------
File Name : CommandBlock.java

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
import eu.fluffici.bot.api.beans.players.BlockedUser;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_REPORT_SEARCH;

@CommandHandle
public class CommandBlock extends Command {
    public CommandBlock() {
        super("block", "Block a user from interacting with you.", CommandCategory.PROFILE);

        List<SubcommandData> management = new ArrayList<>();
        management.add(new SubcommandData("add", "Add someone to your block-list")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Select the user to add")
                                .setRequired(true)
                )
        );
        management.add(new SubcommandData("remove", "Remove someone from your block-list")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Select the user to remove")
                                .setRequired(true)
                )
        );
        management.add(new SubcommandData("list", "Display your block-list"));

        this.getSubcommandData().addAll(management);
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String command = interaction.getSubcommandName();
        this.handleManagement(interaction, command);
    }

    private void handleManagement(CommandInteraction interaction, String command) {
        switch (command) {
            case "add" -> {
                User target = interaction.getOption("user").getAsUser();

                if (this.getUserManager().isBlocked(interaction.getUser(), target)) {
                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.block.already_blocked", target.getAsMention())))
                            .setEphemeral(true).queue();
                } else {
                    this.getUserManager().blockUser(interaction.getUser(), target);

                    interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.block.added", target.getAsMention())))
                            .setEphemeral(true).queue();
                }
            }
            case "remove" -> {
                User target = interaction.getOption("user").getAsUser();

                if (!this.getUserManager().isBlocked(interaction.getUser(), target)) {
                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.block.not_blocked", target.getAsMention())))
                            .setEphemeral(true).queue();
                } else {
                    this.getUserManager().unblockUser(interaction.getUser(), target);

                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.block.removed", target.getAsMention())))
                            .setEphemeral(true).queue();
                }
            }
            case "list" -> {
                List<BlockedUser> blockedUsers = this.getUserManager().blockedUsers(interaction.getUser());
                if (blockedUsers.isEmpty()) {
                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.block.none")))
                            .setEphemeral(true).queue();
                } else {
                    List<String> usernames = blockedUsers.stream()
                            .map(blockedUser -> FluffBOT.getInstance().getJda().getUserById(blockedUser.getTargetId()).getAsMention())
                            .toList();

                    interaction.replyEmbeds(this.getEmbed()
                            .simpleAuthoredEmbed()
                                    .setAuthor(this.getLanguageManager().get("command.block.list", "https://fluffici.eu", ICON_REPORT_SEARCH))
                                    .setDescription(this.getLanguageManager().get("command.block.description"))
                                    .setTimestamp(Instant.now())
                                    .setFooter(this.getLanguageManager().get("command.block.footer"), "https://cdn.discordapp.com/attachments/1224419443300372592/1225847106808447088/question-mark.png")
                                    .addField(this.getLanguageManager().get("command.block.users"), String.join("\n", usernames), true)
                            .build()
                    ).setEphemeral(true).queue();
                }
            }
        }
    }
}
