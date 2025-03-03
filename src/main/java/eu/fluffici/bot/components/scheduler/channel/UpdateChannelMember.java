package eu.fluffici.bot.components.scheduler.channel;

/*
---------------------------------------------------------------------------------
File Name : UpdateChannelMember.java

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
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.api.interactions.Task;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
public class UpdateChannelMember extends Task {

    @Getter
    public static UpdateChannelMember instanceChannelUpdater;

    private final FluffBOT instance;

    public UpdateChannelMember(FluffBOT instance) {
        this.instance = instance;
        instanceChannelUpdater = this;

        this.instance.getLogger().debug("Loading UpdateChannelMember scheduler.");
    }

    private final Map<String, Timestamp> lastLimit = new LinkedHashMap<>();

    @Override
    public void execute() {
        this.instance.getScheduledExecutorService().scheduleAtFixedRate(() -> CompletableFuture.runAsync(() -> {
            this.instance.getExecutorMonoThread().schedule(() -> {
                if (this.lastLimit.containsKey("members")) {
                    Duration duration = Duration.between(lastLimit.get("members").toLocalDateTime(), LocalDateTime.now());
                    if (duration.toMinutes() >= 11) {
                        lastLimit.put("members", Timestamp.valueOf(LocalDateTime.now()));
                        this.updateMembers();
                    }
                } else {
                    lastLimit.put("members", Timestamp.valueOf(LocalDateTime.now()));
                    this.updateMembers();
                }
            }, 5, TimeUnit.SECONDS);

            this.instance.getExecutorMonoThread().schedule(() -> {
                if (this.lastLimit.containsKey("messages")) {
                    Duration duration = Duration.between(lastLimit.get("messages").toLocalDateTime(), LocalDateTime.now());
                    if (duration.toMinutes() >= 11) {
                        lastLimit.put("messages", Timestamp.valueOf(LocalDateTime.now()));
                        this.updateMessages();
                    }
                } else {
                    lastLimit.put("messages", Timestamp.valueOf(LocalDateTime.now()));
                    this.updateMessages();
                }
            }, 5, TimeUnit.SECONDS);
        }), 60, 600, TimeUnit.SECONDS);
    }

    public void updateMembers() {
        int members = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild")).getMemberCount();
        VoiceChannel limitChannel = instance.getJda().getVoiceChannelById(instance.getChannelConfig().getProperty("channel.stats.members"));
        if (limitChannel != null) {
            limitChannel.getManager().setName(instance.getLanguageManager().get("common.members", members)).queue();
        }

        this.lastLimit.put("members", Timestamp.valueOf(LocalDateTime.now()));
    }

    public void updateMessages() {
        try {
            List<Message> messages = this.instance.getGameServiceManager().sumAll();
            long count = messages.stream()
                    .filter(message -> message.getCreatedAt().toLocalDateTime().toLocalDate().isEqual(LocalDate.now(ZoneId.systemDefault())))
                    .filter(message -> this.instance.getJda().getUserById(message.getUserId()) != null)
                    .count();

            VoiceChannel messageChannel = this.instance.getJda().getVoiceChannelById(this.instance.getChannelConfig().getProperty("channel.stats.messages"));
            if (messageChannel != null) {
                messageChannel.getManager().setName(this.instance.getLanguageManager().get("common.messages", count)).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.lastLimit.put("messages", Timestamp.valueOf(LocalDateTime.now()));
    }
}
