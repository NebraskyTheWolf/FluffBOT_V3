package eu.fluffici.bot.events.user;

/*
---------------------------------------------------------------------------------
File Name : MemberBoostListener.java

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
import eu.fluffici.bot.api.beans.players.PlayerBoost;
import eu.fluffici.bot.api.inventory.InventoryItem;
import eu.fluffici.bot.api.rewards.RewardBuilder;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Timestamp;
import java.time.Instant;
public class MemberBoostListener extends ListenerAdapter {

    private final FluffBOT instance;

    public MemberBoostListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onGuildMemberUpdateBoostTime(@NonNull GuildMemberUpdateBoostTimeEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));

        // We check if the main guild is available or exist in the cache.
        if (guild != null) {
            TextChannel announcement = guild.getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.level"));

            // Preparing the announcement message
            EmbedBuilder announcementMessage = this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setFooter(this.instance.getLanguageManager().get("common.boosted.footer"))
                    .setTimestamp(Instant.now());

            // We check if the channel exist and if the application can write in the channel
            if (announcement != null && announcement.canTalk(guild.getSelfMember())) {
                if (this.instance.getGameServiceManager().isBoosting(event.getMember())) {
                    announcement.sendMessageEmbeds(announcementMessage
                            .setAuthor(this.instance.getLanguageManager().get("common.boosted.twice"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1226486478897414216/upvote_icon2.png")
                            .build()
                    ).queue();
                } else {
                    this.instance.getGameServiceManager().applyBoost(new PlayerBoost(
                            event.getMember().getId(),
                            new Timestamp(System.currentTimeMillis()),
                            false
                    ));

                    RewardBuilder reward =  this.instance.getRewardManager().drawRandomReward(
                            this.instance.getUserManager().fetchUser(event.getMember())
                    );

                    reward.getItems().forEach(itemBuilder -> this.instance.getUserManager().addItem(event.getMember(), InventoryItem.builder()
                            .name(itemBuilder.getName())
                            .itemId(itemBuilder.getItemId())
                            .quantity(itemBuilder.getQuantity())
                            .description(itemBuilder.getDescription())
                            .rarity(itemBuilder.getRarity())
                            .build()
                    ));

                    this.instance.getLogger().debug("%s received %s for boosting the server", event.getMember().getId(), reward.getSeed());

                    announcementMessage
                            .setAuthor(this.instance.getLanguageManager().get("common.boosted.once"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1226486478897414216/upvote_icon2.png")
                            .addField(this.instance.getLanguageManager().get("common.boosted.once.reward"), reward.getName(), true);
                    announcement.sendMessageEmbeds(announcementMessage.build()).queue();
                }
            } else {
                this.instance.getLogger().warn("announcementChannel does not exists or is unavailable or I do not have the permission to write there!");
            }
        } else {
            this.instance.getLogger().warn("main-guild does not exists or is unavailable.");
        }
    }
}
