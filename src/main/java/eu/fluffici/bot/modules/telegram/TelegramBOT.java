package eu.fluffici.bot.modules.telegram;

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
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.module.Category;
import eu.fluffici.bot.api.module.Module;
import eu.fluffici.bot.config.ConfigManager;
import eu.fluffici.bot.logger.Logger;
import eu.fluffici.bot.modules.telegram.instance.CommandHandler;
import eu.fluffici.bot.modules.telegram.listeners.UserMessageEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.Properties;

@Getter
public class TelegramBOT extends Module {

    @Getter
    private static TelegramBOT instance;

    private final Logger logger = new Logger("TelegramBOT");

    private ConfigManager configManager;
    private Properties defaultConfig;
    private Properties gitProperties;
    private EventBus eventBus;

    private TelegramBotsLongPollingApplication botsApplication;

    private final FluffBOT fluffbot;

    public TelegramBOT(FluffBOT instance) {
        super("telegram", "Telegram BOT", "Managing the telegram bot", "1.0.0", "Vakea", Category.SYSTEM);

        this.fluffbot = instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!Boolean.parseBoolean(this.fluffbot.getDefaultConfig().getProperty("telegram.enabled"))) {
            logger.warn("TelegramBOT is disabled on configuration.");
            return;
        }

        logger.info("TelegramBOT enabled");

        this.logger.info("Loading registries.");
        this.logger.info("Loading configs..");

        this.configManager = new ConfigManager();
        this.configManager.loadConfig();
        this.defaultConfig = this.configManager.getConfig("default");
        this.gitProperties = this.configManager.getConfig("versioning");
        this.eventBus = this.fluffbot.getEventBus();

        try {
            botsApplication = new TelegramBotsLongPollingApplication();
            String token = this.defaultConfig.getProperty("telegram.token");

            botsApplication.registerBot(token, new CommandHandler(FluffBOT.getInstance(), token, "Fluffici"));

            this.logger.info("'TelegramBOT' Successfully Started!");
        } catch (Exception e) {
            this.logger.error("An error occurred while running the telegram instance: ", e);
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public void onDisable() {
        if (botsApplication != null) {
            botsApplication.close();
            this.logger.info("TelegramBOT stopped.");
        }
    }
}
