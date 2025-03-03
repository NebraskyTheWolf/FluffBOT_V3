/*
---------------------------------------------------------------------------------
File Name : DropItemBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 06/06/2024
Last Modified : 06/06/2024

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
public class DropItemBuilder {
    private String dropId;
    private UserSnowflake droppedBy;
    private UserSnowflake claimedBy;
    private boolean isClaimed;
    private int itemId;
    private int quantity;
}