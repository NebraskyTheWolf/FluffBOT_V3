package eu.fluffici.furraid.components.scheduler.verification;

import com.google.gson.Gson;
import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Deprecated(forRemoval = true)
public class RedisSyncData extends Task {

    private final Gson gson = new Gson();
    private final FurRaidDB instance;
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build(); // Shared OkHttpClient instance

    public RedisSyncData(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void execute() {
    }
}