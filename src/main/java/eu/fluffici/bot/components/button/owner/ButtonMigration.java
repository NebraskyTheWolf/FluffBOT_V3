package eu.fluffici.bot.components.button.owner;

/*
---------------------------------------------------------------------------------
File Name : ButtonMigration.java

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


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.fluffici.bot.FluffBOT;
import eu.fluffici.bot.api.ProfileMigration;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.button.PersonalButton;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ButtonMigration extends PersonalButton  {
    public ButtonMigration() {
        super("migrate:guild", "Migrate", ButtonStyle.PRIMARY);
    }

    private final List<String> propertyKeys = Arrays.asList(
            "channel.moderation", "channel.logging", "channel.staff",
            "channel.welcome", "channel.goodbye", "channel.commands",
            "channel.achievements", "channel.birthday", "channel.level",
            "channel.stats.messages", "channel.stats.members"
    );

    private final List<String> defaultPropertyKeys = Arrays.asList(
            "token", "debug", "api.fluffici.token",
            "mysqlHost", "mysqlDatabase", "mysqlUsername", "mysqlPassword",
            "main.guild", "role.birthday", "role.boost", "role.artist",
            "category.rooms", "command.gamble.matrix.x", "command.gamble.matrix.z",
            "command.gamble.matrix.gap"
    );

    Map<String, String> properties = new HashMap<>();
    private final Map<String, String> channelProperties = new HashMap<>();

    private final OkHttpClient client = new OkHttpClient();

    @Override
    @SneakyThrows
    public void handle(ButtonInteraction interaction) {
        EmbedBuilder embedBuilder = this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor("Synchronising the member list", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1243315178859135056/circle-1.png")
                .setDescription("Please wait, the members is getting synchronised...");

        interaction.deferEdit().queue();

       Message message = interaction.getChannel().sendMessageEmbeds(embedBuilder.build()).complete();

        AtomicInteger sMembers = new AtomicInteger(0);
        AtomicInteger errored = new AtomicInteger(0);

        Objects.requireNonNull(interaction.getGuild()).getMembers().forEach(member -> {
            if (!member.getUser().isBot()) {
                CompletableFuture.runAsync(() -> {
                    PlayerBean handler = this.getUserManager().fetchUser(member);

                    Request request = new Request.Builder()
                            .url("https://api.fluffici.eu/api/discord/profile/".concat(member.getId()))
                            .header("Authorization", "Bearer ".concat(this.getInstance().getDefaultConfig().getProperty("api.fluffici.token")))
                            .get()
                            .build();

                    ProfileMigration profileMigration;
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            JsonObject data = new Gson().fromJson(response.body().string(), JsonObject.class);

                            if (data.has("status") && data.get("status").getAsBoolean()) {
                                profileMigration = new Gson().fromJson(data.toString(), ProfileMigration.class);
                            } else {
                                profileMigration = null;
                            }
                        } else {
                            errored.incrementAndGet();
                            return;
                        }
                    } catch (Exception e) {
                        errored.incrementAndGet();
                        return;
                    }

                    if (handler == null) {
                        this.getUserManager().createPlayer(new PlayerBean(
                                member.getId(),
                                null,
                                null,
                                false,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                null,
                                100,
                                100,
                                100,
                                100,
                                15,
                                "",
                                0
                        ));

                        handler = this.getInstance().getUserManager().fetchUser(member);

                        this.getInstance().getUserManager().addTokens(handler, 200);
                        this.getInstance().getUserManager().addKarma(handler, 2);

                        if (profileMigration != null) {
                            this.getInstance().getUserManager().addUpvote(handler, profileMigration.getUpvote());

                            handler.setMigrationId(profileMigration.get_id());
                            handler.setLegacyMessageCount(profileMigration.getMessages());
                            handler.setSeed(new Random().nextInt(-5000000, 5000000));

                            this.getUserManager().saveUser(handler);
                        } else {
                            this.getInstance().getUserManager().addUpvote(handler, 2);
                        }

                        this.getInstance().getUserManager().addItem(member, "nickname-coin", 1);
                        this.getInstance().getAchievementManager().unlock(member, 33);

                        if (member.isBoosting()) {
                            this.getInstance().getAchievementManager().unlock(member, 34);
                        }

                        sMembers.incrementAndGet();
                    }
                }).whenComplete((unused, throwable) -> this.getInstance().getLogger().debug("User created: " + member.getId() + " index #".concat(String.valueOf(sMembers.get()))));
            }
        });

        if (errored.get() > 0) {
            message.editMessageEmbeds(embedBuilder
                    .setAuthor("Error while migrating users profile.", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1243315179136090232/circle-2.png")
                    .setDescription("Cannot contact the API /discord/profile/{user}.")
                    .build()
            ).queue();
            return;
        }

        message.editMessageEmbeds(embedBuilder
                .setAuthor("Checking channel check-list", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1243315179136090232/circle-2.png")
                .setDescription("I'm currently checking the channels, please wait...")
                .build()
        ).queue();

        List<String> missingChannel = new ArrayList<>();

        boolean isAnyNull = false;
        for (String key : propertyKeys) {
            String value = FluffBOT.getInstance().getDefaultConfig().getProperty(key);
            channelProperties.put(key, value);
            if (value == null) {
                missingChannel.add(key);
                isAnyNull = true;
            } else {
                TextChannel channel = interaction.getJDA().getTextChannelById(value);
                VoiceChannel voiceChannel = interaction.getJDA().getVoiceChannelById(value);
                if (channel == null && voiceChannel == null) {
                    missingChannel.add(value);
                    isAnyNull = true;
                }
            }
        }

        if (isAnyNull) {
            message.editMessageEmbeds(embedBuilder
                    .setAuthor("Checking channel check-list (failed)", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1226168517124952145/alert-triangle.png")
                    .setDescription("Missing channels: " + String.join(", ", missingChannel))
                    .build()
            ).queue();
            return;
        }

        message.editMessageEmbeds(embedBuilder
                .setAuthor("Validating configuration", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1243315179366907984/circle-3.png")
                .setDescription("I'm currently checking the configuration, please wait...")
                .build()
        ).queue();

        List<String> missingKey = new ArrayList<>();

        for (String key : defaultPropertyKeys) {
            String value = FluffBOT.getInstance().getDefaultConfig().getProperty(key);
            properties.put(key, value);
            if (value == null) {
                missingKey.add(key);
                isAnyNull = true;
            }
        }

        if (isAnyNull) {
            message.editMessageEmbeds(embedBuilder
                    .setAuthor("Configuration validation (failed)", "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1226168517124952145/alert-triangle.png")
                    .setDescription("Missing configuration key(s): " + String.join(", ", missingKey))
                    .build()
            ).queue();
            return;
        }

        List<String> handled = this.channelProperties.values().stream()
                                .map(channel -> "<#".concat(channel).concat(">"))
                                .toList();

        embedBuilder.setAuthor(this.getLanguageManager().get("button.migration.title"), "https://fluffici.eu", "https://cdn.discordapp.com/attachments/1224419443300372592/1234597920905625630/artboard.png")
                .setDescription(this.getLanguageManager().get("button.migration.description"))
                .addField("Channels", String.join("\n", handled), true)
                .addField("Members synchronised", String.valueOf(sMembers.get()), true);

        message.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
