/*
---------------------------------------------------------------------------------
File Name : ChunkLoadingCallback

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 10/06/2024
Last Modified : 10/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game.fish.map;

import eu.fluffici.bot.api.game.fish.environment.Environment;

public interface ChunkLoadingCallback {
    void step(int x, int y, int z, Environment environment);
    void error();
}