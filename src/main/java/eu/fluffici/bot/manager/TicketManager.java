/*
---------------------------------------------------------------------------------
File Name : TicketManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.manager;

import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.beans.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.ticket.TicketMessageBuilder;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.ITicketManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.fluffici.bot.api.IconRegistry.*;
import static eu.fluffici.bot.api.TranscriptTemplate.TRANSCRIPT;

/**
 * TicketManager represents a ticket management system that allows creating, closing, and
 * interacting with tickets in a guild.
 *
 * @deprecated Sees FurRaidDB source code.
 */
@Deprecated(forRemoval = true, since = "3.0.4-ALPHA")
public class TicketManager implements ITicketManager<TicketBuilder, TicketMessageBuilder, String> {

    private final FluffBOT instance;

    public TicketManager(FluffBOT instance) {
        this.instance = instance;
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
    public TextChannel createTicket(Guild guild0, UserSnowflake user, boolean isStaff) throws Exception {
        try {
            String ticketId = GameId.generateId();
            User ticketOwner = this.instance.getJda().getUserById(user.getId());

            long allowBitmask = Permission.MESSAGE_SEND.getRawValue()
                    |Permission.VIEW_CHANNEL.getRawValue()
                    |Permission.MESSAGE_HISTORY.getRawValue()
                    |Permission.MESSAGE_ATTACH_FILES.getRawValue();
            long denyBitmask = allowBitmask;

            Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));
            Category openedCategory = guild.getCategoryById(this.instance.getDefaultConfig().getProperty("category.ticket.opened"));
            if (openedCategory == null) {
                return null;
            }

            TextChannel ticketChannel = guild.createTextChannel(ticketOwner.getGlobalName().concat("-ticket"), openedCategory)
                    .setNSFW(false)
                    .setTopic(this.instance.getLanguageManager().get("ticket.topic.explain"))
                    .addMemberPermissionOverride(ticketOwner.getIdLong(), allowBitmask, 0)
                    .addMemberPermissionOverride(this.instance.getJda().getSelfUser().getIdLong(), allowBitmask, 0)
                    .addRolePermissionOverride(606542137819136020L, 0, denyBitmask)
                    .addRolePermissionOverride(606542004708573219L, 0, denyBitmask)
                    .addRolePermissionOverride(606534136806637589L, 0, denyBitmask)

                    // BETA SERVER default role id
                    .addRolePermissionOverride(1243247749550440508L, 0, denyBitmask)

                    .addRolePermissionOverride(606535408117088277L, allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0)
                    .addRolePermissionOverride(606540681867034634L, allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0)
                    .addRolePermissionOverride(782578470135660585L, allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0)
                    .addRolePermissionOverride(606540994909044756L, allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0)
                    .addRolePermissionOverride(943216911980822569L, allowBitmask|Permission.MESSAGE_MANAGE.getRawValue(), 0)
                    .complete();

            ticketChannel.sendMessageEmbeds(this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                            .setAuthor("FluffBOT - Nový ticket", "https://fluffici.eu", ICON_NOTE)
                            .setDescription(
                                    """                                   
                                    - **Právě jsi vytvořil nový ticket**: Popiš prosím podrobně, s čím máš problém a náš moderátorský tým se ti bude co nejdříve věnovat. Prosíme o trpělivost.    
                                                      
                                    - **Note for foreign speakers**: This server's primary language is Czech / Slovakian. Although you can join as an English speaker, please note that communication across all of the chats should be in CZ / SK language.                             
                                    """
                            )
                    .build()
            ).addActionRow(
                    FluffBOT.getInstance().getButtonManager().findByName("row:close-ticket").build(Emoji.fromCustom("receiptoff", 1221209074499322016L, false))
            ).queue();

            this.instance.getGameServiceManager().createTicket(new TicketBuilder(
                    ticketId,
                    user.getId(),
                    ticketChannel.getId(),
                    "OPENED",
                    null,
                    isStaff
            ));

            return ticketChannel;
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
        User ticketOwner = this.instance.getJda().getUserById(ticket.getUserId());

        Guild guild = this.instance.getJda().getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"));

        Member member = guild.getMember(ticketOwner);

        TextChannel ticketChannel = guild.getTextChannelById(ticket.getChannelId());

        Category closedCategory = guild.getCategoryById(this.instance.getDefaultConfig().getProperty("category.ticket.closed"));
        if (closedCategory == null) {
            return;
        }

        ticketChannel.getManager().setParent(closedCategory).queue();
        ticketChannel.getManager().setName(ticketOwner.getGlobalName().concat("-closed")).queue();

        if (member != null) {
            ticketChannel.upsertPermissionOverride(member)
                    .reset().reason("Ticket closed").queue();
        }


        List<TicketMessageBuilder> messages = this.fetchTicketMessages(ticketChannel.getId(), "");

        List<User> usersConcerned = messages.stream()
                .map(message -> this.instance.getJda().getUserById(message.getUserId()))
                .distinct()
                .toList();

        AtomicInteger index = new AtomicInteger(0);
        List<String> formattedUsersList = usersConcerned.stream()
                .filter(Objects::nonNull)
                .map(user -> {
                    if (usersConcerned.isEmpty())
                        return "No user(s)";
                    return index.incrementAndGet() + " " + user.getAsMention() + " - " + user.getGlobalName();
                }).collect(Collectors.toList());

        EmbedBuilder closingMessage = new EmbedBuilder();
        closingMessage.setFooter("Ticket ID: " + ticket.getTicketId());
        closingMessage.setAuthor(this.instance.getLanguageManager().get("ticket.closed.title"), "https://fluffici.eu", ICON_FOLDER);
        closingMessage.addField("Ticket Owner", ticketOwner.getAsMention(), true);
        closingMessage.addField("Ticket Name", ticketChannel.getName(), true);
        closingMessage.addField("Panel Name", ticketChannel.getParentCategory().getName(), true);
        closingMessage.addField("Direct Transcript", "Use Button", true);
        closingMessage.addField("Users Concerned", String.join("\n", formattedUsersList), true);
        closingMessage.setColor(Color.decode("#EF4423"));

        this.instance.getJda()
                .getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                .getTextChannelById(this.instance.getChannelConfig().getProperty("channel.staff"))
                .sendMessageEmbeds(closingMessage.build())
                .setContent("-# <:frdb:883125178320687184> The ticket was closed by **" + closedBy + "**")
                .addActionRow(
                        Button.link(ticketChannel.getJumpUrl(), "Archived channel"),
                        Button.link("https://fluffbot.fluffici.eu/transcript/".concat(ticket.getTicketId()), "Transcript")
                ).queue();

        ticket.setStatus("CLOSED");
        FluffBOT.getInstance().getGameServiceManager().updateTicket(ticket);
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
    public List<TicketMessageBuilder> fetchTicketMessages(String channelId, String guild) throws Exception {
        TicketBuilder ticket = FluffBOT.getInstance()
                .getGameServiceManager()
                .fetchTicket(channelId);

        if (ticket == null) {
            return Collections.emptyList();
        }

        return FluffBOT.getInstance().getGameServiceManager().fetchMessages(ticket.getTicketId());
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
        FluffBOT.getInstance()
                .getGameServiceManager()
                .addTicketMessage(ticketId, message);
    }

    @Override
    @SuppressWarnings("All")
    public String transcript(String ticketId, String guildId) {
        try {
            TicketBuilder ticket = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .fetchTicketById(ticketId);

            if (ticket == null) {
                return null;
            }

            String channelName = "Unknown";

            TextChannel ticketChannel = this.instance.getJda()
                    .getGuildById(this.instance.getDefaultConfig().getProperty("main.guild"))
                    .getTextChannelById(ticket.getChannelId());

            if (ticketChannel != null) {
                channelName = ticketChannel.getName();
            }

            User owner = this.instance.getJda().getUserById(ticket.getUserId());
            if (owner == null) {
                throw new IllegalArgumentException("Owner user not found: " + ticket.getUserId());
            }

            List<TicketMessageBuilder> messages = this.fetchTicketMessages(ticket.getChannelId(), "");
            List<User> usersConcerned = messages.stream()
                    .map(message -> this.instance.getJda().getUserById(message.getUserId()))
                    .filter(Objects::nonNull) // Filter out null users
                    .distinct()
                    .toList();

            List<String> users = usersConcerned.stream().map(user -> "<p><i class=\"fas fa-user\"></i> %s</p>".formatted(user.getGlobalName())).toList();
            List<String> formattedMessages = messages.stream().map(message -> this.processMessageAsync(message)).toList();

            Map<String, String> replacements = new HashMap<>();

            replacements.put("owner", owner.getGlobalName());
            replacements.put("ticketid", ticketId);

            LocalDateTime createdDate = ticket.getCreatedAt().toLocalDateTime();
            LocalDateTime nowDate = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = createdDate.format(formatter);
            String nowDateFormat = nowDate.format(formatter);

            replacements.put("opened", formattedDate);
            replacements.put("closed", nowDateFormat);
            replacements.put("channel", channelName);
            replacements.put("users", String.join("\n", users));
            replacements.put("messages", String.join("\n", formattedMessages));

            return this.replacePlaceholdersInFile(TRANSCRIPT, replacements);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Replaces placeholders in the given file content with the provided replacements.
     *
     * @param fileContent the content of the file as a string
     * @param replacements a map of placeholders and their corresponding values
     * @return the modified file content with placeholders replaced by their values
     */
    private String replacePlaceholdersInFile(String fileContent, @NotNull Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            String value = entry.getValue();
            fileContent = fileContent.replaceAll(placeholder, value);
        }

        return fileContent;
    }

    /**
     * Processes the given Discord message asynchronously.
     *
     * @param m the Discord message to process
     * @return the formatted HTML representation of the message
     */
    private String processMessageAsync(@NotNull TicketMessageBuilder m) {
        User user = this.instance.getJda().getUserById(m.getUserId());

        String message = m.getMessageContent();

        Pattern userRegex = Pattern.compile("<@(\\d{16,19})>");
        Matcher userMatcher = userRegex.matcher(message);
        while (userMatcher.find()) {
            String userId = userMatcher.group(1);

            User mentionedUser = this.instance.getJda().getUserById(userId);
            String mentionedUserName = mentionedUser != null ? mentionedUser.getGlobalName() : "@Unknown User";
            message = message.replaceAll("<@\\d{16,19}>", "<strong class='discord-mention'>@" + mentionedUserName + "</strong>");
        }

        Pattern channelRegex = Pattern.compile("<#(\\d{16,19})>");
        Matcher channelMatcher = channelRegex.matcher(message);
        while (channelMatcher.find()) {
            String channelId = channelMatcher.group(1);
            TextChannel channel = this.instance.getJda().getTextChannelById(channelId);
            String channelName = channel != null ? channel.getName() : "Unknown Channel";
            message = message.replaceAll("<#\\d{16,19}>", "<strong style=\"color: orange;\">#" + channelName + "</strong>");
        }

        Pattern roleRegex = Pattern.compile("<@&(\\d{16,19})>");
        Matcher roleMatcher = roleRegex.matcher(message);
        while (roleMatcher.find()) {
            String roleId = roleMatcher.group(1);
            Role role = this.instance.getJda().getRoleById(roleId);
            String roleName = role != null ? role.getName() : "Unknown Role";
            message = message.replaceAll("<@&\\d{16,19}>", "<strong style=\"color: green;\">@" + roleName + "</strong>");
        }

        Pattern urlRegex = Pattern.compile("(https?://\\S+)");
        Matcher urlMatcher = urlRegex.matcher(message);
        while (urlMatcher.find()) {
            String url = urlMatcher.group(1);
            message = message.replaceAll("(https?://\\S+)", "<a href=\"" + url + "\">" + url + "</a>");
        }

        Pattern discordLinkRegex = Pattern.compile("\\[(.*?)]\\((https?://\\S+)\\)");
        Matcher discordLinkMatcher = discordLinkRegex.matcher(message);

        while (discordLinkMatcher.find()) {
            String text = discordLinkMatcher.group(1);
            String url = discordLinkMatcher.group(2);

            String htmlLink = "<a href=\"" + url + "\">" + text + "</a>";
            message = message.replace(discordLinkMatcher.group(0), htmlLink);
        }

        Pattern emojiRegex = Pattern.compile("<:([a-zA-Z0-9_]+):(\\d{16,19})>");
        Matcher emojiMatcher = emojiRegex.matcher(message);
        while (emojiMatcher.find()) {
            String emojiName = emojiMatcher.group(1);
            String emojiId = emojiMatcher.group(2);
            RichCustomEmoji emoji = this.instance.getJda().getEmojiById(emojiId);

            if (emoji != null) {
                String emojiUrl = emoji.getImageUrl();
                message = message.replaceAll("<:[a-zA-Z0-9_]+:\\d{16,19}>", "<img src=\"" + emojiUrl + "\" class=\"emoji\" alt=\"" + emojiName + "\">");
            } else {
                message = message.replaceAll("<:[a-zA-Z0-9_]+:\\d{16,19}>", "");
            }
        }

        String avatarUrl = user.getAvatarUrl();
        String formattedDate = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss").withZone(ZoneId.of("Europe/Prague")).format(m.getCreatedAt().toLocalDateTime());

        return String.format("""
                <div>
                    <div class="message-header">
                        <img class="avatar" src="%s" alt="%s">
                        <div>
                            <p><i class="fas fa-id-badge"></i> <strong>User ID:</strong> %s</p>
                            <p><i class="fas fa-user"></i> <strong>Username:</strong> %s</p>
                            <p><i class="far fa-calendar"></i> <strong>Date:</strong> %s</p>
                        </div>
                    </div>
                    <div class="message-content">
                        <p>%s</p>
                    </div>
                </div>
                <hr class="message-separator">""", avatarUrl, user.getId(), user.getId(), user.getGlobalName(), formattedDate, message);
    }
}