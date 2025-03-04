package eu.fluffici.furraid.components.scheduler.stats;

/*
---------------------------------------------------------------------------------
File Name : SendStatistics.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 10/08/2024
Last Modified : 10/08/2024

---------------------------------------------------------------------------------
*/


import com.google.gson.JsonObject;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.UserSnowflake;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SendStatistics extends Task {
    private final FurRaidDB instance;

    public SendStatistics(FurRaidDB instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading SendStatistics scheduler.");
    }

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> CompletableFuture.runAsync(() -> {
            try {
                JsonObject statistics = new JsonObject();
                statistics.addProperty("guilds", this.instance.getJda().getGuilds().size());
                statistics.addProperty("users", this.instance.getJda().getUsers().size());
                statistics.addProperty("shard_id", this.instance.getJda().getShardInfo().getShardId());

                Request request = new Request.Builder()
                        .url("https://discordbotlist.com/api/v1/bots/" + this.instance.getJda().getSelfUser().getId() + "/stats")
                        .post(RequestBody.create(statistics.toString(), MediaType.parse("application/json")))
                        .header("Authorization", "Bot " + System.getenv("FURRAID_DBL_TOKEN"))
                        .build();

                try (Response response = new OkHttpClient().newCall(request).execute()) {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                this.instance.getLogger().error("Error while updating the sanctions.", e);
                e.printStackTrace();
            }
        }), 5, 10, TimeUnit.MINUTES);
    }
}
