/*
---------------------------------------------------------------------------------
File Name : HomeRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import lombok.SneakyThrows;

/**
 * A class representing the home route of a web application.
 */
public class HomeRoute extends WebRoute {
    public HomeRoute() {
        super("/", RouteMethod.GET);
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    public void onRequest(HttpExchange request) {
        JsonArray developers = new JsonArray();
        developers.add("Main Developer: Vakea <alex.leroy8303@seznam.cz>");

        JsonArray contributors = new JsonArray();
        contributors.add("Translator: Asherro <asherro@fluffici.eu>");
        contributors.add("Ideas: Exibit");
        contributors.add("Ideas: Vitaa");
        contributors.add("Ideas: Krab aka Kyle");
        contributors.add("Ideas: Emeecc");
        contributors.add("Ideas: Vøjstřík");
        contributors.add("Global Project Idea: Technik");

        JsonObject contact = new JsonObject();
        contact.addProperty("owner", FluffBOT.getInstance().getDefaultConfig().getProperty("AUTHOR_COMPANY", "Fluffici, z.s."));
        contact.addProperty("email", FluffBOT.getInstance().getDefaultConfig().getProperty("AUTHOR_EMAIL", "administrace@fluffici.eu"));

        JsonObject details = new JsonObject();
        details.addProperty("application", FluffBOT.getInstance().getDefaultConfig().getProperty("API_NAME", "FluffBOT Data Server"));
        details.addProperty("version", FluffBOT.getInstance().getGitProperties().getProperty("git.build.version", "unofficial"));
        details.add("contact", contact);
        details.add("developers", developers);
        details.add("contributors", contributors);

        sendJsonResponse(request, details);
    }
}