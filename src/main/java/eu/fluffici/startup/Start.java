package eu.fluffici.startup;

/*
---------------------------------------------------------------------------------
File Name : Start.java

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

import eu.fluffici.bot.logger.Logger;
import lombok.SneakyThrows;

public class Start {
    private static final Logger logger = new Logger("FBL");
    private static final InstanceManager instanceManager = new InstanceManager();
    private static final String VERSION = "1.0.0-beta";

    private static final String BANNER =
            """
                            ███████╗██████╗ ██╗    \s
                            ██╔════╝██╔══██╗██║    \s
                            █████╗  ██████╔╝██║    \s
                            ██╔══╝  ██╔══██╗██║    \s
                            ██║     ██████╔╝███████╗
                            ╚═╝     ╚═════╝ ╚══════╝\s
                            Contact: vakea@fluffici.eu
            """;

    // Please do not change the list orders
    private static final String[] DEVELOPERS = {
            "Lead dev: Vakea <vakea@fluffici.eu>"
    };

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(BANNER);
        logger.info("Starting up FBL (Fluffici Bot Loader) ...");
        logger.info("Copyright (c) 2024 Fluffici, z.s.");
        logger.info("v".concat(VERSION));

        logger.info("Stating system in 5 seconds...");

        Thread.sleep(5000);

        instanceManager.load();
        logger.info("Loading stage finished.");
        hook();

        if (args.length == 0) {
            logger.warn("No argument(s) set, please use -help.");
            System.exit(1002);
        } else {
            boolean isArgument = args[0].indexOf('-') != -1;
            if (isArgument) {
                String argument = args[0].replaceAll("-", "");
                switch (argument) {
                    case "fluffbot" -> {
                        logger.info("Loading fluff-bot instance.");
                        instanceManager.enableByName("fluff-bot");
                    }
                    case "furraiddb" -> {
                        logger.info("Loading fur-raid instance.");
                        instanceManager.enableByName("fur-raid");
                    }
                    case "about" -> {
                        logger.info("Github: https://github.com/fluffici/Fluffbot");
                        logger.info("Website: https://fluffici.eu");
                        logger.info("Status: https://status.fluffici.eu");

                        logger.info("Developer(s):");
                        for (String dev : DEVELOPERS)
                            logger.info(" -> ".concat(dev));

                        logger.info("Licence: PROPRIETARY LICENCE");
                        logger.info("Licence Document: https://autumn.fluffici.eu/attachments/xUiAJbvhZaXW3QIiLMFFbVL7g7nPC2nfX7v393UjEn/fluffici_software_license_cz.pdf");
                    }
                    default -> {
                        logger.info("Unknown instance, please refer to the documentation.");
                        logger.info("Available instances: fluff-bot, fur-raid.");
                    }
                }
            } else {
                logger.warn("Invalid argument(s), please use -help.");
            }
        }
    }

    private static void hook() {
        Runtime.getRuntime().addShutdownHook(new Thread(instanceManager::disableAll));
    }
}
