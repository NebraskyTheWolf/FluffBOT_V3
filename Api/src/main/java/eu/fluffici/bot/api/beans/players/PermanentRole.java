/*
---------------------------------------------------------------------------------
File Name : PermanentRole

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 14/06/2024
Last Modified : 14/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.players;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@Setter
@AllArgsConstructor
public class PermanentRole {
    private UserSnowflake user;
    private String roleId;
}