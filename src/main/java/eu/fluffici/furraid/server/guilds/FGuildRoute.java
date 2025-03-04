/*
---------------------------------------------------------------------------------
File Name : FGuildRoute

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.furraid.server.guilds;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import eu.fluffici.bot.api.PaginationHelper;
import eu.fluffici.bot.api.beans.furraid.*;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketBuilder;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketMessageBuilder;
import eu.fluffici.bot.api.beans.furraid.ticket.TicketPatchType;
import eu.fluffici.bot.api.beans.furraid.verification.Verification;
import eu.fluffici.bot.api.beans.furraid.verification.VerificationParser;
import eu.fluffici.bot.api.furraid.InteractionType;
import eu.fluffici.bot.api.hooks.RouteMethod;
import eu.fluffici.bot.api.hooks.furraid.BlacklistBuilder;
import eu.fluffici.bot.api.hooks.furraid.WhitelistBuilder;
import eu.fluffici.bot.api.interactions.WebRoute;
import eu.fluffici.furraid.FurRaidDB;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import static eu.fluffici.bot.api.IconRegistry.*;
import static eu.fluffici.bot.api.furraid.permissive.Permissions.*;
import static eu.fluffici.furraid.server.users.FGetUserRoute.isSpammer;
import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@SuppressWarnings("All")
public class FGuildRoute extends WebRoute {

    private final Gson gson = new Gson();

    private final OkHttpClient client = new OkHttpClient();


    public FGuildRoute() {
        super("/servers", RouteMethod.POST, calculatePermissions(GUILD_MANAGEMENT));
    }

    /**
     * Abstract method called when a request is received.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     */
    @Override
    @SneakyThrows
    @SuppressWarnings("All")
    public void onRequest(HttpExchange request) {
       if (!this.preventWrongMethod(request)) {
           String[] pathParts = request.getRequestURI().getPath().split("/");
           if (pathParts.length < 3) {
               JsonObject error = new JsonObject();
               error.addProperty("status", false);
               error.addProperty("error", "Guild ID not provided");
               sendJsonResponse(request, error);
               return;
           }

           String guildId = pathParts[2];
           if (!isValidUserId(guildId)) {
               JsonObject error = new JsonObject();
               error.addProperty("status", false);
               error.addProperty("error", "Invalid Guild ID format");
               sendJsonResponse(request, error);
               return;
           }

           Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);

           if (guild == null) {
               JsonObject error = new JsonObject();
               error.addProperty("status", false);
               error.addProperty("error", "Guild not found");
               sendJsonResponse(request, error);
               return;
           }

           GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild);

           if (guildSettings == null) {
               JsonObject error = new JsonObject();
               error.addProperty("status", false);
               error.addProperty("error", "Guild settings not found");
               sendJsonResponse(request, error);
               return;
           }

           String actorResult = this.readBody(request.getRequestBody());
           RequestActor actor = this.gson.fromJson(actorResult, RequestActor.class);



           if (actor.getActorId() == null) {
               JsonObject error = new JsonObject();
               error.addProperty("status", false);
               error.addProperty("error", "Actor ID not provided");
               sendJsonResponse(request, error);
               return;
           }

           Member actorHandle = guild.getMemberById(actor.getActorId());

           if (pathParts.length > 3) {
               String subPath = pathParts[3];

               if (actor == null) {
                   JsonObject error = new JsonObject();
                   error.addProperty("status", false);
                   error.addProperty("error", "Invalid actor format");
                   sendJsonResponse(request, error);
                   return;
               } else if (actorHandle == null) {
                   JsonObject error = new JsonObject();
                   error.addProperty("status", false);
                   error.addProperty("error", "Actor not found");
                   sendJsonResponse(request, error);
                   return;
               } else if (guild == null) {
                   sendErrorResponse(request, "Guild not found");
                   return;
               } else if (guildSettings.isBlacklisted()) {
                   sendErrorResponse(request, "Guild is blacklisted");
                   return;
               }

               switch (subPath) {
                   case "fetch-members" -> this.handleFetchMembers(request, guild);
                   case "members" -> this.handleMembers(request, guildId);
                   case "channels" -> this.handleChannels(request, guildId);
                   case "roles" -> this.handleRoles(request, guildId);
                   case "member-monthly-count" -> this.handleMembersMonthlyCount(request, guildId);
                   case "active-members" -> this.handleActiveMembers(request, guildId);
                   case "new-members" -> this.handleNewMembersChart(request, guildId);
                   case "settings" -> this.handleSettings(request, guildId);
                   case "blacklist" -> this.handleBlacklist(request, guildId);
                   case "whitelist" -> this.handleWhitelist(request, guildId);
                   case "quota" -> this.handleQuota(request, guildId);
                   case "add-whitelist" -> this.handleAddWhitelist(request, guild, actor);
                   case "add-blacklist" -> this.handleAddBlacklist(request, guild, actor);
                   case "remove-whitelist" -> this.handleRemoveWhitelist(request, guild, actor);
                   case "remove-blacklist" -> this.handleRemoveBlacklist(request, guild, actor);
                   case "is-blacklisted" -> this.handleIsBlacklisted(request, guild, actor);
                   case "moderation" -> this.handleFetchModerationView(request, guild, actor);
                   case "verifications" -> this.handleFetchVerifications(request, guild, actor);
                   case "verification" -> this.handleUpdateVerification(request, guild, actor, guildSettings);
                   case "fetch-tickets" -> this.handleFetchTickets(request, guild, actor, guildSettings);
                   case "patch-ticket" -> this.handlePatchTickets(request, guild, actor, guildSettings);
                   case "ticket-send-message" -> this.handleSendMessageTickets(request, guild, actor, guildSettings);
                   case "ticket-messages" -> this.handleFetchTicketMessages(request, guild, actor, guildSettings);
                   case "transcript-ticket" -> this.handleTranscriptTicket(request, guild, actor, guildSettings);
                   case "summon-interaction"-> this.handleSummonInteraction(request, guild, actor, guildSettings);
                   default -> handleInvalidSubpath(request);
               }
           } else {
               JsonObject guildData = new JsonObject();

               if (guild != null) {
                   guildData.addProperty("status", true);
                   guildData.addProperty("isBlacklisted", guildSettings.isBlacklisted());
                   guildData.addProperty("id", guild.getId());
                   guildData.addProperty("icon", guild.getIconUrl());
                   guildData.addProperty("permissions", -1);
                   guildData.addProperty("memberCount", guild.getMemberCount());
               } else {
                   guildData.addProperty("status", false);
               }
               sendJsonResponse(request, guildData);
           }
       }
    }

    /**
     * Handles the summon interaction.
     *
     * @param request      The HttpExchange object representing the HTTP request.
     * @param guild        The Guild object representing the Discord server.
     * @param actor        The RequestActor object representing the user who initiated the interaction.
     * @param guildSettings The GuildSettings object containing the settings for the guild.
     */
    private void handleSummonInteraction(HttpExchange request, Guild guild, @NotNull RequestActor actor, GuildSettings guildSettings) {
        if (!actor.getData().has("type")) {
            sendErrorResponse(request, "Missing type field");
            return;
        }
        InteractionType interactionType = InteractionType.valueOf(actor.getData().get("type").getAsString());
        if (interactionType == null) {
            sendErrorResponse(request, "Invalid interaction type");
            return;
        }

        FurRaidDB.getInstance().getLanguageManager().loadProperties(guildSettings.getConfig().getSettings().getLanguage());

        switch (interactionType) {
            case TICKET_FORM -> {
                if (!actor.getData().has("channelId")) {
                    sendErrorResponse(request, "Missing channelId field");
                    return;
                }

                TextChannel ticketGate = guild.getTextChannelById(actor.getData().get("channelId").getAsString());
                if (ticketGate == null || !ticketGate.canTalk(guild.getSelfMember())) {
                    sendErrorResponse(request, "Invalid ticket gate channel");
                    return;
                }

                ticketGate.sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                        .simpleAuthoredEmbed()
                                .setAuthor(FurRaidDB.getInstance().getLanguageManager().get("common.new_ticket.title"), "https://frdb.fluffici.eu", ICON_FILE.getUrl())
                                .setDescription(FurRaidDB.getInstance().getLanguageManager().get("common.new_ticket.desc", "<:ic_ticket:1247537159045648424>"))
                                .setColor(Color.decode("#4CAF50"))
                                .setFooter("FurRaidDB", FurRaidDB.getInstance().getJda().getSelfUser().getAvatarUrl())
                        .build()
                ).addActionRow(
                        FurRaidDB.getInstance().getButtonManager().findByName("row:open-ticket").build(Emoji.fromCustom("ic_ticket", 1247537159045648424L, false))
                ).queue();
            }
            case VERIFICATION_FORM -> {
                FurRaidConfig.VerificationFeature verificationFeature = guildSettings.getConfig().getFeatures().getVerification();
                TextChannel gateChannel = guild.getTextChannelById(verificationFeature.getSettings().getVerificationGate());

                List<ActionRow> actionRows = new ArrayList<>();

                if (guildSettings.getConfig().getFeatures().getTicket().isEnabled()) {
                    actionRows.add(ActionRow.of(
                            FurRaidDB.getInstance().getButtonManager().findByName("row:open-ticket").build(Emoji.fromCustom("ic_ticket", 1247537159045648424L, false))
                    ));
                }

                if (guild.getRulesChannel() != null) {
                    actionRows.add(ActionRow.of(
                            FurRaidDB.getInstance().getButtonManager().findByName("row:verify").build(Emoji.fromCustom("notes", 1256008006110412841L, false)).withLabel(FurRaidDB.getInstance().getLanguageManager().get("common.verify")),
                            Button.link(guild.getRulesChannel().getJumpUrl(), FurRaidDB.getInstance().getLanguageManager().get("common.rules")).withEmoji(Emoji.fromCustom("license", 1256008260905865296L, false))
                    ));
                } else {
                    actionRows.add(ActionRow.of(FurRaidDB.getInstance().getButtonManager().findByName("row:verify").build(Emoji.fromCustom("notes", 1256008006110412841L, false)).withLabel(FurRaidDB.getInstance().getLanguageManager().get("common.verify"))));
                }

                gateChannel.sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                        .simpleAuthoredEmbed()
                        .setDescription(verificationFeature.getSettings().getDescription())
                        .setColor(Color.decode("#4CAF50"))
                        .setFooter("FurRaidDB", FurRaidDB.getInstance().getJda().getSelfUser().getAvatarUrl())
                        .build()
                ).setComponents(actionRows).queue();
            }
        }

        sendSuccessResponse(request);
    }

    /**
     * Handles the fetch members request by returning a JSON response containing
     * the ID and effective name of each member in the specified guild.
     * If the guild is null, an error response is sent with the message "Guild not found".
     *
     * @param request the HttpExchange object representing the HTTP request
     * @param guild the Guild object from which to fetch the members
     */
    private void handleFetchMembers(HttpExchange request, Guild guild) {
        if (guild != null) {
            List<Member> channels = guild.getMembers();
            JsonArray serializedMembers = new JsonArray();
            channels.forEach(member -> {
                JsonObject channelObj = new JsonObject();
                channelObj.addProperty("id", member.getId());
                channelObj.addProperty("name", member.getEffectiveName());
                serializedMembers.add(channelObj);
            });
            JsonObject response = new JsonObject();
            response.add("members", serializedMembers);
            sendJsonResponse(request, response);
        } else {
            sendErrorResponse(request, "Guild not found");
        }
    }

    /**
     * Handles fetching ticket messages for a guild.
     *
     * @param request       the HttpExchange object representing the HTTP request and response
     * @param guild         the Guild object representing the guild
     * @param actor         the RequestActor object representing the actor performing the request
     * @param guildSettings the GuildSettings object representing the settings for the guild
     */
    @SneakyThrows
    private void handleFetchTicketMessages(HttpExchange request, Guild guild, RequestActor actor, GuildSettings guildSettings) {
        if (!actor.getData().has("ticketId")) {
            sendErrorResponse(request, "Invalid request body");
            return;
        }

        String ticketId = actor.getData().get("ticketId").getAsString();
        TicketBuilder ticket = FurRaidDB.getInstance().getGameServiceManager().fetchFTicketById(ticketId);

        List<TicketMessageBuilder> messages = FurRaidDB.getInstance().getTicketManager().fetchTicketMessages(ticket.getChannelId(), ticket.getGuildId());

        sendJsonResponse(request, this.gson.fromJson(this.gson.toJson(messages), JsonArray.class));
    }

    /**
     * Handles the transcript ticket for a guild.
     *
     * @param request       the HttpExchange object representing the HTTP request and response
     * @param guild         the Guild object representing the guild
     * @param actor         the RequestActor object representing the actor performing the request
     * @param guildSettings the GuildSettings object representing the settings for the guild
     */
    @SneakyThrows
    private void handleTranscriptTicket(HttpExchange request, Guild guild, @NotNull RequestActor actor, GuildSettings guildSettings) {
        if (!actor.getData().has("ticketId")) {
            sendErrorResponse(request, "Invalid request body");
            return;
        }

        String ticketId = actor.getData().get("ticketId").getAsString();
        JsonObject response = FurRaidDB.getInstance()
                .getTicketManager()
                .transcript(ticketId, guild.getId());

        if (response == null) {
            sendErrorResponse(request, "Error while transcripting ticket");
            return;
        }

        sendJsonResponse(request, response);
    }

    /**
     * Handles sending a message on specific tickets.
     *
     * @param request       the HttpExchange object representing the HTTP request and response
     * @param guild         the Guild object representing the guild
     * @param actor         the RequestActor object representing the actor performing the request
     * @param guildSettings the GuildSettings object representing the settings for the guild
     */
    @SneakyThrows
    // For safety reasons, the webhook URL will never leave this application
    private void handleSendMessageTickets(HttpExchange request, Guild guild, @NotNull RequestActor actor, GuildSettings guildSettings) {
        if (!actor.getData().has("ticketId") || !actor.getData().has("content")) {
            sendErrorResponse(request, "Invalid request body");
            return;
        }

        String ticketId = actor.getData().get("ticketId").getAsString();
        TicketBuilder ticket = FurRaidDB.getInstance().getGameServiceManager().fetchFTicketById(ticketId);

        if (ticket == null) {
            sendErrorResponse(request, "Ticket not found");
            return;
        }

        Member actorObj = guild.getMemberById(actor.getActorId());
        if (actorObj == null) {
            sendErrorResponse(request, "Actor not found");
            return;
        }

        TextChannel channel = guild.getTextChannelById(ticket.getChannelId());

        if (channel != null) {
            List<Webhook> webhooks = channel.retrieveWebhooks().submit().get();
            if (!webhooks.isEmpty()) {
                Webhook webhook = webhooks.get(0);

                try {
                    webhook.getManager()
                            .setName(actorObj.getUser().getEffectiveName())
                            .setAvatar(Icon.from(this.downloadImageFromURL(actorObj.getUser().getAvatarUrl())))
                            .complete();
                    Message message = webhook.sendMessage(actor.getData().get("content").getAsString()).complete();

                    FurRaidDB.getInstance()
                            .getTicketManager()
                            .addTicketMessage(message, ticketId);

                    sendSuccessResponse(request);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse(request, "Rate-limited");
                    return;
                }
            } else {
                sendErrorResponse(request, "Webhook not found");
                return;
            }
        } else {
            sendErrorResponse(request, "Channel not found");
            return;
        }
    }

    /**
     * Handles the patching of specific tickets for a guild.
     *
     * @param request       the HttpExchange object representing the HTTP request and response
     * @param guild         the Guild object representing the guild to patch tickets for
     * @param actor         the RequestActor object representing the actor performing the request
     * @param guildSettings the GuildSettings object representing the settings for the guild
     */
    @SneakyThrows
    private void handlePatchTickets(@NotNull HttpExchange request, Guild guild, @NotNull RequestActor actor, GuildSettings guildSettings) {
        if (actor.getData().has("type")) {
            TicketPatchType patchType = TicketPatchType.valueOf(actor.getData().get("type").getAsString());

            if (patchType != null) {
                String ticketId = actor.getData().get("ticketId").getAsString();
                TicketBuilder ticket = FurRaidDB.getInstance().getGameServiceManager().fetchFTicketById(ticketId);

                if (ticket == null || ticket.getStatus().equalsIgnoreCase("closed")) {
                    sendErrorResponse(request, "Ticket not found or already closed");
                    return;
                }

                TextChannel ticketChannel = guild.getTextChannelById(ticket.getChannelId());
                Member actorObj = guild.getMemberById(actor.getActorId());

                switch (patchType) {
                    case CLOSE -> {
                        if (ticketChannel != null) {
                            ticketChannel.retrieveWebhooks()
                                    .submit()
                                    .get().forEach(webhook -> webhook.delete(ticket.getWebhookUrl()).reason("Ticket closing, removing webhook").queue());
                        }

                        FurRaidDB.getInstance()
                                .getTicketManager()
                                .closeTicket(ticket, actorObj.getEffectiveName() + " (Dashboard)");
                        sendSuccessResponse(request);
                    }
                    case ADD_USER -> {
                        Member member = guild.getMemberById(actor.getData().get("targetId").getAsString());

                        guild.getTextChannelById(ticket.getChannelId())
                        .upsertPermissionOverride(member)
                                .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY)
                                .queue();

                        ticketChannel.sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                                .simpleAuthoredEmbed()
                                        .setAuthor("User added to the ticket", "https://frdb.fluffici.eu", ICON_USER_PLUS.getUrl())
                                        .setDescription(
                                                """
                                                %s has been added to the ticket by %s from the [Dashboard](https://frdb.fluffici.eu/dashboard)
                                                """.formatted(member.getEffectiveName(), actorObj.getEffectiveName())
                                        )
                                .build()
                        ).queue();

                        sendSuccessResponse(request);
                    }
                    case REMOVE_USER -> {
                        Member member = guild.getMemberById(actor.getData().get("targetId").getAsString());

                        guild.getTextChannelById(ticket.getChannelId())
                                .upsertPermissionOverride(guild.getMemberById(actor.getData().get("targetId").getAsString()))
                                .deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY)
                                .queue();

                        ticketChannel.sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                                .simpleAuthoredEmbed()
                                .setAuthor("User removed from the ticket", "https://frdb.fluffici.eu", ICON_USER_PLUS.getUrl())
                                .setDescription(
                                        """
                                        %s has been removed from ticket by %s from the [Dashboard](https://frdb.fluffici.eu/dashboard)
                                        """.formatted(member.getEffectiveName(), actorObj.getEffectiveName())
                                )
                                .build()
                        ).queue();

                        sendSuccessResponse(request);
                    }
                    default -> sendErrorResponse(request, "Invalid patch type");
                }
            } else {
                sendErrorResponse(request, "Invalid patch type");
            }
        } else {
            sendErrorResponse(request, "Invalid request type");
        }
    }

    /**
     * Handles the fetching of tickets for a guild.
     *
     * @param request       the HttpExchange object representing the HTTP request and response
     * @param guild         the Guild object representing the guild to fetch tickets for
     * @param actor         the RequestActor object representing the actor performing the request
     * @param guildSettings the GuildSettings object representing the settings for the guild
     */
    private void handleFetchTickets(HttpExchange request, Guild guild, RequestActor actor, GuildSettings guildSettings) {
        int limit = Integer.parseInt(extractParameterOrDefault(request, "limit", "10"));
        int page = Integer.parseInt(extractParameterOrDefault(request, "page", "1"));

        PaginationHelper<TicketBuilder> paginationHelper = new PaginationHelper<>(FurRaidDB.getInstance().getGameServiceManager().fetchAllTickets(guild), page, limit);
        JsonObject response = paginationHelper.paginate(ticketBuilder -> this.gson.fromJson(this.gson.toJson(ticketBuilder), JsonObject.class));

        sendJsonResponse(request, response);
    }

    /**
     * Handles the quota for a guild.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guildId  the ID of the guild to handle quota for
     */
    private void handleQuota(HttpExchange request, String guildId) {
        long guild = Long.parseLong(guildId);

        OfferQuota offer = FurRaidDB.getInstance().getOfferManager().getByGuild(guild);
        int whitelistCount = FurRaidDB.getInstance().getGameServiceManager().whitelistCount(guild);
        int localBlacklistCount = FurRaidDB.getInstance().getGameServiceManager().localBlacklistCount(guild);

        boolean isReached = (localBlacklistCount >= offer.getLocalBlacklist()) || (whitelistCount >= offer.getLocalWhitelist());

        JsonObject result = new JsonObject();
        result.addProperty("isQuotaReached", isReached);
        result.addProperty("localBlacklistQuota", offer.getLocalBlacklist());
        result.addProperty("localBlacklistUsed", localBlacklistCount);
        result.addProperty("whitelistQuota", offer.getLocalWhitelist());
        result.addProperty("whitelistUsed", whitelistCount);

        sendJsonResponse(request, result);
    }

    /**
     * Handles the update verification process for a guild.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the Guild object representing the guild to update verification for
     * @param actor   the RequestActor object representing the actor performing the request
     */
    private void handleUpdateVerification(HttpExchange request, @NotNull Guild guild, @NotNull RequestActor actor, GuildSettings settings) {
        Verification verification = FurRaidDB.getInstance()
                .getGameServiceManager()
                .getVerificationRecord(guild.getId(), actor.getData().get("id").getAsInt());

        if (verification == null) {
            sendErrorResponse(request, "Verification record not found");
            return;
        }

        if (!verification.getStatus().equalsIgnoreCase("pending")) {
            sendErrorResponse(request, "Verification already processed");
            return;
        }

        FurRaidConfig.VerificationSettings verificationSettings = settings.getConfig().getFeatures().getVerification().getSettings();

        Member challenger = guild.getMemberById(verification.getUserId());
        Member actorMember = guild.getMemberById(actor.getActorId());

        switch (actor.getData().get("type").getAsString()) {
            case "GRANT" -> {
                verification.setStatus("ACCEPTED");
                verification.setVerifiedBy(actor.getActorId());

                FurRaidDB.getInstance()
                        .getGameServiceManager()
                        .updateVerificationRecord(verification);


                guild.removeRoleFromMember(challenger, guild.getRoleById(verificationSettings.getUnverifiedRole()));
                guild.addRoleToMember(challenger, guild.getRoleById(verificationSettings.getVerifiedRole()));

                guild.getTextChannelById(verificationSettings.getVerificationLoggingChannel())
                        .editMessageComponentsById(verification.getMessageId())
                        .setComponents(ActionRow.of(Button.success("button:none", "Accepted by ".concat(actorMember.getUser().getGlobalName())).asDisabled()))
                        .queue();

                sendSuccessResponse(request);
            }
            case "DENY" -> {
                verification.setStatus("DENIED");
                verification.setVerifiedBy(actor.getActorId());

                FurRaidDB.getInstance()
                        .getGameServiceManager()
                        .updateVerificationRecord(verification);

                User user = FurRaidDB.getInstance().getJda().retrieveUserById(verification.getUserId()).complete();

                PrivateChannel privateChannel = user.openPrivateChannel().complete();
                if (privateChannel.canTalk()) {
                    privateChannel.sendMessageEmbeds(FurRaidDB.getInstance().getEmbed()
                            .simpleAuthoredEmbed()
                            .setAuthor("Verification Result from " + guild.getName(), "https://frdb.fluffici.eu", ICON_CIRCLE_MINUS.getUrl())
                            .setTitle("Verification Denied")
                            .setDescription(String.format(
                                    """
                                    Your verification request was denied for the following reason:
                                    
                                    **Reason**: %s
                                    """, actor.getData().get("reason").getAsString())
                            )
                            .setTimestamp(Instant.now())
                            .setColor(Color.RED)
                            .build()
                    ).addActionRow(Button.link("https://discord.com/channels/" + guild.getId() + "/" + verificationSettings.getVerificationGate(), "Verification")).queue();
                }

                guild.getTextChannelById(verificationSettings.getVerificationLoggingChannel())
                        .editMessageComponentsById(verification.getMessageId())
                        .setComponents(ActionRow.of(Button.danger("button:none", "Denied by ".concat(actorMember.getUser().getGlobalName())).asDisabled()))
                        .queue();

                sendSuccessResponse(request);
            }
            default -> sendErrorResponse(request, "Invalid verification type");
        }
    }

    /**
     * Handles fetching member(s) verifications of a guild.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the Guild object representing the guild to fetch verifications for
     * @param actor   the RequestActor object representing the actor performing the request
     */
    private void handleFetchVerifications(HttpExchange request, @NotNull Guild guild, @NotNull RequestActor actor) {
        JDA jda = FurRaidDB.getInstance().getJda();

        List<VerificationParser> verifications = FurRaidDB.getInstance()
                .getGameServiceManager()
                .getVerificationRecords(guild.getId())
                .stream()
                .map(verification -> {
                    User user = jda.retrieveUserById(verification.getUserId()).complete();

                    boolean globally = FurRaidDB.getInstance().getBlacklistManager().isGloballyBlacklisted(user);
                    boolean locally = FurRaidDB.getInstance().getBlacklistManager().isLocallyBlacklisted(guild, user);

                    boolean isSpammer = isSpammer(user);

                    OffsetDateTime createdDate = user.getTimeCreated();
                    LocalDate dateCreated = createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Long daysPassed = ChronoUnit.DAYS.between(dateCreated, LocalDate.now(ZoneId.of("Europe/Prague")));

                    List<VerificationParser.UserInfo> userInfos = new ArrayList<>();
                    userInfos.add(new VerificationParser.UserInfo("ID", user.getId()));
                    userInfos.add(new VerificationParser.UserInfo("Username", user.getGlobalName()));
                    userInfos.add(new VerificationParser.UserInfo("Is Globally Blacklisted?", (globally ? "Yes" : "No")));
                    userInfos.add(new VerificationParser.UserInfo("Is Locally Blacklisted?", (locally ? "Yes" : "No")));
                    userInfos.add(new VerificationParser.UserInfo("Is Flagged as Spammer?", (isSpammer ? "Yes" : "No")));
                    userInfos.add(new VerificationParser.UserInfo("Account Age", NumberFormat.getNumberInstance().format(daysPassed) + " day(s) old"));

                    JsonObject verifiedBy = new JsonObject();
                    if (verification.getStatus().equalsIgnoreCase("accepted") || verification.getStatus().equalsIgnoreCase("verified") || verification.getStatus().equalsIgnoreCase("denied")) {
                        User verified = jda.retrieveUserById(verification.getVerifiedBy()).complete();

                        verifiedBy.addProperty("username", verified.getGlobalName());
                        verifiedBy.addProperty("avatarUrl", verified.getAvatarUrl());
                    }

                    return new VerificationParser(
                            verification.getId(),
                            user.getId(),
                            user.getGlobalName(),
                            user.getAvatarUrl(),
                            verification.getStatus(),
                            verification.getAnswers(),
                            userInfos,
                            verification.getCreatedAt(),
                            verifiedBy
                    );
                }).toList();

        JsonObject result = new JsonObject();
        result.addProperty("status", true);
        result.add("data", this.gson.fromJson(this.gson.toJson(verifications), JsonArray.class));

        sendJsonResponse(request, result);
    }

    /**
     * Handles the check if a user is blacklisted in a guild.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guild    the Guild object representing the guild to check for blacklist
     * @param actor    the RequestActor object representing the actor performing the request
     */
    private void handleIsBlacklisted(HttpExchange request, Guild guild, @NotNull RequestActor actor) {
        JsonObject result = new JsonObject();
        result.addProperty("status", true);
        result.addProperty("result", FurRaidDB.getInstance()
                .getBlacklistManager()
                .isLocallyBlacklisted(guild, UserSnowflake.fromId(actor.getData().get("user").getAsString()))
        );

        sendJsonResponse(request, result);
    }

    /**
     * Handles fetching the moderation view for a guild member.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the Guild object representing the guild to fetch the moderation view for
     * @param actor   the RequestActor object representing the actor performing the request
     */
    @SneakyThrows
    private void handleFetchModerationView(HttpExchange request, Guild guild, @NotNull RequestActor actor) {
        User user = FurRaidDB.getInstance().getJda().retrieveUserById(actor.getData().get("user").getAsString()).complete();

        if (user == null) {
            sendErrorResponse(request, "User not found");
            return;
        }

        List<Sanction> sanctions = FurRaidDB.getInstance()
                .getGameServiceManager()
                .getAllSanctions(guild.getId())
                .stream()
                .filter(sanction -> sanction.getUserId().equals(user.getId()))
                .toList();

        long warns = sanctions.stream().filter(sanction -> sanction.getTypeId() == Sanction.WARN).count();
        long bans = sanctions.stream().filter(sanction -> sanction.getTypeId() == Sanction.BAN).count();
        long mutes = sanctions.stream().filter(sanction -> sanction.getTypeId() == Sanction.MUTE).count();
        long kicks = sanctions.stream().filter(sanction -> sanction.getTypeId() == Sanction.KICK).count();

        ModerationViewBuilder moderationViewBuilder = new ModerationViewBuilder(user.getEffectiveName(),
                null, warns, bans, mutes, kicks
        );

        JsonObject data = this.gson.fromJson(this.gson.toJson(moderationViewBuilder), JsonObject.class);
        data.addProperty("status", true);
        data.add("sanctions", this.gson.fromJson(this.gson.toJson(sanctions), JsonArray.class));

        sendJsonResponse(request, data);
    }

    /**
     * Handles the removal of a user from the blacklist.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the Guild object representing the guild to remove the user from the blacklist
     */
    private void handleRemoveBlacklist(HttpExchange request, Guild guild, @NotNull RequestActor handle) {
        JsonObject data = handle.getData();

        if (data == null) {
            JsonObject error = new JsonObject();
            error.addProperty("status", true);
            error.addProperty("error", "Invalid data format");
            sendJsonResponse(request, error);
            return;
        } else if (!data.has("user")) {
            JsonObject error = new JsonObject();
            error.addProperty("status", true);
            error.addProperty("error", "Missing user field");
            sendJsonResponse(request, error);
            return;
        }

        FurRaidDB.getInstance().getBlacklistManager().removeLocalBlacklist(
                guild,
                UserSnowflake.fromId(data.get("user").getAsString())
        );

        sendSuccessResponse(request);
    }

    /**
     * Handles the removal of a guild ID from the whitelist.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the ID of the guild to remove from the whitelist
     */
    private void handleRemoveWhitelist(HttpExchange request, Guild guild, @NotNull RequestActor handle) {
        JsonObject data = handle.getData();

        if (data == null) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Invalid data format");
            sendJsonResponse(request, error);
            return;
        } else if (!data.has("user")) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Missing user field");
            sendJsonResponse(request, error);
            return;
        }

        FurRaidDB.getInstance().getBlacklistManager().removeWhitelist(WhitelistBuilder
                .builder()
                        .guild(guild)
                        .user(UserSnowflake.fromId(data.get("user").getAsString()))
                .build()
        );

        sendSuccessResponse(request);
    }

    /**
     * Handles adding a guild ID to the blacklist.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guild   the ID of the guild to add to the blacklist
     */
    private void handleAddBlacklist(HttpExchange request, Guild guild, @NotNull RequestActor handle) {
        JsonObject data = handle.getData();

        if (data == null) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Invalid data format");
            sendJsonResponse(request, error);
            return;
        } else if (!data.has("user")) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Missing user field");
            sendJsonResponse(request, error);
            return;
        } else if (!data.has("reason")) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Missing reason field");
            sendJsonResponse(request, error);
            return;
        }

        long count = FurRaidDB.getInstance().getGameServiceManager().localBlacklistCount(guild.getIdLong());
        OfferQuota quota = FurRaidDB.getInstance().getOfferManager().getByGuild(guild.getIdLong());
        GuildPremiumOffer premiumOffer = FurRaidDB.getInstance().getGameServiceManager().getGuildPremium(guild.getIdLong());

        JsonObject result = new JsonObject();

        if (count >= quota.getLocalBlacklist()) {
            result.addProperty("status", false);
            result.addProperty("message", "You have reached your quota, you can request more in the HQ Discord server");

            sendJsonResponse(request, result);
        } else {
            if (FurRaidDB.getInstance().getBlacklistManager().isLocallyBlacklisted(guild, UserSnowflake.fromId(data.get("user").getAsString()))) {
                JsonObject error = new JsonObject();
                error.addProperty("status", false);
                error.addProperty("error", "User is already blacklisted");
                sendJsonResponse(request, error);
                return;
            }

            FurRaidDB.getInstance().getBlacklistManager().addLocalBlacklist(BlacklistBuilder
                    .builder()
                    .guild(guild)
                    .user(UserSnowflake.fromId(data.get("user").getAsString()))
                    .author(UserSnowflake.fromId(handle.getActorId()))
                    .reason(data.get("reason").getAsString())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build()
            );

            sendSuccessResponse(request);
        }
    }

    /**
     * Handles adding a guild ID to the whitelist.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guild  the ID of the guild to add to the whitelist
     */
    private void handleAddWhitelist(@NotNull HttpExchange request, Guild guild, RequestActor handle) {
        JsonObject data = handle.getData();

        if (data == null) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Invalid data format");
            sendJsonResponse(request, error);
            return;
        } else if (!data.has("user")) {
            JsonObject error = new JsonObject();
            error.addProperty("status", false);
            error.addProperty("error", "Missing user field");
            sendJsonResponse(request, error);
            return;
        }

        long count = FurRaidDB.getInstance().getGameServiceManager().whitelistCount(guild.getIdLong());
        OfferQuota quota = FurRaidDB.getInstance().getOfferManager().getByGuild(guild.getIdLong());
        GuildPremiumOffer premiumOffer = FurRaidDB.getInstance().getGameServiceManager().getGuildPremium(guild.getIdLong());

        JsonObject result = new JsonObject();

        if (count >= quota.getLocalWhitelist()) {
            result.addProperty("status", false);
            result.addProperty("message", "You have reached your quota, you can request more in the HQ Discord server");

            sendJsonResponse(request, result);
        } else {
            WhitelistBuilder whitelistBuilder = WhitelistBuilder
                    .builder()
                    .guild(guild)
                    .user(UserSnowflake.fromId(data.get("user").getAsString()))
                    .build();

            if (FurRaidDB.getInstance().getBlacklistManager().isWhitelisted(whitelistBuilder)) {
                JsonObject error = new JsonObject();
                error.addProperty("status", false);
                error.addProperty("message", "User is already whitelisted");
                sendJsonResponse(request, error);
                return;
            }

            FurRaidDB.getInstance().getBlacklistManager().addWhitelist(whitelistBuilder);

            sendSuccessResponse(request);
        }
    }

    /**
     * Handles the whitelist for a guild.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guildId  the ID of the guild to handle whitelist for
     */
    private void handleWhitelist(HttpExchange request, String guildId) {
        int limit = Integer.parseInt(extractParameterOrDefault(request, "limit", "10"));
        int page = Integer.parseInt(extractParameterOrDefault(request, "page", "1"));
        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);

        if (guild != null) {
            JDA jda = FurRaidDB.getInstance().getJda();
            List<WhitelistFormatted> formattedList = FurRaidDB.getInstance()
                    .getGameServiceManager()
                    .fetchWhitelist(guild)
                    .stream()
                    .map(whitelist -> {
                        User user = jda.retrieveUserById(whitelist.getUser().getId()).complete();

                        return new WhitelistFormatted(
                                user.getId(),
                                user.getAvatarUrl(),
                                user.getEffectiveName(),
                                whitelist.getCreatedAt()
                        );
                    }).toList();

            PaginationHelper<WhitelistFormatted> paginationHelper = new PaginationHelper<>(formattedList, page, limit);
            JsonObject response = paginationHelper.paginate(localBlacklist -> this.gson.fromJson(this.gson.toJson(localBlacklist), JsonObject.class));

            sendJsonResponse(request, response);
        } else {
            sendErrorResponse(request, "Guild not found");
        }
    }

    /**
     * Handles the blacklist for a guild.
     *
     * @param request  the HttpExchange object representing the HTTP request and response
     * @param guildId  the ID of the guild to handle blacklist for
     */
    private void handleBlacklist(HttpExchange request, String guildId) {
        int limit = Integer.parseInt(extractParameterOrDefault(request, "limit", "10"));
        int page = Integer.parseInt(extractParameterOrDefault(request, "page", "1"));
        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);

        if (guild != null) {
            JDA jda = FurRaidDB.getInstance().getJda();
            List<LocalBlacklistFormatted> formattedList = FurRaidDB.getInstance()
                    .getGameServiceManager()
                    .fetchLocalBlacklists(guild)
                    .stream()
                    .map(localBlacklist -> {
                        User user = jda.retrieveUserById(localBlacklist.getUser().getId()).complete();
                        User issuer = jda.retrieveUserById(localBlacklist.getAuthor().getId()).complete();

                        return new LocalBlacklistFormatted(
                                user.getId(),
                                user.getAvatarUrl(),
                                user.getGlobalName(),
                                issuer.getGlobalName(),
                                localBlacklist.getReason(),
                                localBlacklist.getCreatedAt()
                        );
                    }).toList();

            PaginationHelper<LocalBlacklistFormatted> paginationHelper = new PaginationHelper<>(formattedList, page, limit);
            JsonObject response = paginationHelper.paginate(localBlacklist -> this.gson.fromJson(this.gson.toJson(localBlacklist), JsonObject.class));

            sendJsonResponse(request, response);
        } else {
            sendErrorResponse(request, "Guild not found");
        }
    }

    /**
     * Handles the settings for a guild.
     *
     * @param request the HttpExchange object representing the HTTP request and response
     * @param guildId the ID of the guild to handle settings for
     */
    private void handleSettings(HttpExchange request, String guildId) {
        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);
        GuildSettings guildSettings = FurRaidDB.getInstance().getBlacklistManager().fetchGuildSettings(guild);

        if (guildSettings != null) {
            sendJsonResponse(request, this.gson.fromJson(this.gson.toJson(guildSettings), JsonObject.class));
        } else {
            sendErrorResponse(request, "Guild settings not found");
        }
    }

    /**
     * Handles members of a guild and returns a paginated JSON response containing member information.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     * @param guildId  the ID of the guild to handle members for
     */
    @SneakyThrows
    private void handleMembers(HttpExchange exchange, String guildId) {
        int limit = Integer.parseInt(extractParameterOrDefault(exchange, "limit", "10"));
        int page = Integer.parseInt(extractParameterOrDefault(exchange, "page", "1"));

        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);
        if (guild != null) {
            List<Member> allMembers = guild.getMembers();

            PaginationHelper<Member> paginationHelper = new PaginationHelper<>(allMembers, page, limit);
            JsonObject response = paginationHelper.paginate(member -> {
                JsonObject memberObj = buildMemberJsonObject(member);

                JsonArray rolesArray = new JsonArray();
                member.getRoles().forEach(role -> {
                    JsonObject roleObj = new JsonObject();
                    roleObj.addProperty("id", role.getId());
                    roleObj.addProperty("name", role.getName());
                    roleObj.addProperty("color", role.getColorRaw());
                    roleObj.addProperty("position", role.getPosition());
                    rolesArray.add(roleObj);
                });
                memberObj.add("roles", rolesArray);

                return memberObj;
            });

            sendJsonResponse(exchange, response);
        } else {
            sendErrorResponse(exchange, "Guild not found");
        }
    }

    @NotNull
    private static JsonObject buildMemberJsonObject(Member member) {
        JsonObject memberObj = new JsonObject();
        memberObj.addProperty("id", member.getId());
        memberObj.addProperty("username", member.getUser().getName());
        memberObj.addProperty("discriminator", member.getUser().getDiscriminator());
        memberObj.addProperty("avatar", member.getUser().getEffectiveAvatarUrl());
        memberObj.addProperty("joinedAt", member.getTimeJoined().toString());
        memberObj.addProperty("isSpam", isSpammer(member.getUser()));
        memberObj.addProperty("flags", member.getUser().getFlagsRaw());
        return memberObj;
    }

    /**
     * Handles the "channels" subpath of the "/servers/{guildId}" route.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @SneakyThrows
    private void handleChannels(HttpExchange exchange, String guildId) {
        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);
        if (guild != null) {
            List<GuildChannel> channels = guild.getChannels();
            JsonArray serializedChannels = new JsonArray();
            channels.forEach(channel -> {
                JsonObject channelObj = new JsonObject();
                channelObj.addProperty("id", channel.getId());
                channelObj.addProperty("name", channel.getName());
                channelObj.addProperty("type", channel.getType().name());
                serializedChannels.add(channelObj);
            });
            JsonObject response = new JsonObject();
            response.add("channels", serializedChannels);
            sendJsonResponse(exchange, response);
        }
    }

    /**
     * Handles the "roles" subpath of the "/servers/{guildId}" route.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @SneakyThrows
    private void handleRoles(HttpExchange exchange, String guildId) {
        Guild guild = FurRaidDB.getInstance().getJda().getGuildById(guildId);
        if (guild != null) {
            List<Role> roles = guild.getRoles();
            JsonArray serializedRoles = new JsonArray();
            roles.forEach(role -> {
                JsonObject roleObj = new JsonObject();
                roleObj.addProperty("id", role.getId());
                roleObj.addProperty("name", role.getName());
                roleObj.addProperty("color", role.getColorRaw());
                roleObj.addProperty("position", role.getPosition());
                serializedRoles.add(roleObj);
            });
            JsonObject response = new JsonObject();
            response.add("roles", serializedRoles);
            sendJsonResponse(exchange, response);
        }
    }

    /**
     * Handles the monthly count of members.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @SneakyThrows
    private void handleMembersMonthlyCount(HttpExchange exchange, String guildId) {
        fetchDGuild(guildId).thenAccept(guild -> {
            if (guild != null) {
                ZonedDateTime oneMonthAgo = ZonedDateTime.now().minusMonths(1);

                guild.retrieveMembers(guild.getMembers().stream().limit(100).distinct().sorted().toList()).onSuccess(members -> {
                    List<Member> membersJoinedInLastMonth = members.stream()
                            .filter(member -> member.getTimeJoined()
                                    .isAfter(oneMonthAgo.toOffsetDateTime()))
                            .toList();

                    JsonObject data = new JsonObject();
                    data.addProperty("status", true);
                    data.addProperty("count", membersJoinedInLastMonth.size());

                    sendJsonResponse(exchange, data);
                }).onError(Throwable::printStackTrace);
            } else {
                sendErrorResponse(exchange, "Guild Not found");
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Handles the active members in the "/servers/{guildId}" route.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @SneakyThrows
    private void handleActiveMembers(HttpExchange exchange, String guildId) {
        List<Member> activeMembers = FurRaidDB.getInstance().getJda().getGuildById(guildId).getMembers()
                .stream()
                .filter(member -> member.getOnlineStatus() == OnlineStatus.ONLINE)
                .toList();

        JsonArray serializedMembers = new JsonArray();
        activeMembers.forEach(member -> {
            JsonObject memberObj = new JsonObject();
            memberObj.addProperty("id", member.getId());
            memberObj.addProperty("username", member.getUser().getName());
            memberObj.addProperty("discriminator", member.getUser().getDiscriminator());
            memberObj.addProperty("avatar", member.getUser().getEffectiveAvatarUrl());
            memberObj.addProperty("joinedAt", member.getTimeJoined().toString());

            JsonArray rolesArray = new JsonArray();
            member.getRoles().forEach(role -> {
                JsonObject roleObj = new JsonObject();
                roleObj.addProperty("id", role.getId());
                roleObj.addProperty("name", role.getName());
                roleObj.addProperty("color", role.getColorRaw());
                roleObj.addProperty("position", role.getPosition());
                rolesArray.add(roleObj);
            });
            memberObj.add("roles", rolesArray);

            memberObj.addProperty("isSpam", (member.getFlagsRaw() & (1 << 20)) != 0);
            memberObj.addProperty("flags", member.getFlagsRaw());
            serializedMembers.add(memberObj);
        });

        JsonObject response = new JsonObject();
        response.add("activeMembers", serializedMembers);
        response.addProperty("activeMembersCount", serializedMembers.size());

        sendJsonResponse(exchange, response);
    }

    /**
     * Handles the new members chart.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @SneakyThrows
    private void handleNewMembersChart(HttpExchange exchange, String guildId) {
        try {
            JDA jda = FurRaidDB.getInstance().getJda();

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                sendErrorResponse(exchange,"Guild not found");
                return;
            }

            List<LocalDate> lastSixMonths = getLastSixMonths();
            guild.loadMembers().onSuccess(members -> {
                List<Integer> memberCounts = lastSixMonths.stream().map(month ->
                        (int) members.stream().filter(member ->
                                member.getTimeJoined().toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate()
                                        .getMonthValue() == month.getMonthValue() &&
                                        member.getTimeJoined().toInstant().atZone(ZoneId.of("Europe/Prague")).toLocalDate()
                                                .getYear() == month.getYear()
                        ).count()
                ).collect(Collectors.toList());

                List<String> labels = lastSixMonths.stream()
                        .map(month -> month.getMonthValue() + "-" + month.getYear())
                        .collect(Collectors.toList());

                NewMembersChart chart = new NewMembersChart(
                        labels,
                        listOf(
                                new Datasets("New Users (" + LocalDate.now().getYear() + ")", memberCounts)
                        )
                );

                JsonObject jsonResponse = gson.fromJson(gson.toJson(chart), JsonObject.class);
                sendJsonResponse(exchange, jsonResponse);
            }).onError(error -> sendErrorResponse(exchange, "Error loading members"));
        } catch (Exception e) {
            sendErrorResponse(exchange,"Server error: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void handleInvalidSubpath(HttpExchange exchange) {
        JsonObject error = new JsonObject();
        error.addProperty("status", false);
        error.addProperty("error", "Invalid subpath");
        sendJsonResponse(exchange, error);
    }

    protected static void sendErrorResponse(HttpExchange exchange, String error) {
        JsonObject response = new JsonObject();
        response.addProperty("status", true);
        response.addProperty("error", error);
        sendJsonResponse(exchange, response);
    }

    private String extractParameterOrDefault(HttpExchange exchange, String param, String defaultValue) {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);
        return queryParams.getOrDefault(param, defaultValue);
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) return new HashMap<>();
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private CompletionStage<Guild> fetchDGuild(String id) {
        JDA jda = FurRaidDB.getInstance().getJda();

        return jda.getGuildById(id) != null
                ? CompletableFuture.completedStage(jda.getGuildById(id))
                : CompletableFuture.failedStage(new IllegalStateException("Guild not found"));
    }

    private List<LocalDate> getLastSixMonths() {
        List<LocalDate> months = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            months.add(currentDate.minusMonths(i));
        }
        return months;
    }

    static class NewMembersChart {
        List<String> labels;
        List<Datasets> datasets;

        NewMembersChart(List<String> labels, List<Datasets> datasets) {
            this.labels = labels;
            this.datasets = datasets;
        }
    }

    static class Datasets {
        String label;
        List<Integer> data;
        String backgroundColor = "rgba(75, 192, 192, 0.6)";

        Datasets(String label, List<Integer> data) {
            this.label = label;
            this.data = data;
        }
    }

    /**
     * Downloads an image from the specified URL and returns it as an InputStream.
     *
     * @param imageUrl the URL of the image to download
     * @return an InputStream representing the downloaded image
     * @throws java.io.IOException if an I/O error occurs while downloading the image
     */
    @SneakyThrows
    public InputStream downloadImageFromURL(String imageUrl) {
        URL url = new URL(imageUrl);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }
}