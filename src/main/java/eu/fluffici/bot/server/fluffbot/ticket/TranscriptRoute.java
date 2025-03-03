/*
---------------------------------------------------------------------------------
File Name : TranscriptRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.server.fluffbot.ticket;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a web route for retrieving a transcript.
 */
public class TranscriptRoute extends WebRoute {

    public TranscriptRoute() {
        super("/transcript", RouteMethod.GET);
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
            error.addProperty("error", "Ticket ID not provided");
            sendJsonResponse(request, error);
            return;
        }

        String ticketId = pathParts[2];
        if (!isValidTicketId(ticketId)) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "false");
            error.addProperty("error", "Invalid ticket ID format");
            sendJsonResponse(request, error);
            return;
        }

        try {
            String transcript = FluffBOT.getInstance().getTicketManager().transcript(ticketId, "");
            if (transcript == null) {
                JsonObject error = new JsonObject();
                error.addProperty("status", "false");
                error.addProperty("error", "Ticket not found");
                sendJsonResponse(request, error);
            } else {
                sendHtmlResponse(request, transcript);
            }
        } catch (Exception e) {
            FluffBOT.getInstance().getLogger().error("Exception while processing transcript request: ", e);
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("status", "false");
            error.addProperty("error", "Internal server error");
            sendJsonResponse(request, error);
        }
    }

    @Contract(pure = true)
    private boolean isValidTicketId(@NotNull String ticketId) {
        return ticketId.matches("[a-zA-Z0-9]+");
    }
}