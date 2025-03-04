package eu.fluffici.furraid.components.commands.staff;

import eu.fluffici.bot.api.DiscordUser;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.hooks.furraid.WhitelistBuilder;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import eu.fluffici.furraid.server.users.FGetUserRoute;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

import static eu.fluffici.bot.api.IconRegistry.ICON_QUESTION_MARK;
import static eu.fluffici.furraid.events.guild.GuildUserJoinListener.SPAMMER_MASK;

@SuppressWarnings("All")
public class CommandUserInfo extends FCommand {
    public CommandUserInfo() {
        super("user-info", "Get information about a user", CommandCategory.STAFF);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("isStaff", true);
        this.getOptionData().add(new OptionData(OptionType.STRING, "user-id", "Specify the user id", true));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction, GuildSettings settings) {
        String userId = interaction.getOption("user-id").getAsString();

        if (isValidUserId(userId)) {
            User user = interaction.getJDA().getUserById(userId);
            if (user == null) {
                interaction.replyEmbeds(this.buildError("This user doesn't exist in our records.")).queue();
                return;
            }

            boolean isGloballyBlacklisted = FurRaidDB.getInstance().getBlacklistManager().isGloballyBlacklisted(user);
            boolean isLocallyBlacklisted = FurRaidDB.getInstance().getBlacklistManager().isLocallyBlacklisted(interaction.getGuild(), user);
            boolean isWhitelisted = FurRaidDB.getInstance().getBlacklistManager().isWhitelisted(WhitelistBuilder
                    .builder()
                    .guild(interaction.getGuild())
                    .user(user)
                    .build()
            );
            boolean isStaff = FurRaidDB.getInstance().getBlacklistManager().isStaff(user);
            boolean isSpammer = FGetUserRoute.isSpammer(user);

            EmbedBuilder userInformation = this.getEmbed().simpleAuthoredEmbed();
            userInformation.setColor(Color.magenta);
            userInformation.setAuthor(user.getAsMention(), "https://frdb.fluffici.eu", ICON_QUESTION_MARK.getUrl());
            userInformation.setThumbnail(user.getAvatarUrl());

            userInformation.addField("ID", user.getId(), false);
            userInformation.addField("Account Age", user.getTimeCreated().format(FurRaidDB.getInstance().getDateTimeFormatter()), false);
            userInformation.addBlankField(false);
            userInformation.addField("Is Globally Blacklisted?", (isGloballyBlacklisted ? "Yes" : "No"), false);
            userInformation.addField("Is Locally Blacklisted?", (isLocallyBlacklisted ? "Yes" : "No"), false);
            userInformation.addField("Is Whitelisted?", (isWhitelisted ? "Yes" : "No"), false);
            userInformation.addField("Is FurRaidDB Staff?", (isStaff ? "Yes" : "No"), false);
            userInformation.addField("Is the user flagged a spam?", (isSpammer ? "Yes" : "No"), false);
            userInformation.setFooter(interaction.getJDA().getSelfUser().getName(), interaction.getJDA().getSelfUser().getAvatarUrl());
            userInformation.setTimestamp(Instant.now());

            interaction.replyEmbeds(userInformation.build()).queue();
        } else {
            interaction.replyEmbeds(this.buildError("This user-id is invalid.")).queue();
        }
    }
}
