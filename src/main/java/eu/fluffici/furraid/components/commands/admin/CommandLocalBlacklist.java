/*
---------------------------------------------------------------------------------
File Name : CommandGlobalBlacklist

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.admin;

import eu.fluffici.bot.api.beans.furraid.GuildPremiumOffer;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.LocalBlacklist;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;
import static eu.fluffici.bot.components.modal.custom.CustomModalHandler.handleCustomModal;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

public class CommandLocalBlacklist extends FCommand {
    public CommandLocalBlacklist() {
        super("local-blacklist", "Managing the global blacklist of FurRaidDB", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);

        this.getSubcommandData().add(new SubcommandData("add", "Add a member to your local blacklist")
                .addOption(OptionType.USER, "user", "Select a member", true)
        );

        this.getSubcommandData().add(new SubcommandData("remove", "Remove a member from your local blacklist")
                .addOption(OptionType.USER, "user", "Select a member", true)
        );
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        String command = interaction.getSubcommandName();

        switch (command) {
            case "add" -> this.handleAddBlacklist(interaction, settings);
            case "remove" -> this.handleRemoveBlacklist(interaction, settings);
        }
    }

    /**
     * Handles the addition of a member to the local blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleAddBlacklist(CommandInteraction interaction, GuildSettings settings) {
        if (!settings.getConfig().getSettings().isUsingLocalBlacklist()) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.disabled"))).setEphemeral(true).queue();
            return;
        }

        int count = FurRaidDB.getInstance().getGameServiceManager().localBlacklistCount(interaction.getGuild().getIdLong());
        int quota = FurRaidDB.getInstance().getOfferManager().getByGuild(interaction.getGuild().getIdLong()).getLocalBlacklist();

        GuildPremiumOffer premiumOffer = FurRaidDB.getInstance().getGameServiceManager().getGuildPremium(interaction.getGuild().getIdLong());

        if (count >= quota) {
            switch (premiumOffer.getOfferId()) {
                case 1 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.quota_reached_next", count, quota, "FurRaid Lite+"))).setEphemeral(true).queue();
                case 2 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.quota_reached_next", count, quota, "FurRaid Advanced+"))).setEphemeral(true).queue();
                case 3 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.quota_reached", count, quota))).setEphemeral(true).queue();
            }
            return;
        }

        User user = interaction.getOption("user").getAsUser();

        LocalBlacklist localBlacklist = FurRaidDB.getInstance().getBlacklistManager().fetchLocalBlacklist(interaction.getGuild(), user);

        if (localBlacklist != null) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.already_blacklisted", user.getAsMention()))).setEphemeral(true).queue();
            return;
        }

        handleCustomModal(interaction, this.getLanguageManager().get("command.local_blacklist.modal.title"), listOf(ActionRow.of(
                TextInput.create("row:reason", this.getLanguageManager().get("common.reason"), TextInputStyle.PARAGRAPH)
                        .setMaxLength(1000)
                        .setMinLength(2)
                        .build()
        )), GameId.generateId(), (modalInteraction, acceptanceId) -> {
            String reason = modalInteraction.getValue("row:reason").getAsString();

            handleConfirmationFurRaid(modalInteraction,
                    this.getLanguageManager().get("command.local_blacklist.add.confirm", user.getGlobalName(), reason),
                    this.getLanguageManager().get("command.local_blacklist.add.confirm.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                            FurRaidDB.getInstance().getBlacklistManager().addLocalBlacklist(BlacklistBuilder
                                    .builder()
                                            .guild(interaction.getGuild())
                                            .user(user)
                                            .reason(reason)
                                            .author(interaction.getUser())
                                    .build()
                            );

                            buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.local_blacklist.add.success", user.getAsMention(), reason))).setEphemeral(true).queue();
                        }

                        @Override
                        public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                            buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.add.cancelled"))).setEphemeral(true).queue();
                        }
                    }
            );
        });
    }

    /**
     * Handles the removal of a member from the local blacklist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleRemoveBlacklist(@NotNull CommandInteraction interaction, GuildSettings settings) {
        if (!settings.getConfig().getSettings().isUsingLocalBlacklist()) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.remove.disabled"))).setEphemeral(true).queue();
            return;
        }

        User user = interaction.getOption("user").getAsUser();

        LocalBlacklist localBlacklist = FurRaidDB.getInstance().getBlacklistManager().fetchLocalBlacklist(interaction.getGuild(), user);

        if (localBlacklist == null) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.remove.not_found", user.getAsMention()))).setEphemeral(true).queue();
            return;
        }

        handleConfirmationFurRaid(interaction,
                this.getLanguageManager().get("command.local_blacklist.remove.confirm", user.getGlobalName()),
                this.getLanguageManager().get("command.local_blacklist.remove.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                        FurRaidDB.getInstance().getBlacklistManager().removeLocalBlacklist(interaction.getGuild(), user);

                        buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.local_blacklist.remove.success", user.getAsMention()))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                        buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.local_blacklist.remove.cancelled"))).setEphemeral(true).queue();
                    }
                }
        );
    }
}