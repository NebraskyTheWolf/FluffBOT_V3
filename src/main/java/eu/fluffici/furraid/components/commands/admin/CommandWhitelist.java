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
import eu.fluffici.bot.api.hooks.furraid.WhitelistBuilder;
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
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmation;
import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;

public class CommandWhitelist extends FCommand {
    public CommandWhitelist() {
        super("whitelist", "Managing the local whitelist of your server", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);

        this.getSubcommandData().add(new SubcommandData("add-whitelist", "Add a member to your local whitelist")
                .addOption(OptionType.USER, "user", "Select a member", true)
        );

        this.getSubcommandData().add(new SubcommandData("remove-whitelist", "Remove a member from your local whitelist")
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
            case "add-whitelist" -> this.handleAddWhitelist(interaction);
            case "remove-whitelist" -> this.handleRemoveWhitelist(interaction);
        }
    }

    /**
     * Handles adding a user to the whitelist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleAddWhitelist(@NotNull CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();

        int count = FurRaidDB.getInstance().getGameServiceManager().localBlacklistCount(interaction.getGuild().getIdLong());
        int quota = FurRaidDB.getInstance().getOfferManager().getByGuild(interaction.getGuild().getIdLong()).getLocalWhitelist();

        GuildPremiumOffer premiumOffer = FurRaidDB.getInstance().getGameServiceManager().getGuildPremium(interaction.getGuild().getIdLong());

        if (count >= quota) {
            switch (premiumOffer.getOfferId()) {
                case 1 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.whitelist.add.quota_reached_next", count, quota, "FurRaid Lite+"))).setEphemeral(true).queue();
                case 2 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.whitelist.add.quota_reached_next", count, quota, "FurRaid Advanced+"))).setEphemeral(true).queue();
                case 3 ->
                        interaction.replyEmbeds(buildError(getLanguageManager().get("command.whitelist.add.quota_reached", count, quota))).setEphemeral(true).queue();
            }
            return;
        }

        if (FurRaidDB.getInstance().getBlacklistManager().isWhitelisted(WhitelistBuilder
                .builder()
                .guild(interaction.getGuild())
                .user(user)
                .build())) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("common.whitelist.add.already_whitelisted"))).setEphemeral(true).queue();
            return;
        }

        handleConfirmationFurRaid(interaction,
                this.getLanguageManager().get("common.whitelist.add.confirm", user.getAsMention()),
                this.getLanguageManager().get("common.whitelist.add.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        FurRaidDB.getInstance().getBlacklistManager().addWhitelist(WhitelistBuilder
                                .builder()
                                        .guild(interaction.getGuild())
                                        .user(user)
                                .build()
                        );

                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("common.whitelist.add.success", user.getAsMention()))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("common.whitelist.add.cancelled"))).setEphemeral(true).queue();
                    }
                }
        );
    }
    /**
     * Handles removing a user from the whitelist.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleRemoveWhitelist(@NotNull CommandInteraction interaction) {
        User user = interaction.getOption("user").getAsUser();

        if (!FurRaidDB.getInstance().getBlacklistManager().isWhitelisted(WhitelistBuilder
                .builder()
                .guild(interaction.getGuild())
                .user(user)
                .build())) {
            interaction.replyEmbeds(buildError(getLanguageManager().get("common.whitelist.remove.not_found"))).setEphemeral(true).queue();
            return;
        }

        handleConfirmationFurRaid(interaction,
                this.getLanguageManager().get("common.whitelist.remove.confirm", user.getAsMention()),
                this.getLanguageManager().get("common.whitelist.remove.confirm.button"),
                new ConfirmCallback() {
                    @Override
                    public void confirm(ButtonInteraction interaction) throws Exception {
                        FurRaidDB.getInstance().getBlacklistManager().removeWhitelist(WhitelistBuilder
                                .builder()
                                .guild(interaction.getGuild())
                                .user(user)
                                .build()
                        );

                        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("common.whitelist.remove.success", user.getAsMention()))).setEphemeral(true).queue();
                    }

                    @Override
                    public void cancel(ButtonInteraction interaction) throws Exception {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("common.whitelist.remove.cancelled"))).setEphemeral(true).queue();
                    }
                }
        );
    }
}