/*
---------------------------------------------------------------------------------
File Name : CommandCraft

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.commands.games;

import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

public class CommandSmelt extends Command {
    public CommandSmelt() {
        super("smelt", "Smelt your material(s) and use them for crafting item(s)", CommandCategory.GAMES);
    }

    @Override
    public void execute(CommandInteraction interaction) {

    }
}