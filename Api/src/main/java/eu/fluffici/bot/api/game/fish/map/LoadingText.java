/*
---------------------------------------------------------------------------------
File Name : LoadingText

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 10/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.fish.map;

import javax.swing.*;
import java.awt.*;

public class LoadingText extends JPanel {

    private String loadingText = "";


    public void setLoadingText(String text) {
        this.loadingText = text;
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!loadingText.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(loadingText)) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g.setColor(Color.RED);
            g.drawString(loadingText, x, y);
        }
    }
}