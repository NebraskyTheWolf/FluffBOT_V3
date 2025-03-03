/*
---------------------------------------------------------------------------------
File Name : ChangelogsRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/07/2024
Last Modified : 04/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.Changelogs;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;

/**
 * A class representing the home route of a web application.
 */
public class ChangelogsRoute extends WebRoute {

    private final Gson gson = new Gson();

    public ChangelogsRoute() {
        super("/changelogs", RouteMethod.GET);
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(HttpExchange request) {
        Changelogs changelogs = FurRaidDB.getInstance().getGameServiceManager().getLatestChangelogs();

        if (changelogs == null) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "No changelogs have been found.");
            sendJsonResponse(request, error);
            return;
        }

        sendJsonResponse(request, this.gson.fromJson(this.gson.toJson(changelogs), JsonObject.class));
    }
}