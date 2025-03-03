package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandRelationship.java

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
import eu.fluffici.bot.api.beans.players.RelationshipInviteBuilder;
import eu.fluffici.bot.api.beans.players.RelationshipMember;
import eu.fluffici.bot.api.bucket.Beta;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.Interactions;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_CIRCLE_MINUS;
import static eu.fluffici.bot.api.IconRegistry.ICON_HEART;

@CommandHandle
@Beta
@SuppressWarnings("All")
public class CommandRelationship extends Command {

    private final FluffBOT instance;

    /**
     * Represents the 'relationship' command which allows adding and removing partners as relationships.
     */
    public CommandRelationship(FluffBOT instance) {
        super("relationship", "This command allow you to add your partner(s) as relationship.", CommandCategory.MISC);

        this.instance = instance;

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        this.getSubcommandData().add(new SubcommandData("add", "Add your partener(s)")
                .addOptions(new OptionData(OptionType.USER, "user", "Select a user", true))
        );

        this.getSubcommandData().add(new SubcommandData("remove", "Remove one of your partener(s) :(")
                .addOptions(new OptionData(OptionType.USER, "user", "Select a user", true))
        );

        this.getSubcommandData().add(new SubcommandData("lookup", "See the list of your partner(s)"));
        this.getSubcommandData().add(new SubcommandData("leave", "Leave your current relatiobship"));

        this.getOptions().put("noSelfUser", true);
    }

    /**
     * Executes the command interaction by handling different subcommands.
     *
     * @param interaction The CommandInteraction object representing the interaction.
     */
    @Override
    public void execute(CommandInteraction interaction) {
        switch (interaction.getSubcommandName()) {
            case "add" -> this.handleAdd(interaction);
            case "remove" -> this.handleRemove(interaction);
            case "lookup" -> this.handleLookup(interaction);
            case "leave" -> this.handleLeave(interaction);
        }
    }

