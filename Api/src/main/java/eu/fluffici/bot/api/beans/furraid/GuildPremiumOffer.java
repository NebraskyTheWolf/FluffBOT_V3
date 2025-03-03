/*
---------------------------------------------------------------------------------
File Name : GuildPremiumOffer

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 26/06/2024
Last Modified : 26/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.furraid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class GuildPremiumOffer {
    private UserSnowflake customer;
    private int offerId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp expirationAt;
    private boolean isActive;
}