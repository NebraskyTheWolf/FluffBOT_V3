/*
---------------------------------------------------------------------------------
File Name : PodiumBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/07/2024
Last Modified : 15/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.podium;

import eu.fluffici.bot.api.podium.impl.MessageLeaderboard;
import eu.fluffici.bot.api.podium.impl.PodiumType;
import lombok.Data;

import java.util.List;

@Data
public class PodiumBuilder {
    private final PodiumType podiumType;
    private final List<MessageLeaderboard> results;
}