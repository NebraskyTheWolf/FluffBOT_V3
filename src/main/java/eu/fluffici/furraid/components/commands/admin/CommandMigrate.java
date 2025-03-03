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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class CommandMigrate extends FCommand {
    public CommandMigrate() {
        super("migrate", "Add your server to FurRaidDB Handle.", CommandCategory.ADMINISTRATOR);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("noSelfUser", true);
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    @Override
    @SuppressWarnings("All")
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        if (settings == null) {

        }
    }
}