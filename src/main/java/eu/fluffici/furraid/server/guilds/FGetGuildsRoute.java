/*
---------------------------------------------------------------------------------
File Name : FGetGuildsRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.guilds;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.furraid.permissive.Permissions;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class FGetGuildsRoute extends WebRoute {
    private final Gson gson = new Gson();

    public FGetGuildsRoute() {
        super("/get-servers", RouteMethod.GET,
                calculatePermissions(GET_SERVERS)
        );
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            JsonArray array = new JsonArray();
            FurRaidDB.getInstance()
                    .getJda()
                    .getGuilds()
                    .forEach(guild -> {
                        JsonObject guild0 = new JsonObject();
                        guild0.addProperty("id", guild.getId());
                        guild0.addProperty("icon", guild.getIconUrl());
                        guild0.addProperty("name", guild.getName());
                        guild0.addProperty("memberCount", guild.getMemberCount());

                        array.add(guild0);
                    });

            JsonObject data = new JsonObject();
            data.addProperty("status", true);
            data.add("data", array);

            sendJsonResponse(request, data.getAsJsonObject());
        }
    }
}