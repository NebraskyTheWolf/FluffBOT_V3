package eu.fluffici.bot.api.interactions;

/*
---------------------------------------------------------------------------------
File Name : Command.java

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


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.hooks.IEmbed;
import eu.fluffici.bot.api.hooks.IFoodManager;
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.hooks.IUserManager;
import eu.fluffici.bot.logger.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static eu.fluffici.bot.api.IconRegistry.*;

@Getter
public abstract class FCommand extends ListenerAdapter {
    @Setter
    private IEmbed embed;
    @Setter
    private ILanguageManager languageManager;

    @Setter
    private IUserManager userManager;

    @Setter
    private IFoodManager foodManager;

    private final String name;
    private final String description;

    @Setter
    private String nameCzech;
    @Setter
    private String descriptionCzech;

    @Setter
    private DefaultMemberPermissions permissions = DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS);

    private final Map<String, Boolean> options = new LinkedHashMap<>();
    private final List<OptionData> optionData = new CopyOnWriteArrayList<>();
    private final List<SubcommandGroupData> subcommandGroupData = new CopyOnWriteArrayList<>();
    private final List<SubcommandData> subcommandData = new CopyOnWriteArrayList<>();

    private final Logger logger;

    private final CommandCategory category;

    public FCommand(String name, String description, CommandCategory category) {
        this.name = name.toLowerCase(Locale.ROOT);
        this.description = description;

        if (this.options.getOrDefault("isProtected", false)) {
            this.permissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS);
        }

        this.logger = new Logger(this.name);

        this.category = category;
    }

    /**
     * Executes the command based on the given CommandInteraction.
     *
     * @param interaction The CommandInteraction representing the event triggered by the user.
     */
    public abstract void execute(CommandInteraction interaction, GuildSettings settings);

    /**
     * Builds a SlashCommandData object based on the current state of the Command object.
     *
     * @return The built SlashCommandData object.
     */
    public SlashCommandData buildCommand() {
        // Fallback
        if (this.getNameCzech() == null) {
            this.setNameCzech(this.name);
        }
        // Fallback
        if (this.getDescriptionCzech() == null) {
            this.setDescriptionCzech(this.description);
        }

        if (!this.optionData.isEmpty()) {
            return Commands
                    .slash(this.name, this.description)
                    .setDefaultPermissions(this.permissions)
                    .addSubcommandGroups(this.subcommandGroupData)
                    .addSubcommands(this.subcommandData)
                    .setNameLocalization(DiscordLocale.CZECH, this.nameCzech)
                    .setDescriptionLocalization(DiscordLocale.CZECH, this.descriptionCzech)
                    .addOptions(this.optionData)
                    .setGuildOnly(true)
                    .setNSFW(false);
        }
        return Commands
                .slash(this.name, this.description)
                .setDefaultPermissions(this.permissions)
                .addSubcommandGroups(this.subcommandGroupData)
                .addSubcommands(this.subcommandData)
                .setNameLocalization(DiscordLocale.CZECH, this.nameCzech)
                .setDescriptionLocalization(DiscordLocale.CZECH, this.descriptionCzech)
                .setGuildOnly(true)
                .setNSFW(false);
    }

    /**
     * Builds an error message embed with the given description.
     *
     * @param description The description of the error message.
     * @return The built MessageEmbed object representing the error message.
     */
    public MessageEmbed buildError(String description) {
        return this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.error"), "https://fluffici.eu", ICON_ALERT_CIRCLE)
                .setDescription(description)
                .setTimestamp(Instant.now())
                .setFooter(this.getLanguageManager().get("common.error.footer"), ICON_QUESTION_MARK)
        .build();
    }

    /**
     * Builds a success message embed with the given description.
     *
     * @param description The description of the success message.
     * @return The built MessageEmbed object representing the success message.
     */
    public MessageEmbed buildSuccess(String description) {
        return this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED)
                .setDescription(description)
                .setTimestamp(Instant.now())
        .build();
    }

    /**
     * Retrieves the usage string for the command.
     *
     * @return The formatted usage string for the command.
     */
    public String getUsage() {
        StringBuilder usageBuilder = new StringBuilder();

        usageBuilder.append("/").append(name);

        if (!subcommandData.isEmpty()) {
            for (SubcommandData subcommand : subcommandData) {
                usageBuilder.append(" ").append(subcommand.getName());
                for (OptionData option : subcommand.getOptions()) {
                    usageBuilder.append(" [").append(option.getName()).append("]");
                }
            }
        }

        if (!optionData.isEmpty()) {
            for (OptionData option : optionData) {
                usageBuilder.append(" [").append(option.getName()).append("]");
            }
        }

        return usageBuilder.toString();
    }

    @Contract(pure = true)
    protected boolean isValidUserId(@NotNull String userId) {
        return userId.matches("[a-zA-Z0-9]+");
    }
}
