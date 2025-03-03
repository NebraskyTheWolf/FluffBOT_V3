package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandHelp.java

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
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;

@CommandHandle
@SuppressWarnings("All")
public class CommandHelp extends Command {

    // Caching all categories in a list to avoid memory leaks.
    private final List<CommandCategory> categories = FluffBOT.getInstance()
            .getCommandManager()
            .getAllCommands()
            .stream()
            .filter(command -> !command.getCategory().isRestricted())
            .map(command -> command.getCategory())
            .toList();
    private final List<Command> commands = FluffBOT.getInstance().getCommandManager().getAllCommands();
    public CommandHelp() {
        super("help", "Provides detailed information about all available commands", CommandCategory.MISC);

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getSubcommandData().add(new SubcommandData("category", "See all command by category")
                .addOption(OptionType.STRING, "category", "Select a category", true, true)
        );
        this.getSubcommandData().add(new SubcommandData("command", "See all command category")
                .addOption(OptionType.STRING, "command", "Select a command", true, true)
        );
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        Map<CommandCategory, List<Command>> commandsByCategory = new HashMap<>();
        try {
            CompletableFuture.runAsync(() -> {
                List<CommandCategory> categories = FluffBOT.getInstance()
                        .getCommandManager()
                        .getAllCommands()
                        .stream()
                        .filter(commandHandle -> {
                            CommandCategory category = commandHandle.getCategory();
                            return !category.isRestricted() || (category.isRestricted() && interaction.getMember().hasPermission(category.getPermission()));
                        })
                        .filter(commandHandle -> {
                            CommandCategory category = commandHandle.getCategory();
                            return !category.isDeveloper() || (category.isDeveloper() && this.getUserManager().isDeveloper(interaction.getMember()));
                        })
                        .map(commandHandle -> commandHandle.getCategory())
                        .toList();

                for (CommandCategory category : categories) {
                    List<Command> commands = FluffBOT.getInstance()
                            .getCommandManager()
                            .getAllCommands()
                            .stream()
                            .filter((Command commandHandle) -> commandHandle.getCategory() == category)
                            .toList();
                    commandsByCategory.put(category, commands);
                }
            }).thenRunAsync(() -> {
                switch (command) {
                    case "category" -> {
                        String categoryString = interaction.getOption("category").getAsString().toUpperCase();
                        CommandCategory category;
                        try {
                            category = CommandCategory.valueOf(categoryString);
                        } catch (IllegalArgumentException e) {
                            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.invalid_category"))).queue();
                            return;
                        }

                        List<Command> categoryCommands = commandsByCategory.get(category);
                        if (categoryCommands == null || categoryCommands.isEmpty()) {
                            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.no_commands"))).queue();
                            return;
                        }

                        StringBuilder builder = new StringBuilder();
                        builder.append(this.getLanguageManager().get("command.list.category", category.name())).append("\n");
                        for (Command command1 : categoryCommands) {
                            builder.append("**• ").append(command1.getName()).append("**\n");
                            builder.append("*").append(this.getLanguageManager().get("command.list.description")).append(":* ").append(command1.getDescription()).append("\n");
                            builder.append("*").append(this.getLanguageManager().get("common.usage")).append(":* `").append(command1.getUsage()).append("`\n\n");
                        }

                        interaction.replyEmbeds(this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("command.help"), "https://fluffici.eu", ICON_QUESTION_MARK)
                                .setFooter(this.getLanguageManager().get("command.help.category", this.getLanguageManager().get("command.category.".concat(category.name().toLowerCase()))), ICON_SCROLL)
                                .setDescription(builder.toString())
                                .build()
                        ).queue();
                    }
                    case "command" ->  {
                        Command command1 = FluffBOT.getInstance().getCommandManager().findByName(interaction.getOption("command").getAsString().toLowerCase());
                        if (command1 == null) {
                            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.no_commands"))).queue();
                            return;
                        }

                        EmbedBuilder commandEmbed = this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("command.help"), "https://fluffici.eu", ICON_QUESTION_MARK)
                                .setDescription(command1.getDescription())
                                .addField(this.getLanguageManager().get("common.usage"), command1.getUsage(), false)
                                .addField(this.getLanguageManager().get("common.category"), this.getLanguageManager().get("command.category.".concat(command1.getCategory().name().toLowerCase())), false);

                        interaction.replyEmbeds(commandEmbed.build()).queue();
                    }
                }
            }).whenCompleteAsync(((unused, throwable) -> commandsByCategory.clear()))
              .get(30, TimeUnit.SECONDS);
        } catch (InterruptedException|ExecutionException|TimeoutException e) {
            e.printStackTrace();
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.timeout"))).queue();
        }
    }
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();

            switch (event.getSubcommandName()) {
                case "category" -> {
                    List<CommandCategory> categories = FluffBOT.getInstance()
                            .getCommandManager()
                            .getAllCommands()
                            .stream()
                            .filter(command -> {
                                CommandCategory category = command.getCategory();
                                return !category.isRestricted() || (category.isRestricted() && event.getMember().hasPermission(category.getPermission()));
                            })
                            .filter(command -> {
                                CommandCategory category = command.getCategory();
                                return !category.isDeveloper() || (category.isDeveloper() && FluffBOT.getInstance().getUserManager().isDeveloper(event.getMember()));
                            })
                            .map(command -> command.getCategory())
                            .distinct()
                            .toList();

                    List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = categories.stream()
                            .map(CommandCategory::name)
                            .filter(name -> name.toLowerCase().startsWith(userInput))
                            .limit(25)
                            .map(name -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(name, name))
                            .collect(Collectors.toList());

                    event.replyChoices(choices).queue();
                }
                case "command" -> {
                    List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = FluffBOT.getInstance().getCommandManager().getAllCommands().stream()
                            .filter(command -> {
                                CommandCategory category = command.getCategory();
                                return !category.isRestricted() || (category.isRestricted() && event.getMember().hasPermission(category.getPermission()));
                            })
                            .filter(command -> {
                                CommandCategory category = command.getCategory();
                                return !category.isDeveloper() || (category.isDeveloper() && FluffBOT.getInstance().getUserManager().isDeveloper(event.getMember()));
                            })
                            .map(Command::getName)
                            .filter(name -> name.toLowerCase().startsWith(userInput))
                            .limit(25)
                            .map(name -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(name, name))
                            .collect(Collectors.toList());

                    event.replyChoices(choices).queue();
                }
            }
        }
}
