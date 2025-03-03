/*
---------------------------------------------------------------------------------
File Name : WebServerManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.fluffici.bot.api.furraid.permissive.Permissions;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.server.*;
import eu.fluffici.furraid.server.blacklist.FetchBlacklistRoute;
import eu.fluffici.furraid.server.guilds.*;
import eu.fluffici.furraid.server.health.HealthRoute;
import eu.fluffici.furraid.server.health.StatisticsRoute;
import eu.fluffici.furraid.server.users.FGetUserRoute;
import eu.fluffici.furraid.server.users.FIsStaffRoute;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebServerManager {
    private final List<WebRoute> webRoutes = new CopyOnWriteArrayList<>();
    private final HttpServer server;

    /**
     * The WebServerManager class is responsible for managing the web server functionality of the FluffBOT application.
     * It creates necessary web routes and starts the HttpServer to listen for incoming requests.
     */
    @SneakyThrows
    public WebServerManager() {
        this.server = HttpServer.create(new InetSocketAddress(8005), 0);

        this.webRoutes.add(new HomeRoute());
        this.webRoutes.add(new HealthRoute());

        this.webRoutes.add(new FIsStaffRoute());
        this.webRoutes.add(new FIsPremiumRoute());
        this.webRoutes.add(new FGetGuildsRoute());
        this.webRoutes.add(new FGuildRoute());

        this.webRoutes.add(new FetchBlacklistRoute());
        this.webRoutes.add(new PatchSettingsRoute());

        this.webRoutes.add(new OffersRoute());
        this.webRoutes.add(new OfferByIdRoute());

        this.webRoutes.add(new ChangelogsRoute());

        this.webRoutes.add(new StatisticsRoute());
        this.webRoutes.add(new CommandsRoute());

        this.webRoutes.add(new VoteRoute());
        this.webRoutes.add(new SendMessageRoute());

        this.webRoutes.add(new FIsVerified());
        this.webRoutes.add(new FGetUserRoute());
    }

    /**
     * Starts the server by creating the necessary web routes and starting the HttpServer.
     * Each web route in the webRoutes list is registered with the HttpServer and its onRequest method is called when a request is received.
     * The HttpServer is started to begin listening for incoming requests.
     */
    public void startServer() {
        this.webRoutes.forEach(webRoute -> this.server.createContext(webRoute.getRoutePath(), exchange -> {
            if (webRoute.isAuthEnabled()) {
                Optional<String> token = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Authorization"));
                if (token.isPresent()) {
                    Permissions permissions = FurRaidDB.getInstance().getGameServiceManager().getApiUserFromToken(token.get());
                    if (permissions != null) {
                        if (permissions.hasPermission(webRoute.getRequiredPermission())) {
                            webRoute.onRequest(exchange);
                        } else {
                            webRoute.sendErrorResponse(exchange, "UNAUTHORIZED", "Insufficient permission.", 403);
                        }
                    } else {
                        webRoute.sendErrorResponse(exchange, "UNAUTHORIZED", "Invalid API Token.", 403);
                    }
                } else {
                    webRoute.sendErrorResponse(exchange, "UNAUTHORIZED", "No bearer token found.", 403);
                }
            } else {
                try {
                    webRoute.onRequest(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, "Internal error");
                }
            }
        }));
        this.server.start();
    }

    private void sendResponse(HttpExchange exchange, String message) throws IOException {
        exchange.sendResponseHeaders(401, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
}