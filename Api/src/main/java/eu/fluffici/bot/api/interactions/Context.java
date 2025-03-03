package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : Context.java

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
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.hooks.IUserManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
@Getter
public abstract class Context<T> {

    @Setter
    private IEmbed embed;
    @Setter
    private ILanguageManager languageManager;

    @Setter
    private IUserManager userManager;

    private final Command.Type type;
    private final String name;
    private DefaultMemberPermissions permissions = DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS);
    private final Map<String, Boolean> options = new LinkedHashMap<>();

    public Context(Command.Type type, String name) {
        this.type = type;
        this.name = name.toLowerCase(Locale.ROOT);

        if (this.options.getOrDefault("isProtected", false)) {
            this.permissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS);
        }
    }

    public abstract void execute(T interaction);

    public CommandData buildContext() {
        return Commands
                .context(this.type, this.name)
                .setDefaultPermissions(this.permissions)
                .setGuildOnly(true)
                .setNSFW(false);
    }
}
