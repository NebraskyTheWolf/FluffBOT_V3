/*
---------------------------------------------------------------------------------
File Name : RestrictedAccess

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.players;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.Timestamp;

/**
 * The {@code RestrictedAccess} class represents restricted access information.
 * It contains information about the user, the author, the reason for restriction,
 * the status of restriction, and the creation timestamp.
 */
@Getter
@Setter
@AllArgsConstructor
public class RestrictedAccess {
    private UserSnowflake user;
    private UserSnowflake author;
    private String reason;
    private boolean isActive;
    private Timestamp createdAt;
}