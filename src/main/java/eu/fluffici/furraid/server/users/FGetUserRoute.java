/*
---------------------------------------------------------------------------------
File Name : FGetUserRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.GET_USER_INFO;
import static eu.fluffici.bot.api.furraid.permissive.Permissions.calculatePermissions;

public class FGetUserRoute extends WebRoute {
    public FGetUserRoute() {
        super("/users", RouteMethod.GET, calculatePermissions(GET_USER_INFO));
    }

    @Override
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            String[] pathParts = request.getRequestURI().getPath().split("/");
            if (pathParts.length < 3) {
                JsonObject error = new JsonObject();
                error.addProperty("status", false);
                error.addProperty("error", "User ID not provided");
                sendJsonResponse(request, error);
                return;
            }

            String userId = pathParts[2];
            if (!isValidUserId(userId)) {
                JsonObject error = new JsonObject();
                error.addProperty("status", false);
                error.addProperty("error", "Invalid User ID format");
                sendJsonResponse(request, error);
                return;
            }

            UserSnowflake user = UserSnowflake.fromId(userId);
            if (FurRaidDB.getInstance().getJda().getUserById(userId) == null) {
                sendErrorResponse(request, "USER_NOT_AVAILABLE", "This user is not found in the users node.");
            } else {
                DiscordUser discordUser = fetchUser(user);
                if (discordUser != null) {
                    sendJsonResponse(request, discordUser.toJSON());
                } else {
                    sendErrorResponse(request, "USER_NOT_FOUND", "This user is not found on discord.");
                }
            }
        }
    }

    @Nullable
    public static DiscordUser fetchUser(@NotNull UserSnowflake user) {
        Request request = new Request.Builder()
                .url("https://discord.com/api/users/" + user.getId())
                .header("Authorization", "Bot " + System.getenv("TOKEN"))
                .get()
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("Raw JSON Response: " + responseBody); // Debugging: Log the raw JSON response

                return new Gson().fromJson(responseBody, DiscordUser.class);
            } else {
                System.out.println("Failed to fetch user. Response code: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isSpammer(User user) {
        int SPAMMER_MASK = 1 << 20;

        return ((user.getFlagsRaw() & SPAMMER_MASK) == SPAMMER_MASK);
    }
}