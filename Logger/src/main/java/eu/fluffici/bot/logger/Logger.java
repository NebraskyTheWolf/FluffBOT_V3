package eu.fluffici.bot.logger;

/*
---------------------------------------------------------------------------------
File Name : Logger.java

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

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

public class Logger {
    private final String prefix;

    @Getter
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @Setter
    private Boolean debug = false;

    private final String INFO_PREFIX = ConsoleColors.PURPLE_BOLD + " > " + ConsoleColors.GREEN_BOLD + " " +
            EnumType.INFO.name().toUpperCase() + ConsoleColors.CYAN +
            " - " + ConsoleColors.GREEN;
    private final String WARN_PREFIX = ConsoleColors.PURPLE_BOLD + " > " + ConsoleColors.YELLOW_BOLD + " " +
            EnumType.WARN.name().toUpperCase() + ConsoleColors.CYAN +
            " - " + ConsoleColors.YELLOW;
    private final String DOWNLOAD_PREFIX = ConsoleColors.CYAN + " > " + ConsoleColors.CYAN_BOLD + " " +
            EnumType.DEBUG.name().toUpperCase() + ConsoleColors.PURPLE +
            " - " + ConsoleColors.YELLOW_BOLD;
    private final String ERROR_PREFIX = ConsoleColors.PURPLE_BOLD + " > " + ConsoleColors.RED_BOLD + " " +
            EnumType.ERROR.name().toUpperCase() + ConsoleColors.CYAN +
            " - " + ConsoleColors.RED;

    public Logger(String prefix) {
        this.prefix = prefix;
    }

    public void info(String message, Object... args) {
        this.info(String.format(message, args));
    }


    public void info(String message) {
        this.log(EnumType.INFO, message, null);
    }

    public void warn(String message, Object... args) {
        this.warn(String.format(message, args));
    }

    public void warn(String message) {
        this.log(EnumType.WARN, message, null);
    }

    public void error(String message, Throwable throwable) {
        this.log(EnumType.ERROR, message, throwable);
        this.errorCount.incrementAndGet();
    }

    public void debug(String message, Object... args) {
        this.debug(String.format(message, args));
    }

    public void debug(String message) {
        if (this.debug)
            this.log(EnumType.DEBUG, message, null);
    }

    private void log(EnumType type, String message, Throwable throwable) {
        switch (type) {
            case INFO:
                System.out.printf("(%s) %s %s \n", ConsoleColors.BLUE_BOLD + this.prefix , INFO_PREFIX , message + ConsoleColors.RESET);
                break;
            case WARN:
                System.out.printf("(%s) %s %s\n", ConsoleColors.BLUE_BOLD + this.prefix , WARN_PREFIX , message + ConsoleColors.RESET);
                break;
            case ERROR:
                if (throwable != null) {
                    System.out.printf("(%s) %s %s %s\n", ConsoleColors.BLUE_BOLD + this.prefix , ERROR_PREFIX , message + ConsoleColors.RESET, throwable);
                    throwable.printStackTrace();
                    return;
                }
                System.out.printf("(%s) %s %s\n", ConsoleColors.BLUE_BOLD + this.prefix , ERROR_PREFIX , message + ConsoleColors.RESET);
                break;
            case DEBUG:
                System.out.printf("(%s) %s %s\n", ConsoleColors.BLUE_BOLD + this.prefix , DOWNLOAD_PREFIX , message + ConsoleColors.RESET);
                break;
        }
    }
}
