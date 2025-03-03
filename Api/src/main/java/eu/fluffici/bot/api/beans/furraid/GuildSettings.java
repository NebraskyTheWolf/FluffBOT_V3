/*
---------------------------------------------------------------------------------
File Name : GuildSettings

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

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class GuildSettings {
    private String guildId;
    private FurRaidConfig config;
    private String loggingChannel;
    private boolean isBlacklisted;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}