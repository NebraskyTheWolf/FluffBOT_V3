/*
---------------------------------------------------------------------------------
File Name : Staff

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 19/06/2024
Last Modified : 19/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@Setter
@AllArgsConstructor
public class Staff {
    private UserSnowflake user;
}