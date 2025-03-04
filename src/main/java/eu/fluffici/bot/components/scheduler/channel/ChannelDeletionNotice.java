package eu.fluffici.bot.components.scheduler.channel;

/*
---------------------------------------------------------------------------------
File Name : ChannelDeletionNotice.java

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
import eu.fluffici.bot.api.beans.players.ChannelBean;
import eu.fluffici.bot.api.beans.players.ChannelRent;
import eu.fluffici.bot.api.beans.players.DummyChannel;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.api.interactions.Task;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("All")
public class ChannelDeletionNotice extends Task {

    private final Map<UserSnowflake, String> notified = new HashMap<>();
    private final FluffBOT instance;

    public ChannelDeletionNotice(FluffBOT instance) {
        this.instance = instance;

        this.instance.getLogger().debug("Loading 'ChannelDeletionNotice' scheduler.");
    }

    /**
     * Executes the task of deleting unpaid channels.
     * Retrieves the main guild from the instance and checks if it exists.
     * If the main guild exists, it schedules a repeated task with a fixed rate.
     * Inside the task, it asynchronously retrieves a list of not paid channels from the game service manager.
     * For each not paid channel, it retrieves the voice channel from the main guild and checks if it exists.
     * If the voice channel exists, it sends a deletion notice to the owner of the channel.
     * Then it deletes the voice channel and provides a reason for deletion.
     * The task runs every 10 seconds (fixed rate).
     */
    @Override
    public void execute() {
        Guild mainGuild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));
        if (mainGuild == null) {
            return;
        }

        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> CompletableFuture.runAsync(() -> this.instance
                .getGameServiceManager()
                .getChannelsExpiringSoon()
                .forEach(dummyChannel -> {
                    VoiceChannel voiceChannel = mainGuild.getVoiceChannelById(dummyChannel.getChannelId());
                    if (voiceChannel != null) {
                        User owner = this.instance.getJda().getUserById(dummyChannel.getOwnerId());
                        if (owner != null) {
                            this.sendDeletionNotice(owner, dummyChannel);
                        }
                    }
                })), 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Sends a deletion notice to the owner of a DummyChannel.
     *
     * @param owner The user who owns the DummyChannel.
     * @param dummyChannel The DummyChannel to send the deletion notice for.
     */
    @SneakyThrows
    private void sendDeletionNotice(User owner, DummyChannel dummyChannel) {
        PlayerBean currentOwner = this.instance.getUserManager().fetchUser(owner);
        ChannelBean channel = this.instance.getGameServiceManager().getChannel(owner);

        LocalDateTime latestRentAt = dummyChannel.getLatestRentAt().toLocalDateTime();
        LocalDateTime deletionDate = latestRentAt.plusDays(4).plusMonths(1);
        LocalDateTime deletionNextDueDate = latestRentAt.plusMonths(2);
        Instant deletionInstant = deletionDate.atZone(ZoneId.of("Europe/Prague")).toInstant();
        Instant nextDueInstant = deletionNextDueDate.atZone(ZoneId.of("Europe/Prague")).toInstant();
        long deletionEpochSeconds = deletionInstant.getEpochSecond();
        long nextDueEpochSeconds = nextDueInstant.getEpochSecond();
        String discordFormattedDeletionDate = "<t:" + deletionEpochSeconds + ":F>";  // "F" for a full date and time
        String discordFormattedNextDueDate = "<t:" + nextDueEpochSeconds + ":F>";  // "F" for a full date and time

        MessageEmbed message;
        if (channel.getChannelOption().isAutoRenew()) {
            if (this.instance.getUserManager().hasEnoughTokens(currentOwner, 300)) {
                this.instance.getUserManager().removeTokens(currentOwner, 300);

                this.instance.getGameServiceManager().addChannelRent(new ChannelRent(
                        owner.getId(),
                        channel.getChannelId(),
                        GameId.generateId(),
                        new Timestamp(System.currentTimeMillis())
                ));

                message = this.instance.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.instance.getLanguageManager().get("channel.auto.renewal.success.title"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED.getUrl())
                        .setDescription(this.instance.getLanguageManager().get("channel.auto.renewal.success.description", this.instance.getJda().getVoiceChannelById(dummyChannel.getChannelId()).getName(), discordFormattedNextDueDate))
                        .setFooter(this.instance.getLanguageManager().get("channel.auto.renewal.success.footer"))
                        .setColor(Color.GREEN)
                        .build();
            } else {
                message = this.instance.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.instance.getLanguageManager().get("channel.auto.renewal.failure.title"), "https://fluffici.eu", ICON_ALERT.getUrl())
                        .setDescription(this.instance.getLanguageManager().get("channel.auto.renewal.failure.description", this.instance.getJda().getVoiceChannelById(dummyChannel.getChannelId()).getName(), discordFormattedDeletionDate))
                        .setFooter(this.instance.getLanguageManager().get("channel.auto.renewal.failure.footer"))
                        .setColor(Color.RED)
                        .build();
            }
        } else {
            message = this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("channel.deletion.notice.title"), "https://fluffici.eu", ICON_WARNING.getUrl())
                    .setDescription(this.instance.getLanguageManager().get("channel.deletion.notice.description", this.instance.getJda().getVoiceChannelById(dummyChannel.getChannelId()).getName(), discordFormattedDeletionDate))
                    .setFooter(this.instance.getLanguageManager().get("channel.deletion.notice.footer"))
                    .setColor(Color.YELLOW)
                    .build();
        }

        if (!notified.containsKey(owner)) {
            PrivateChannel ownerDm = owner.openPrivateChannel().complete();
            if (ownerDm.canTalk()) {
                ownerDm.sendMessageEmbeds(message).queue();
            } else {
                this.instance.getJda().getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.level"))
                        .sendMessageEmbeds(message)
                        .setContent(owner.getAsMention())
                        .queue();

                this.instance.getLogger().warn("Cannot send deletion notice to owner " + owner.getName() + " (" + owner.getId() + ") because the DM channel is not accessible.");
            }

            notified.put(owner, dummyChannel.getChannelId());
        }
    }
}
