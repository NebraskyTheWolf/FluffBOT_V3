package eu.fluffici.bot;

/*
---------------------------------------------------------------------------------
File Name : FluffBOT.java

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
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.sun.net.httpserver.HttpServer;
import eu.fluffici.Benchmark;
import eu.fluffici.bot.api.Instance;
import eu.fluffici.bot.api.chart.DefaultCharts;
import eu.fluffici.bot.api.events.PusherCallback;
import eu.fluffici.bot.api.events.UserTransferEvent;
import eu.fluffici.bot.api.pubsub.PendingMessage;
import eu.fluffici.bot.api.pubsub.RedisServer;
import eu.fluffici.bot.components.button.ButtonManager;
import eu.fluffici.bot.components.commands.CommandManager;
import eu.fluffici.bot.components.context.ContextManager;
import eu.fluffici.bot.components.modal.ModalManager;
import eu.fluffici.bot.components.scheduler.SchedulerManager;
import eu.fluffici.bot.config.ConfigManager;
import eu.fluffici.bot.database.GameServiceManager;
import eu.fluffici.bot.database.MessagingListener;
import eu.fluffici.bot.database.SecurityListener;
import eu.fluffici.bot.database.redis.DatabaseConnector;
import eu.fluffici.bot.database.redis.PubSubAPI;
import eu.fluffici.bot.events.EventManager;
import eu.fluffici.bot.logger.Logger;
import eu.fluffici.bot.manager.*;
import eu.fluffici.bot.modules.ModuleManager;
import eu.fluffici.bot.modules.achievements.impl.AchievementManager;
import eu.fluffici.bot.utils.Embed;
import eu.fluffici.bot.utils.LevelUtil;
import eu.fluffici.language.LanguageManager;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

/**
 * The FluffBOT class represents an instance of the FluffBOT application.
 * It extends the Instance class.
 */
@Getter
public class FluffBOT extends Instance {
    @Getter
    private static FluffBOT instance;

    private final Logger logger = new Logger("FluffBOT");

    @Getter
    private final Map<String, Integer> inviteUses = new HashMap<>();

    private GameServiceManager gameServiceManager;
    private LanguageManager languageManager;
    private UserManager userManager;

    private DatabaseConnector databaseConnector;;

    private ConfigManager configManager;
    private Properties defaultConfig;
    private Properties emojiConfig;
    private Properties channelConfig;
    private Properties gitProperties;
    private Embed embed;

    private EventManager eventManager;
    private CommandManager commandManager;
    private ContextManager contextManager;
    private ButtonManager buttonManager;
    private ModuleManager moduleManager;
    private SchedulerManager schedulerManager;
    private RewardManager rewardManager;
    private FoodItemManager foodItemManager;
    private ItemCraftManager itemCraftManager;
    private ItemManager itemManager;
    private ClanManager clanManager;
    private InteractionManager interactionManager;
    private WebServerManager webServerManager;
    private LevelUtil levelUtil;
    private TicketManager ticketManager;
    private FileUploadManager fileUploadManager;

    private ModalManager modalManager;
    private SanctionManager sanctionManager;

    private final ScheduledExecutorService executorMonoThread;
    private final ScheduledExecutorService scheduledExecutorService;

    private PubSubAPI pubSub;

    @Setter
    private AchievementManager achievementManager;

    private JDA jda;

    @Setter
    private Boolean isLoading = true;

    @Setter
    private Boolean debug = false;

    @Setter
    private float globalMultiplier = 1F;
    private final EventBus eventBus = new EventBus();
    private final Map<UserSnowflake, UserTransferEvent> transferEventMap = new HashMap<>();

    @Setter
    private long reconnectStartTime = 0;

    private final Pusher pusher = new Pusher("e96ac9c8809b190f796d", new PusherOptions().setCluster("eu"));
    private final List<Command> commandsRegistry = new ArrayList<>();
    private final DefaultCharts defaultCharts = new DefaultCharts();
    private final Benchmark benchmark = new Benchmark();
    private HttpServer server;

