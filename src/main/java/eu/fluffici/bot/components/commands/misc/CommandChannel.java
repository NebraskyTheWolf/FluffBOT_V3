package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandChannel.java

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
import eu.fluffici.bot.api.MessageUtil;
import eu.fluffici.bot.api.beans.players.ChannelBean;
import eu.fluffici.bot.api.beans.players.ChannelOption;
import eu.fluffici.bot.api.beans.players.ChannelRent;
import eu.fluffici.bot.api.beans.players.DummyChannel;
import eu.fluffici.bot.api.beans.shop.ItemDescriptionBean;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import eu.fluffici.bot.components.button.shop.PurchaseHandler;
import eu.fluffici.bot.components.button.shop.impl.OperationType;
import eu.fluffici.bot.components.button.shop.impl.Purchase;
import eu.fluffici.bot.components.button.shop.impl.PurchaseCallback;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

import static eu.fluffici.bot.api.IconRegistry.*;
import static eu.fluffici.bot.components.commands.misc.CommandNickname.*;

@CommandHandle
@SuppressWarnings("ALL")
public class CommandChannel extends Command {
    private static Map<String, String> interactionMap = new HashMap<>();
    private final int INITIAL_PRICE = 300;
    private final int INITIAL_FEE = 150;

    public CommandChannel() {
        super("channel", "Manage your personal channel", CommandCategory.MISC);

        List<SubcommandData> management = new ArrayList<>();
        management.add(new SubcommandData("create", "Create your personal channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "Channel name")
                                .setRequired(true)
                )
        );

        management.add(new SubcommandData("add", "Allow someone to join your channel")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Select a member")
                                .setRequired(true)
                )
        );

        management.add(new SubcommandData("remove", "Remove your channel permanently"));
        management.add(new SubcommandData("info", "Get information about your channel."));
        management.add(new SubcommandData("renew", "Renew your rent for this month."));

        this.getSubcommandData().addAll(management);
        this.getOptions().put("noSelfUser", true);
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String command = interaction.getSubcommandName();

        User currentUser = interaction.getUser();

