package eu.fluffici.bot.events.guild;

/*
---------------------------------------------------------------------------------
File Name : GuildLoggingListener.java

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

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.DurationUtil;
import eu.fluffici.bot.api.beans.players.OutingSubscriber;
import eu.fluffici.bot.api.beans.statistics.AkceDummy;
import eu.fluffici.bot.api.beans.statistics.AkceNotification;
import eu.fluffici.bot.api.events.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.GenericScheduledEventUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static eu.fluffici.bot.api.IconRegistry.*;

@SuppressWarnings("All")
public class GuildLoggingListener extends ListenerAdapter {

    private final String ICON_NOTES = "https://cdn.discordapp.com/attachments/1224419443300372592/1229355043824865321/notes.png";
    private final String ICON_BAN = "https://cdn.discordapp.com/attachments/1224419443300372592/1225815039131390014/circle-minus.png";
    private final String ICON_UNBAN = "https://cdn.discordapp.com/attachments/1224419443300372592/1226182691540701235/forbid.png";
    private final String ICON_WARN = "https://cdn.discordapp.com/attachments/1224419443300372592/1226168517124952145/alert-triangle.png";
    private final String ICON_USER_MINUS = "https://cdn.discordapp.com/attachments/1224419443300372592/1226183028326531102/user-x.png";
    private final String ICON_USER_PLUS = "https://cdn.discordapp.com/attachments/1224419443300372592/1226183106072150026/user-plus.png";
    private final String ICON_CALENDAR = "https://cdn.discordapp.com/attachments/1224419443300372592/1226184393987723454/calendar-time.png";
    private final FluffBOT instance;

    public GuildLoggingListener(FluffBOT instance) {
        this.instance = instance;
    }

    @Override
    public void onException(ExceptionEvent event) {
        this.instance.getLogger().error(event.getCause().getMessage(), event.getCause());
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.old_name"), event.getOldValue()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.new_name"), event.getNewName()));

        this.sendAudit(AuditBuilder
                .builder()
                    .title(this.instance.getLanguageManager().get("logger.user.update_name"))
                    .actions(pairs)
                    .from(event.getUser())
                .build()
        );
    }

    @Override
    public void onUserUpdateGlobalName(@NotNull UserUpdateGlobalNameEvent event) {
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.old_global_name"), event.getOldGlobalName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.new_global_name"), event.getNewGlobalName()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.user.global_name"))
                .actions(pairs)
                .from(event.getUser())
                .build()
        );
    }

    @Override
    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent event) {
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.old_avatar"), event.getOldAvatarId()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.new_avatar"), event.getNewAvatarId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.user.update_avatar"))
                .actions(pairs)
                .from(event.getUser())
                .build()
        );
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_name"), event.getChannel().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_id"), event.getChannel().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.channel.create"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_name"), event.getChannel().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_id"), event.getChannel().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.channel.delete"))
                .actions(pairs)
                .build()
        );
    }


    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_id"), event.getChannel().getId()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.old_name"), event.getOldValue()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.new_name"), event.getNewValue()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.channel.name"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.channel_id"), event.getChannel().getId()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.old_parent"), event.getOldValue().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.new_parent"), event.getNewValue().getName()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.channel.parent"))
                .actions(pairs)
                .build()
        );
    }
    @Override
    public void onGuildBan(GuildBanEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .icon(this.ICON_BAN)
                .title(this.instance.getLanguageManager().get("logger.ban"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .icon(this.ICON_UNBAN)
                .title(this.instance.getLanguageManager().get("logger.unbon"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role"), event.getRoles().get(0).getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRoles().get(0).getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_add"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.name"), event.getUser().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user.id"), event.getUser().getId()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.name"), event.getRoles().get(0).getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRoles().get(0).getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_remove"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.name"), event.getRole().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRole().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_create"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.name"), event.getRole().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRole().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_delete"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.old_name"), event.getOldName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.new_name"), event.getNewName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRole().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_update"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onRoleUpdatePosition(RoleUpdatePositionEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.old_pos"), String.valueOf(event.getOldPosition())));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.new_pos"), String.valueOf(event.getNewPosition())));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.role.id"), event.getRole().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.role_pos"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onEmojiAdded(EmojiAddedEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.emoji.name"), event.getEmoji().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.emoji.id"), event.getEmoji().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.emoji_add"))
                .actions(pairs)
                .build()
        );
    }

    @Override
    public void onEmojiRemoved(EmojiRemovedEvent event) {
        if (!FluffBOT.getInstance().getDefaultConfig().getProperty("main.guild").equals(event.getGuild().getId()))
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.emoji.name"), event.getEmoji().getName()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.emoji.id"), event.getEmoji().getId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.emoji_removed"))
                .actions(pairs)
                .build()
        );
    }

    @Subscribe
    public void onUserTransfer(UserTransferEvent event) {
        List<Pair<String, String>> pairs = new ArrayList<>();

        User issuedBy = this.instance.getJda().getUserById(event.getPlayer().getUserId());
        User targetPlayer = this.instance.getJda().getUserById(event.getTargetPlayer().getUserId());

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user_transfer.currency"), event.getCurrencyType().name()));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user_transfer.amount"), String.valueOf(event.getAmount())));
        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user_transfer.transfer_id"), event.getTransferId()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.currency_transfer"))
                .from(issuedBy)
                .to(targetPlayer)
                .actions(pairs)
                .build()
        );

        this.instance.getTransferEventMap().put(issuedBy, event);
    }

    @Subscribe
    public void onUserSanction(UserSanctionEvent event) {
        List<Pair<String, String>> pairs = new ArrayList<>();

        Pair<String, String> sanctionType = this.fetchSanctionType(event.getSanction().getTypeId());

        User issuedBy = this.instance.getJda().getUserById(event.getIssuedBy().getUserId());
        User targetPlayer = this.instance.getJda().getUserById(event.getTargetPlayer().getUserId());

        if (event.getSanction().getExpirationTime() != null) {
            String expireAt = DurationUtil.getDuration(Instant.ofEpochMilli(event.getSanction().getExpirationTime().getTime())).toString();
            pairs.add(Pair.of(this.instance.getLanguageManager().get("common.duration"), expireAt));
        }

        pairs.add(Pair.of(this.instance.getLanguageManager().get("common.user_sanction.sanction_type"), sanctionType.getLeft()));

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.".concat(sanctionType.getLeft().toLowerCase()), targetPlayer.getEffectiveName()))
                .from(issuedBy)
                .to(targetPlayer)
                .reason(event.getSanction().getReason())
                .icon(sanctionType.getRight())
                .actions(pairs)
                .attachment(event.getAttachment())
                .isModeration(event.isModeration())
                        .modAction(sanctionType.getLeft())
                .build()
        );
    }

    @Subscribe
    public void onUserUpdateSanction(UserUpdateSanctionEvent event) {
        User issuedBy = this.instance.getJda().getUserById(event.getIssuedBy().getId());
        User targetPlayer = this.instance.getJda().getUserById(event.getTargetPlayer().getId());

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.user_unbanned"))
                .description(this.instance.getLanguageManager().get("logger.user_unbanned.description"))
                .icon(this.fetchSanctionType(5).getRight())
                .from(issuedBy)
                .to(targetPlayer)
                .build()
        );
    }

    @Subscribe
    public void onPusherEvent(PusherCallback callback) {
        switch (callback.getPusherEvent().getChannelName()) {
            case "system-event" -> {
               this.handleSystem(callback);
            }
            case "notifications-event" -> {
                this.handleTrello(callback);
            }
        }
    }

    @Subscribe
    public void onUserRestrictionEvent(UserRestrictionEvent event) {
        User issuedBy = this.instance.getJda().getUserById(event.getAuthor().getId());
        User targetPlayer = this.instance.getJda().getUserById(event.getUser().getId());

        this.sendAudit(AuditBuilder
                .builder()
                .title(this.instance.getLanguageManager().get("logger.user_restriction"))
                .description(this.instance.getLanguageManager().get("logger.user_restriction.description"))
                .icon(ICON_ALERT.getUrl())
                .from(issuedBy)
                .to(targetPlayer)
                .reason(event.getReason())
                .build()
        );
    }

    private void handleSystem(PusherCallback callback) {
        switch (callback.getPusherEvent().getEventName()) {
            case "system-restart" -> {
                this.instance.getJda().getGuildById("1243247749550440508").getTextChannelById("1243922239611605063").sendMessageEmbeds(this.instance.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor("Restart schedulled in 60 seconds from (Github)", "https://fluffici.eu", ICON_ALERT.getUrl())
                        .setDescription("New update was pushed and FluffBOT is now planned to restart...")
                        .setTimestamp(Instant.now())
                        .build()
                ).queue();

                CompletableFuture.runAsync(() -> this.instance.getJda().updateCommands().queue());

                this.instance.getScheduledExecutorService().schedule(() -> {
                    this.instance.onDisable();
                }, 60, TimeUnit.SECONDS);
            }
        }
    }

    private void handleTrello(PusherCallback callback) {
        JsonObject goat = new Gson().fromJson(callback.getPusherEvent().getData(), JsonObject.class);
        if (goat.has("type") && goat.get("type").getAsString().equals("ONLINE"))
            return;

        String eventUrl = "https://akce.fluffici.eu";
        EmbedBuilder eventEmbed = new EmbedBuilder();

        List<OutingSubscriber> subscribers = FluffBOT.getInstance()
                .getUserManager()
                .fetchAllSubscriber()
                .stream()
                .filter(user -> FluffBOT.getInstance().getJda().getUserById(user.getUser().getId()) != null)
                .toList();

        switch (callback.getChannel()) {
            case "create-trello" -> {
                AkceDummy data = new Gson().fromJson(callback.getPusherEvent().getData(), AkceDummy.class);
                eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.new_event", data.getTime()), eventUrl, ICON_CALENDAR);

                if (data.getThumbnail() != null)
                    eventEmbed.setImage(data.getThumbnail());

                eventEmbed.setDescription(data.getDescription());
                eventEmbed.setColor(Color.GREEN);
                eventEmbed.addField("Datum", data.getTime().replace("Datum:", ""), true);

                eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
            }
            case "update-trello" -> {
                AkceNotification data = new Gson().fromJson(callback.getPusherEvent().getData(), AkceNotification.class);
                eventUrl = "https://akce.fluffici.eu/event?id=".concat(data.getEvent());

                if (data.getCurrent().getTime() != data.getPrevious().getTime()) {
                    eventEmbed.addField("Předchozí datum: ", "~~".concat(data.getPrevious().getTime().replace("Datum:", "")).concat("~~"), true);
                    eventEmbed.addField("Nové datum: ", data.getCurrent().getTime().replace("Datum:", ""), true);
                }

                switch (data.getStatus()) {
                    case "INCOMING" -> {
                        eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.updated", data.getCurrent().getName()), eventUrl, ICON_CALENDAR);
                        eventEmbed.setTitle(this.instance.getLanguageManager().get("common.event.updated.description", eventUrl));

                        if (data.getCurrent().getThumbnail() != null)
                            eventEmbed.setImage(data.getCurrent().getThumbnail());

                        eventEmbed.setDescription(data.getCurrent().getDescription());
                        eventEmbed.setColor(Color.YELLOW);

                        eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
                    }
                    case "STARTED" -> {
                        eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.started", data.getCurrent().getName()), eventUrl, ICON_CALENDAR);
                        eventEmbed.setTitle(this.instance.getLanguageManager().get("common.event.started.description", eventUrl));

                        if (data.getCurrent().getThumbnail() != null)
                            eventEmbed.setImage(data.getCurrent().getThumbnail());

                        eventEmbed.setDescription(data.getCurrent().getDescription());
                        eventEmbed.setColor(Color.ORANGE);

                        eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
                    }
                    case "ENDED" -> {
                        eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.ended", data.getCurrent().getName()), eventUrl, ICON_CALENDAR);
                        eventEmbed.setTitle(this.instance.getLanguageManager().get("common.event.ended.description", eventUrl));

                        if (data.getCurrent().getThumbnail() != null)
                            eventEmbed.setImage(data.getCurrent().getThumbnail());

                        eventEmbed.setDescription(data.getCurrent().getDescription());
                        eventEmbed.setColor(Color.MAGENTA);

                        Instant now = Instant.now();
                        Instant future = now.plus(30, ChronoUnit.DAYS);
                        long timestamp = future.getEpochSecond();

                        eventEmbed.addField("Termín pro nahrávání fotografií: ", "<t:" + timestamp + ":R>", true);

                        eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
                    }
                    case "CANCELLED" -> {
                        eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.cancelled", data.getCurrent().getName()), eventUrl, ICON_CALENDAR);
                        eventEmbed.setTitle(this.instance.getLanguageManager().get("common.event.cancelled.description", eventUrl));

                        if (data.getCurrent().getThumbnail() != null)
                            eventEmbed.setImage(data.getCurrent().getThumbnail());

                        eventEmbed.setDescription(data.getCurrent().getDescription());
                        eventEmbed.setColor(Color.RED);

                        eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
                    }
                }
            }
            case "remove-trello" -> {
                AkceDummy data = new Gson().fromJson(callback.getPusherEvent().getData(), AkceDummy.class);
                eventEmbed.setAuthor(this.instance.getLanguageManager().get("common.event.cancelled", data.getName()), eventUrl, ICON_CALENDAR);
                eventEmbed.setTitle(this.instance.getLanguageManager().get("common.event.cancelled.description", eventUrl));

                if (data.getThumbnail() != null)
                    eventEmbed.setImage(data.getThumbnail());

                eventEmbed.setDescription(data.getDescription());
                eventEmbed.setColor(Color.RED);

                eventEmbed.setFooter(this.instance.getLanguageManager().get("common.event.footer"), this.instance.getJda().getSelfUser().getAvatarUrl());
            }
        }

        if (goat.has("event")) {
            UUID eventId = UUID.fromString(goat.get("event").getAsString());
            boolean isAcknowledged = this.instance.getGameServiceManager().hasSubscriptionStatus(eventId, callback.getChannel());

            if (!isAcknowledged) {
                this.instance.getGameServiceManager().createOrUpdateStatus(eventId, callback.getChannel());
            } else {
                this.instance.getLogger().debug("%s event was already acknowledged.", eventId);
                return;
            }
        }

        String finalEventUrl = eventUrl;
        subscribers.forEach(OutingSubscriber -> {
            try {
                CompletableFuture.runAsync(() -> {
                    PrivateChannel channel = new FluffBOT().getInstance().getJda().getUserById(OutingSubscriber.getUser().getId()).openPrivateChannel().complete();
                    if (channel.canTalk()) {
                        channel.sendMessageEmbeds(eventEmbed.build()).addActionRow(Button.link(finalEventUrl, "Vice info")).queue();

                        this.instance.getLogger().debug("%s was notified from %s", channel.getName(), callback.getChannel());
                    } else {
                        this.instance.getUserManager().removeOutingSubscriber(OutingSubscriber.getUser());
                        this.instance.getLogger().info(String.format("Removing %s from the outing-subscriptions, as they have disabled their DMs.", OutingSubscriber.getUser().getId()));
                    }
                });
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                // Dumb foxes around? :0
                // Thread.currentThread().interrupt();
                this.instance.getLogger().error("A error occurred while sending akce-notification", e);
            }
        });
    }

    @SneakyThrows
    private void sendAudit(AuditBuilder auditBuilder) {
        TextChannel logging = this.instance.getJda()
                .getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                .getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.logging"));
        if (logging != null) {
            EmbedBuilder embedBuilder = this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(auditBuilder.getTitle(), "https://fluffici.eu", (auditBuilder.getIcon() != null ? auditBuilder.getIcon() : ICON_NOTES))
                    .setDescription(auditBuilder.getDescription())
                    .setTimestamp(Instant.now());

            if (auditBuilder.getFrom() != null)
                embedBuilder.addField(this.instance.getLanguageManager().get("common.issued_by"), auditBuilder.getFrom().getAsMention(), true);

            if (auditBuilder.getTo() != null)
                embedBuilder.addField(this.instance.getLanguageManager().get("common.issued_for"), auditBuilder.getTo().getAsMention(), true);

            if (auditBuilder.getReason() != null)
                embedBuilder.addField(this.instance.getLanguageManager().get("common.reason"), auditBuilder.getReason(), true);

            if (auditBuilder.getActions() != null && auditBuilder.getActions().size() > 0)
                for (Pair<String, String> action : auditBuilder.getActions())
                    embedBuilder.addField(action.getLeft(), action.getRight(), false);

            if (auditBuilder.getFrom() != null)
                embedBuilder.setFooter(auditBuilder.getFrom().getId(), ICON_REPORT_SEARCH.getUrl());

            if (!logging.canTalk()) {
                this.instance.getLogger().warn("Cannot interact with channel %s", logging.getId());
                return;
            }

            logging.sendMessageEmbeds(embedBuilder.build()).queue();

            if (auditBuilder.isModeration()) {
                TextChannel moderation = this.instance.getJda()
                        .getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                        .getTextChannelById(this.instance.getDefaultConfig().getProperty("channel.moderation"));
                if (moderation != null) {
                    String id = UUID.randomUUID().toString();

                    User from = this.instance.getJda().getUserById(auditBuilder.getFrom().getId());
                    User to = this.instance.getJda().retrieveUserById(auditBuilder.getTo().getId()).complete();

                    moderation.sendMessageEmbeds(this.instance.getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor(this.instance.getLanguageManager().get("common.sanction.applied"), "https://fluffici.eu", ICON_NOTES)
                            .setTitle(auditBuilder.getModAction())
                            .setThumbnail(to.getAvatarUrl())
                            .addField("**ID**", auditBuilder.getTo().getId(), false)
                            .addField("**Důvod**", auditBuilder.getReason(), false)
                            .setImage("attachment://".concat(id.concat(".png")))
                            .setFooter(from.getEffectiveName(), from.getAvatarUrl())
                            .build()
                    ).setFiles(FileUpload.fromData(auditBuilder.getAttachment().downloadToFile().get(), id.concat(".png"))).queue();
                }
            }
        } else {
            this.instance.getLogger().error("Logging channel not found", null);
        }
    }

    @Override
    public void onGenericScheduledEventUpdate(@NotNull GenericScheduledEventUpdateEvent event) { }

    private Pair<String, String> fetchSanctionType(int type) {
        return switch (type) {
            case 1 -> Pair.of("Warn", ICON_WARNING.getUrl());
            case 2 -> Pair.of("Ban", ICON_CIRCLE_SLASHED.getUrl());
            case 3 -> Pair.of("Kick", ICON_USER_MINUS);
            case 4 -> Pair.of("Mute", ICON_MESSAGE_EXCLAMATION.getUrl());
            case 5 -> Pair.of("Unban", ICON_FOLDER.getUrl());

            default -> Pair.of("Unknown", ICON_QUESTION_MARK.getUrl());
        };
    }

    @Getter
    @Setter
    @Builder
    public static class AuditBuilder {
        private List<Pair<String, String>> actions;
        private UserSnowflake from;
        private UserSnowflake to;

        private String title;
        private String description;
        private String reason;
        private String icon;
        private Message.Attachment attachment;
        private boolean isModeration;
        private String modAction;
    }
}
