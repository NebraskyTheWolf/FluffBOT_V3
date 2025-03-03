package eu.fluffici.bot.utils;

/*
---------------------------------------------------------------------------------
File Name : RarityUtil.java

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
import eu.fluffici.bot.api.Rarity;

public class RarityUtil {
    private static final FluffBOT instance = FluffBOT.getInstance();

    public static String getRarityString(Rarity accessibility) {
        String rarity = "";
        if (isMythic(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.mythic");
        } else if (isLegendary(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.legendary");
        } else if (isEpic(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.epic");
        } else if (isRare(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.rare");
        } else if (isUncommon(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.uncommon");
        } else if (isCommon(accessibility)) {
            rarity = instance.getLanguageManager().get("rarity.common");
        }

        return rarity;
    }

    private static boolean isMythic(Rarity rarity) {
        return rarity == Rarity.MYTHIC;
    }
    private static boolean isLegendary(Rarity rarity) {
        return rarity == Rarity.LEGENDARY;
    }
    private static boolean isEpic(Rarity rarity) {
        return rarity == Rarity.EPIC;
    }
    private static boolean isRare(Rarity rarity) {
        return rarity == Rarity.RARE;
    }
    private static boolean isUncommon(Rarity rarity) {
        return rarity == Rarity.UNCOMMON;
    }
    private static boolean isCommon(Rarity rarity) {
        return rarity == Rarity.COMMON;
    }
}
