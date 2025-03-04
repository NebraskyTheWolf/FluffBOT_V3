/*
---------------------------------------------------------------------------------
File Name : CommandHelp

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.components.commands.misc;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.bot.api.IconRegistry.ICON_SCROLL;

@SuppressWarnings("All")
public class CommandHelp extends FCommand {
    private final List<FCommand> commands = FurRaidDB.getInstance().getCommandManager().getAllCommands();
    public CommandHelp() {
        super("help", "All the commands help", CommandCategory.MISC);

        this.getSubcommandData().add(new SubcommandData("category", "See all command by category")
                .addOptions(new OptionData(OptionType.STRING, "category", "Select a category", true)
                        .addChoice("Administrator", "ADMINISTRATOR")
                        .addChoice("Misc", "MISC")
                )
        );

        this.getSubcommandData().add(new SubcommandData("command", "See all command category")
                .addOption(OptionType.STRING, "command", "Select a command", true, true)
        );
    }

    @Override
    public void execute(CommandInteraction interaction, GuildSettings settings) {
        String command = interaction.getSubcommandName();

        Map<CommandCategory, List<FCommand>> commandsByCategory = new HashMap<>();
        try {
            CompletableFuture.runAsync(() -> {
                        List<CommandCategory> categories = FurRaidDB.getInstance()
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
                                .filter(commandHandle -> {
                                    CommandCategory category = commandHandle.getCategory();
                                    return !category.isStaff() || (category.isStaff() && FurRaidDB.getInstance().getBlacklistManager().isStaff(interaction.getMember()));
                                })
                                .map(commandHandle -> commandHandle.getCategory())
                                .toList();

                        for (CommandCategory category : categories) {
                            List<FCommand> commands = FurRaidDB.getInstance()
                                    .getCommandManager()
                                    .getAllCommands()
                                    .stream()
                                    .filter((FCommand commandHandle) -> commandHandle.getCategory() == category)
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

                                List<FCommand> categoryCommands = commandsByCategory.get(category);
                                if (categoryCommands == null || categoryCommands.isEmpty()) {
                                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.no_commands"))).queue();
                                    return;
                                }

                                StringBuilder builder = new StringBuilder();
                                builder.append(this.getLanguageManager().get("command.list.category", category.name())).append("\n");
                                for (FCommand command1 : categoryCommands) {
                                    builder.append("**• ").append(command1.getName()).append("**\n");
                                    builder.append("*").append(this.getLanguageManager().get("command.list.description")).append(":* ").append(command1.getDescription()).append("\n");
                                    builder.append("*").append(this.getLanguageManager().get("common.usage")).append(":* `").append(command1.getUsage()).append("`\n\n");
                                }

                                interaction.replyEmbeds(this.getEmbed()
                                        .simpleAuthoredEmbed()
                                        .setAuthor(this.getLanguageManager().get("command.help"), "https://frdb.fluffici.eu", ICON_QUESTION_MARK.getUrl())
                                        .setFooter(this.getLanguageManager().get("command.help.category", this.getLanguageManager().get("command.category.".concat(category.name().toLowerCase()))), ICON_SCROLL.getUrl())
                                        .setDescription(builder.toString())
                                        .build()
                                ).addActionRow(
                                        Button.link("https://frdbdocs.fluffici.eu", "Docs")
                                ).queue();
                            }
                            case "command" ->  {
                                FCommand command1 = FurRaidDB.getInstance().getCommandManager().findByName(interaction.getOption("command").getAsString().toLowerCase());
                                if (command1 == null) {
                                    interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.no_commands"))).queue();
                                    return;
                                }

                                EmbedBuilder commandEmbed = this.getEmbed()
                                        .simpleAuthoredEmbed()
                                        .setAuthor(this.getLanguageManager().get("command.help"), "https://frdb.fluffici.eu", ICON_QUESTION_MARK.getUrl())
                                        .setDescription(command1.getDescription())
                                        .addField(this.getLanguageManager().get("common.usage"), command1.getUsage(), false)
                                        .addField(this.getLanguageManager().get("common.category"), this.getLanguageManager().get("command.category.".concat(command1.getCategory().name().toLowerCase())), false);

                                interaction.replyEmbeds(commandEmbed.build()).addActionRow(
                                        Button.link("https://frdbdocs.fluffici.eu", "Docs")
                                ).queue();
                            }
                        }
                    }).whenCompleteAsync(((unused, throwable) -> commandsByCategory.clear()))
                    .get(30, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.help.timeout"))).queue();
        }
    }
}