/*
---------------------------------------------------------------------------------
File Name : CommandRoles

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.profile;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.roles.PurchasableRoles;
import eu.fluffici.bot.api.beans.roles.PurchasedRoles;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;

import static eu.fluffici.bot.api.IconRegistry.ICON_NOTE;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;

public class CommandRoles extends Command {
    public CommandRoles() {
        super("roles", "Select or purchase roles with your tokens", CommandCategory.PROFILE);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
        this.getOptions().put("noSelfUser", true);

        this.getSubcommandData().add(new SubcommandData("list", "Look at the roles you own."));
        this.getSubcommandData().add(new SubcommandData("select-role", "Select one role that you own.")
                .addOption(OptionType.STRING, "role-name", "The name of the role", true, true)
        );
        this.getSubcommandData().add(new SubcommandData("purchase-role", "Purchase a new role.")
                .addOption(OptionType.STRING, "role-name", "The name of the role", true, true)
        );
    }

    @Override
    @SuppressWarnings("All")
    public void execute(@NotNull CommandInteraction interaction) {
        switch (interaction.getSubcommandName()) {
            case "list" -> this.handlePurchasedRoleList(interaction, interaction.getUser());
            case "select" -> this.handleSelectPurchasedRole(interaction, interaction.getUser());
            case "purchase" -> this.handlePurchase(interaction, interaction.getUser());
        }
    }

    /**
     * Handles the list of purchased roles for a user in the role command.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param currentUser The User object representing the current user.
     */
    private void handlePurchasedRoleList(CommandInteraction interaction, User currentUser) {
        List<PurchasedRoles> purchasedRoles = FluffBOT.getInstance()
                .getGameServiceManager().fetchPurchasedRoles(currentUser);
        if (purchasedRoles.isEmpty()) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.roles.no_roles"))).setEphemeral(true).queue();
        } else {
            StringBuilder rolesList = new StringBuilder();
            for (PurchasedRoles role : purchasedRoles) {
                rolesList.append(interaction.getJDA().getRoleById(role.getRoleId()).getAsMention()).append("\n");
            }

            interaction.replyEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.getLanguageManager().get("command.roles.roles.list"), "https://fluffici.eu", ICON_NOTE)
                    .setDescription(rolesList.toString())
                    .setTimestamp(Instant.now())
                    .build()
            ).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the selection of a purchased role for a user in the role command.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param currentUser The User object representing the current user.
     */
    @SuppressWarnings("All")
    private void handleSelectPurchasedRole(@NotNull CommandInteraction interaction, User currentUser) {
        String roleName = interaction.getOption("role-name").getAsString();

        boolean selectedRole = FluffBOT.getInstance().getGameServiceManager()
                .hasPurchasedRole(currentUser, roleName);
        PurchasedRoles oldSelection = FluffBOT.getInstance().getGameServiceManager()
                .fetchSelectedRole(interaction.getUser());
        PurchasedRoles newSelection = FluffBOT.getInstance().getGameServiceManager()
                .fetchRoleById(interaction.getUser(), roleName);

        if (selectedRole) {
            Role selected = interaction.getJDA().getRoleById(newSelection.getRoleId());
            interaction.getGuild().addRoleToMember(interaction.getMember(), selected).queue();

            if (oldSelection != null) {
                oldSelection.setSelected(false);
                FluffBOT.getInstance().getGameServiceManager().updatePurchasedRole(oldSelection);
                interaction.getGuild().removeRoleFromMember(interaction.getMember(), interaction.getJDA().getRoleById(oldSelection.getRoleId())).queue();
            }

            newSelection.setSelected(true);
            FluffBOT.getInstance().getGameServiceManager().updatePurchasedRole(newSelection);

            interaction.replyEmbeds(buildError(getLanguageManager().get("command.roles.selected", selected.getAsMention()))).setEphemeral(true).queue();
        } else {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.roles.not_purchased", roleName))).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the purchase of a role for a user in the role command.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     * @param currentUser The User object representing the current user.
     */
    private void handlePurchase(@NotNull CommandInteraction interaction, User currentUser) {
        String roleName = interaction.getOption("role-name").getAsString();
        PlayerBean player = this.getUserManager().fetchUser(currentUser);

        PurchasableRoles selectedRole = FluffBOT.getInstance()
                .getGameServiceManager().fetchRoleById(roleName);
        if (selectedRole == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.roles.role_not_found"))).setEphemeral(true).queue();
        } else {
            Role role = interaction.getJDA().getRoleById(selectedRole.getRoleId());

            if (role == null) {
                interaction.replyEmbeds(buildError(getLanguageManager().get("command.roles.role_not_found"))).setEphemeral(true).queue();
                return;
            }

            if (FluffBOT.getInstance().getGameServiceManager().hasPurchasedRole(currentUser, selectedRole.getRoleId())) {
                interaction.replyEmbeds(buildError(getLanguageManager().get("command.roles.already_purchased"))).setEphemeral(true).queue();
                return;
            }

            handleConfirmation(interaction,
                    this.getLanguageManager().get("command.roles.purchase.confirm", role.getAsMention(), selectedRole.getPrice()),
                    this.getLanguageManager().get("command.roles.purchase.confirm", selectedRole.getPrice()), new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction interaction) throws Exception {
                            if (getUserManager().hasEnoughTokens(player, selectedRole.getPrice())) {
                                FluffBOT.getInstance().getGameServiceManager()
                                        .addPurchasedRoles(new PurchasedRoles(
                                                currentUser,
                                                selectedRole.getRoleId(),
                                                false
                                        ));

                                getUserManager().removeTokens(player, selectedRole.getPrice());

                                interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.roles.purchase.successful"))).setEphemeral(true).queue();
                            } else {
                                interaction.replyEmbeds(buildError(getLanguageManager().get("command.roles.insufficient_tokens"))).setEphemeral(true).queue();
                            }
                        }

                        @Override
                        public void cancel(ButtonInteraction interaction) throws Exception {
                            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.roles.purchase.cancelled"))).setEphemeral(true).queue();
                        }
                    });
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.getSubcommandName() != null) {
            switch (event.getSubcommandName()) {}
        }
    }
}