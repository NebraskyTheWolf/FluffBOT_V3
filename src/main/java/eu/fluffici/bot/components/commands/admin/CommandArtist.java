package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandArtist.java

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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.time.Instant;
import java.util.Objects;

import static eu.fluffici.bot.api.IconRegistry.ICON_CHECKS;

@CommandHandle
@SuppressWarnings("ALL")
public class CommandArtist extends Command {
    public CommandArtist() {
        super("artist", "Add the verified artist role to a member.", CommandCategory.ADMINISTRATOR);

        this.getSubcommandData().add(
                new SubcommandData("add", "Add the role artist to someone.")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the coins for.").setRequired(true))
        );

        this.getSubcommandData().add(
                new SubcommandData("remove", "Remove the role artist to someone.")
                        .addOptions(new OptionData(OptionType.USER, "user", "The user to manage the coins for.").setRequired(true))
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        Role artistRole = interaction.getGuild().getRoleById(FluffBOT.getInstance().getDefaultConfig().getProperty("role.artist"));
        if (artistRole == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.artist.role_error"))).setEphemeral(true).queue();
        } else {
            User currentUser = interaction.getOption("user").getAsUser();
            Member currentMember = interaction.getGuild().getMemberById(currentUser.getId());

            if (currentMember != null) {
                switch (Objects.requireNonNull(command)) {
                    case "add" -> {
                        if (currentMember.getRoles().contains(artistRole)) {
                           interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.artist.already_artist"))).setEphemeral(true).queue();
                        } else {
                            interaction.getGuild().addRoleToMember(currentUser, artistRole).reason("Adding user as server-artist.").queue();

                            TextChannel channel = (TextChannel) interaction.getChannel();
                            if (channel != null && channel.canTalk(interaction.getGuild().getSelfMember()) && channel.getName().endsWith("-ticket")) {
                                channel.sendMessageEmbeds(this.getEmbed()
                                            .simpleAuthoredEmbed()
                                            .setAuthor(this.getLanguageManager().get("command.artist.accepted"), "https://fluffici.eu", ICON_CHECKS)
                                            .setDescription(this.getLanguageManager().get("command.artist.accepted.description"))
                                            .setFooter(interaction.getUser().getEffectiveName(), interaction.getUser().getAvatarUrl())
                                            .setTimestamp(Instant.now())
                                        .build()
                                ).setContent(currentUser.getAsMention()).queue();
                            }

                            this.sendLogging(
                                    interaction.getGuild(),
                                    this.getLanguageManager().get("command.artist.logging.added"),
                                    interaction.getUser(),
                                    currentUser
                            );

                            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.artist.role_added"))).setEphemeral(true).queue();
                        }
                    }
                    case "remove" -> {
                        if (!currentMember.getRoles().contains(artistRole)) {
                            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.artist.not_an_artist"))).setEphemeral(true).queue();
                        } else {
                            interaction.getGuild().removeRoleFromMember(currentUser, artistRole).reason("Removed as server-artist").queue();
                            this.buildSuccess(this.getLanguageManager().get("command.artist.role_removed"));

                            this.sendLogging(
                                    interaction.getGuild(),
                                    this.getLanguageManager().get("command.artist.logging.removed"),
                                    interaction.getUser(),
                                    currentUser
                            );

                            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.artist.removed", currentMember.getAsMention()))).setEphemeral(true).queue();
                        }
                    }
                }
            } else {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.artist.cannot_interact"))).setEphemeral(true).queue();
            }
        }
    }
}
