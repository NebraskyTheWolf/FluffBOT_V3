package eu.fluffici.bot.api;

/*
---------------------------------------------------------------------------------
File Name : Instance.java

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

import eu.fluffici.bot.api.game.GameId;
import lombok.Getter;
import lombok.Setter;

/**
 * The Instance class represents an abstract instance of a software application.
 * It provides common functionality and methods that can be implemented by subclasses.
 */
public abstract class Instance {

    @Setter
    @Getter
    public boolean loaded;

    /**
     * The channel variable represents the name of the communication channel.
     *
     * It is a protected final string that is set to "fluffbot@pubsub/security". This channel name is used to access
     * the specific communication channel for the software application. It should not be modified or overwritten
     * by subclasses or external code.
     *
     * Example usage:
     *
     * String communicationChannel = channel;
     */
    protected final String securityChannel = "fbl@pubsub/security";
    protected final String messagingChannel = "fbl@pubsub/messaging";
    protected final String notifyChannel = "fbl@pubsub/notification";

    /**
     * The instanceId variable represents the unique identifier of an instance.
     *
     * It is a private final string that is generated using the UUID.randomUUID().toString() method
     * to ensure uniqueness. This identifier can be used to distinguish one instance from another.
     * The instanceId is set once during the initialization of the Instance class and cannot be changed afterward.
     *
     * Example usage:
     *
     * String id = instanceId;
     */
    protected final String instanceId = GameId.generateId();

    /**
     * The onEnable method is an abstract method that represents the initialization process of an instance.
     * It is called when an instance is being enabled. Subclasses should implement this method to define the necessary
     * actions to be taken during the initialization phase. If an exception occurs during the initialization process,
     * it should be thrown by the implementation of this method.
     *
     * @throws Exception if an error occurs during the initialization process.
     */
    public abstract void onEnable() throws Exception;

    /**
     * The onDisable method is an abstract method that represents the clean-up process of an instance.
     * It is called when an instance is being disabled. Subclasses should implement this method to define the necessary
     * actions to be taken during the clean-up phase.
     */
    public abstract void onDisable();
}
