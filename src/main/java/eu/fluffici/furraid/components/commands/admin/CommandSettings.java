/*
---------------------------------------------------------------------------------
File Name : CommandSettings

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 19/06/2024
Last Modified : 19/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.admin;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.bot.components.button.confirm.ConfirmCallback;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.components.button.confirm.ConfirmHandler.handleConfirmationFurRaid;

public class CommandSettings extends FCommand {
    public CommandSettings() {
        super("settings", "Manage the settings of your server for FurRaidDB", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        this.getOptions().put("noSelfUser", true);

        this.getSubcommandData().add(new SubcommandData("set-logging-channel", "Select the channel where the log is sent")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Select a channel", true)
                        .setChannelTypes(ChannelType.TEXT))
        );

        this.getSubcommandData().add(new SubcommandData("set-language", "Change the language of FurRaidDB for your server")
                .addOptions(new OptionData(OptionType.STRING, "language", "Select the language")
                        .addChoice("English", "en")
                        .addChoice("Czech", "cs")
                )
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
            case "set-logging-channel" -> this.handleLoggingChannel(interaction, settings);
            case "set-language" -> this.handleSetLanguage(interaction, settings);
        }
    }

    /**
     * Handles setting the language for a command.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleSetLanguage(@NotNull CommandInteraction interaction, GuildSettings settings) {
        String language = interaction.getOption("language").getAsString();

        settings.getConfig().getSettings().setLanguage(language);
        FurRaidDB.getInstance().getGameServiceManager().updateGuildSettings(settings);

        interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.settings.language.success", language))).setEphemeral(true).queue();
    }

    /**
     * Handles the logging channel for the command.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @SuppressWarnings("All")
    private void handleLoggingChannel(@NotNull CommandInteraction interaction, GuildSettings settings) {
        TextChannel channel = interaction.getOption("channel").getAsChannel().asTextChannel();

        Role publicRole = interaction.getGuild().getPublicRole();
        PermissionOverride permissionOverride = channel.getPermissionOverride(publicRole);
        boolean isPublic = permissionOverride != null && permissionOverride.getAllowed().contains(Permission.VIEW_CHANNEL);

        if (isPublic) {
            handleConfirmationFurRaid(interaction,
                    this.getLanguageManager().get("command.settings.logging_channel.confirm", channel.getName()),
                    this.getLanguageManager().get("command.local_blacklist.logging_channel.confirm.button"),
                    new ConfirmCallback() {
                        @Override
                        public void confirm(ButtonInteraction buttonInteraction) throws Exception {
                            settings.setLoggingChannel(channel.getId());
                            FurRaidDB.getInstance().getBlacklistManager().updateGuild(settings);

                            buttonInteraction.replyEmbeds(buildSuccess(getLanguageManager().get("command.settings.logging_channel.success", channel.getAsMention()))).setEphemeral(true).queue();
                        }

                        @Override
                        public void cancel(ButtonInteraction buttonInteraction) throws Exception {
                            buttonInteraction.replyEmbeds(buildError(getLanguageManager().get("command.settings.logging_channel.cancelled"))).setEphemeral(true).queue();
                        }
                    }
            );
        } else {
            settings.setLoggingChannel(channel.getId());
            FurRaidDB.getInstance().getBlacklistManager().updateGuild(settings);

            interaction.replyEmbeds(buildSuccess(getLanguageManager().get("command.settings.logging_channel.success", channel.getAsMention()))).setEphemeral(true).queue();
        }
    }
}