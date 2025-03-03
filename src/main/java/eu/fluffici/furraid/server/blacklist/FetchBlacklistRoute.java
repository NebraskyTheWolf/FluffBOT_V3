/*
---------------------------------------------------------------------------------
File Name : FetchBlacklistRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.blacklist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.Blacklist;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class FetchBlacklistRoute extends WebRoute {
    private final Gson gson = new Gson();

    public FetchBlacklistRoute() {
        super("/fetch-global-blacklist", RouteMethod.GET, calculatePermissions(BLACKLIST_READ));
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    @SuppressWarnings("All")
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            String[] pathParts = request.getRequestURI().getPath().split("/");
            if (pathParts.length < 3) {
                JsonObject error = new JsonObject();
                error.addProperty("status", true);
                error.addProperty("error", "User ID not provided");
                sendJsonResponse(request, error);
                return;
            }

            String userId = pathParts[2];
            if (!isValidUserId(userId)) {
                JsonObject error = new JsonObject();
                error.addProperty("status", true);
                error.addProperty("error", "Invalid User ID format");
                sendJsonResponse(request, error);
                return;
            }

            String actorId = request.getRequestHeaders().get("X-Actor-ID").get(0);
            if (!FurRaidDB.getInstance().getGameServiceManager().isStaff(UserSnowflake.fromId(actorId))) {
                JsonObject error = new JsonObject();
                error.addProperty("status", true);
                error.addProperty("error", "Unauthorized");
                sendJsonResponse(request, error);
                return;
            }

            UserSnowflake user = UserSnowflake.fromId(userId);
            boolean isBlacklisted = FurRaidDB.getInstance().getBlacklistManager().isGloballyBlacklisted(user);

            if (isBlacklisted) {
                Blacklist blacklist = FurRaidDB.getInstance()
                        .getBlacklistManager()
                        .fetchGlobalBlacklist(user);

                sendJsonResponse(request, this.gson.fromJson(this.gson.toJson(blacklist), JsonObject.class));
            } else {
                JsonObject response = new JsonObject();
                response.addProperty("status", false);
                response.addProperty("message", "User is not blacklisted");
                sendJsonResponse(request, response);
            }
        }
    }
}