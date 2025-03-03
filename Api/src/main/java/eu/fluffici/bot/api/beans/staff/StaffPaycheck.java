/*
---------------------------------------------------------------------------------
File Name : StaffPaycheck

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 17/07/2024
Last Modified : 17/07/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.staff;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.Timestamp;

@Getter
@Setter
public class StaffPaycheck {
    private UserSnowflake user;
    private int points;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}