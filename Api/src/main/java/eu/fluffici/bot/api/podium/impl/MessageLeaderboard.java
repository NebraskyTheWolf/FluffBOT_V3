/*
---------------------------------------------------------------------------------
File Name : MessageLeaderboard

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/07/2024
Last Modified : 15/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.podium.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@AllArgsConstructor
public class MessageLeaderboard {
    private User user;
    private int count;
}