package eu.fluffici.bot.components.commands;

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
import eu.fluffici.bot.api.hooks.IEmbed;
import eu.fluffici.bot.api.hooks.IFoodManager;
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.hooks.IUserManager;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.logger.Logger;
import eu.fluffici.language.LanguageManager;
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
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationMap;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static eu.fluffici.bot.api.IconRegistry.*;

@Getter
public abstract class Command extends ListenerAdapter {
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
    private DefaultMemberPermissions permissions = DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS);

    private final Map<UserSnowflake, Instant> cache = new HashMap<>();

    private final Map<String, Boolean> options = new LinkedHashMap<>();
    private final List<OptionData> optionData = new CopyOnWriteArrayList<>();
    private final List<SubcommandGroupData> subcommandGroupData = new CopyOnWriteArrayList<>();
    private final List<SubcommandData> subcommandData = new CopyOnWriteArrayList<>();

    private final Logger logger;

    private final CommandCategory category;

    public Command(String name, String description, CommandCategory category) {
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
    public abstract void execute(CommandInteraction interaction);

    /**
     * Builds a SlashCommandData object based on the current state of the Command object.
     *
     * @return The built SlashCommandData object.
     */
    public SlashCommandData buildCommand() {
        LanguageManager commandLang = new LanguageManager("cs_commands", true);
        return Commands
                .slash(commandLang.get("command." + this.name.toLowerCase() + ".name"), commandLang.get("command." + this.name.toLowerCase() + ".description"))
                .setDefaultPermissions(this.permissions)
                .addSubcommandGroups(this.subcommandGroupData)
                .addSubcommands(this.subcommandData)
                .addOptions(this.optionData)
                .setGuildOnly(true)
                .setNSFW(false);
    }

    /**
     * Retrieves an array of images based on the given file name.
     *
     * @param fileName The name of the file containing the images.
     * @return The retrieved JsonArray of images.
     * @throws FileNotFoundException if the file does not exist.
     */
    @SneakyThrows
    protected JsonArray getImages(String fileName)  {
        File root = new File(System.getProperty("user.dir") + "/data/fun/" + fileName);
        Gson gson = new Gson();

        JsonObject data = gson.fromJson(new FileReader(root.getAbsolutePath()), JsonObject.class);

        return data.getAsJsonArray("images");
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
     * Sends a logging message to the specified guild's logging channel.
     *
     * @param guild  The guild to send the logging message to.
     * @param action The action description for the logging message.
     * @param author The author of the action.
     * @param target The target user of the action.
     */
    public void sendLogging(Guild guild, String action, User author, User target) {
        guild.getTextChannelById("747872419091316848").sendMessageEmbeds(
                this.embed.simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("common.logging", author), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED)
                        .setDescription(action)
                        .addField("target", target.getEffectiveName(), true)
                        .setTimestamp(Instant.now())
                        .setFooter(this.getLanguageManager().get("common.logging.footer"), ICON_QUESTION_MARK)
                        .build()
        ).queue();
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

    /**
     * Checks if a user is rate-limited for a given number of minutes.
     *
     * @param user    The UserSnowflake representing the user to check.
     * @param minutes The number of minutes to check for rate-limiting.
     * @return A Pair object containing a boolean indicating whether the user is rate-limited and a Duration object representing the remaining time if rate-limited.
     */
    protected Pair<Boolean, Duration> isRateLimited(UserSnowflake user, int minutes) {
        Instant timer = this.cache.getOrDefault(user, null);
        if (timer != null) {
            Duration durationSinceTimer = Duration.between(timer, Instant.now());
            Duration oneHour = Duration.ofMinutes(minutes);
            Duration remainingTime = oneHour.minus(durationSinceTimer);

            if (remainingTime.toMinutes() > 0) {
                return Pair.of(true, remainingTime);
            } else {
                this.cache.remove(user);
            }
        }

        this.cache.put(user, Instant.now());
        return Pair.of(false, null);
    }
}
