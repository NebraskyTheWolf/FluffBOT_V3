/*
---------------------------------------------------------------------------------
File Name : FIsStaffRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.users;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class FIsStaffRoute extends WebRoute {
    public FIsStaffRoute() {
        super("/is-staff", RouteMethod.GET, calculatePermissions(CHECK_STAFF));
    }

    /**
     * Handles an HTTP request.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
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
                error.addProperty("error", "Invalid user ID format");
                sendJsonResponse(request, error);
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("status", true);
            response.addProperty("isStaff", FurRaidDB.getInstance().getGameServiceManager().isStaff(UserSnowflake.fromId(userId)));
            sendJsonResponse(request, response);
        }
    }
}