        switch (Objects.requireNonNull(command)) {
            case "create" -> this.handleCreateChannel(interaction, currentUser);
            case "add" -> this.handleAddUser(interaction, currentUser);
            case "remove" -> this.handleRemove(interaction, currentUser);
            case "info" -> this.handleInfo(interaction, currentUser);
            case "renew" -> this.handleRenew(interaction, currentUser);
        }
    }

    /**
     * Handles the renew command.
     *
     * @param interaction The command interaction.
     * @param currentUser The current user.
     */
    @SneakyThrows
    private void handleRenew(CommandInteraction interaction, User currentUser) {
        PlayerBean player = this.getUserManager().fetchUser(currentUser);
        ChannelBean channel = this.getUserManager().getChannel(currentUser);

        System.out.println("Running renew command...");

        if (channel != null) {
            DummyChannel dummyChannel = FluffBOT.getInstance().getGameServiceManager().getChannelsExpiringSoon(channel.getChannelId());
            if (dummyChannel != null) {
                ItemDescriptionBean channelRenew = FluffBOT.getInstance().getGameServiceManager().fetchInternalItem("channel_subscription");
                PurchaseHandler.handlePurchase(interaction, OperationType.PURCHASE, channelRenew, 1, new PurchaseCallback() {
                    @Override
                    public void cancelled(ButtonInteraction interaction) {
                        interaction.replyEmbeds(buildError(getLanguageManager().get("owoce"))).setEphemeral(true).queue();
                    }

                    @Override
                    public void execute(ButtonInteraction interaction, Purchase purchase) {
                        if (getUserManager().hasEnoughTokens(player, channelRenew.getPriceTokens())) {
                            getUserManager().removeTokens(player, channelRenew.getPriceTokens());

                            FluffBOT.getInstance().getGameServiceManager().addChannelRent(new ChannelRent(
                                    currentUser.getId(),
                                    channel.getChannelId(),
                                    GameId.generateId(),
                                    new Timestamp(System.currentTimeMillis())
                            ));

                            LocalDateTime latestRentAt = dummyChannel.getLatestRentAt().toLocalDateTime();
                            LocalDateTime deletionNextDueDate = latestRentAt.plusMonths(2);
                            Instant nextDueInstant = deletionNextDueDate.atZone(ZoneId.of("Europe/Prague")).toInstant();
                            long nextDueEpochSeconds = nextDueInstant.getEpochSecond();
                            String discordFormattedNextDueDate = "<t:" + nextDueEpochSeconds + ":F>";

                            interaction.replyEmbeds(getEmbed()
                                    .simpleAuthoredEmbed()
                                    .setAuthor(getLanguageManager().get("channel.auto.renewal.success.title"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED)
                                    .setDescription(getLanguageManager().get("channel.auto.renewal.success.description", interaction.getJDA().getVoiceChannelById(dummyChannel.getChannelId()).getName(), discordFormattedNextDueDate))
                                    .setFooter(getLanguageManager().get("channel.auto.renewal.success.footer"))
                                    .setColor(Color.GREEN)
                                    .build()
                            ).setEphemeral(true).queue();
                        } else {
                            interaction.replyEmbeds(buildError(getLanguageManager().get("command.channel.not_enough_tokens"))).setEphemeral(true).queue();
                        }
                    }

                    @Override
                    public void error(String message) {
                        interaction.replyEmbeds(buildError(message)).setEphemeral(true).queue();
                    }
                });
            } else {
                interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.channel.renew.not_yet"))).queue();
            }
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.not_owned"))).queue();
        }
    }

    /**
     * Handles the information command.
     *
     * @param interaction The command interaction.
     * @param currentUser The current user.
     */
    private void handleInfo(CommandInteraction interaction, User currentUser) {
        ChannelBean channel = this.getUserManager().getChannel(currentUser);

        if (channel != null) {
            VoiceChannel voiceChannel = interaction.getJDA().getVoiceChannelById(channel.getChannelId());

            List<ChannelRent> channelRents = channel.getChannelRents();
            ChannelOption channelOption = channel.getChannelOption();

            EmbedBuilder embedBuilder = this.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(voiceChannel != null ? voiceChannel.getName() : "Neznámý", "https://fluffici.eu", ICON_FILE)
                    .setFooter("Pro více informací nás kontaktuj přes tickety.")
                    .setTimestamp(Instant.now());

            StringBuilder descriptionBuilder = new StringBuilder();
            descriptionBuilder.append("Zde jsou některé informace o tvých pronájmech kanálů:\n");
            descriptionBuilder.append("**Zaplacený nájem:**\n\n");

            channelRents.sort(Comparator.comparing(ChannelRent::getPaidAt).reversed());

            int rentCount = Math.min(12, channelRents.size());
            for (int i = 0; i < rentCount; i++) {
                ChannelRent rent = channelRents.get(i);
                descriptionBuilder.append("- ID Transakce: **").append(rent.getTransactionId()).append("**")
                        .append("\n - Zaplatil: **").append(UserSnowflake.fromId(rent.getPayerId()).getAsMention()).append("**")
                        .append("\n - Datum platby: **").append(convertDateToDiscord(rent.getPaidAt())).append("**").append("\n\n");
            }

            embedBuilder.setDescription(descriptionBuilder.toString());

            // Add information about channel options
            embedBuilder.addField("**Automatické obnovení**", channelOption.isAutoRenew() ? "Ano" : "Ne", true);
            embedBuilder.addField("**Cena předplatného**", "300 <:flufftoken:820777573046812693> /Měsíc", true);
            embedBuilder.addField("**Datum vytvoření kanálu**", convertDateToDiscord(channel.getCreatedAt()), true);

            interaction.replyEmbeds(embedBuilder.build()).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.not_owned"))).queue();
        }
    }

    private String convertDateToDiscord(Timestamp date) {
        LocalDateTime createdAt = date.toLocalDateTime();
        Instant createdAtInstant = createdAt.atZone(ZoneId.of("Europe/Prague")).toInstant();
        long createdAtEpochSeconds = createdAtInstant.getEpochSecond();
        return "<t:" + createdAtEpochSeconds + ":F>";
    }

    /**
     * Handles the creation of a voice channel.
     *
     * @param interaction The command interaction.
     * @param currentUser The current user.
     */
    private void handleCreateChannel(@NotNull CommandInteraction interaction, User currentUser) {
        ConfirmChannelPurchase confirmChannelPurchase = new ConfirmChannelPurchase(FluffBOT.getInstance(), interaction, currentUser);
        interaction.getJDA().addEventListener(confirmChannelPurchase);

        int initialPrice = this.INITIAL_PRICE;
        int initialFee = this.INITIAL_FEE;
        int totalInitialCost = initialPrice + initialFee;
        int monthlyCost = initialPrice;

        String description = String.format("""
                Chystáš se zakoupit nový kanál.
                Počáteční nájem činí **%d** flufftokenů (s přidaným poplatkem za založení: **%d** flufftokenů).
                Celkové počáteční náklady jsou **%d** flufftokenů.
                                                                        
                Po prvním měsíci bude měsíční nájem **%d** flufftokenů.
                """, initialPrice, initialFee, totalInitialCost, monthlyCost);

        interaction.replyEmbeds(
                this.getEmbed()
                        .simpleAuthoredEmbed()
                        .setAuthor(this.getLanguageManager().get("command.channel.create.confirm"), "https://fluffici.eu", ICON_QUESTION_MARK)
                        .setDescription(description)
                        .setFooter(this.getLanguageManager().get("command.channel.create.footer"))
                        .build()
        ).addActionRow(
                Button.success("button:confirm_".concat(interaction.getUser().getId()), this.getLanguageManager().get("common.pay", totalInitialCost)),
                Button.danger("button:cancel_".concat(interaction.getUser().getId()), this.getLanguageManager().get("common.cancel")),
                Button.success("button:enable_renewal_".concat(interaction.getUser().getId()),  this.getLanguageManager().get("common.enable_auto_renew"))
        ).setEphemeral(true).queue();

        interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.channel.purchase.follow_instructions"))).setEphemeral(true).queue();
    }

    /**
     * Handles the addition of a user to a voice channel.
     *
     * @param interaction The command interaction.
     * @param currentUser The current user.
     */
    private void handleAddUser(CommandInteraction interaction, User currentUser) {
        if (this.getUserManager().hasChannel(currentUser)) {
            User targetUser = interaction.getOption("user").getAsUser();
            Member targetMember = interaction.getGuild().getMemberById(targetUser.getId());

            ChannelBean currentChannel = this.getUserManager().getChannel(currentUser);
            VoiceChannel channel = interaction.getGuild().getVoiceChannelById(currentChannel.getChannelId());

            if (channel == null || targetMember == null) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.channel_not_found"))).queue();
                return;
            }

            channel.upsertPermissionOverride(targetMember)
                    .grant(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_STREAM)
                    .queue();

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.channel.user_added", targetMember.getAsMention(), channel.getName()))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.not_owned"))).queue();
        }
    }

    /**
     * Handles the removal of a channel.
     *
     * @param interaction The command interaction.
     * @param currentUser The current user.
     */
    private void handleRemove(CommandInteraction interaction, User currentUser) {
        if (this.getUserManager().hasChannel(currentUser)) {
            ChannelBean currentChannel = this.getUserManager().getChannel(currentUser);
            VoiceChannel channel = interaction.getGuild().getVoiceChannelById(currentChannel.getChannelId());

            if (channel == null) {
                interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.channel_not_found"))).queue();
                return;
            }

            channel.delete().queue();
            this.getUserManager().deleteChannel(currentUser);

            interaction.replyEmbeds(this.buildSuccess(this.getLanguageManager().get("command.channel.deleted", channel.getName()))).queue();
        } else {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.channel.not_owned"))).queue();
        }
    }

    /**
     * The ConfirmChannelPurchase class is a private static class that extends ListenerAdapter and handles the confirmation of a channel purchase.
     */
    private static class ConfirmChannelPurchase extends ListenerAdapter {
        private FluffBOT instance;
        private CommandInteraction interaction;
        private User currentUser;

        private boolean withAutoRenewal = false;

        public ConfirmChannelPurchase(FluffBOT instance, CommandInteraction interaction, User currentUser) {
            this.instance = instance;
            this.interaction = interaction;
            this.currentUser = currentUser;
        }

        /**
         * Handles button interactions.
         *
         * @param event The button interaction event.
         */
        @Override
        public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
            if (!event.getUser().getId().equals(interaction.getUser().getId())) {
                return;
            }

            String buttonid = event.getButton().getId();

            if (buttonid.equals("button:confirm_".concat(interaction.getUser().getId()))) {
                this.handleCreateChannel(event.getInteraction(), currentUser);
            } else if (buttonid.equals("button:cancel_".concat(interaction.getUser().getId()))) {
                event.getInteraction().replyEmbeds(this.buildError(instance.getLanguageManager().get("command.channel.creation_cancelled"))).setEphemeral(true).queue();
            } else if (buttonid.equals("button:enable_renewal_".concat(interaction.getUser().getId()))) {
                withAutoRenewal = true;
                this.editMessage(event.getMessage(), interaction.getUser().getId());
                event.getInteraction().replyEmbeds(this.buildSuccess(instance.getLanguageManager().get("command.channel.auto_renewal.enabled"))).setEphemeral(true).queue();
                return;
            } else if (buttonid.equals("button:disable_renewal_".concat(interaction.getUser().getId()))) {
                withAutoRenewal = false;
                this.editMessage(event.getMessage(), interaction.getUser().getId());
                event.getInteraction().replyEmbeds(this.buildSuccess(instance.getLanguageManager().get("command.channel.auto_renewal.disabled"))).setEphemeral(true).queue();
                return;
            }

            MessageUtil.updateInteraction(event.getMessage());
            event.getJDA().removeEventListener(this);
        }

        private void editMessage(@NotNull Message message, String id) {
            List<ActionRow> modifiedComponents = new ArrayList<>();
            List<ActionRow> actionRows = message.getActionRows();

            List<Button> modifiedButtons = new ArrayList<>();

            for (ActionRow actionRow : actionRows) {
                List<Button> buttons = actionRow.getButtons();

                for (Button button : buttons) {
                    if (button.getId().equals("button:enable_renewal_".concat(id))) {
                        Button modifiedButton = button.withLabel(instance.getLanguageManager().get("common.disable_auto_renew")).withId("button:disable_renewal_".concat(id)).withStyle(ButtonStyle.DANGER);
                        modifiedButtons.add(modifiedButton);
                    } else if (button.getId().equals("button:disable_renewal_".concat(id))) {
                        Button modifiedButton = button.withLabel(instance.getLanguageManager().get("common.enable_auto_renew")).withId("button:enable_renewal_".concat(id)).withStyle(ButtonStyle.SUCCESS);
                        modifiedButtons.add(modifiedButton);
                    }  else {
                        modifiedButtons.add(button);
                    }
                }

                ActionRow modifiedActionRow = ActionRow.of(modifiedButtons);
                modifiedButtons.clear();
                modifiedComponents.add(modifiedActionRow);
            }

            message.editMessageComponents(modifiedComponents).queue();
        }

        /**
         * Handles the creation of a voice channel.
         *
         * @param interaction The command interaction.
         * @param currentUser The current user.
         */
        private void handleCreateChannel(@NotNull ButtonInteraction interaction, User currentUser) {
            Category roomCategory = interaction.getGuild().getCategoryById(FluffBOT.getInstance().getDefaultConfig().getProperty("category.rooms"));
            if (roomCategory == null) {
                interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.category_not_found"))).queue();
                return;
            }

            if (!this.instance.getUserManager().hasChannel(currentUser)) {
                String channelName = this.interaction.getOption("name").getAsString();
                PlayerBean currentPlayer = this.instance.getUserManager().fetchUser(currentUser);

                if (!this.instance.getUserManager().hasEnoughTokens(currentPlayer, 450)) {
                    interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.not_enough_tokens"))).queue();
                } else if (channelName.length() > 16) {
                    interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.name_too_long"))).queue();
                } else if (containsUrl(channelName)) {
                    interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.name_contains_url"))).queue();
                } else if (validateIp(channelName)) {
                    interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.name_contains_ip"))).queue();
                } else if (containsSpecialCharacters(channelName)) {
                    interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.name_contains_schars"))).queue();
                } else {
                    VoiceChannel channel = interaction.getGuild().createVoiceChannel(channelName, roomCategory)
                            .addMemberPermissionOverride(currentUser.getIdLong(), 1, 0)
                            .addMemberPermissionOverride(interaction.getGuild().getSelfMember().getIdLong(), 1, 0)
                            .addRolePermissionOverride(606542137819136020L, 0, 1)
                            .addRolePermissionOverride(606542004708573219L, 0, 1)
                            .complete();

                    this.instance.getUserManager().createChannel(new ChannelBean(
                            currentUser.getId(),
                            channel.getId(),
                            GameId.generateId(),
                            new Timestamp(Instant.now().toEpochMilli()),
                            Collections.emptyList(),
                            new ChannelOption(this.withAutoRenewal),
                            false
                    ));

                    this.instance.getUserManager().removeTokens(currentPlayer,  450);

                    interaction.replyEmbeds(this.buildSuccess(this.instance.getLanguageManager().get("command.channel.created"))).setEphemeral(true).queue();
                }
            } else {
                interaction.replyEmbeds(this.buildError(this.instance.getLanguageManager().get("command.channel.already_owned"))).setEphemeral(true).queue();
            }
        }

        @NotNull
        public MessageEmbed buildError(String description) {
            return this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.error"), "https://fluffici.eu", ICON_ALERT_CIRCLE)
                    .setDescription(description)
                    .setTimestamp(Instant.now())
                    .setFooter(this.instance.getLanguageManager().get("common.error.footer"), ICON_QUESTION_MARK)
                    .build();
        }

        @NotNull
        public MessageEmbed buildSuccess(String description) {
            return this.instance.getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(this.instance.getLanguageManager().get("common.success"), "https://fluffici.eu", ICON_CLIPBOARD_CHECKED)
                    .setDescription(description)
                    .setTimestamp(Instant.now())
                    .build();
        }
    }
}
