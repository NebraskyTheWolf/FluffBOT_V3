package eu.fluffici.bot.events.user;

/*
---------------------------------------------------------------------------------
File Name : MemberVoiceChatListener.java

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
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class MemberVoiceChatListener extends ListenerAdapter {
    private final Map<Member, OffsetDateTime> voiceJoinTimes = new HashMap<>();

    /**
     * This method is called whenever there is an update in the voice status of a guild member.
     * <p>
     * Please note that this method is not persistent. In case of a system crash or restart, any runtime data will be cleared out.
     *
     * @param event The GuildVoiceUpdateEvent object containing information about the voice update event.
     */
    @Override
    @SneakyThrows
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        if (event.getChannelJoined() != null) {
            boolean isAFK = event.getChannelJoined().getId().equals(FluffBOT.getInstance().getDefaultConfig().getProperty("channel.afk", "0"));
            if (isAFK)
                return;

            voiceJoinTimes.put(event.getMember(), OffsetDateTime.now());
            FluffBOT.getInstance().getLogger().info("%s joined the voice-chat", event.getMember().getEffectiveName());

            FluffBOT.getInstance().getAchievementManager().unlock(event.getMember(), 39);
        } else if (event.getChannelLeft() != null){
            OffsetDateTime joinTime = voiceJoinTimes.remove(event.getMember());
            if (joinTime != null) {
                int timeInVoice = (int) (OffsetDateTime.now().toEpochSecond() - joinTime.toEpochSecond());
                int timeInVoiceHours = Math.abs(timeInVoice / 3600);

                // Saving the statistic score in bulk.
                FluffBOT.getInstance()
                        .getUserManager()
                        .incrementStatistics(event.getMember(), "voice-chat", timeInVoice);

                // Apply the XP after the member leave the channel
                FluffBOT.getInstance()
                        .getLevelUtil()
                        .addXPVoice(
                                FluffBOT.getInstance().getUserManager().fetchUser(event.getMember()),
                                timeInVoiceHours
                        );

                FluffBOT.getInstance()
                                .getGameServiceManager()
                                .insertVoiceActivity(
                                        event.getMember(),
                                        timeInVoice
                                );

                FluffBOT.getInstance().getLogger().info(event.getMember().getUser().getName() + " spent %s seconds in voice chat", timeInVoice);
            }
        }
    }
}
