/*
---------------------------------------------------------------------------------
File Name : FIsPremiumRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.guilds;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.GuildPremiumOffer;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class FIsPremiumRoute extends WebRoute {
    public FIsPremiumRoute() {
        super("/is-premium", RouteMethod.GET, calculatePermissions(CHECK_PREMIUM));
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
            String[] pathParts = request.getRequestURI().getPath().split("/");
            if (pathParts.length < 3) {
                JsonObject error = new JsonObject();
                error.addProperty("status", true);
                error.addProperty("error", "Guild ID not provided");
                sendJsonResponse(request, error);
                return;
            }

            String guildId = pathParts[2];
            if (!isValidUserId(guildId)) {
                JsonObject error = new JsonObject();
                error.addProperty("status", true);
                error.addProperty("error", "Invalid Guild ID format");
                sendJsonResponse(request, error);
                return;
            }

            GuildPremiumOffer guildPremiumOffer = FurRaidDB.getInstance()
                    .getGameServiceManager()
                    .getGuildPremium(Long.parseLong(guildId));

            sendJsonResponse(request, getResponse(guildPremiumOffer));
        }
    }

    @NotNull
    private static JsonObject getResponse(@NotNull GuildPremiumOffer guildPremiumOffer) {
        JsonObject response = new JsonObject();
        response.addProperty("status", true);
        if (guildPremiumOffer.isActive()) {
            response.addProperty("isPremium", true);
            response.addProperty("planName", FurRaidDB.getInstance().getGameServiceManager().getOfferNameById(guildPremiumOffer.getOfferId()));
            response.addProperty("expiration", guildPremiumOffer.getExpirationAt().getTime());
        } else {
            response.addProperty("isPremium", false);
            response.addProperty("planName", "FurRaid Classic");
        }
        return response;
    }
}