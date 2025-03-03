package eu.fluffici.furraid.server.guilds;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class FIsVerified extends WebRoute {
    public FIsVerified() {
        super("/is-verified", RouteMethod.GET);
    }

    @Override
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            String[] pathParts = request.getRequestURI().getPath().split("/");
            if (pathParts.length < 4) {
                sendErrorResponse(request, "MISSING_ARGUMENTS", "The Guild ID and User ID Must be specified.", 400);
                return;
            }

            String guildId = pathParts[2];
            if (!isValidUserId(guildId)) {
                sendErrorResponse(request, "INVALID_GUILD", "Invalid Guild ID format", 400);
                return;
            }

            String userId = pathParts[3];
            if (!isValidUserId(userId)) {
                sendErrorResponse(request, "INVALID_USER", "Invalid User ID format", 400);
                return;
            }

            Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);
            if (guild == null) {
                sendErrorResponse(request, "GUILD_PRESENCE", "This guild is not found. Please invite the bot on your server first.", 400);
                return;
            }

            Member member = guild.getMemberById(userId);
            if (member == null) {
                sendErrorResponse(request, "USER_PRESENCE", "This user is not on the server", 400);
                return;
            }

            GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild);
            if (guildSettings.isBlacklisted()) {
                sendErrorResponse(request, "TERMINATED", "This guild has been blacklisted due to TOS violation.", 400);
                return;
            }

            FurRaidConfig.VerificationFeature verificationFeature = guildSettings.getConfig().getFeatures().getVerification();

            if (verificationFeature.isEnabled()) {
                Role verifiedRole = guild.getRoleById(verificationFeature.getSettings().getVerifiedRole());
                if (verifiedRole == null) {
                    sendErrorResponse(request, "VERIFICATION_FEATURE", "Please configure the server verification first.", 403);
                } else {
                    JsonObject result = new JsonObject();
                    result.addProperty("status", true);
                    result.addProperty("isVerified", member.getRoles().contains(verifiedRole));

                    sendJsonResponse(request, result);
                }
            } else {
                sendErrorResponse(request, "VERIFICATION_FEATURE", "The verification feature is disabled in this guild.", 403);
            }
        }
    }
}
