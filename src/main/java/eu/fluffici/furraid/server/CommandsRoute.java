/*
---------------------------------------------------------------------------------
File Name : CommandsRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/07/2024
Last Modified : 08/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandsRoute extends WebRoute {

    private final List<String> FILTERED_COMMANDS = new CopyOnWriteArrayList<>();

    public CommandsRoute() {
        super("/commands", RouteMethod.GET);

        this.FILTERED_COMMANDS.add("about");
        this.FILTERED_COMMANDS.add("donate");
        this.FILTERED_COMMANDS.add("feedback");
        this.FILTERED_COMMANDS.add("help");
        this.FILTERED_COMMANDS.add("support");
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    public void onRequest(HttpExchange request) {
        JsonArray commands = new JsonArray();

        FurRaidDB.getInstance()
                .getJda()
                .retrieveCommands()
                .complete()
                .stream()
                .toList()
                .forEach(fCommand -> {
                    if (!this.FILTERED_COMMANDS.contains(fCommand.getName())) {
                        JsonObject command = new JsonObject();
                        command.addProperty("id", fCommand.getId());
                        command.addProperty("name", fCommand.getName());
                        commands.add(command);
                    }
                });

        sendJsonResponse(request, commands);
    }
}