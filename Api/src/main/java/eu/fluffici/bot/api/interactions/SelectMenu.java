package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : SelectMenu.java

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


import eu.fluffici.bot.api.hooks.IEmbed;
import eu.fluffici.bot.api.hooks.IInteractionManager;
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.hooks.IUserManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import java.util.LinkedHashMap;
import java.util.Map;
@Getter
public abstract class SelectMenu<T extends Interaction>  extends ListenerAdapter {
    private final String customId;
    private final Map<String, Boolean> options = new LinkedHashMap<>();
    private final Map<String, String> arguments = new LinkedHashMap<>();

    @Setter
    private IEmbed embed;
    @Setter
    private ILanguageManager languageManager;

    @Setter
    private IUserManager userManager;
    @Setter
    private IInteractionManager interactionManager;

    public SelectMenu(String customId) {
        this.customId = customId;
    }

    public abstract void execute(T interaction);
}
