package eu.fluffici.bot.components.commands.profile;

/*
---------------------------------------------------------------------------------
File Name : CommandProfile.java

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
import eu.fluffici.bot.api.PointCalculator;
import eu.fluffici.bot.api.TimeConverter;
import eu.fluffici.bot.api.achievement.Achievement;
import eu.fluffici.bot.api.beans.achievements.AchievementBean;
import eu.fluffici.bot.api.beans.players.LeaderboardBuilder;
import eu.fluffici.bot.api.beans.players.Message;
import eu.fluffici.bot.api.bucket.CommandHandle;
import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.PlayerBean;
import eu.fluffici.bot.components.commands.Command;
import eu.fluffici.bot.api.interactions.CommandCategory;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static eu.fluffici.bot.api.RomanNumeralParser.parseRomanNumeral;
import static eu.fluffici.bot.api.game.GraphicsAPI.*;
import static eu.fluffici.bot.api.IconRegistry.ICON_BOOK;
import static eu.fluffici.bot.api.IconRegistry.ICON_REPORT_SEARCH;

@CommandHandle
@SuppressWarnings("All")
public class CommandProfile extends Command {

    private final String DEFAULT_CARD = "default_profile_bg.png";

    private Font customFont;
    private Font neon;

    public CommandProfile() {
        super("profile", "This command will show your assigned profile on the server.", CommandCategory.PROFILE);

        this.getOptionData().add(new OptionData(OptionType.USER,"user", "Select a user.", false));

        this.getOptions().put("channelRestricted", true);
        this.getOptions().put("rate-limit", true);
        this.getOptions().put("noSelfUser", true);

        try {
            InputStream is = FluffBOT.getInstance().getClass().getResourceAsStream("/fonts/lexend.ttf");
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, is);

            InputStream is1 = FluffBOT.getInstance().getClass().getResourceAsStream("/fonts/neon.otf");
            this.neon = Font.createFont(Font.TRUETYPE_FONT, is1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public void execute(CommandInteraction interaction) {
        User currentUser = interaction.getUser();
        PlayerBean currentPlayer =  this.getUserManager().fetchUser(currentUser);

        if (interaction.getOption("user") != null) {
            User user = interaction.getOption("user").getAsUser();
            if (user != null && interaction.getJDA().getUserById(user.getId()) != null) {
                currentPlayer = this.getUserManager().fetchUser(user);
                currentUser = user;
            }
        }

        if (currentPlayer == null) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.unknown"))).queue();
            return;
        }

        String requestId = GameId.generateId();

        PlayerBean finalCurrentPlayer = currentPlayer;
        User finalCurrentUser = currentUser;
        CompletableFuture<FileUpload> generatedProfile = CompletableFuture.supplyAsync(() -> this.handleProfile(finalCurrentPlayer, finalCurrentUser, requestId, false));
        generatedProfile.whenComplete((fileUpload, throwable) -> {
            interaction.replyEmbeds(getEmbed()
                    .simpleAuthoredEmbed()
                    .setAuthor(getLanguageManager().get("command.profile.title", finalCurrentUser.getEffectiveName()), "https://fluffici.eu", ICON_BOOK.getUrl())
                    .setImage("attachment://".concat(requestId.concat("_profile.png")))
                    .setTimestamp(Instant.now())
                    .setFooter(getLanguageManager().get("command.profile.footer"), ICON_REPORT_SEARCH.getUrl())
                    .build()
            ).addFiles(fileUpload).queue();
        }).exceptionally(e -> {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.failed"))).queue();
            FluffBOT.getInstance().getLogger().error("A error occurred while generating a profile.", e);
            e.printStackTrace();
            return null;
        });

        if (generatedProfile.isCancelled()
                || generatedProfile.state() == Future.State.FAILED
                || generatedProfile.state() == Future.State.CANCELLED) {
            interaction.replyEmbeds(this.buildError(this.getLanguageManager().get("command.profile.completeable.failed"))).queue();
        }
    }

    public FileUpload handleProfile(@NotNull PlayerBean currentPlayer, User currentUser, String requestId, boolean isPreview) {
        String profilePath = DEFAULT_CARD;
        boolean isSelected = false;
        if (currentPlayer.getPlayerProfile().getBackground() != null) {
            profilePath = currentPlayer.getPlayerProfile().getBackground().getAssetPath();
            isSelected = true;
        }

        try {
            BufferedImage background = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/profile/backgrounds/".concat(profilePath)));
            BufferedImage baseImage = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/profile/base.png"));

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g2d = background.createGraphics();
            BufferedImage avatar = ImageIO.read(new URL(currentUser.getAvatarUrl()));
            BufferedImage roundedAvatar = makeRoundedCorner(avatar, avatar.getWidth());

            ge.registerFont(customFont);
            ge.registerFont(neon);

            Font customFont = this.customFont.deriveFont(Font.BOLD, 24);
            Font neon = this.neon.deriveFont(Font.BOLD, 40);

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.drawImage(baseImage, 0, 0, null);

            g2d.setFont(customFont);

            // Header

            g2d.drawImage(roundedAvatar, 16, 16, 128, 128, null); // replace constants with your values

            g2d.setColor(Color.BLACK);
            g2d.setFont(customFont.deriveFont(Font.BOLD, 16));
            drawStringWithMaxWidth(g2d, customFont, currentUser.getGlobalName().toUpperCase(), 60f, 90, 330, 75);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 20));
            g2d.setColor(Color.WHITE);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 12));
            g2d.drawString(currentUser.getId(), 185, 103);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 20));

            g2d.setFont(neon.deriveFont(Font.PLAIN, 40));
            g2d.setColor(Color.orange);
            drawStringWithMaxWidth(g2d, neon, String.valueOf(currentPlayer.getLevel()), 40f, 60, 655, 90);

            g2d.setColor(Color.WHITE);
            g2d.setFont(customFont.deriveFont(Font.BOLD, 12));

            // Bank end
            List<Message> messages = FluffBOT.getInstance().getGameServiceManager().sumAll();
            long count = messages.stream()
                    .filter(message -> message.getUserId().equals(currentUser.getId()))
                    .count() + currentPlayer.getLegacyMessageCount();

            long points = PointCalculator.calculatePoints(currentPlayer, (int) count, 0);
            long rrPoints = PointCalculator.calculateRr(points);

            BufferedImage division = ImageIO.read(FluffBOT.getInstance().getClass().getResourceAsStream("/assets/divisions/" + this.getRankSlug(rrPoints) + "/" + this.getRankDeivisions(rrPoints) + ".png"));

            g2d.drawImage(division, 250, 700, 105, 105, null);

            int yGap = 20;

            g2d.setFont(customFont.deriveFont(20f));
            g2d.drawString(getLanguageManager().get("common.rank", getRank(rrPoints, 10000)), 355, 760 - yGap);

            g2d.setFont(customFont.deriveFont(16f));
            g2d.drawString(NumberFormat.getNumberInstance().format(rrPoints) + " RR", 355, 780 - yGap);

            // Header end

            int gapY = 25;
            int gapX = 10;

            // Bank
            g2d.setFont(customFont.deriveFont(Font.BOLD, 22));
            g2d.drawString(getLanguageManager().get("common.balance.coins", formatNumberHumanReadable(Math.abs(currentPlayer.getCoins()))), 40 + gapY, 355 + gapY);
            g2d.drawString(getLanguageManager().get("common.balance.tokens", formatNumberHumanReadable(Math.abs(currentPlayer.getTokens()))), 40 + gapY, 385 + gapY);
            g2d.drawString(getLanguageManager().get("common.balance.upvote", formatNumberHumanReadable(Math.abs(currentPlayer.getUpvote()))), 40 + gapY, 416 + gapY);

            LeaderboardBuilder voiceChatStatistics = FluffBOT.getInstance()
                    .getGameServiceManager()
                    .getPlayerStatistics(currentUser, "voice-chat");
            long hours = TimeConverter.secondsToHours(voiceChatStatistics.getScore());

            // Statistics
            g2d.setFont(customFont.deriveFont(Font.BOLD, 22));
            g2d.drawString(getLanguageManager().get("common.statistics.messages", formatNumberHumanReadable(Math.abs(count))), 416 + gapY, 355 + gapY);
            g2d.drawString(getLanguageManager().get("common.statistics.events", formatNumberHumanReadable(Math.abs(currentPlayer.getEvents()))), 416 + gapY, 385 + gapY);
            g2d.drawString(getLanguageManager().get("common.statistics.karma", formatNumberHumanReadable(Math.abs(currentPlayer.getKarma()))), 416 + gapY, 416 + gapY);
            g2d.drawString(getLanguageManager().get("common.statistics.vc", formatNumberHumanReadable(Math.abs(hours))), 416 + gapY, 450 + gapY);

            // Statistics end

            // Achievements
            Achievement nextGoal = FluffBOT.getInstance().getAchievementManager().getAchievements()
                    .stream()
                    .filter(achievement -> !achievement.isUnlocked(currentUser.getId()))
                    .sorted(Comparator.comparing(achievement -> achievement.getID()))
                    .findFirst()
                    .orElse(null);

            AchievementBean handle = FluffBOT.getInstance().getGameServiceManager().getAchievement(nextGoal.getID());

            if (nextGoal != null) {
                g2d.setColor(Color.BLACK);

                g2d.setFont(customFont.deriveFont(Font.BOLD, 25));
                g2d.drawString(getLanguageManager().get("common.goal", nextGoal.getDisplayName()), 250, 210);

                g2d.setFont(customFont.deriveFont(Font.BOLD, 20));
                if (nextGoal.getProgress(currentUser.getId()) != null)
                    g2d.drawString(getLanguageManager().get("command.profile.goal.unlock.desc", nextGoal.getProgress(currentUser.getId()).getProgress(), handle.getProgressTarget()), 200, 255);
                else
                    g2d.drawString(getLanguageManager().get("command.profile.goal.unlock.desc.unav"), 200, 255);
            } else {
                g2d.setColor(Color.BLACK);

                g2d.setFont(customFont.deriveFont(Font.BOLD, 25));
                g2d.drawString(getLanguageManager().get("common.not_so_fast"), 250, 210);

                g2d.setFont(customFont.deriveFont(Font.BOLD, 20));
                g2d.drawString(getLanguageManager().get("common.unlocked"), 200, 255);
            }
            // Achievements end

            // Porgress bar / XP

            g2d.setColor(Color.WHITE);

            long max_exp = FluffBOT.getInstance().getLevelUtil().getMaxExp();

            g2d.setFont(customFont.deriveFont(Font.BOLD, 14));
            g2d.drawString(getLanguageManager().get("common.exp", formatNumberHumanReadable(currentPlayer.getExperience()), formatNumberHumanReadable(max_exp)), 635, 645);

            int messagesRequired = FluffBOT.getInstance().getLevelUtil().getRemainingMessagesToNextLevel(currentPlayer);
            double remainingHours = FluffBOT.getInstance().getLevelUtil().getRemainingHoursToNextLevelFromXP(currentPlayer);

            g2d.setFont(customFont.deriveFont(Font.BOLD, 12));
            g2d.drawString(getLanguageManager().get("common.messages.required.to_level", Math.abs(messagesRequired)), 7, 650);
            g2d.setFont(customFont.deriveFont(Font.PLAIN, 8));
            g2d.drawString(getLanguageManager().get("common.messages.required.to_voice_level", Math.abs(remainingHours)), 7, 650 - 12);

            double progress = 1.0;
            if (currentPlayer.getExperience() > 0) {
                progress = ((double) currentPlayer.getExperience() / max_exp)*710;
            }

            if (progress <= 50)
                progress = 50;

            g2d.fillRect(10, 657, (int)(progress), 18);

            if (isPreview) {
                BufferedImage previewWatermark = ImageIO.read(this.getClass().getResourceAsStream("/assets/profile/preview_watermark.png"));
                g2d.drawImage(previewWatermark, 0, 0, null);
            }

            g2d.dispose();

            // Convert the output image to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(background, "png", baos);
            byte[] finalImage = baos.toByteArray();

            return FileUpload.fromData(finalImage, requestId.concat("_profile.png"));
        } catch (Exception e) {
            FluffBOT.getInstance().getLogger().error("A error occurred while generating a profile", e);
            e.printStackTrace();
        }
        return null;
    }

    private String getRank(double points, int overallRank) {
        String[] divisions = {
                this.getLanguageManager().get("rank.bronze"),
                this.getLanguageManager().get("rank.iron"),
                this.getLanguageManager().get("rank.silver"),
                this.getLanguageManager().get("rank.gold"),
                this.getLanguageManager().get("rank.platinum"),
                this.getLanguageManager().get("rank.diamond"),
                this.getLanguageManager().get("rank.master")
        };

        String[] tiers = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII"};

        int divisionBlockSize = 180000;
        int tierBlockSize = 10000;

        int divisionIndex = Math.min((int)Math.floor(points / divisionBlockSize), divisions.length - 1);
        int tierIndex = Math.min((int)Math.floor((points % divisionBlockSize) / tierBlockSize), tiers.length - 1);

        String rank = divisions[divisionIndex] + " " + tiers[tierIndex];

        if (overallRank <= 500)
            rank = "Predator";

        return rank;
    }

    private String getRankSlug(double points) {
        String[] divisions = {
                "bronze",
                "iron",
                "silver",
                "gold",
                "platinum",
                "diamond",
                "master"
        };

        int divisionBlockSize = 180000;
        int divisionIndex = Math.min((int)Math.floor(points / divisionBlockSize), divisions.length - 1);

        return divisions[divisionIndex];
    }

    private int getRankDeivisions(double points) {
        String[] tiers = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII"};

        int divisionBlockSize = 180000;
        int tierBlockSize = 10000;

        int tierIndex = Math.min((int)Math.floor((points % divisionBlockSize) / tierBlockSize), tiers.length - 1);

        return parseRomanNumeral(tiers[tierIndex]);
    }
}
