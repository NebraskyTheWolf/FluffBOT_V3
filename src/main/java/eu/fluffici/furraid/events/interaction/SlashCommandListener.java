package eu.fluffici.furraid.events.interaction;

/*
---------------------------------------------------------------------------------
File Name : SlashCommandListener.java

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

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {

    private final FurRaidDB instance;

    public SlashCommandListener(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * This method is a listener for slash command events. It handles the execution of slash commands
     * triggered by users in a Discord server. It ensures that necessary objects are not null to avoid
     * NullPointerExceptions. Upon receiving a slash command event, it executes the associated command
     * and logs information about the execution, including the canonical name of the command handle class,
     * the ID of the guild where the command was executed, the ID of the member who executed the command,
     * as well as the name and description of the command itself.
     *
     * @param event The SlashCommandInteractionEvent object representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        GuildSettings guildSettings = this.instance
                .getBlacklistManager()
                .fetchGuildSettings(event.getGuild());

        if (guildSettings == null) {
            event.getInteraction().reply("A problem occurred while reading your server settings, Please contact the support ASAP.").queue();
            return;
        }

        // Loading the guild language
        this.instance.getLanguageManager()
                .loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        FCommand command = this.instance.getCommandManager().findByName(event.getName());
        // Replacing the instance of LanguageManager for inheritance
        command.setLanguageManager(this.instance.getLanguageManager());

        if (command != null) {
            if (guildSettings.isBlacklisted()) {
                event.getInteraction().reply("""
                        Your server has been blacklisted due to TOS violation
                        -# Please go to the [Dashboard](https://frdb.fluffici.eu/dashboard) to learn more.
                        """).queue();
                return;
            }

            if (command.getOptions().getOrDefault("isStaff", false) && !this.instance.getBlacklistManager().isStaff(event.getUser())) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.staff_only"))).setEphemeral(true).queue();
                return;
            }

            if (guildSettings.getConfig().getSettings().getDisabledCommands().contains(event.getCommandId())) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.disabled"))).setEphemeral(true).queue();
                return;
            }

            if (command.getOptions().getOrDefault("noSelfUser", false)) {
                if (event.getInteraction().getOption("user") != null) {
                    UserSnowflake user = event.getInteraction().getOption("user").getAsUser();
                    if (user.equals(event.getUser())) {
                        event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.cannot_self_execute"))).setEphemeral(true).queue();
                        return;
                    }
                }

                if (event.getInteraction().getOption("target") != null) {
                    UserSnowflake target = event.getInteraction().getOption("target").getAsUser();
                    if (target.equals(event.getUser())) {
                        event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.cannot_self_execute"))).setEphemeral(true).queue();
                        return;
                    }
                }
            }

            command.execute(event.getInteraction(), guildSettings);
        } else {
            event.reply(this.instance.getLanguageManager().get("command.unknown")).setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

        if (event.getSubcommandName() != null) {
            if (event.getSubcommandName().equals("command")) {
                List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = this.instance.getCommandManager().getAllCommands().stream()
                        .filter(command -> {
                            CommandCategory category = command.getCategory();
                            return !category.isRestricted() || (category.isRestricted() && event.getMember().hasPermission(category.getPermission()));
                        })
                        .map(FCommand::getName)
                        .filter(name -> name.toLowerCase().startsWith(userInput))
                        .limit(25)
                        .map(name -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(name, name))
                        .collect(Collectors.toList());

                event.replyChoices(choices).queue();
            }
        }
    }
}