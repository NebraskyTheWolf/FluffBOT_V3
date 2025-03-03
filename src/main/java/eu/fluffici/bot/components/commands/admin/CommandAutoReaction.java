package eu.fluffici.bot.components.commands.admin;

/*
---------------------------------------------------------------------------------
File Name : CommandAutoReaction.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 12/06/2024
Last Modified : 12/06/2024

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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

@CommandHandle
public class CommandAutoReaction extends Command {
    public CommandAutoReaction() {
        super("auto-reactions", "Manage the channels where the auto-reactions is declared.", CommandCategory.ADMINISTRATOR);

        this.getSubcommandData().add(
                new SubcommandData("add", "Add a channel to the auto-reactions")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Select a channel").setRequired(true)
                        .setChannelTypes(ChannelType.TEXT)
                )
                .addOptions(new OptionData(OptionType.STRING, "reaction-type", "The type of reaction wished.").setRequired(true)
                    .addChoice("Media", "MEDIA")
                    .addChoice("Meme", "MEME")
                )
        );

        this.getSubcommandData().add(
                new SubcommandData("remove", "Remove the automatic reaction on a channel.")
                        .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Select a channel").setRequired(true)
                                .setChannelTypes(ChannelType.TEXT)
                        )
        );

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String commandName = interaction.getSubcommandName();


        switch (Objects.requireNonNull(commandName)) {
            case "add" -> handleAdd(interaction);
            case "remove" -> handleRemove(interaction);
        }
    }

    /**
     * Handles the "add" subcommand of the "auto-reactions" command.
     *
     * @param interaction The command interaction object.
     */
    private void handleAdd(CommandInteraction interaction) {
        Channel channelHandle = interaction.getOption("channel").getAsChannel();
        String type = interaction.getOption("reaction-type").getAsString();

        if (channelHandle instanceof TextChannel channel) {
            FluffBOT.getInstance().getGameServiceManager().createAutoReaction(channel.getId(), type);

            interaction.replyEmbeds(this.buildSuccess("Auto-reaction added successfully on " + channel.getAsMention())).setEphemeral(true).queue();
        } else {
            interaction.replyEmbeds(this.buildError("Invalid channel type. Please select a text channel.")).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the "remove" subcommand of the "auto-reactions" command.
     *
     * @param interaction The command interaction object.
     */
    private void handleRemove(CommandInteraction interaction) {
        Channel channelHandle = interaction.getOption("channel").getAsChannel();

        if (channelHandle instanceof TextChannel channel) {
            FluffBOT.getInstance().getGameServiceManager()
                    .deleteAutoReaction(channel.getId());

            interaction.replyEmbeds(this.buildSuccess("Auto-reaction removed successfully on " + channel.getAsMention())).setEphemeral(true).queue();
        } else {
            interaction.replyEmbeds(this.buildError("Invalid channel type. Please select a text channel.")).setEphemeral(true).queue();
        }
    }
}
