package eu.fluffici.language;

/*
---------------------------------------------------------------------------------
File Name : LanguageManager.java

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


import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.logger.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;
@Getter
@Setter
public class LanguageManager implements ILanguageManager {

    private Logger logger = new Logger("LanguageManager");

    private Properties prop = new Properties();
    private String lang;

    private boolean debug  = false;

    public LanguageManager(String lang) {
        this.lang = lang;
        loadProperties();
    }

    public LanguageManager(String lang, boolean debug) {
        this(lang);
        this.debug = debug;
    }

    @Override
    public void loadProperties() {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(lang + ".properties")) {
            assert stream != null;
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                prop.load(reader);
            }
        } catch(IOException ex) {
            this.logger.error("Sorry, unable to find " + lang + ".properties", ex);
        }
    }

    @Override
    public void loadProperties(String languageCode) {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(languageCode + ".properties")) {
            assert stream != null;
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                prop.load(reader);
            }
        } catch(IOException ex) {
            this.logger.error("Sorry, unable to find " + languageCode + ".properties", ex);
        }
    }

    @Override
    public String get(String key, Object... args) {
        String value = this.prop.getProperty(key);

        if (value != null) {
            MessageFormat formatter = new MessageFormat(value);
            value = formatter.format(args);
            return value.trim();
        }

        if (debug)
            return key.replaceAll("\\.", "");
        return key;
    }
}