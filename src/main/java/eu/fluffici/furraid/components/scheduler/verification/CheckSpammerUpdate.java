package eu.fluffici.furraid.components.scheduler.verification;

import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.interactions.Task;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;
import static eu.fluffici.furraid.server.users.FGetUserRoute.isSpammer;

public class CheckSpammerUpdate extends Task {

    private final static String SPAMMER_KEY = "guild:%s/spammer";

    private final FurRaidDB instance;

    public CheckSpammerUpdate(FurRaidDB instance) {
        this.instance = instance;
    }

    @Override
    public void execute() { this.instance.getScheduledExecutorService().scheduleAtFixedRate(this::performCheck, 1, 60, TimeUnit.MINUTES); }

    private void performCheck() {
        this.instance.getJda().getGuilds().forEach(guild -> {
            GuildSettings guildSettings = this.instance.getBlacklistManager().fetchGuildSettings(guild);
            if (guildSettings.isBlacklisted() || !guildSettings.getConfig().getFeatures().getVerification().isEnabled())  return;

            this.instance.getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());
            guild.getMembers().forEach(member -> {
                if (isSpammer(member.getUser())) {
                    this.sendMessage(guild, guildSettings, member);
                    this.instance.getLogger().warn("Flag found in %s ID %s", guild.getId(), member.getId());
                }
            });
        });
    }

    private void sendMessage(@NotNull Guild guild, GuildSettings guildSettings, Member triggered) {
        try (Jedis redis = this.instance.getRedisResource()) {
            if (redis.hexists(SPAMMER_KEY.formatted(guild.getId()), triggered.getId()))
                return;

            TextChannel alertChannel = guild.getTextChannelById(guildSettings.getLoggingChannel());
            if (alertChannel != null && alertChannel.canTalk(guild.getSelfMember())) {
                EmbedBuilder embedBuilder = this.instance.getEmbed().simpleAuthoredEmbed();
                embedBuilder.setAuthor(this.instance.getLanguageManager().get("check.spammer.title", triggered.getId()), "https://frdb.fluffici.eu", ICON_ALERT);
                embedBuilder.setColor(Color.decode("#D70040"));
                embedBuilder.setThumbnail(triggered.getAvatarUrl());
                embedBuilder.setDescription(this.instance.getLanguageManager().get("check.spammer.desc"));
                embedBuilder.setTimestamp(Instant.now());
                embedBuilder.setFooter(this.instance.getJda().getSelfUser().getName(), this.instance.getJda().getSelfUser().getAvatarUrl());

                embedBuilder.addField("ID", triggered.getId(), true);
                embedBuilder.addField("User", triggered.getAsMention(), true);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy HH:mm:ss", Locale.forLanguageTag("cs"));
                embedBuilder.addField("Joined At", triggered.getTimeJoined().format(formatter), false);
                embedBuilder.addField("Account Created At", triggered.getUser().getTimeCreated().format(formatter), false);

                alertChannel.sendMessageEmbeds(embedBuilder.build()).queue();

                redis.hset(SPAMMER_KEY.formatted(guild.getId()), triggered.getId(), "true");
            }
        }
    }
}
