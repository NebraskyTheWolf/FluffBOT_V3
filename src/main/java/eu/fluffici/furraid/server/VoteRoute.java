package eu.fluffici.furraid.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.UserSnowflake;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class VoteRoute extends WebRoute {
    public VoteRoute() {
        super("/vote", RouteMethod.POST,
                calculatePermissions(READ_VOTE, WRITE_VOTE)
        );
    }

    @Override
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            JsonObject data = new Gson().fromJson(this.readBody(request.getRequestBody()), JsonObject.class);

            if (data.has("id")) {
                FurRaidDB.getInstance().getGameServiceManager().addVote(UserSnowflake.fromId(data.get("id").getAsString()));
            } else {
                sendErrorResponse(request, "MISSING_ARGUMENTS", "The 'admin' or 'id' field is missing.", 404);
                return;
            }

            sendSuccessResponse(request);
        }
    }
}
