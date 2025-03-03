package eu.fluffici.furraid.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;

public class SendMessageRoute extends WebRoute {
    public SendMessageRoute() {
        super("/send-message", RouteMethod.POST,
                calculatePermissions(SEND_MESSAGE)
        );
    }

    @Override
    public void onRequest(HttpExchange request) {
        if (!this.preventWrongMethod(request)) {
            JsonObject data = new Gson().fromJson(this.readBody(request.getRequestBody()), JsonObject.class);

            if (data.has("channelId") && data.has("embeds") && data.has("buttons")) {
                TextChannel channel = FurRaidDB.getInstance().getJda().getGuildById("829064798896652389").getTextChannelById(data.get("channelId").getAsString());

                List<MessageEmbed> embeds = new ArrayList<>();
                List<ItemComponent> components = new ArrayList<>();

                data.get("embeds").getAsJsonArray().forEach(data0 -> {
                    JsonObject embed = data0.getAsJsonObject();
                    embeds.add(new EmbedBuilder()
                                    .setTitle(embed.get("title").getAsString())
                                    .setDescription(embed.get("description").getAsString())
                                    .setColor(Color.green)
                                    .setFooter(FurRaidDB.getInstance().getJda().getSelfUser().getName(), FurRaidDB.getInstance().getJda().getSelfUser().getAvatarUrl())
                            .build()
                    );
                });

                data.get("buttons").getAsJsonArray().forEach(data0 -> {
                    JsonObject button = data0.getAsJsonObject();
                    components.add(Button.link(button.get("link").getAsString(), button.get("label").getAsString()));
                });

                channel.sendMessageEmbeds(embeds).addActionRow(components).queue();
            } else {
                sendErrorResponse(request, "MISSING_ARGUMENTS", "The 'channelId' or 'embeds' or 'buttons' field is missing.", 404);
                return;
            }

            sendSuccessResponse(request);
        }
    }
}
