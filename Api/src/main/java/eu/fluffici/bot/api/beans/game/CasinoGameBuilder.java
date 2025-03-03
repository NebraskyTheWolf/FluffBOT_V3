/*
---------------------------------------------------------------------------------
File Name : CasinoGameBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 03/06/2024
Last Modified : 03/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@Setter
@AllArgsConstructor
public class CasinoGameBuilder {
    private String gameId;
    private GameStatus gameStatus;
    private UserSnowflake user;
    private int playerScore;
    private int neonScore;

    public enum GameStatus {
        STARTED, ONGOING, FINISHED
    }
}