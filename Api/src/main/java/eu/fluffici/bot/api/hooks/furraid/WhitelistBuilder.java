/*
---------------------------------------------------------------------------------
File Name : WhitelistBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 18/06/2024
Last Modified : 18/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.hooks.furraid;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

@Builder
@Getter
@Setter
public class WhitelistBuilder {
    private Guild guild;
    private UserSnowflake user;
}