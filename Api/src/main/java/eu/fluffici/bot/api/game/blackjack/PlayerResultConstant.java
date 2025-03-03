/*
---------------------------------------------------------------------------------
File Name : PlayerResultConstant

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.blackjack;

import lombok.Getter;

@Getter
public class PlayerResultConstant {
    // AVATAR
    private final int AVATAR_X = 31;
    private final int AVATAR_Y = 205;
    private final int AVATAR_W = 126;
    private final int AVATAR_H = 118;

    // NAME
    private final int NAME_X = 181;
    private final int NAME_Y = 240;

    // SCORE
    private final int SCORE_Y = 275;
    private final int PLAYER_SCORE_X = 181;

    // DEFEAT/WON ICON
    private final int ICON_X = 422;
    private final int ICON_Y = 269;
    private final int ICON_W = 58;
    private final int ICON_H = 58;

    // STATUS TEXT

    private final int STATUS_TEXT_Y = 430;
    private final int PLAYER_STATUS_TEXT_X = 140;

    // CARDS POSITION / SCALES
    private final int CARDS_W = 135;
    private final int CARDS_H = 210;
    private final int CARDS_Y = 514;

    // PLAYER

    private final int PLAYER_CARD_ONE_X = 30;
    private final int PLAYER_CARD_TWO_X = 190;
    private final int PLAYER_CARD_THREE_X = 350;

    // NEON
    // CARDS
    private final int NEON_CARD_ONE_X = 879;
    private final int NEON_CARD_TWO_X = 1039;
    private final int NEON_CARD_THREE_X = 1200;

    // SCORE
    private final int NEON_SCORE_X = 889;

    // ICON
    private final int NEON_ICON_X = 1268;

    // WATERMARKS
    private final int GAME_ID_X = 20;
    private final int GAME_ID_Y = 766;
}