package eu.fluffici.bot.components.commands.moderator;

/*
---------------------------------------------------------------------------------
File Name : CommandWarn.java

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
import eu.fluffici.bot.api.beans.players.PermanentRole;
import eu.fluffici.bot.api.beans.players.SanctionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

@CommandHandle
@SuppressWarnings("All")
public class CommandWarn extends Command {

    private final FluffBOT instance;

    public CommandWarn(FluffBOT instance) {
        super("warn", "Issue a warning to a user in the server.", CommandCategory.MODERATOR);
        this.instance = instance;

        this.getOptionData().add(new OptionData(OptionType.USER, "user", "Enter the ID of the user", true));
        this.getOptionData().add(new OptionData(OptionType.STRING, "reason", "Specify the reason for issuing the warn", true));
        this.getOptionData().add(new OptionData(OptionType.ATTACHMENT, "attachment", "The screenshot for proof", true));

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        Member currentMember = interaction.getGuild().getMemberById(user.getId());

        String reason = interaction.getOption("reason").getAsString();

        Message.Attachment attachment = interaction.getOption("attachment").getAsAttachment();

        boolean result = this.instance.getSanctionManager().warn(
                interaction.getGuild(),
                user,
                interaction.getUser(),
                reason,
                attachment
        );

        int warns = FluffBOT.getInstance()
                .getGameServiceManager()
                .getAllSanctions()
                .stream()
                .filter(sanctionBean -> sanctionBean.getUserId().equals(user.getId()))
                .filter(sanctionBean -> sanctionBean.getTypeId() == SanctionBean.WARN)
                .toList().size();

        if (result) {
            Role role = this.getRoleFromWarn(warns);

            if (role != null) {
                interaction.getGuild().addRoleToMember(currentMember, role).queue();
                FluffBOT.getInstance().getGameServiceManager().addPermanentRole(new PermanentRole(
                        currentMember,
                        role.getId()
                ));
            }

            if (warns >= 3) {
                int banDays = (warns - 1 >= 3 ? 14 : 7);
                handleConfirmation(interaction,
                        this.getLanguageManager().get("command.warn.ban.approval", user.getAsMention(), warns, banDays),
                        this.getLanguageManager().get("command.warn.ban.approval.button"),
                        new ConfirmCallback() {
                            @Override
                            public void confirm(ButtonInteraction interaction) throws Exception {
                                boolean result = instance.getSanctionManager().ban(
                                        interaction.getGuild(),
                                        user,
                                        interaction.getUser(),
                                        getLanguageManager().get("command.warn.ban.reason", warns, banDays),
                                        Instant.now().plus(banDays, ChronoUnit.DAYS).toEpochMilli(),
                                        attachment
                                );

                                if (result) {
                                    interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.warn.ban.success", user.getAsMention(), banDays))).setEphemeral(true).queue();
                                } else {
                                    interaction.replyEmbeds(buildError(getLanguageManager().get("command.warn.ban.failed"))).setEphemeral(true).queue();
                                }
                            }

                            @Override
                            public void cancel(ButtonInteraction interaction) throws Exception {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.warn.ban.cancelled", user.getAsMention()))).setEphemeral(true).queue();
                            }
                        }
                );
            } else {
                interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.warn.success", user.getEffectiveName()))).setEphemeral(true).queue();
            }
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.warn.failed"))).setEphemeral(true).queue();
        }

        this.getUserManager().addPointToStaff(interaction.getUser(), 5);
    }

    /**
     * Retrieves a Role object corresponding to the specified index from the warn roles.
     *
     * @param index The index of the warn role (1, 2, or 3)
     * @return The Role object corresponding to the specified index, or null if the index is invalid
     */
    private Role getRoleFromWarn(int index) {
        return switch (index) {
            case 1 -> this.instance.getJda().getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.one"));
            case 2 -> this.instance.getJda().getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.two"));
            case 3 -> this.instance.getJda().getRoleById(this.instance.getDefaultConfig().getProperty("role.warn.three"));
            default -> null;
        };
    }
}