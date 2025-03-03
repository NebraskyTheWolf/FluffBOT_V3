package eu.fluffici.bot.events.interaction;

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


import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.players.RestrictedAccess;
import eu.fluffici.bot.components.commands.Command;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_MINUS;

public class SlashCommandListener extends ListenerAdapter {

    private final FluffBOT instance;

    public SlashCommandListener(FluffBOT instance) {
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
        // Blocking command access to the DMs.
        // Discord already does it by default, but this is safety feature.
        if (event.getChannelType() == ChannelType.PRIVATE) {
            event.getInteraction().reply(this.instance.getLanguageManager().get("command.dm_blocked")).setEphemeral(true).queue();
            return;
        }
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;

        // Handling terminated user(s).
        RestrictedAccess restrictedAccess = this.instance.getGameServiceManager().fetchRestrictedPlayer(event.getUser());
        boolean isRestricted = restrictedAccess != null;

        // Handling command execution.

        String commandChannel = this.instance.getChannelConfig().getProperty("channel.commands");

        Command command = this.instance.getCommandManager().findByName(event.getName());
        if (command != null) {
            if (command.getOptions().getOrDefault("channelRestricted", false) && !event.getChannel().getId().equals(commandChannel)) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.restricted"))).setEphemeral(true).queue();
                return;
            }

            if (command.getOptions().getOrDefault("isDeveloper", false) && !this.instance.getUserManager().isDeveloper(event.getUser())) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.developerOnly"))).setEphemeral(true).queue();
                return;
            }

            if (FluffBOT.getInstance().getGameServiceManager().isQuarantined(event.getUser())) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.quarantined"))).setEphemeral(true).queue();
                return;
            }

            if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(this.instance.getDefaultConfig().getProperty("roles.verified")))) {
                event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.unverified"))).setEphemeral(true).queue();
                return;
            }

            if (isRestricted) {
                User author = this.instance.getJda().getUserById(restrictedAccess.getAuthor().getId());

                event.getInteraction().replyEmbeds(command.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(command.getLanguageManager().get("common.interaction.restricted"), "https://fluffici.eu", ICON_CIRCLE_MINUS)
                        .setDescription(command.getLanguageManager().get("common.interaction.restricted.desc", restrictedAccess.getReason()))
                        .setFooter(author.getGlobalName(), author.getAvatarUrl())
                        .setColor(Color.RED)
                        .setTimestamp(restrictedAccess.getCreatedAt().toInstant())
                        .build()
                ).setEphemeral(true).queue();
                return;
            }

            if (command.getOptions().getOrDefault("noSelfUser", false)) {
                if (event.getInteraction().getOption("user") != null) {
                    UserSnowflake user = event.getInteraction().getOption("user").getAsUser();
                    if (user.equals(event.getUser())) {
                        event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.unknown"))).setEphemeral(true).queue();
                        return;
                    }
                }

                if (event.getInteraction().getOption("target") != null) {
                    UserSnowflake target = event.getInteraction().getOption("target").getAsUser();
                    if (target.equals(event.getUser())) {
                        event.getInteraction().replyEmbeds(command.buildError(command.getLanguageManager().get("command.unknown"))).setEphemeral(true).queue();
                        return;
                    }
                }
            }

            try {
                command.execute(event.getInteraction());
            } catch (Exception e) {
                throw e;
            }

            // Displaying the debug information.
            this.instance.getLogger().debug(
                    """
                    [%s] command executed on guild %s: \n
                        -> Member: %s - %s
                        -> Command:
                            -> Name: %s
                            -> Description: %s
                    """,
                    command.getClass().getCanonicalName(),
                    event.getInteraction().getGuild().getId(),
                    event.getInteraction().getMember().getId(),
                    event.getInteraction().getMember().getEffectiveName(),
                    command.getName(),
                    command.getDescription()
            );
        } else {
            event.reply(this.instance.getLanguageManager().get("command.unknown")).setEphemeral(true).queue();
        }
    }
}