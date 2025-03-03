/*
---------------------------------------------------------------------------------
File Name : PodiumGenerator

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/07/2024
Last Modified : 15/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.podium;

import eu.fluffici.bot.api.game.GameId;
import eu.fluffici.bot.api.hooks.ILanguageManager;
import eu.fluffici.bot.api.podium.impl.MessageLeaderboard;
import eu.fluffici.bot.api.podium.impl.PodiumConstants;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;

import static eu.fluffici.bot.api.game.GraphicsAPI.makeRoundedCorner;

/**
 * The PodiumGenerator class is responsible for generating a podium display by combining various images and positioning them according to specified constants.
 * It uses the ILanguageManager implementation to manage language properties and the PodiumBuilder object to provide results for building the podium display.
 *
 * The class contains private fields for a base image, a background image, and two Fonts (customFont and neon).
 * It also has an instance of the PodiumConstants class, which defines constants for positioning elements on the podium display.
 * The class has a constructor that takes an ILanguageManager object and a PodiumBuilder object as parameters.
 *
 * The generatePodium() method is used to generate the actual podium display. It does not take any parameters and does not return any value.
 * The method combines the base image and the background image, and positions other elements based on the specified constants.
 * The method also takes into account MessageLeaderboard objects, which contain information about users and their rankings.
 * Note that the availability of necessary images and resources specified in the constants is crucial for successful generation of the podium display.
 *
 * See also:
 * - ILanguageManager interface
 * - PodiumBuilder class
 * - PodiumConstants class
 * - MessageLeaderboard class
 * - User class
 */
@SuppressWarnings("All")
public class PodiumGenerator {

    private final BufferedImage baseImage;
    private final BufferedImage brandingImage;
    private final BufferedImage backgroundImage;
    private Font customFont;
    private Font neon;

    private final PodiumConstants podiumConstants = new PodiumConstants();
    private final ILanguageManager languageManager;
    private final PodiumBuilder podiumBuilder;

    /**
     * Constructs a new PodiumGenerator object.
     *
     * @param languageManager the ILanguageManager implementation used to manage language properties
     * @param builder the PodiumBuilder object used to provide the results for building the podium display
     *
     * @throws IOException if there is an error reading the image resources
     * @throws FontFormatException if there is an error creating the custom fonts
     *
     * @see ILanguageManager
     * @see PodiumBuilder
     */
    @SneakyThrows
    public PodiumGenerator(ILanguageManager languageManager, PodiumBuilder builder) {
        this.languageManager = languageManager;
        this.podiumBuilder = builder;

        this.baseImage = ImageIO.read(this.getClass().getResourceAsStream("/assets/podium/bases/".concat(this.podiumBuilder.getPodiumType().name().toLowerCase()).concat(".png")));
        this.brandingImage = ImageIO.read(this.getClass().getResourceAsStream(this.podiumConstants.getLogo().getLogoPath()));
        this.backgroundImage = ImageIO.read(this.getClass().getResourceAsStream(this.podiumConstants.getBackgroundPath()));

        InputStream is = this.getClass().getResourceAsStream("/fonts/LexendDeca.ttf");
        this.customFont = Font.createFont(Font.TRUETYPE_FONT, is);

        InputStream is1 = this.getClass().getResourceAsStream("/fonts/neon.otf");
        this.neon = Font.createFont(Font.TRUETYPE_FONT, is1);
    }

    /**
     * Generates a podium display.
     *
     * This method generates a podium display by combining various images and positioning them according to the specified constants.
     * The method takes no parameters and does not return any value.
     *
     * The generated podium display is based on a base image and a background image, which are specified as constants in the PodiumConstants class.
     * It also uses constants from the Logo, FirstPlace, SecondPlace, and ThirdPlace classes to position the corresponding elements on the display.
     *
     * The podium display includes information from a list of MessageLeaderboard objects, which contain a User and a count value.
     * The User object contains information such as the avatar image path, the user's name, and the position on the podium.
     * The count value represents the score or ranking of the user.
     *
     * Note that the generation of the podium display depends on the availability of the necessary images and resources specified in the constants.
     *
     * @see PodiumConstants
     * @see PodiumConstants.Logo
     * @see PodiumConstants.FirstPlace
     * @see PodiumConstants.FirstPlace.User
     * @see PodiumConstants.SecondPlace
     * @see PodiumConstants.SecondPlace.User
     * @see PodiumConstants.ThirdPlace
     * @see PodiumConstants.ThirdPlace.User
     * @see MessageLeaderboard
     * @see User
     */
    public FileUpload generatePodium(String callbackId) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Graphics2D g2d = this.backgroundImage.createGraphics();

            ge.registerFont(customFont);
            ge.registerFont(neon);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.drawImage(this.baseImage, 0, 0, null);

            g2d.setFont(this.customFont.deriveFont(16f));
            g2d.drawString("FluffBOT v3", 18, 935);
            g2d.drawString("Image Generated by FluffBOT (c) Fluffici, z.s. - 2024 | All Rights Reserved.", 18, 960);

