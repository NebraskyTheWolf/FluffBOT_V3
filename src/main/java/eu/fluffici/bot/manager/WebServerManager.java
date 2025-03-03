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

package eu.fluffici.bot.manager;

import com.sun.net.httpserver.HttpServer;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.bot.server.health.HealthRoute;
import eu.fluffici.bot.server.HomeRoute;
import eu.fluffici.bot.server.fluffbot.verification.IsVerifiedRoute;
import eu.fluffici.bot.server.fluffbot.ticket.TranscriptRoute;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebServerManager {
    private final List<WebRoute> webRoutes = new CopyOnWriteArrayList<>();
    private final HttpServer server;
    private final HttpServer localOnly;

    /**
     * The WebServerManager class is responsible for managing the web server functionality of the FluffBOT application.
     * It creates necessary web routes and starts the HttpServer to listen for incoming requests.
     */
    @SneakyThrows
    public WebServerManager() {
        this.server = FluffBOT.getInstance().getServer();
        this.localOnly = HttpServer.create(new InetSocketAddress(8002), 0);

        this.webRoutes.add(new HomeRoute());
        this.webRoutes.add(new HealthRoute());
        this.webRoutes.add(new TranscriptRoute());
        this.webRoutes.add(new IsVerifiedRoute());
    }

    /**
     * Starts the server by creating the necessary web routes and starting the HttpServer.
     * Each web route in the webRoutes list is registered with the HttpServer and its onRequest method is called when a request is received.
     * The HttpServer is started to begin listening for incoming requests.
     */
    public void startServer() {
        this.webRoutes.forEach(webRoute -> {
            if (webRoute.isAuthEnabled()) {
                this.localOnly.createContext(webRoute.getRoutePath(), webRoute::onRequest);
            } else {
                this.server.createContext(webRoute.getRoutePath(), webRoute::onRequest);
            }
        });
        this.server.start();
    }
}