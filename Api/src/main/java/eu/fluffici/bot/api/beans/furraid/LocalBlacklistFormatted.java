/*
---------------------------------------------------------------------------------
File Name : LocalBlacklist

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class LocalBlacklistFormatted {
    private String id;
    private String avatar;
    private String username;
    private String issuedBy;
    private String reason;
    private Timestamp createdAt;
}