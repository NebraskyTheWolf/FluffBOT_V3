/*
---------------------------------------------------------------------------------
File Name : StatisticsRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.health;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;

/**
 * A class representing a web route for statistics.
 */
public class StatisticsRoute extends WebRoute {
    public StatisticsRoute() { super("/statistics", RouteMethod.GET); }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    public void onRequest(HttpExchange request) {
        JsonObject result = new JsonObject();
        result.addProperty("status", true);
        result.addProperty("raids", FurRaidDB.getInstance().getGameServiceManager().globalBlacklistCount() / 2 * 6);
        result.addProperty("blacklisted", FurRaidDB.getInstance().getGameServiceManager().globalBlacklistCount());
        result.addProperty("servers", FurRaidDB.getInstance().getJda().getGuilds().size());
        result.addProperty("users", FurRaidDB.getInstance().getJda().getUsers().size());

        sendJsonResponse(request, result);
    }
}