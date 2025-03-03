package eu.fluffici.bot.api.achievement;

/*
---------------------------------------------------------------------------------
File Name : AchievementCategory.java

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

/*
---------------------------------------------------------------------------------
File Name : AchievementCategory

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/

import lombok.Getter;
@Getter
public class AchievementCategory {
    private final int id;
    private final String displayName;
    private final String icon;
    private final String[] description;
    private final AchievementCategory parent;

    /**
     * Constructor
     *
     * @param id Achievement category's ID
     * @param displayName Achievement category's display name in GUIs
     * @param icon Achievement category's icon {@link String } in GUIs
     * @param description Achievement category's description in GUIs
     */
    public AchievementCategory(int id, String displayName, String icon, String[] description, AchievementCategory parent)
    {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.parent = parent;
    }
}
