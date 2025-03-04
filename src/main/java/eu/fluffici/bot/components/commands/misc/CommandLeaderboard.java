package eu.fluffici.bot.components.commands.misc;

/*
---------------------------------------------------------------------------------
File Name : CommandLeaderboard.java

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
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.game.GraphicsAPI;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.beans.players.LeaderboardBuilder;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.fluffici.bot.api.game.GraphicsAPI.formatNumberHumanReadable;
import static eu.fluffici.bot.api.game.GraphicsAPI.makeRoundedCorner;
import static eu.fluffici.bot.api.IconRegistry.ICON_SORT;

@CommandHandle
@SuppressWarnings("All")
public class CommandLeaderboard extends Command {

    private Font customFont = null;
    private BufferedImage baseImage = null;

    public CommandLeaderboard() {
        super("leaderboard", "Seeking the leaderboard of the server.", CommandCategory.MISC);

        this.getOptionData().add(new OptionData(OptionType.STRING, "leaderboard", "Select the leaderboard to display.", true)
                .addChoice("Levely", "level")
                .addChoice("Zpravy", "message")
                .addChoice("Smeny", "shift")
                .addChoice("Tokeny", "token")
                .addChoice("Coins", "coin")
                .addChoice("Karma", "karma")
                .addChoice("Upvoty", "upvote")
                .addChoice("Eventy", "event")
        );

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);

        InputStream is = FluffBOT.getInstance().getClass().getResourceAsStream("/fonts/lexend.ttf");
        try {
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(CommandInteraction interaction) {
        String option = interaction.getOption("leaderboard").getAsString();

        CompletableFuture<List<LeaderboardBuilder>> topUsersFuture;

        switch (option) {
            case "level" -> {
                topUsersFuture = this.getTopUsersByLevelAsync(10);
            }
            case "message" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "messages");
            }
            case "shift" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "shifts");
            }
            case "token" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "tokens");
            }
            case "coin" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "coins");
            }
            case "karma" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "karmas");
            }
            case "upvote" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "upvotes");
            }
            case "event" -> {
                topUsersFuture = this.getTopUsersByStatsAsync(10, "events");
            }
            default -> {
                interaction.reply("Invalid leaderboard option").queue();
                return;
            }
        }

        try {
            this.baseImage = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/leaderboard/leaderboard_default.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        topUsersFuture.thenAccept(topUsers -> generateImage(interaction, topUsers, option))
                .exceptionally(ex -> {
                    interaction.reply("Failed to retrieve leaderboard: " + ex.getMessage()).queue();
                    return null;
                });
    }


    @SneakyThrows
    private void generateImage(CommandInteraction interaction, List<LeaderboardBuilder> topUsers, String option) {
        EmbedBuilder builder = this.getEmbed()
                .simpleAuthoredEmbed()
                .setAuthor(this.getLanguageManager().get("common.leaderboard"), "https://fluffici.eu", ICON_SORT.getUrl())
                .setDescription(this.getLanguageManager().get("common.leaderboard.desc"));

        CompletableFuture.runAsync(() -> {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(this.customFont);

                Font deriveFont = this.customFont.deriveFont(Font.BOLD, 24);

                BufferedImage deriveImage = this.baseImage;

                Graphics2D g2d = deriveImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(deriveFont);

                // Set initial positions for drawing user information
                int initialX = 80; // X position for profile picture
                int usernameX = 275; // X position for username
                int levelX = 180; // X position for level
                int expY = 155; // Y position for experience
                int rankY = 175; // Y position for rank
                int minDeltaY = 105; // Minimum Y difference to create spacing between player cards
                int deltaY = 108; // Y difference between two positions

                for (int i = 0; i < topUsers.size(); i++) {
                    LeaderboardBuilder user = topUsers.get(i);

                    User currentUser = FluffBOT.getInstance().getJda().getUserById(user.getUserId());
                    if (currentUser == null || currentUser.isBot() || currentUser.isSystem())
                        continue;

                    PlayerBean currentPlayer = getUserManager().fetchUserAsync(currentUser); // Blocking here is okay as we're already in an async context

                    BufferedImage avatar = ImageIO.read(new URL(currentUser.getAvatarUrl()));
                    BufferedImage roundedAvatar = makeRoundedCorner(avatar, 64);

                    int spacing = i == 0 ? 0 : minDeltaY;
                    deltaY += spacing - 2;

                    g2d.drawImage(roundedAvatar, initialX, (i + 1 * deltaY) + 5, 84, 84, null);

                    GraphicsAPI.drawStringWithMaxWidth(g2d, deriveFont.deriveFont(Font.BOLD, 24), currentUser.getEffectiveName(), 24f, 64, usernameX, (i + 1 * deltaY) + 25);

                    g2d.setFont(deriveFont.deriveFont(Font.PLAIN, 16));

                    switch (option) {
                        case "level" -> {
                            g2d.drawString(getLanguageManager().get("common.level", user.getScore()), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.exp", formatNumberHumanReadable(currentPlayer.getExperience())), levelX, (i + 1 * deltaY) - 110 + rankY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(currentPlayer.getExperience(), 1000)), 180, (i + 1 * deltaY) - 90 + rankY);
                        }
                        case "message" -> {
                            g2d.drawString(getLanguageManager().get("common.messages", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "shift" -> {
                            g2d.drawString(getLanguageManager().get("common.shifts", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "token" -> {
                            g2d.drawString(getLanguageManager().get("common.balance.tokens", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "coin" -> {
                            g2d.drawString(getLanguageManager().get("common.balance.coins", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "karma" -> {
                            g2d.drawString(getLanguageManager().get("common.statistics.karma", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "upvote" -> {
                            g2d.drawString(getLanguageManager().get("common.balance.upvote", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                        case "event" -> {
                            g2d.drawString(getLanguageManager().get("common.statistics.events", formatNumberHumanReadable(user.getScore())), levelX, (i + 1 * deltaY) - 110 + expY);
                            g2d.drawString(getLanguageManager().get("common.rank", getRank(user.getScore(), 1000)), levelX, (i + 1 * deltaY) - 110 + rankY);
                        }
                    }
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(deriveImage, "png", baos);
                byte[] finalImage = baos.toByteArray();
                baos.close();

                String id = GameId.generateId();

                builder.setImage("attachment://".concat("leaderboard_".concat(id.concat(".png"))));

                g2d.dispose();

                interaction.replyEmbeds(builder.build())
                        .addFiles(FileUpload.fromData(finalImage, "leaderboard_".concat(id.concat(".png"))))
                        .queue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<LeaderboardBuilder>> getTopUsersByLevelAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> this.getUserManager().getTopUsersByLevel(limit));
    }

    public CompletableFuture<List<LeaderboardBuilder>> getTopUsersByStatsAsync(int limit, String statType) {
        return CompletableFuture.supplyAsync(() -> this.getUserManager().getTopUsersByStats(limit, statType));
    }

    public CompletableFuture<PlayerBean> fetchUserAsync(User user) {
        return CompletableFuture.supplyAsync(() -> this.getUserManager().fetchUser(user));
    }

    private String getRank(long xp, int overallRank) {
        String[] divisions = {
                this.getLanguageManager().get("rank.bronze"),
                this.getLanguageManager().get("rank.iron"),
                this.getLanguageManager().get("rank.silver"),
                this.getLanguageManager().get("rank.gold"),
                this.getLanguageManager().get("rank.platinum"),
                this.getLanguageManager().get("rank.diamond"),
                this.getLanguageManager().get("rank.master")
        };
        String[] tiers = {"IV", "III", "II", "I"};

        long divisionIndex = Math.min(xp / 10000, divisions.length - 1);  // Amount of 10k XP blocks
        long tierIndex = Math.min((Math.floorDiv(xp, 10000)) % 10, tiers.length - 1); // Amount of remaining 1k XP blocks

        String rank = divisions[(int) divisionIndex] + " " + tiers[(int) tierIndex];

        if (overallRank <= 500)
            rank = "Fluff Master";
        return rank;
    }
}
