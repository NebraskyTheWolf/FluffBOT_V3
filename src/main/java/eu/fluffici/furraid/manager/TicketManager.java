/*
---------------------------------------------------------------------------------
File Name : TicketManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/07/2024
Last Modified : 03/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.fluffici.bot.api.beans.furraid.FurRaidConfig;
import eu.fluffici.bot.api.beans.furraid.GuildSettings;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketMessageBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.ITicketManager;
import eu.fluffici.furraid.FurRaidDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.ICON_FILE;
import static eu.fluffici.bot.api.IconRegistry.ICON_NOTE;

public class TicketManager implements ITicketManager<TicketBuilder, TicketMessageBuilder, JsonObject> {
    private final FurRaidDB instance;

    public TicketManager(FurRaidDB instance) {
        this.instance = instance;
    }

    /**
     * Creates a webhook in the specified {@link TextChannel}.
     *
     * @param channel the text channel in which the webhook will be created
     * @return the URL of the created webhook
     */
    public String createWebhook(TextChannel channel) {
        WebhookAction webhookAction = channel.createWebhook("My Webhook");
        return webhookAction.complete().getUrl();
    }

    /**
     * Creates a new ticket in the specified guild's ticket system.
     *
     * @param user the user creating the ticket
     * @param isStaff whether the user is a staff member or not
     * @return the created ticket text channel
     * @throws Exception if an error occurs during ticket creation
     */
    @Override
    @SuppressWarnings("All")
    public TextChannel createTicket(Guild guild, UserSnowflake user, boolean isStaff) throws Exception {
        try {
            String ticketId = GameId.generateId();
            User ticketOwner = this.instance.getJda().getUserById(user.getId());

            long allowBitmask = Permission.MESSAGE_SEND.getRawValue()
                    |Permission.VIEW_CHANNEL.getRawValue()
                    |Permission.MESSAGE_HISTORY.getRawValue()
                    |Permission.MESSAGE_ATTACH_FILES.getRawValue();
            long denyBitmask = allowBitmask;

            GuildSettings guildSettings = this.instance.getBlacklistManager().fetchGuildSettings(guild);
            FurRaidConfig.TicketFeature ticketFeature = guildSettings.getConfig().getFeatures().getTicket();

            if (!ticketFeature.isEnabled()) {
                return null;
            }

            Category openedCategory = guild.getCategoryById(ticketFeature.getSettings().getCategoryId());
            if (openedCategory == null) {
                return null;
            }

            ChannelAction ticketChannel = guild.createTextChannel(ticketOwner.getGlobalName().concat("-ticket"), openedCategory)
                    .setNSFW(false)
                    .setTopic(this.instance.getLanguageManager().get("ticket.topic.explain"))
                    .addMemberPermissionOverride(ticketOwner.getIdLong(), allowBitmask, 0)
                    .addMemberPermissionOverride(this.instance.getJda().getSelfUser().getIdLong(), allowBitmask, 0)
                    .addRolePermissionOverride(guild.getPublicRole().getIdLong(), 0, denyBitmask);

            // Adding all the staff inside the ticket support
            guildSettings.getConfig().getSettings().getStaffRoles().forEach(string -> ticketChannel.addRolePermissionOverride(Long.parseLong(string), allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0));
            TextChannel channel = (TextChannel) ticketChannel.complete();

            channel.sendMessageEmbeds(this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(ticketFeature.getSettings().getInitialTitle(), "https://frdb.fluffici.eu", ICON_NOTE)
                    .setDescription(ticketFeature.getSettings().getInitialMessage())
                    .build()
            ).addActionRow(
                    FurRaidDB.getInstance().getButtonManager().findByName("row:close-ticket").build(Emoji.fromCustom("receiptoff", 1221209074499322016L, false))
            ).queue();

            User user1 = this.instance.getJda().getUserById(user.getId());

            this.instance.getGameServiceManager().createFTicket(new TicketBuilder(
                    guild.getId(),
                    ticketId,
                    user.getId(),
                    user1.getEffectiveName(),
                    channel.getId(),
                    "OPENED",
                    null,
                    isStaff,
                    this.createWebhook(channel)
            ));

            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Closes a ticket.
     *
     * @param ticket the TicketBuilder object representing the ticket to be closed
     * @throws Exception if an error occurs while closing the ticket
     */
    @Override
    @SuppressWarnings("All")
    public void closeTicket(TicketBuilder ticket, String closedBy) throws Exception {
        Guild guild = this.instance.getJda().getGuildById(ticket.getGuildId());
        User ticketOwner = this.instance.getJda().getUserById(ticket.getUserId());
        Member member = guild.getMember(ticketOwner);

        GuildSettings guildSettings = this.instance.getBlacklistManager().fetchGuildSettings(guild);
        FurRaidConfig.TicketFeature ticketFeature = guildSettings.getConfig().getFeatures().getTicket();

        TextChannel ticketChannel = guild.getTextChannelById(ticket.getChannelId());

        Category closedCategory = guild.getCategoryById(ticketFeature.getSettings().getClosingCategoryId());
        if (closedCategory == null) {
            return;
        }

        ticketChannel.getManager().setParent(closedCategory).queue();
        ticketChannel.getManager().setName(ticketOwner.getGlobalName().concat("-closed")).queue();

        if (member != null) {
            ticketChannel.upsertPermissionOverride(member)
                    .reset().deny(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL).reason("Ticket closed").queue();
        }

        // Clearing out members without permissions or staff roles.
        // FIX: #R6 : This bug was avoiding a proper clearance for the channel, and potentially keeping the same permissionOverride to all members in the channel.
        guildSettings.getConfig().getSettings().getStaffRoles().forEach(roleId -> {
            Role staffRole = this.instance.getJda().getRoleById(roleId);
            ticketChannel.getMembers().forEach(member1 -> {
                if (!member1.getRoles().contains(staffRole))
                    ticketChannel.upsertPermissionOverride(member1).reset().deny(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL).reason("ticket closed").queue();
            });
        });

        List<TicketMessageBuilder> messages = this.fetchTicketMessages(ticketChannel.getId(), guild.getId());
        List<User> usersConcerned = messages.stream()
                .map(message -> this.instance.getJda().getUserById(message.getUserId()))
                .distinct()
                .toList();

        AtomicInteger index = new AtomicInteger(0);
        List<String> formattedUsersList = usersConcerned.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    return index.incrementAndGet() + " " + user.getAsMention() + " - " + user.getGlobalName();
                }).collect(Collectors.toList()).reversed();

        EmbedBuilder closingMessage = new EmbedBuilder();
        closingMessage.setAuthor("Ticket closed", "https://frdb.fluffici.eu", ICON_FILE);
        closingMessage.addField("Ticket Owner", ticketOwner.getAsMention(), true);
        closingMessage.addField("Ticket Name", ticketChannel.getName(), true);
        closingMessage.addField("Panel Name", ticketChannel.getParentCategory().getName(), true);
        closingMessage.addField("Direct Transcript", "Use Button", true);

        if (formattedUsersList.size() <= 0)
            closingMessage.addField("Users Concerned", "No User(s)", true);
        else
            closingMessage.addField("Users Concerned", String.join("\n", formattedUsersList), true);
        closingMessage.setFooter("Ticket ID: " + ticket.getTicketId());

        guild.getTextChannelById(guildSettings.getLoggingChannel())
                .sendMessageEmbeds(closingMessage.build())
                .addActionRow(
                        Button.link(ticketChannel.getJumpUrl(), "Archived channel"),
                        Button.link("https://frdb.fluffici.eu/dashboard", "Transcript")
                ).queue();

        ticket.setStatus("CLOSED");
        FurRaidDB.getInstance().getGameServiceManager().updateFTicket(ticket);

        FurRaidDB.getInstance()
                .getPusherServer()
                .trigger("ticket-".concat(ticket.getTicketId()), "ticket-closed", new JsonObject());
    }

    /**
     * Retrieves the messages associated with a ticket channel.
     *
     * @param channelId the ID of the ticket channel
     * @return a list of Message objects representing the messages in the ticket channel
     * @throws Exception if an error occurs while retrieving the ticket messages
     */
    @Override
    @SuppressWarnings("All")
    public List<TicketMessageBuilder> fetchTicketMessages(String channelId, String guildId) throws Exception {
        TicketBuilder ticket = FurRaidDB.getInstance()
                .getGameServiceManager()
                .fetchFTicket(channelId, guildId);

        if (ticket == null) {
            return Collections.emptyList();
        }

        return FurRaidDB.getInstance().getGameServiceManager().fetchFTicketMessages(ticket.getTicketId());
    }

    /**
     * Adds a message to a ticket.
     *
     * @param message   the message to be added
     * @param ticketId  the ID of the ticket
     * @throws Exception if an error occurs while adding the message to the ticket
     */
    @Override
    public void addTicketMessage(Message message, String ticketId) throws Exception {
        FurRaidDB.getInstance()
                .getGameServiceManager()
                .addFTicketMessage(ticketId, message);

        TicketMessageBuilder ticketMessageBuilder = FurRaidDB.getInstance().getGameServiceManager().fetchFTicketMessageById(ticketId, message.getId());

        FurRaidDB.getInstance()
                .getPusherServer()
                .trigger("ticket-".concat(ticketId), "new-message", this.gson.fromJson(this.gson.toJson(ticketMessageBuilder), JsonObject.class));
    }

    // FurRaidDB use different kind of transcriptions!

    private final Gson gson = new Gson();

    @Override
    @SuppressWarnings("All")
    public JsonObject transcript(String ticketId, String guildId) throws Exception {
        JsonObject data = new JsonObject();

        TicketBuilder ticket = FurRaidDB.getInstance()
                .getGameServiceManager()
                .fetchFTicketById(ticketId);

        if (ticket == null) {
            return null;
        }

        String channelName = "Unknown";

        TextChannel ticketChannel = this.instance.getJda()
                .getGuildById(guildId)
                .getTextChannelById(ticket.getChannelId());

        if (ticketChannel != null) {
            channelName = ticketChannel.getName();
        }

        User owner = this.instance.getJda().getUserById(ticket.getUserId());
        if (owner == null) {
            throw new IllegalArgumentException("Owner user not found: " + ticket.getUserId());
        }

        List<TicketMessageBuilder> messages = this.fetchTicketMessages(ticket.getChannelId(), guildId);
        List<User> usersConcerned = messages.stream()
                .map(message -> this.instance.getJda().getUserById(message.getUserId()))
                .filter(Objects::nonNull) // Filter out null users
                .distinct()
                .toList();

        data.add("users", this.gson.fromJson(this.gson.toJson(usersConcerned), JsonArray.class));
        data.add("messages", this.gson.fromJson(this.gson.toJson(messages), JsonArray.class));

        Map<String, String> replacements = new HashMap<>();

        data.addProperty("owner", owner.getGlobalName());
        data.addProperty("ticketid", ticketId);

        LocalDateTime createdDate = ticket.getCreatedAt().toLocalDateTime();
        LocalDateTime nowDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = createdDate.format(formatter);
        String nowDateFormat = nowDate.format(formatter);

        data.addProperty("opened", formattedDate);
        data.addProperty("closed", nowDateFormat);
        data.addProperty("channel", channelName);

        return data;
    }
}