    /**
     * Handles the add command interaction.
     *
     * @param interaction The CommandInteraction object representing the interaction.
     */
    private void handleAdd(CommandInteraction interaction) {
        User target = interaction.getOption("user").getAsUser();

        if (target.getId().equals(interaction.getUser().getId())) {
            interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("common.relationship.error.self"))).setEphemeral(true).queue();
            return;
        } else if (this.getUserManager().fetchRelationship(interaction.getUser(), target)) {
            interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("common.relationship.error.already"))).setEphemeral(true).queue();
            return;
        }

        this.getUserManager().sendInvite(RelationshipInviteBuilder
                .builder()
                        .relationshipOwner(interaction.getUser())
                        .userId(target)
                .build());

        Pair<Button, String> acceptInvite = this.instance.getButtonManager().toInteraction("row_accept_relationship", target, null, true);
        Message message = target.openPrivateChannel().complete().sendMessageEmbeds(
                        this.getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor(this.getLanguageManager().get("common.relationship.invite", interaction.getUser().getEffectiveName()), "https://fluffici.eu", ICON_HEART)
                                .setDescription(this.getLanguageManager().get("common.relationship.invite.desc"))
                                .build()
                ).mention(target)
                .addActionRow(acceptInvite.getLeft())
                .complete();

        Interactions interactions = this.instance.getInteractionManager().fetchInteraction(acceptInvite.getRight());
        interactions.setAttached(true);
        interactions.setMessageId(message.getId());
        this.instance.getInteractionManager().updateInteraction(interactions);

        interaction.replyEmbeds(this.buildSuccess(this.instance.getLanguageManager().get("common.relationship.confirm.invited", target.getAsMention()))).setEphemeral(true).queue();
    }

    /**
     * Handles the remove command interaction.
     *
     * @param interaction The CommandInteraction object representing the interaction.
     */
    private void handleRemove(CommandInteraction interaction) {
        User target = interaction.getOption("user").getAsUser();
        this.getUserManager().removeRelationship(interaction.getUser(), target);
        interaction.replyEmbeds(
                this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("common.relationship.remove.title", interaction.getUser().getName()), "https://fluffici.eu", ICON_CIRCLE_MINUS)
                        .setDescription(this.getLanguageManager().get("common.relationship.remove.desc", target.getAsMention()))
                        .build()
        ).setEphemeral(true).queue();
    }

    /**
     * Handles the leave command interaction.
     *
     * @param interaction The CommandInteraction object representing the interaction.
     */
    @SneakyThrows
    private void handleLeave(CommandInteraction interaction) {
        if (this.getUserManager().fetchRelationship(interaction.getUser())) {
            boolean isOwner = FluffBOT.getInstance().getGameServiceManager().isRealationshipOwner(interaction.getUser());

            if (isOwner) {
                List<RelationshipMember> members = this.instance.getGameServiceManager().fetchAllRelationshipMembers(interaction.getUser());
                List<String> mentions = members.stream().map(relationshipMember -> this.instance.getJda().getUserById(relationshipMember.getUserId().getIdLong()).getAsMention())
                        .toList();
                for (RelationshipMember member : members) {
                    PlayerBean updatedProfile = this.getUserManager().fetchUser(member.getUserId());
                    updatedProfile.setBoundTo(null);

                    this.instance.getGameServiceManager().updatePlayer(updatedProfile);
                }

                interaction.replyEmbeds(
                        this.buildSuccess(this.instance.getLanguageManager().get("common.relationship.left", String.join("\n", mentions)))
                ).setEphemeral(true).queue();
            } else {
                this.instance.getGameServiceManager().leaveRelationship(interaction.getUser());
                PlayerBean updatedProfile = this.getUserManager().fetchUser(interaction.getUser());
                UserSnowflake target = UserSnowflake.fromId(updatedProfile.getBoundTo());
                updatedProfile.setBoundTo(null);

                this.instance.getGameServiceManager().updatePlayer(updatedProfile);

                interaction.replyEmbeds(
                        this.buildSuccess(this.instance.getLanguageManager().get("common.relationship.left", target.getAsMention()))
                ).setEphemeral(true).queue();
            }
        } else {
            interaction.replyEmbeds(
                    this.buildError(this.instance.getLanguageManager().get("common.relationship.error.no_relationship"))
            ).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the lookup command interaction.
     *
     * @param interaction The CommandInteraction object representing the interaction.
     */
    private void handleLookup(CommandInteraction interaction) {
        List<RelationshipMember> relationships = this.getUserManager().fetchAllRelationshipMembers(interaction.getUser());
        if (relationships.isEmpty()) {
            interaction.replyEmbeds(
                    this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("common.relationship.lookup.title", interaction.getUser().getName()), "https://fluffici.eu", ICON_CIRCLE_MINUS)
                            .setDescription(this.getLanguageManager().get("common.relationship.lookup.desc"))
                            .addField(this.getLanguageManager().get("common.relationship.lookup.field"), "No relationships found", false)
                            .build()
            ).setEphemeral(true).queue();
        } else {
            List<User> users = relationships.stream()
                    .map((RelationshipMember relationship) -> {
                        return this.instance.getJda().getUserById((relationship.getUserId() != null ? relationship.getUserId().getId() : relationship.getRelationshipOwner() != null ? relationship.getRelationshipOwner().getId() : null));
                    })
                    .collect(Collectors.toList());

            List<String> userMentions = users
                    .stream()
                    .filter(user -> user != null)
                    .map(User::getAsMention)
                    .collect(Collectors.toList());

            String userMentionsString = String.join("\n", userMentions);

            interaction.replyEmbeds(
                    this.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.getLanguageManager().get("common.relationship.lookup.title", interaction.getUser().getName()), "https://fluffici.eu", ICON_HEART)
                            .setDescription(this.getLanguageManager().get("common.relationship.lookup.desc"))
                            .addField(this.getLanguageManager().get("common.relationship.lookup.field"), userMentionsString, false)
                            .build()
            ).setEphemeral(true).queue();
        }
    }
}
