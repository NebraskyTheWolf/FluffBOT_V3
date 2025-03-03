package eu.fluffici.bot.events.user;

/*
---------------------------------------------------------------------------------
File Name : MemberMessageListener.java

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
import eu.fluffici.bot.api.beans.clans.ClanBean;
import eu.fluffici.bot.api.beans.level.LevelReward;
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import eu.fluffici.bot.api.events.MessageEvent;
import eu.fluffici.bot.api.hooks.PlayerBean;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class MemberMessageListener extends ListenerAdapter {

    private final FluffBOT instance;

    private final List<ItemDescriptionBean> items;

    @SneakyThrows
    public MemberMessageListener(FluffBOT instance) {
        this.instance = instance;
        this.items = this.instance.getGameServiceManager().getAllItems();
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        if (event.getAuthor().isBot())
            return;

        this.instance.getEventBus().post(new MessageEvent(event.getMessage()));

        CompletableFuture.runAsync(() -> {
            PlayerBean handler = this.instance.getUserManager().fetchUser(event.getMember());
            if (handler == null) {
                return;
            }

            Member member =  event.getMember();
            User author =  event.getAuthor();

            try {
                this.instance.getGameServiceManager().incrementMessage(new Message(
                        author.getId(),
                        event.getMessageId(),
                        new Timestamp(Instant.now().toEpochMilli())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.instance.getUserManager().incrementStatistics(author, "messages", 1);

            boolean levelledUp = this.instance.getLevelUtil().addXP(handler);
            boolean hasUnlockable = this.instance.getGameServiceManager().hasRewardAtLevel(handler.getLevel());

            if (levelledUp) {
                List<String> unlockedItems = this.fetchUnlockedItems(handler.getLevel());

                if (hasUnlockable) {
                    LevelReward levelReward = this.instance.getGameServiceManager().getRewardAtLevel(handler.getLevel());
                    Role role = event.getJDA().getRoleById(levelReward.getRoleId());

                    event.getGuild().addRoleToMember(member, role).reason("Levelup reward").queue();

                    event.getGuild().getTextChannelById(this.instance.getChannelConfig().getProperty("channel.level")).sendMessageEmbeds(
                            this.instance.getEmbed()
                                    .simpleAuthoredEmbed()
                                    .setAuthor(author.getName(), author.getAvatarUrl(), author.getAvatarUrl())
                                    .setThumbnail(author.getAvatarUrl())
                                    .setDescription(author.getEffectiveName() + " postoupil/a na level " + (handler.getLevel()))
                                    .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl())
                                    .setTimestamp(Instant.now())
                                    .addField(this.instance.getLanguageManager().get("common.unlocked.items"), (!unlockedItems.isEmpty() ?
                                            this.instance.getLanguageManager().get("common.unlocked.no_items")
                                            : String.join("\n", unlockedItems)
                                    ), true)
                                    .addField(this.instance.getLanguageManager().get("common.unlocked.role"),
                                            this.instance.getLanguageManager().get("common.unlocked.unlocked_role", role.getAsMention()),
                                            false
                                    )
                                    .build()
                    ).setContent(author.getAsMention()).queue();
                } else {
                    event.getGuild().getTextChannelById(this.instance.getChannelConfig().getProperty("channel.level")).sendMessageEmbeds(
                            this.instance.getEmbed()
                                    .simpleAuthoredEmbed()
                                    .setAuthor(author.getName(), author.getAvatarUrl(), author.getAvatarUrl())
                                    .setThumbnail(author.getAvatarUrl())
                                    .setDescription(author.getEffectiveName() + " postoupil/a na level " + (handler.getLevel()))
                                    .setFooter(this.instance.getJda().getSelfUser().getGlobalName(), this.instance.getJda().getSelfUser().getAvatarUrl())
                                    .setTimestamp(Instant.now())
                                    .addField(this.instance.getLanguageManager().get("common.unlocked.items"), (!unlockedItems.isEmpty() ?
                                            this.instance.getLanguageManager().get("common.unlocked.no_items")
                                            : String.join("\n", unlockedItems)
                                    ), true)
                                    .build()
                    ).setContent(author.getAsMention()).queue();
                }

                this.updateNickname(member);
            }
        });


        TicketBuilder ticket = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchTicket(event.getChannel().getId());

        if (ticket != null) {
           try {
               FluffBOT.getInstance()
                       .getTicketManager()
                       .addTicketMessage(event.getMessage(), ticket.getTicketId());
           } catch (Exception e) {
               e.printStackTrace();
           }
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@NonNull GuildMemberUpdateNicknameEvent event) {
        if (!this.instance.getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        CompletableFuture.runAsync(() -> this.updateNickname(event.getMember()));
    }

    private void updateNickname(Member member) {
        PlayerBean handler = this.instance.getUserManager().fetchUser(member);
        if (handler == null) {
            return;
        }

        String currentName = member.getUser().getEffectiveName();
        if (handler.isHasNickname()) {
            currentName = handler.getNickname();
        }

        Member self = member.getGuild().getSelfMember();

        if (self.canInteract(member)) {
            if (this.instance.getUserManager().hasClan(handler)) {
                ClanBean clan =  this.instance.getClanManager().fetchClan(handler);

                this.instance.getJda().getGuildById(member.getGuild().getId()).getMemberById(member.getId())
                        .modifyNickname(String.format("[%s] %s (Lvl.%s)", clan.getPrefix(), currentName, handler.getLevel())).reason("Update nickname.").queue();
            } else {
                this.instance.getJda().getGuildById(member.getGuild().getId()).getMemberById(member.getId())
                        .modifyNickname(String.format("%s (Lvl.%s)",  currentName, handler.getLevel())).reason("Update nickname.").queue();
            }

            this.instance.getLogger().debug(String.format("%s changed their nickname with the command, changing back.", member.getUser().getEffectiveName()));
        } else {
            // Server owners cannot get their nickname's edited since the API v10 of discord.
            this.instance.getLogger().debug(String.format("Cannot interact with %s, insufficient permissions.", member.getUser().getEffectiveName()));
        }
    }

    /**
     * Fetches a list of unlocked items based on the specified level.
     *
     * @param level The level to filter the unlocked items.
     * @return The list of unlocked items that satisfy the given conditions.
     */
    private List<String> fetchUnlockedItems(long level) {
        return this.items
                .stream()
                .map(item -> String.format("Item: %s, Rarity: %s", item.getItemName(), this.instance.getLanguageManager().get("rarity.".concat(item.getItemRarity().name().toLowerCase()))))
                .collect(Collectors.toList());
    }
}
