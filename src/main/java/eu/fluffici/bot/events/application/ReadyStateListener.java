package eu.fluffici.bot.events.application;

/*
---------------------------------------------------------------------------------
File Name : ReadyStateListener.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/*
                            LICENCE PRO PROPRIETÁRNÍ SOFTWARE
            Verze 1, Organizace: Fluffici, z.s. IČO: 19786077, Rok: 2024
                            PODMÍNKY PRO POUŽÍVÁNÍ

    a. Použití: Software lze používat pouze podle přiložené dokumentace.
    b. Omezení reprodukce: Kopírování softwaru bez povolení je zakázáno.
    c. Omezení distribuce: Distribuce je povolena jen přes autorizované kanály.
    d. Oprávněné kanály: Distribuci určuje výhradně držitel autorských práv.
    e. Nepovolené šíření: Šíření mimo povolené podmínky je zakázáno.
    f. Právní důsledky: Porušení podmínek může vést k právním krokům.
    g. Omezení úprav: Úpravy softwaru jsou zakázány bez povolení.
    h. Rozsah oprávněných úprav: Rozsah úprav určuje držitel autorských práv.
    i. Distribuce upravených verzí: Distribuce upravených verzí je povolena jen s povolením.
    j. Zachování autorských atribucí: Kopie musí obsahovat všechny autorské atribuce.
    k. Zodpovědnost za úpravy: Držitel autorských práv nenese odpovědnost za úpravy.

    Celý text licence je dostupný na adrese:
    https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf
*/


import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.events.ActivityUpdateEvent;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
public class ReadyStateListener extends ListenerAdapter {

    private final FluffBOT instance;

    public ReadyStateListener(FluffBOT instance) {
        this.instance = instance;

        this.instance.getExecutorMonoThread().scheduleAtFixedRate(() -> {
            this.activities.clear();
            this.activities.add(Activity.listening(String.format("%s uživatelů", instance.getJda().getUsers().size())));
            this.activities.add(Activity.competing("nejvíce tokenů"));
            this.activities.add(Activity.competing("pečení sušenek s Emecc"));
            this.activities.add(Activity.watching("chat"));
            this.activities.add(Activity.listening("hudbu s Nanem"));
            this.activities.add(Activity.playing("Train Simulator s Ashem"));

            this.instance.getLogger().info("Activities updated.");
        }, 0, 60, TimeUnit.MINUTES);
    }

    private final List<Activity> activities = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        FluffBOT.getInstance().getJda().getPresence().setStatus(OnlineStatus.ONLINE);
        for (Guild guild : event.getJDA().getGuilds()) {
            guild.retrieveInvites().queue(invites -> {
                for (Invite invite : invites) {
                    this.instance.getInviteUses().put(invite.getUrl(), invite.getUses());
                }
            });
        }

        this.instance.getExecutorMonoThread().schedule(() -> this.instance.setIsLoading(false), 5, TimeUnit.SECONDS);
        this.instance.getExecutorMonoThread().scheduleAtFixedRate(() ->
                this.instance.getJda().getPresence().setActivity(this.activities.get(random.nextInt(this.activities.size() - 1))), 5, 5, TimeUnit.SECONDS
        );
        this.instance.getExecutorMonoThread().schedule(this::patchDescription, 10, TimeUnit.SECONDS);

        this.instance.getLogger().info("System ready.");
    }

    private void patchDescription() {
        try {
            JsonObject appProperties = new JsonObject();
            appProperties.addProperty("description", String.format("""
                    **Oficiální bot komunity Fluffíci.**
                    **Prefix**: /
                    **Verze**: V3-%s
                    """, this.instance.getGitProperties().getProperty("git.build.version", "unofficial")));

            JsonArray tags = new JsonArray();
            this.instance.getCommandManager().getAllCommands()
                    .stream()
                    .limit(4)
                    .filter(command -> command.getOptions().containsKey("isRestricted"))
                    .filter(command -> command.getOptions().containsKey("isDeveloper"))
                    .forEach(command -> tags.add("/".concat(command.getName())));

            appProperties.add("tags", tags);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://discord.com/api/v9/applications/@me")
                    .header("Authorization", "Bot ".concat(this.instance.getDefaultConfig().getProperty("token")))
                    .patch(RequestBody.create(appProperties.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            response.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
