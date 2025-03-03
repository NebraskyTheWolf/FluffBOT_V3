/*
---------------------------------------------------------------------------------
File Name : AutoReactions

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AutoReactions {
    private String channelId;
    private String reactionType;
}