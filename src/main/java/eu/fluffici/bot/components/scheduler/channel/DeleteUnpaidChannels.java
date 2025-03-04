package eu.fluffici.bot.components.scheduler.channel;

/*
---------------------------------------------------------------------------------
File Name : DeleteUnpaidChannels.java

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
import eu.fluffici.bot.api.beans.players.DummyChannel;
import eu.fluffici.bot.api.interactions.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.ICON_ALERT;

@SuppressWarnings("All")
public class DeleteUnpaidChannels extends Task {
    private final FluffBOT instance;

    public DeleteUnpaidChannels(FluffBOT instance) {
        this.instance = instance;
        this.instance.getLogger().debug("Loading 'DeleteUnpaidChannels' scheduler.");
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
                .deleteNotPaidChannels()
                .forEach(dummyChannel -> {
                    VoiceChannel voiceChannel = mainGuild.getVoiceChannelById(dummyChannel.getChannelId());
                    if (voiceChannel != null) {
                        User owner = this.instance.getJda().getUserById(dummyChannel.getOwnerId());
                        if (owner != null) {
                            this.sendDeletionNotice(owner, dummyChannel);
                        }

                        voiceChannel.delete().reason("Unpaid rent (Last: " + dummyChannel.getLatestRentAt().toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME) + ")").queue();
                    }
                })), 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Sends a deletion notice to the owner of a channel.
     *
     * @param owner        The owner of the channel
     * @param dummyChannel The dummy channel to send the notice for
     */
    private void sendDeletionNotice(User owner, DummyChannel dummyChannel) {

        LocalDateTime latestRentAt = dummyChannel.getLatestRentAt().toLocalDateTime();
        LocalDateTime deletionDate = latestRentAt.plusDays(4).plusMonths(1);
        Instant deletionInstant = deletionDate.atZone(ZoneId.of("Europe/Prague")).toInstant();
        long deletionEpochSeconds = deletionInstant.getEpochSecond();
        String discordFormattedDeletionDate = "<t:" + deletionEpochSeconds + ":F>";  // "F" for a full date and time

        EmbedBuilder message = this.instance.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.instance.getLanguageManager().get("channel.deleted.notice.title"), "https://fluffici.eu", ICON_ALERT.getUrl())
                .setDescription(this.instance.getLanguageManager().get("channel.deleted.notice.description", dummyChannel.getChannelId(), discordFormattedDeletionDate))
                .setFooter(this.instance.getLanguageManager().get("channel.deleted.notice.footer"))
                .setColor(Color.RED);

        PrivateChannel ownerDm = owner.openPrivateChannel().complete();
        if (ownerDm.canTalk()) {
            ownerDm.sendMessageEmbeds(message.build()).queue();
        } else {
            this.instance.getJda().getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.level"))
                    .sendMessageEmbeds(message.build())
                    .setContent(owner.getAsMention())
                    .queue();
        }
    }
}
