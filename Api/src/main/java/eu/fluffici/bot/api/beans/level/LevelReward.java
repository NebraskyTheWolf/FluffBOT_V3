/*
---------------------------------------------------------------------------------
File Name : LevelReward

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 12/06/2024
Last Modified : 12/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.level;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LevelReward {
    private String roleId;
    private int requiredLevel;
}