package eu.fluffici.bot.api.module;

/*
---------------------------------------------------------------------------------
File Name : Module.java

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
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
/**
 * The Module class is an abstract class that represents a module within an application.
 * It extends the ListenerAdapter class from the Discord4J library.
 */
@Getter
public abstract class Module extends ListenerAdapter {

    protected final Logger logger;

    private final String slug;
    private final String name;
    private final String description;
    private final String version;
    private final String author;
    private final Category category;

    public Module(String slug, String name, String description, String version, String author, Category category) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
        this.category = category;

        this.logger = new Logger(this.name);
        this.logger.info(String.format("Loading %s v%s author %s (Category: %s)", this.name, this.version, this.author, this.category.name()));
    }

    /**
     * This method is called when the module is enabled.
     * It should be implemented in a subclass of Module.
     */
    public abstract void onEnable();

    /**
     * This method is called when the module is disabled.
     * It should be implemented in a subclass of Module.
     */
    public abstract void onDisable();
}
