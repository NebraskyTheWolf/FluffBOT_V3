package eu.fluffici.furraid.components.commands.staff;

import eu.fluffici.bot.api.beans.furraid.FurRaidPremiumOffer;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.OfferQuota;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.api.interactions.FCommand;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("All")
public class CommandServerInfo extends FCommand {
    public CommandServerInfo() {
        super("server-info", "Get information about a server", CommandCategory.STAFF);

        this.setPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
        this.getOptions().put("isStaff", true);
        this.getOptionData().add(new OptionData(OptionType.STRING, "server-id", "Specify the server id", true, true));
    }

    @Override
    public void execute(@NotNull CommandInteraction interaction, GuildSettings settings) {
        String serverId = interaction.getOption("server-id").getAsString();

        if (isValidUserId(serverId)) {
            Guild guild = interaction.getJDA().getGuildById(serverId);
            if (guild == null) {
                interaction.replyEmbeds(this.buildError("This server doesn't exist in our records.")).queue();
                return;
            }

            GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild);

            boolean isGloballyBlacklisted = guildSettings.isBlacklisted();

            OfferQuota quota = FurRaidDB.getInstance().getOfferManager().getByGuild(guild.getIdLong());
            FurRaidPremiumOffer premiumOffer = FurRaidDB.getInstance().getGameServiceManager().getOfferById(quota.getOfferId());

            EmbedBuilder guildInformation = this.getEmbed().simpleAuthoredEmbed();
            guildInformation.setColor((isGloballyBlacklisted ? Color.RED : Color.green));
            guildInformation.setAuthor((isGloballyBlacklisted ? "This server is blacklisted" : guild.getName()), "https://frdb.fluffici.eu", (isGloballyBlacklisted ? ICON_ALERT.getUrl() : ICON_CHECKS.getUrl()));
            guildInformation.setThumbnail(guild.getIconUrl());
            if (guild.getBannerUrl() != null)
                guildInformation.setImage(guild.getBannerUrl());

            if (guild.getVanityUrl() != null)
                guildInformation.addField("**Vanity Url**", guild.getVanityUrl(), false);
            if (isGloballyBlacklisted)
                guildInformation.addField("**Name**", guild.getName(), false);

            guildInformation.addField("ID", guild.getId(), false);
            guildInformation.addField("Owner", guild.getOwner().getAsMention(), false);
            guildInformation.addField("Created ", guild.getTimeCreated().format(FurRaidDB.getInstance().getDateTimeFormatter()) + " days ago.", false);
            guildInformation.addBlankField(false);
            guildInformation.addField("**Is Blacklisted?**", (isGloballyBlacklisted ? "Yes" : "No"), false);
            guildInformation.addField("**Member Count**", NumberFormat.getNumberInstance().format(guild.getMemberCount()), false);
            guildInformation.addField("**Channel Count**",  NumberFormat.getNumberInstance().format(guild.getChannels().size()), false);
            if (premiumOffer != null && !premiumOffer.getOfferName().equals("FurRaid Classic")) {
                guildInformation.addBlankField(false);
                guildInformation.addField("**Offer ID**", String.valueOf(premiumOffer.getId()), false);
                guildInformation.addField("**Offer Name**", premiumOffer.getOfferName(), false);
            }

            guildInformation.setFooter(interaction.getJDA().getSelfUser().getName(), interaction.getJDA().getSelfUser().getAvatarUrl());
            guildInformation.setTimestamp(Instant.now());

            interaction.replyEmbeds(guildInformation.build()).queue();
        } else {
            interaction.replyEmbeds(this.buildError("This server-id is invalid.")).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String userInput = event.getFocusedOption().getValue().toLowerCase();
        try {
            List<Command.Choice> choices = event.getJDA().getGuilds()
                    .stream()
                    .filter(guild -> guild.getName().toLowerCase().startsWith(userInput))
                    .limit(25)
                    .map(guild -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(guild.getName(), guild.getId()))
                    .collect(Collectors.toList());
            event.replyChoices(choices).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
