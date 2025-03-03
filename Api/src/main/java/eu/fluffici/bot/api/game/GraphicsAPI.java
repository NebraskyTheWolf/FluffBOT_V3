package eu.fluffici.bot.api.game;

/*
---------------------------------------------------------------------------------
File Name : GraphicsAPI.java

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


import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
public class GraphicsAPI {
    /**
     * Formats a given number in a human-readable format.
     * If the number is less than 1000, it is returned as is.
     * If the number is greater than or equal to 1000, it is divided by powers of 1000 (K, M, B, T),
     * and formatted with comma separators.
     *
     * @param number The number to format.
     * @return The formatted number as a string in a human-readable format.
     */
    public static String formatNumberHumanReadable(long number) {
        if (number < 1000) return "" + number;
        char[] units = new char[] {'K', 'M', 'B', 'T'};
        int digitGroups = (int) (Math.log10(number) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(number / Math.pow(1000, digitGroups)) + "" + units[digitGroups - 1];
    }

    /**
     * Creates a new BufferedImage with rounded corners based on the given image and corner radius.
     *
     * @param image        The image to create rounded corners for.
     * @param cornerRadius The radius of the corners.
     * @return A new BufferedImage with rounded corners.
     */
    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    /**
     * Draws a string with a maximum width on a Graphics2D object.
     *
     * @param g2d             the Graphics2D object to draw on
     * @param customFont      the custom font to use for drawing the string
     * @param text            the string to draw
     * @param initialFontSize the initial font size to use
     * @param maxWidth        the maximum width for the string
     * @param x               the x-coordinate of the starting position of the string
     * @param y               the y-coordinate of the starting position of the string
     */
    public static void drawStringWithMaxWidth(Graphics2D g2d, Font customFont, String text, float initialFontSize, int maxWidth, int x, int y) {
        // Create a new Font with BOLD style, and initial size
        Font currentFont = customFont.deriveFont(initialFontSize);

        // Get the width of the characters in the current font
        int strWidth = g2d.getFontMetrics(currentFont).stringWidth(text);

        // Reduce font size if the string is too wide
        while (strWidth > maxWidth && initialFontSize > 0) {
            initialFontSize--;
            currentFont = customFont.deriveFont(initialFontSize);
            strWidth = g2d.getFontMetrics(currentFont).stringWidth(text);
        }

        // Calculate the x coordinate to center the text within the maximum width
        int centeredX = x + (maxWidth - strWidth) / 2;

        g2d.setFont(currentFont);
        g2d.drawString(text, centeredX, y);
    }
}
