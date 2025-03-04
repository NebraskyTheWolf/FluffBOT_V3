/*
---------------------------------------------------------------------------------
File Name : IsVerifiedRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.server.fluffbot.verification;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;

/**
 * Represents a web route for checking if a user is verified.
 */
public class IsVerifiedRoute extends WebRoute {

    public IsVerifiedRoute() {
        super("/is-verified", RouteMethod.GET, true);
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(HttpExchange request) {
        String requestMethod = request.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("GET")) {
            request.sendResponseHeaders(405, -1);
            request.close();
            return;
        }

        String[] pathParts = request.getRequestURI().getPath().split("/");
        if (pathParts.length < 3) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "false");
            error.addProperty("error", "User ID not provided");
            sendJsonResponse(request, error);
            return;
        }

        String userId = pathParts[2];
        if (!isValidUserId(userId)) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "false");
            error.addProperty("error", "Invalid user ID format");
            sendJsonResponse(request, error);
            return;
        }

        try {
            boolean isVerified = FluffBOT.getInstance().getUserManager().isVerified(UserSnowflake.fromId(userId));
            JsonObject response = new JsonObject();
            response.addProperty("status", "true");
            response.addProperty("isVerified", isVerified);
            sendJsonResponse(request, response);
        } catch (Exception e) {
            FluffBOT.getInstance().getLogger().error("Exception while processing is-verified request: ", e);
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("status", "false");
            error.addProperty("error", "Internal server error");
            sendJsonResponse(request, error);
        }
    }
}