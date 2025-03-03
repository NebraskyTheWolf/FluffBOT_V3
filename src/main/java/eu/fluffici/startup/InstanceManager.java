package eu.fluffici.startup;

/*
---------------------------------------------------------------------------------
File Name : InstanceManager.java

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

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.Instance;
import eu.fluffici.furraid.FurRaidDB;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The InstanceManager class is responsible for managing instances of the Instance class.
 */
public class InstanceManager {
    private final Map<String, Instance> instances = new LinkedHashMap<>();
    private final Object[] lock = new Object[] {};

    /**
     * Clears the existing instances and loads new instances into the InstanceManager. The loaded instances include
     * a "fluff-bot" instance of type FluffBOT and a "fur-raid" instance of type FurRaidDB.
     * This method is thread-safe and synchronized on an internal lock object.
     */
    public void load() {
        synchronized (this.lock) {
            this.instances.clear();
            this.instances.put("fluff-bot", new FluffBOT());
            this.instances.put("fur-raid", new FurRaidDB());
        }
    }

    /**
     * Enables all instances by setting the 'loaded' flag to true and calling the 'onEnable' method for each instance.
     * If an exception occurs while calling the 'onEnable' method, a RuntimeException is thrown.
     */
    public void enableAll() {
        this.instances.forEach((name, val) -> {
            this.instances.get(name).setLoaded(true);
            try {
                val.onEnable();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Enables an instance in the InstanceManager by its name. It sets the 'loaded' flag of the instance to true and
     * calls the 'onEnable' method of the instance. If an exception occurs while calling the 'onEnable' method, a
     * RuntimeException is thrown.
     *
     * @param instanceId The name of the instance to enable.
     */
    public void enableByName(String instanceId) {
        this.instances.forEach((name, val) -> {
            if (name.equals(instanceId)) {
                this.instances.get(name).setLoaded(true);
                try {
                    val.onEnable();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Disables all instances in the InstanceManager that are currently loaded.
     * If the 'isLoaded' flag is true for an instance, the 'onDisable' method of that instance is called.
     */
    public void disableAll() {
        this.instances.forEach((name, val) -> {
            if (val.isLoaded())
                val.onDisable();
        });
    }
}
