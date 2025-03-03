/*
---------------------------------------------------------------------------------
File Name : PlayerDeathInfo

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 08/06/2024
Last Modified : 08/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.game;

import eu.fluffici.bot.api.inventory.InventoryItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Getter
@Setter
@Builder
public class PlayerDeathInfo {
    private UserSnowflake from;
    private DeathType deathType;
    private InventoryItem hitter;
}