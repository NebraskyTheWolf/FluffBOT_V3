/*
---------------------------------------------------------------------------------
File Name : PlayerResultGenerator

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.blackjack;

import eu.fluffici.bot.api.hooks.ILanguageManager;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.fluffici.bot.api.game.GraphicsAPI.makeRoundedCorner;

@SuppressWarnings("ALL")
public class PlayerResultGenerator {

    private final BufferedImage bufferedImage;
    private final BufferedImage lost;
    private final BufferedImage trophy;
    private final BufferedImage tie;
    private final PlayerResultBuilder currentGame;
    private final PlayerResultConstant constant = new PlayerResultConstant();

    private Font customFont;
    private Font neon;

    private final ILanguageManager languageManager;


    @SneakyThrows
    public PlayerResultGenerator(ILanguageManager languageManager, PlayerResultBuilder currentGame) {
        this.currentGame = currentGame;
        this.bufferedImage = ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/base.png"));
        this.languageManager = languageManager;

        this.lost = ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/components/circle-minus.png"));
        this.trophy = ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/components/trophy.png"));
        this.tie = ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/components/circle-dotted.png"));

        try {
            InputStream is = this.getClass().getResourceAsStream("/fonts/LexendDeca.ttf");
            this.customFont = Font.createFont(Font.TRUETYPE_FONT, is);

            InputStream is1 = this.getClass().getResourceAsStream("/fonts/neon.otf");
            this.neon = Font.createFont(Font.TRUETYPE_FONT, is1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a result card for a game.
     * The result card includes various sections such as player, neon, status, and miscellaneous.
     * The result card is rendered as a BufferedImage and then converted to a FileUpload object.
     *
     * @return The generated FileUpload object containing the result card image.
     *         Returns null if an error occurs during the generation process.
     */
    public FileUpload generateResultCard() throws IOException {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g2d = this.bufferedImage.createGraphics();
            BufferedImage avatar = ImageIO.read(new URL(this.currentGame.getUser().getAvatarUrl()));
            BufferedImage roundedAvatar = makeRoundedCorner(avatar, avatar.getWidth());

            ge.registerFont(customFont);
            ge.registerFont(neon);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Player
            this.handlePlayerSection(g2d);

            this.handleStatusSection(g2d);

            // Neon
            this.handleNeonSection(g2d);

            // Misc
            g2d.setFont(customFont.deriveFont(45f));
            g2d.setColor(Color.decode("#FF002C"));
            g2d.drawString(this.currentGame.getUser().getGlobalName().toUpperCase(), this.constant.getNAME_X(), this.constant.getNAME_Y());

            g2d.drawImage(roundedAvatar, this.constant.getAVATAR_X(), this.constant.getAVATAR_Y(), this.constant.getAVATAR_W(), this.constant.getAVATAR_H(), null);
            g2d.setColor(Color.white);
            g2d.setFont(customFont.deriveFont(16f));
            g2d.drawString("GameID: " + this.currentGame.getCurrentGame().getGameId(), this.constant.getGAME_ID_X(), this.constant.getGAME_ID_Y());
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(this.bufferedImage, "png", baos);
            byte[] finalImage = baos.toByteArray();

            return FileUpload.fromData(finalImage, this.currentGame.getCurrentGame().getGameId().concat("_result.png"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Handles the rendering of the player section in the result card.
     *
     * @param g2d The Graphics2D object used for rendering.
     */
    private void handlePlayerSection(Graphics2D g2d) {
        List<BufferedImage> playerCards = this.currentGame.getPlayerHand()
                .stream()
                .map(code -> {
                    try {
                        return ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/decks/".concat(this.currentGame.getDeckSlug()).concat("/").concat(code).concat(".png")));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toList();

        BufferedImage cardOne = playerCards.get(0);
        BufferedImage cardTwo = playerCards.get(1);

        g2d.drawImage(cardOne, this.constant.getPLAYER_CARD_ONE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        g2d.drawImage(cardTwo, this.constant.getPLAYER_CARD_TWO_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        if (playerCards.size() > 2) {
            g2d.drawImage(playerCards.get(2), this.constant.getPLAYER_CARD_THREE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        } else {
            g2d.drawImage(this.getDeckCover(), this.constant.getPLAYER_CARD_THREE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        }

        g2d.setColor(Color.white);
        g2d.setFont(customFont.deriveFont(20f));
        g2d.drawString(this.languageManager.get("common.blackjack.score") + ": " + this.currentGame.getPlayerScore(), this.constant.getPLAYER_SCORE_X(), this.constant.getSCORE_Y());
    }

    /**
     * Handles the rendering of the neon section in the result card.
     *
     * @param g2d The Graphics2D object used for rendering.
     */
    private void handleNeonSection(Graphics2D g2d) {
        List<BufferedImage> neonCards = this.currentGame.getNeonHand()
                .stream()
                .map(code -> {
                    try {
                        return ImageIO.read(this.getClass().getResourceAsStream("/assets/blackjack/decks/".concat(this.currentGame.getDeckSlug()).concat("/").concat(code).concat(".png")));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toList();

        BufferedImage cardOne = neonCards.get(0);
        BufferedImage cardTwo = neonCards.get(1);

        g2d.drawImage(cardOne, this.constant.getNEON_CARD_ONE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        g2d.drawImage(cardTwo, this.constant.getNEON_CARD_TWO_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        if (neonCards.size() > 2) {
            g2d.drawImage(neonCards.get(2), this.constant.getNEON_CARD_THREE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        } else {
            g2d.drawImage(this.getDeckCover(), this.constant.getNEON_CARD_THREE_X(), this.constant.getCARDS_Y(), this.constant.getCARDS_W(), this.constant.getCARDS_H(), null);
        }

        g2d.setColor(Color.white);
        g2d.setFont(customFont.deriveFont(20f));
        g2d.drawString(this.languageManager.get("common.blackjack.score") + ": " + this.currentGame.getNeonScore(), this.constant.getNEON_SCORE_X(), this.constant.getSCORE_Y());
    }

    /**
     * Retrieves the deck cover image for the player section in the result card.
     * The deck cover serves as a placeholder image if the player has fewer than 3 cards.
     *
     * @return The deck cover BufferedImage for the player section.
     */

    @Nullable
    private BufferedImage getDeckCover() {
        try {
            return ImageIO.read(this.getClass()
                    .getResourceAsStream("/assets/blackjack/decks/".concat(this.currentGame.getDeckSlug()).concat("/cover.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Handles the rendering of the status section in the result card.
     *
     * @param g2d The Graphics2D object used for rendering.
     */
    private void handleStatusSection(Graphics2D g2d) {
        if (this.currentGame.getGameStatus() == PlayerResultBuilder.GameStatus.WIN) {
            g2d.setColor(Color.green);
            g2d.setFont(customFont.deriveFont(40f));
            g2d.drawString(this.languageManager.get("common.blackjack.win"), this.constant.getPLAYER_STATUS_TEXT_X(), this.constant.getSTATUS_TEXT_Y());

            g2d.drawImage(this.trophy, this.constant.getICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
            g2d.drawImage(this.lost, this.constant.getNEON_ICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
        } else if (this.currentGame.getGameStatus() == PlayerResultBuilder.GameStatus.LOST) {
            g2d.setColor(Color.red);
            g2d.setFont(customFont.deriveFont(40f));
            g2d.drawString(this.languageManager.get("common.blackjack.lost"), this.constant.getPLAYER_STATUS_TEXT_X(), this.constant.getSTATUS_TEXT_Y());

            g2d.drawImage(this.lost, this.constant.getICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
            g2d.drawImage(this.trophy, this.constant.getNEON_ICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
        } else if (this.currentGame.getGameStatus() == PlayerResultBuilder.GameStatus.TIE) {
            g2d.setColor(Color.yellow);
            g2d.setFont(customFont.deriveFont(40f));
            g2d.drawString(this.languageManager.get("common.blackjack.tie"), this.constant.getPLAYER_STATUS_TEXT_X(), this.constant.getSTATUS_TEXT_Y());

            g2d.drawImage(this.tie, this.constant.getICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
            g2d.drawImage(this.tie, this.constant.getNEON_ICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
        } else if (this.currentGame.getGameStatus() == PlayerResultBuilder.GameStatus.BUSTED) {
            g2d.setColor(Color.orange);
            g2d.setFont(customFont.deriveFont(40f));
            g2d.drawString(this.languageManager.get("common.blackjack.busted"), this.constant.getPLAYER_STATUS_TEXT_X(), this.constant.getSTATUS_TEXT_Y());

            g2d.drawImage(this.tie, this.constant.getICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
            g2d.drawImage(this.tie, this.constant.getNEON_ICON_X(), this.constant.getICON_Y(), this.constant.getICON_W(), this.constant.getICON_H(), null);
        }
    }
}