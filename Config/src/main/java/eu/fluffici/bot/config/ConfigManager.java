package eu.fluffici.bot.config;

/*
---------------------------------------------------------------------------------
File Name : ConfigManager.java

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


import lombok.SneakyThrows;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
public class ConfigManager {
    private final Map<String, Properties> configs = new HashMap<>();
    private final Object[] lock = new Object[] {};

    @SneakyThrows
    public void loadConfig() {
        synchronized (this.lock) {
            this.configs.clear();

            InputStream gitProperties = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("git.properties");
            Properties gitPropertiesProp = new Properties();
            gitPropertiesProp.load(gitProperties);
            this.configs.put("versioning", gitPropertiesProp);
        }
    }

    public Properties getConfig(String key) {
        return this.configs.get(key);
    }
}