package eu.fluffici.bot.api;

/*
---------------------------------------------------------------------------------
File Name : Rarity.java

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
import java.awt.*;

/**
 * The Rarity class represents the rarity of an item.
 */
@Getter
public enum Rarity {
    COMMON(80, new Color(200, 200, 200)),       // Light Gray
    UNCOMMON(78, new Color(0, 255, 0)),         // Green
    RARE(67, new Color(0, 0, 255)),             // Blue
    EPIC(45, new Color(128, 0, 128)),           // Purple
    LEGENDARY(26, new Color(255, 165, 0)),      // Orange
    MYTHIC(1, new Color(255, 0, 0));            // Red

    private final int chance;
    private final Color color;

    /**
     * The Rarity class represents the rarity of an item.
     * <p>
     * It contains the chance and color associated with each rarity level.
     * <p>
     * Each rarity level is defined as an enum constant with its respective chance and color.
     * <p>
     * To create a new Rarity object, use the constructor with the following parameters:
     *   - chance: an integer representing the chance of obtaining an item with this rarity level
     *   - color: a Color object representing the color associated with this rarity level
     * <p>
     * Note that the chance value determines the relative probability of obtaining an item with this rarity level compared to other levels.
     */
    Rarity(int chance, Color color) {
        this.chance = chance;
        this.color = color;
    }
}