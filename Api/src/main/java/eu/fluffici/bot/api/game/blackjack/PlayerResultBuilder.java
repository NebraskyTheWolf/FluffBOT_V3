/*
---------------------------------------------------------------------------------
File Name : PlayerResultBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.blackjack;

import eu.fluffici.bot.api.beans.game.CasinoGameBuilder;
import eu.fluffici.bot.api.hooks.PlayerBean;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import java.util.List;

@Getter
@Setter
public class PlayerResultBuilder {
    private PlayerBean player;
    private User user;

    private CasinoGameBuilder currentGame;

    private List<String> playerHand;
    private List<String> neonHand;

    private GameStatus gameStatus;

    private int playerScore;
    private int neonScore;

    private String deckSlug;


    public enum GameStatus {
        BUSTED,
        TIE,
        LOST,
        WIN
    }
}