package eu.fluffici.furraid;

/*
---------------------------------------------------------------------------------
File Name : FurRaidDB.java

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

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pusher.rest.Pusher;
import eu.fluffici.bot.api.Instance;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.pubsub.RedisServer;
import eu.fluffici.bot.config.ConfigManager;
import eu.fluffici.bot.database.GameServiceManager;
import eu.fluffici.bot.logger.Logger;
import eu.fluffici.bot.manager.FileUploadManager;
import eu.fluffici.furraid.components.button.ButtonManager;
import eu.fluffici.furraid.components.commands.CommandManager;
import eu.fluffici.furraid.components.scheduler.SchedulerManager;
import eu.fluffici.furraid.events.EventManager;
import eu.fluffici.furraid.manager.*;
import eu.fluffici.furraid.redis.DatabaseConnector;
import eu.fluffici.furraid.util.Embed;
import eu.fluffici.language.LanguageManager;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.JDALogger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

@Getter
@SuppressWarnings("All")
public class FurRaidDB extends Instance {
    @Getter
    private static FurRaidDB instance;

    private BlacklistManager blacklistManager;

    private GameServiceManager gameServiceManager;
    private LanguageManager languageManager;
    private ConfigManager configManager;
    private Properties emojiConfig;
    private Properties channelConfig;
    private Properties gitProperties;
    private Embed embed;

    private ButtonManager buttonManager;

    private OfferManager offerManager;
    private SanctionManager sanctionManager;

    private FileUploadManager fileUploadManager;

    private final EventBus eventBus = new EventBus();

    private final List<Command> commandsRegistry = new ArrayList<>();

    private final ScheduledExecutorService executorMonoThread;
    private final ScheduledExecutorService scheduledExecutorService;

    private DatabaseConnector databaseConnector;

    private JDA jda;

    private WebServerManager webServerManager;

    @Setter
    private Boolean isLoading = true;

    @Setter
    private Boolean debug = false;

    private final Logger logger = new Logger("FurRaidDB");

    private EventManager eventManager;
    private CommandManager commandManager;
    private InviteManager inviteManager;
    private TicketManager ticketManager;
    private AntiScamManager antiScamManager;
    private AutoModManager autoModManager;
    private SchedulerManager schedulerManager;

    @Getter
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Prague"));

    @Getter
    private Pusher pusherServer;

    public FurRaidDB() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(4);
        this.executorMonoThread = Executors.newScheduledThreadPool(1);
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;

        this.logger.info("Loading registries.");
        this.logger.info("Loading configs..");

        this.configManager = new ConfigManager();
        this.configManager.loadConfig();
        this.emojiConfig = this.configManager.getConfig("emoji");
        this.channelConfig = this.configManager.getConfig("channels");
        this.gitProperties = this.configManager.getConfig("versioning");

        this.gameServiceManager = new GameServiceManager(
                String.format("jdbc:mysql://%s:3306/%s?verifyServerCertificate=false&useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true", System.getenv("MYSQL_HOST"), System.getenv("MYSQL_DB")),
                System.getenv("MYSQL_USER"),
                System.getenv("MYSQL_PASS"),
                1,
                50
        );

        this.setDebug(Boolean.parseBoolean(System.getenv("DEBUG")));
        this.logger.setDebug(this.getDebug());
        this.logger.debug("Debug mode: Enabled.");

        this.languageManager = new LanguageManager("en");
        this.logger.info("Language selected: " + this.languageManager.getLang());
        this.logger.info("Constructor initialization finished.");

        this.eventManager = new EventManager();
        this.commandManager = new CommandManager(this);

        this.embed = new Embed(this);

        RedisServer redisServer = new RedisServer(
                System.getenv("REDIS_HOST"),
                Integer.parseInt(System.getenv("REDIS_PORT")),
                System.getenv("REDIS_PASSWORD")
        );

        this.databaseConnector = new DatabaseConnector(this, redisServer);

        String token = System.getenv("TOKEN");
        JDALogger.setFallbackLoggerEnabled(false);

        try {
            this.jda = JDABuilder.createDefault(token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_TYPING,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.SCHEDULED_EVENTS,
                            GatewayIntent.AUTO_MODERATION_CONFIGURATION,
                            GatewayIntent.AUTO_MODERATION_EXECUTION,
                            GatewayIntent.GUILD_MODERATION,
                            GatewayIntent.GUILD_MESSAGE_POLLS
                    )
                    .setMaxReconnectDelay(100)
                    .setMaxBufferSize(Integer.MAX_VALUE)
                    .build();
        } catch (Exception e) {
            this.logger.error("Unable to login to Discord.com, ", e);
            e.printStackTrace();
        }

        this.getJda().getPresence().setPresence(Activity.customStatus("https://frdb.fluffici.eu"), true);

        CompletableFuture.runAsync(() -> this.commandsRegistry.addAll(this.getExistingCommands())).thenRunAsync(() -> {
            this.eventManager.register(this);

            this.blacklistManager = new BlacklistManager(this);
            this.fileUploadManager = new FileUploadManager();
            this.offerManager = new OfferManager();
            this.webServerManager = new WebServerManager();
            this.webServerManager.startServer();
            this.buttonManager = new ButtonManager();
            this.buttonManager.load();
            this.inviteManager = new InviteManager();
            this.getJda().addEventListener(this.inviteManager);

            this.schedulerManager = new SchedulerManager(this);
            this.schedulerManager.enableAll();
            this.ticketManager = new TicketManager(this);
            this.pusherServer = new com.pusher.rest.Pusher(
                    System.getenv("PUSHER_APP_ID"),
                    System.getenv("PUSHER_APP_KEY"),
                    System.getenv("PUSHER_APP_SECRET")
            );
            this.pusherServer.setCluster("eu");

            this.antiScamManager = new AntiScamManager();
            this.getJda().addEventListener(this.antiScamManager);

            this.sanctionManager = new SanctionManager(this);
            this.autoModManager = new AutoModManager(this);
            this.getJda().addEventListener(this.autoModManager);

            this.commandManager.load();
            this.registerCommand();
        }).exceptionally(ex -> {
            this.getLogger().error("A error occurred while registering the interactions : ", ex);
            ex.printStackTrace();
            return null;
        }).whenComplete((unused, throwable) -> this.isLoading = false);
    }

    private void registerCommand() {
        try {
            this.getCommandManager().getAllCommands().forEach(cmd -> {
                if (this.commandsRegistry.stream().noneMatch(c -> c.getName().equals(cmd.getName()))) {
                    this.getJda().upsertCommand(cmd.buildCommand()).queue();
                    this.getLogger().info(cmd.getName() + " pushed to the command registry.");
                }
            });

            this.logger.info("Initializing DBL command registry.");

            JsonArray commands = new JsonArray();

            this.commandsRegistry.forEach(command -> {
                JsonObject cmd0 = new JsonObject();
                cmd0.addProperty("name", command.getName());
                cmd0.addProperty("description", command.getDescription());
                cmd0.addProperty("type", command.getType().getId());

                this.logger.info("Command " + command.getName() + " added to the registry.");

                commands.add(cmd0);
            });

            Request sendCommand = new Request.Builder()
                    .url("https://discordbotlist.com/api/v1/bots/" + this.getJda().getSelfUser().getId() + "/commands")
                    .header("Authorization", System.getenv("FURRAID_DBOT_TOKEN"))
                    .post(RequestBody.create(MediaType.parse("application/json"), commands.toString()))
                    .build();

            this.logger.info("Sending " + commands.size() + " commands to DBL...");

            try (Response response = new OkHttpClient().newCall(sendCommand).execute()) {
                this.logger.info(response.toString());
                if (response.isSuccessful()) {
                    this.logger.info("All commands has been sent to DBL.");
                } else {
                    this.logger.error("Unable to send commands to DBL", null);
                    this.logger.error(new Gson().fromJson(response.body().string(), JsonObject.class).toString(), null);
                }
            } catch (Exception e) {
                this.logger.error("Unable to send commands to DBL", e);
            }
        } catch (Exception e) {
            this.getLogger().error("Unable to register commands.", e);
            e.printStackTrace();
        }
    }

    public List<Command> getExistingCommands(){
        return this.getJda().retrieveCommands().complete();
    }

    @Override
    @SuppressWarnings("All")
    public void onDisable() {
        this.logger.info("Shutdown initiated.");
        this.logger.info("Be aware! The discord command won't be handled anymore until restart.");

        int errorCount = this.getLogger().getErrorCount().get();
        this.jda.getGuildById(System.getenv("MAIN_GUILD"))
                .getTextChannelById(this.channelConfig.getProperty("channel.furraiddb.logging"))
                .sendMessageEmbeds(this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor("FurRaidDB - Shutting down initiated", "https://fluffici.eu", ICON_ALERT)
                        .setDescription("Be aware! The discord command won't be handled anymore until restart.")
                        .addField("Error state:", (errorCount > 0 ? errorCount + " error" + (errorCount > 1 ? "(s)": "") + " detected." : "Nominal"), true)
                        .setTimestamp(Instant.now())
                        .build()
                ).queue();

        this.jda.shutdown();
        this.databaseConnector.killConnection();
    }

    public Jedis getRedisResource() {
        return this.databaseConnector.getResource();
    }
}
