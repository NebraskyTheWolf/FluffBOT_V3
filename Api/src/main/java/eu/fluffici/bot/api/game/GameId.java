/*
---------------------------------------------------------------------------------
File Name : GameId

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 04/06/2024
Last Modified : 04/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game;

import java.util.UUID;

public class GameId {
    public static String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
    }
}