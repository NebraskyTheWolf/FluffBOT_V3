/*
---------------------------------------------------------------------------------
File Name : OffersRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.FurRaidPremiumOffer;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;

import java.util.List;

public class OffersRoute extends WebRoute {
    public OffersRoute() {
        super("/offers", RouteMethod.GET);
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            List<FurRaidPremiumOffer> premiumOffers = FurRaidDB.getInstance()
                    .getGameServiceManager()
                    .getAllOffers();

            sendJsonResponse(request, new Gson().fromJson(new Gson().toJson(premiumOffers), JsonArray.class));
        }
    }
}