/*
---------------------------------------------------------------------------------
File Name : CommandVerification

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.moderator;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_SLASHED;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class CommandVerification extends Command {
    public CommandVerification() {
        super("verification", "Management of the user verification", CommandCategory.MODERATOR);


        this.getSubcommandGroupData().add(new SubcommandGroupData("verification", "Managing the verification of members")
                .addSubcommands(new SubcommandData("manual-verification", "Manually verifying a member on the server")
                        .addOption(OptionType.USER, "user", "Select the member to verify")
                )
                .addSubcommands(new SubcommandData("kick-all-unverified", "Kicking all unverified members"))
        );

        this.getSubcommandGroupData().add(new SubcommandGroupData("verification-reminder", "Managing the reminder of verification")
                .addSubcommands(new SubcommandData("pending-reminder", "List all members that is on pending list"))
                .addSubcommands(new SubcommandData("add-reminder", "Add a member to the reminder manually"))
                .addSubcommands(new SubcommandData("remove-reminder", "Remove a member manually from the reminder"))
        );
    }

    /**
     * Executes the specified command based on the subcommand name.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        switch (command) {
            case "manual-verification" -> this.handleManualVerification(interaction);
            case "kick-all-unverified" -> this.handleKickAllUnverified(interaction);
            case "pending-reminder" -> this.handlePendingReminders(interaction);
            case "add-reminder" -> this.handleAddReminder(interaction);
            case "remove-reminder" -> this.handleRemoveReminder(interaction);
        }
    }

    /**
     * Handles the manual verification of a member on the server.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleManualVerification(@NotNull CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        Member targetMember = interaction.getGuild().getMember(user);

        if (targetMember == null) {
            interaction.replyEmbeds(this.buildError("Invalid user specified.")).queue();
            return;
        }

        Role unverified = interaction.getGuild().getRoleById(
                FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified")
        );

        Role verified = interaction.getGuild().getRoleById(
                FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified")
        );

        if (verified == null || unverified == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.roles_not_found"))).setEphemeral(true).queue();
            return;
        } else if (targetMember.getRoles().contains(verified)) {
            interaction.replyEmbeds(buildError(this.getLanguageManager().get("command.verification.manual.already_verified"))).queue();
            return;
        }

        this.sendLogging(
                interaction.getGuild(),
                "Manually Verified",
                interaction.getUser(),
                user
        );

        FluffBOT.getInstance().getGameServiceManager().removeReminder(targetMember);

        interaction.getGuild().addRoleToMember(targetMember, verified).queue();
        interaction.getGuild().removeRoleFromMember(targetMember, unverified).queue();
        interaction.replyEmbeds(buildSuccess(this.getLanguageManager().get("command.verification.manual.success"))).setEphemeral(true).queue();
    }

    /**
     * Handles kicking all unverified members.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleKickAllUnverified(@NotNull CommandInteraction interaction) {
        Role unverified = interaction.getGuild().getRoleById(
                FluffBOT.getInstance().getDefaultConfig().getProperty("roles.unverified")
        );
        List<Member> unverifiedMembers = interaction.getGuild().getMembersWithRoles(unverified)
                .stream()
                .filter(member -> !FluffBOT.getInstance().getGameServiceManager().hasExpiredReminder(member))
                .distinct()
                .toList();
        List<String> memberGlobalNames = unverifiedMembers
                .stream()
                .map(member -> member.getUser().getGlobalName()).toList();

        handleCustomModal(interaction, "Kicking all unverified members", listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"),  TextInputStyle.PARAGRAPH)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();

            if (unverifiedMembers.isEmpty()) {
                modalInteraction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.verification.no_unverified_members")))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String unverifiedCount = NumberFormat.getNumberInstance().format(unverifiedMembers.size());

            if (unverifiedMembers.size() >= 10) {
                handleConfirmation(modalInteraction,
                        this.getLanguageManager().get("command.verification.kick.confirm"),
                        this.getLanguageManager().get("command.verification.kick.confirm.button", unverifiedCount),
                        new ConfirmCallback() {
                            @Override
                            public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                                for (Member member : unverifiedMembers) {
                                    if (!member.getUser().isBot()) {
                                        FluffBOT.getInstance().getSanctionManager().kick(
                                                member,
                                                modalInteraction.getUser(),
                                                reason,
                                                null
                                        );
                                    }
                                }

                                buttonInteraction.replyEmbeds(getEmbed()
                                        .simpleAuthoredEmbed()
                                        .setAuthor(getLanguageManager().get("common.verification.kick.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                                        .setDescription(getLanguageManager().get("common.verification.kick.success.description", String.join("\n * ", memberGlobalNames), reason))
                                        .build()
                                ).setEphemeral(true).queue();
                            }

                            @Override
                            public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                                buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.verification.kick.cancelled"))).setEphemeral(true).queue();
                            }
                        }
                );
            } else {
                for (Member member : unverifiedMembers) {
                    if (!member.getUser().isBot()) {
                        FluffBOT.getInstance().getSanctionManager().kick(
                                member,
                                modalInteraction.getUser(),
                                reason,
                                null
                        );
                    }
                }

                modalInteraction.replyEmbeds(this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("common.verification.kick.success"), "https://fluffici.eu", ICON_CIRCLE_SLASHED.getUrl())
                        .setDescription(this.getLanguageManager().get("common.verification.kick.success.description", String.join("\n * ", memberGlobalNames), reason))
                        .build()
                ).setEphemeral(true).queue();
            }
        });
    }

    /**
     * Handles pending reminders for user verification.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handlePendingReminders(CommandInteraction interaction) {
        HashMap<UserSnowflake, Long> pendingReminders = FluffBOT.getInstance().getGameServiceManager().fetchAllReminders();
        StringBuilder builder = new StringBuilder();

        AtomicInteger index = new AtomicInteger(0);

        for (HashMap.Entry<UserSnowflake, Long> e : pendingReminders.entrySet()) {
            builder.append(" * ").append(index.incrementAndGet()).append(" - ").append(e.getKey().getAsMention()).append(" : Remaining time: ").append(NumberFormat.getNumberInstance().format(e.getValue())).append(" hours").append("\n");
        }

        if (pendingReminders.isEmpty()) {
            builder.append("* No pending reminders.");
        }

        interaction.replyEmbeds(this.buildSuccess(builder.toString())).setEphemeral(true).queue();
    }

    /**
     * Handles the addition of a member to the reminder manually.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleAddReminder(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        Member targetMember = interaction.getGuild().getMember(user);

        if (targetMember == null) {
            interaction.replyEmbeds(this.buildError("Invalid user specified.")).setEphemeral(true).queue();
            return;
        }

        FluffBOT.getInstance().getGameServiceManager().addReminder(targetMember);
        interaction.replyEmbeds(this.buildSuccess("Member added to reminder list.")).setEphemeral(true).queue();
    }

    /**
     * Handles the removal of a member from the reminder manually.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    private void handleRemoveReminder(CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();
        Member targetMember = interaction.getGuild().getMember(user);

        if (targetMember == null) {
            interaction.replyEmbeds(this.buildError("Invalid user specified.")).setEphemeral(true).queue();
            return;
        }

        FluffBOT.getInstance().getGameServiceManager().removeReminder(targetMember);
        interaction.replyEmbeds(this.buildSuccess("Member removed from reminder list.")).setEphemeral(true).queue();
    }
}