    /**
     * The Fluffbot class represents an instance of the Fluffbot application.
     */
    public FluffBOT() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(12);
        this.executorMonoThread = Executors.newScheduledThreadPool(1);
    }

    /**
     * Called when the Fluffbot application is enabled.
     */
    @Override
    public void onEnable() throws Exception {
        instance = this;
        this.logger.info("Loading registries.");
        this.logger.info("Loading configs..");

        this.configManager = new ConfigManager();
        this.configManager.loadConfig();
        this.defaultConfig = this.configManager.getConfig("default");
        this.emojiConfig = this.configManager.getConfig("emoji");
        this.channelConfig = this.configManager.getConfig("channels");
        this.gitProperties = this.configManager.getConfig("versioning");

        String mysqlHost = this.defaultConfig.getProperty("mysqlHost");
        String mysqlDb = this.defaultConfig.getProperty("mysqlDatabase");
        String mysqlUser = this.defaultConfig.getProperty("mysqlUsername");
        String mysqlPass = this.defaultConfig.getProperty("mysqlPassword");

        this.gameServiceManager = new GameServiceManager(
                String.format("jdbc:mysql://%s:3306/%s?verifyServerCertificate=false&useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true", mysqlHost, mysqlDb),
                mysqlUser,
                mysqlPass,
                1,
                50
        );

        this.setDebug(Boolean.parseBoolean(this.getDefaultConfig().getProperty("debug", "false")));
        this.logger.setDebug(this.getDebug());
        this.logger.debug("Debug mode: Enabled.");

        this.languageManager = new LanguageManager(this.defaultConfig.getProperty("language"));
        this.logger.info("Language selected: " + this.languageManager.getLang());
        this.logger.info("Constructor initialization finished.");

        this.eventManager = new EventManager();
        this.contextManager = new ContextManager();
        this.buttonManager = new ButtonManager();
        this.commandManager = new CommandManager(this);
        this.sanctionManager = new SanctionManager(this);
        this.userManager = new UserManager(this);
        this.schedulerManager = new SchedulerManager(this);
        this.moduleManager = new ModuleManager(this);
        this.rewardManager = new RewardManager(this);
        this.foodItemManager = new FoodItemManager(this);
        this.itemCraftManager = new ItemCraftManager(this);
        this.itemManager = new ItemManager(this);
        this.clanManager = new ClanManager(this);
        this.interactionManager = new InteractionManager(this);
        this.embed = new Embed(this);
        this.levelUtil = new LevelUtil(this);
        this.modalManager = new ModalManager();
        this.ticketManager = new TicketManager(this);
        this.fileUploadManager = new FileUploadManager();

        this.server = HttpServer.create(new InetSocketAddress(8000), 0);
        this.webServerManager = new WebServerManager();

        if (Boolean.parseBoolean(this.defaultConfig.getProperty("redis.enabled"))) {
            this.databaseConnector = new DatabaseConnector(this, new RedisServer(
                    this.defaultConfig.getProperty("redis.host"),
                    Integer.parseInt(this.defaultConfig.getProperty("redis.port")),
                    this.defaultConfig.getProperty("redis.password")
            ));
            this.pubSub = new PubSubAPI(this);

            this.pubSub.subscribe(this.securityChannel, new SecurityListener());
            this.pubSub.subscribe(this.messagingChannel, new MessagingListener());

            this.pubSub.send(new PendingMessage(this.notifyChannel, this.instanceId, () ->
                    this.logger.info("FBL: instance %s was registered successfully on the network.", this.instanceId))
            );
        } else {
            this.logger.warn("Redis PubSub feature is disabled.");
        }

        String token = this.defaultConfig.getProperty("token");

        JDALogger.setFallbackLoggerEnabled(false);

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

        this.getJda().getPresence().setPresence(Activity.customStatus("Initializing..."), true);

        this.foodItemManager.init();
        this.itemCraftManager.init();
        this.itemManager.init();

        CompletableFuture.runAsync(() -> this.commandsRegistry.addAll(this.getExistingCommands()))
        .thenRunAsync(() -> {
            this.commandManager.load();
            this.registerCommand();
        }).thenRunAsync(() -> {
            this.contextManager.load();
            this.registerContext();
        }).exceptionally(ex -> {
            this.getLogger().error("A error occurred while registering the interactions : ", ex);
            ex.printStackTrace();
            return null;
        }).whenComplete(((unused, throwable) -> {
            this.buttonManager.load();
            this.eventManager.register(this);

            this.moduleManager.load();
            this.moduleManager.enableAll();

            this.schedulerManager.enableAll();

            this.rewardManager.init();
            this.modalManager.load();

            this.pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(ConnectionStateChange change) {
                    logger.info("State changed from %s to %s", change.getPreviousState(), change.getCurrentState());
                }

                @Override
                public void onError(String message, String code, Exception e) {
                    logger.error("There was a problem connecting!", e);
                }
            }, ConnectionState.ALL);

            Channel akceChannel = this.pusher.subscribe("notifications-event");
            akceChannel.bind("create-trello", event -> this.getEventBus().post(new PusherCallback("create-trello", event)));
            akceChannel.bind("update-trello", event -> this.getEventBus().post(new PusherCallback("update-trello", event)));
            akceChannel.bind("remove-trello", event -> this.getEventBus().post(new PusherCallback("remove-trello", event)));

            Channel systemChannel = this.pusher.subscribe("system-event");
            systemChannel.bind("system-restart", event -> this.getEventBus().post(new PusherCallback("system-restart", event)));
            systemChannel.bind("system-reset", event -> this.getEventBus().post(new PusherCallback("system-reset", event)));

            this.webServerManager.startServer();
        })).exceptionally(ex -> {
            this.getLogger().error("A error occurred while loading the commands : ", ex);
            ex.printStackTrace();
            return null;
        }).whenComplete((unused, throwable) -> this.logger.info("All component(s) has been loaded successfully.")).get(60, TimeUnit.SECONDS);
    }

    /**
     * Registers all commands by upsetting them to JDA.
     */
    public void registerCommand() {
        try {
            this.getCommandManager().getAllCommands().forEach(cmd -> {
                if (this.commandsRegistry.stream().noneMatch(c -> c.getName().equals(cmd.getName()))) {
                    this.getJda().upsertCommand(cmd.buildCommand()).queue();
                    this.getLogger().info(cmd.getName() + " pushed to the command registry.");
                }
            });
        } catch (Exception e) {
            this.getLogger().error("Unable to register commands.", e);
            e.printStackTrace();
        }
    }

    public void registerContext() {
        try {
            this.getContextManager().getAllCommands().forEach(cmd -> {
                if (this.commandsRegistry.stream().noneMatch(c -> c.getName().equals(cmd.getName()))) {
                    this.getJda().upsertCommand(cmd.buildContext()).queue();
                    this.getLogger().info(cmd.getName() + " pushed to the context registry.");
                }
            });
        } catch (Exception e) {
            this.logger.error("Unable to register commands.", e);
            e.printStackTrace();
        }
    }

    public List<net.dv8tion.jda.api.interactions.commands.Command> getExistingCommands(){
        return this.getJda().retrieveCommands().complete();
    }

    /**
     * Called when the FluffBOT application is disabled. Performs cleanup tasks and shuts down the application.
     * This method logs a shutdown message, sends an embed message to a specified Discord channel, initiates the shutdown of JDA, disables all modules, shuts down the database manager
     *, and disconnects from a pusher.
     */
    @Override
    @SuppressWarnings("All")
    public void onDisable() {
        this.logger.info("Shutdown initiated.");
        this.logger.info("Be aware! The discord command won't be handled anymore until restart.");

        int errorCount = this.getLogger().getErrorCount().get();
        this.jda.getGuildById(this.defaultConfig.getProperty("main.guild"))
                .getTextChannelById(this.channelConfig.getProperty("channel.logging"))
                .sendMessageEmbeds(this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor("Shutting down initiated", "https://fluffici.eu", ICON_ALERT)
                    .setDescription("Be aware! The discord command won't be handled anymore until restart.")
                    .addField("Error state:", (errorCount > 0 ? errorCount + " error" + (errorCount > 1 ? "(s)": "") + " detected." : "Nominal"), true)
                    .setTimestamp(Instant.now())
                    .build()
        ).queue();

        this.jda.shutdown();
        this.moduleManager.disableAll();
        this.gameServiceManager.getDatabaseManager().shutdownDataSource();
        this.pusher.disconnect();
        this.executorMonoThread.shutdownNow();
        this.scheduledExecutorService.shutdownNow();

        this.pubSub.disable();
        this.databaseConnector.killConnection();

        this.logger.info("FluffBOT Safe Shutdown (Completed)");
        this.server.stop(1);
        System.exit(9009);
    }

    @SneakyThrows
    public JDA getJda() {
        return this.jda;
    }
}
