/*
---------------------------------------------------------------------------------
File Name : HealthRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.server.health;

import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

/**
 * A class representing a route for checking the health of the application.
 * Extends the WebRoute class.
 */
public class HealthRoute extends WebRoute {

    public HealthRoute() {
        super("/health", RouteMethod.GET);
    }

    /**
     * Called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(@NotNull HttpExchange request) {
        request.sendResponseHeaders(200, 0);
        request.getResponseBody().close();
    }
}