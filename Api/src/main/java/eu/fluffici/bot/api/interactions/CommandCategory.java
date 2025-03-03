package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : CommandCategory.java

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
import net.dv8tion.jda.api.Permission;

/**
 * An enumeration representing different categories of commands.
 * Each category has a description, permission level, and other properties.
 *
 * @Getter - Lombok annotation to generate getters for all fields
 */
@Getter
public enum CommandCategory {
    ADMINISTRATOR(true, Permission.ADMINISTRATOR, "command.categories.administrator", false, false),
    STAFF(true, Permission.MODERATE_MEMBERS, "command.categories.staff", false, true),
    MODERATOR(true, Permission.MODERATE_MEMBERS, "command.categories.moderator", false, false),
    DEVELOPER(true, Permission.MODERATE_MEMBERS, "command.categories.developer", true, false),
    ECONOMY(false, Permission.UNKNOWN, "command.categories.economy", false, false),
    PROFILE(false, Permission.UNKNOWN, "command.categories.profile", false, false),
    GAMES(false, Permission.UNKNOWN, "command.categories.games", false, false),
    MISC(false, Permission.UNKNOWN, "command.categories.misc", false, false),
    FUN(false, Permission.UNKNOWN, "command.categories.fun", false, false);

    private final boolean isRestricted;
    private final boolean isDeveloper;
    private final boolean isStaff;
    private final Permission permission;
    private final String description;

    CommandCategory(boolean isRestricted, Permission permission, String description, boolean isDeveloper, boolean isStaff) {
        this.isRestricted = isRestricted;
        this.permission = permission;
        this.description = description;
        this.isDeveloper = isDeveloper;
        this.isStaff = isStaff;
    }

    @Override
    public String toString() {
        return "CommandCategory{" +
                "isRestricted=" + isRestricted +
                ", isDeveloper=" + isDeveloper +
                ", isStaff=" + isStaff +
                ", permission=" + permission +
                ", description='" + description + '\'' +
                '}';
    }
}