            switch (this.podiumBuilder.getPodiumType()) {
                case MINI -> {
                    this.handleFirstPlace(g2d);
                    this.handleSecondPlace(g2d);
                    this.handleThirdPlace(g2d);
                }
                case FULL -> {
                    this.handleFirstPlace(g2d);
                    this.handleSecondPlace(g2d);
                    this.handleThirdPlace(g2d);

                    // Handle all entries except the 3 first entries
                    this.handleAllOtherPlaces(g2d);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(this.backgroundImage, "png", baos);
            byte[] finalImage = baos.toByteArray();

            return FileUpload.fromData(finalImage, callbackId.concat(".png"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handles the first place on the podium.
     *
     * This method is responsible for handling the elements and positioning of the first place on the podium display. It does not take any parameters and does not return any value
     * . It is a private method called within the PodiumGenerator class.
     *
     * The first place on the podium is determined by the constants defined in the PodiumConstants class. The position, size, and other details for the first place element are specified
     *  in the FirstPlace class, which is nested within the PodiumConstants. The User class, also nested within FirstPlace, contains additional details about the user, such as the
     *  avatar image path, name, and count.
     *
     * This method is part of the process of generating the complete podium display in the generatePodium method of the PodiumGenerator class. It is used together with handleSecond
     * Place and handleThirdPlace methods to handle all the places on the podium.
     *
     * @see PodiumGenerator
     * @see PodiumConstants
     * @see PodiumConstants.FirstPlace
     * @see PodiumConstants.FirstPlace.User
     */
    private void handleFirstPlace(@NotNull Graphics2D g2d) {
        MessageLeaderboard firstPlace = this.podiumBuilder.getResults().get(0);
        User firstUser = firstPlace.getUser();

        PodiumConstants.FirstPlace place = this.podiumConstants.getFirstPlace();

        g2d.setFont(this.customFont.deriveFont(25f));
        FontMetrics fm = g2d.getFontMetrics();

        int yGap = 35;

        g2d.drawImage(this.getAvatarImage(firstUser),
                place.getUser().getAvatarX(),
                place.getUser().getAvatarY(),
                place.getUser().getAvatarW(),
                place.getUser().getAvatarH(),
                null);

        int avatarCenterX = place.getUser().getAvatarX() + (place.getUser().getAvatarW() / 2);
        int nameX = avatarCenterX - (fm.stringWidth(firstUser.getEffectiveName()) / 2);
        int countX = avatarCenterX - (fm.stringWidth(NumberFormat.getNumberInstance().format(firstPlace.getCount()) + " Z.O") / 2);

        g2d.drawString(firstUser.getEffectiveName(), nameX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap);
        g2d.drawString(NumberFormat.getNumberInstance().format(firstPlace.getCount()) + " Z.O", countX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap + fm.getHeight());
    }

    /**
     * Handles the second place on the podium.
     *
     * This method is responsible for handling the elements and positioning of the second place on the podium display. It does not take any parameters and does not return any value
     * .
     * It is a private method called within the PodiumGenerator class.
     *
     * The second place on the podium is determined by the constants defined in the PodiumConstants class. The position, size, and other details for the second place element are specified
     *
     * in the SecondPlace class, which is nested within the PodiumConstants. The User class, also nested within SecondPlace, contains additional details about the user, such as the
     *
     * avatar image path, name, and count.
     *
     * This method is part of the process of generating the complete podium display in the generatePodium method of the PodiumGenerator class. It is used together with handleFirst
     * Place
     * and handleThirdPlace methods to handle all the places on the podium.
     *
     * @see PodiumGenerator
     * @see PodiumConstants
     * @see PodiumConstants.SecondPlace
     * @see PodiumConstants.SecondPlace.User
     */
    private void handleSecondPlace(@NotNull Graphics2D g2d) {
        MessageLeaderboard secondPlace = this.podiumBuilder.getResults().get(1);
        User secondUser = secondPlace.getUser();

        PodiumConstants.SecondPlace place = this.podiumConstants.getSecondPlace();

        g2d.setFont(this.customFont.deriveFont(25f));
        FontMetrics fm = g2d.getFontMetrics();

        int yGap = 35;

        g2d.drawImage(this.getAvatarImage(secondUser),
                place.getUser().getAvatarX(),
                place.getUser().getAvatarY(),
                place.getUser().getAvatarW(),
                place.getUser().getAvatarH(),
                null);

        int avatarCenterX = place.getUser().getAvatarX() + (place.getUser().getAvatarW() / 2);
        int nameX = avatarCenterX - (fm.stringWidth(secondUser.getEffectiveName()) / 2);
        int countX = avatarCenterX - (fm.stringWidth(NumberFormat.getNumberInstance().format(secondPlace.getCount()) + " Z.O") / 2);

        g2d.drawString(secondUser.getEffectiveName(), nameX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap);
        g2d.drawString(NumberFormat.getNumberInstance().format(secondPlace.getCount()) + " Z.O", countX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap + fm.getHeight());
    }

    /**
     * Handles the third place on the podium display.
     *
     * This method is responsible for handling the elements and positioning of the third place on the podium display. It does not take any parameters and does not return any value
     * .
     * It is a private method called within the PodiumGenerator class.
     *
     * The third place on the podium is determined by the constants defined in the PodiumConstants class. The position, size, and other details for the third place element are specified
     *
     * in the ThirdPlace class, which is nested within the PodiumConstants. The User class, also nested within ThirdPlace, contains additional details about the user, such as the
     *
     * avatar image path, name, and count.
     *
     * This method is part of the process of generating the complete podium display in the generatePodium method of the PodiumGenerator class. It is used together with handleFirst
     * Place
     * and handleSecondPlace methods to handle all the places on the podium.
     *
     * @see PodiumGenerator
     * @see PodiumConstants
     * @see PodiumConstants.ThirdPlace
     * @see PodiumConstants.ThirdPlace.User
     */
    private void handleThirdPlace(@NotNull Graphics2D g2d) {
        MessageLeaderboard thirdPlace = this.podiumBuilder.getResults().get(2);
        User thirdPlaceUser = thirdPlace.getUser();

        PodiumConstants.ThirdPlace place = this.podiumConstants.getThirdPlace();

        g2d.setFont(this.customFont.deriveFont(25f));
        FontMetrics fm = g2d.getFontMetrics();

        int yGap = 35;

        g2d.drawImage(this.getAvatarImage(thirdPlaceUser),
                place.getUser().getAvatarX(),
                place.getUser().getAvatarY(),
                place.getUser().getAvatarW(),
                place.getUser().getAvatarH(),
                null);

        int avatarCenterX = place.getUser().getAvatarX() + (place.getUser().getAvatarW() / 2);
        int nameX = avatarCenterX - (fm.stringWidth(thirdPlaceUser.getEffectiveName()) / 2);
        int countX = avatarCenterX - (fm.stringWidth(NumberFormat.getNumberInstance().format(thirdPlace.getCount()) + " Z.O") / 2);

        g2d.drawString(thirdPlaceUser.getEffectiveName(), nameX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap);
        g2d.drawString(NumberFormat.getNumberInstance().format(thirdPlace.getCount()) + " Z.O", countX, place.getUser().getAvatarY() + place.getUser().getAvatarH() + yGap + fm.getHeight());
    }

    /**
     * Handles all other places on the podium display.
     *
     * This method is responsible for handling the elements and positioning of all other places on the podium display, excluding the first, second, and third places. It does not take
     *  any parameters and does not return any value.
     *
     * This private method is called within the PodiumGenerator class. It is part of the process of generating the complete podium display in the generatePodium method.
     *
     * The positions, sizes, and other details for the other places elements are specified in the PodiumConstants class. The User class, nested within PodiumConstants for each place
     * , contains additional details about the user, such as the avatar image path, name, and count.
     *
     * This method is used together with handleFirstPlace, handleSecondPlace, and handleThirdPlace methods to handle all the places on the podium.
     *
     * @see PodiumGenerator
     * @see PodiumConstants
     * @see PodiumConstants.OtherPlace
     * @see PodiumConstants.OtherPlace.User
     */
    private void handleAllOtherPlaces(@NotNull Graphics2D g2d) {
        int startX = 172;
        int startY = 575;

        int endX = -942;
        int endY = -95;

        int totalWidth = Math.abs(endX - startX);
        int totalHeight = Math.abs(endY - startY);

        int numRows = 3;
        int numCols = 3;

        double factor = 0.5;

        int gapX = (int)(totalWidth / (numCols - 1) * factor);
        int gapY = (int)(totalHeight / (numRows - 1) * factor);

        g2d.setFont(this.customFont.deriveFont(25f));
        g2d.setColor(Color.WHITE);

        java.util.List<MessageLeaderboard> results = this.podiumBuilder.getResults();
        java.util.List<MessageLeaderboard> sublist = results.subList(3, results.size());

        for (int i = 4; i <= 10; i++) {
            int col = (i - 4) % numCols;
            int row = (i - 4) / numCols;

            int x = startX + col * gapX;
            int y = startY + row * gapY;



            if (i - 4 < sublist.size()) {
                MessageLeaderboard user = sublist.get(i - 4);

                g2d.drawString("#" + i + " - " + user.getUser().getEffectiveName(), x - 25, y);
                g2d.drawString(NumberFormat.getNumberInstance().format(user.getCount()) + " Z.O", x + 30, y + 30);
                g2d.drawImage(this.getAvatarImage(user.getUser()), x - 40, y + 12, 58, 58, null);
            }
        }

    }

    @NotNull
    @SneakyThrows
    private BufferedImage getAvatarImage(@NotNull User user) {
        BufferedImage avatar = ImageIO.read(new URL(user.getAvatarUrl()));
        return makeRoundedCorner(avatar, avatar.getWidth());
    }
}