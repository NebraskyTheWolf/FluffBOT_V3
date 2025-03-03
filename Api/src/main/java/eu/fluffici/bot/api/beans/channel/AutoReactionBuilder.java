/*
---------------------------------------------------------------------------------
File Name : AutoReactionBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.channel;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AutoReactionBuilder {
    private AutoReactions channel;
    private List<String> reactions;
}