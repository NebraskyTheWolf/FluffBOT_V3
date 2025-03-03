/*
---------------------------------------------------------------------------------
File Name : ReactionType

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 11/06/2024
Last Modified : 11/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.api.beans.channel;

import lombok.Getter;

import java.util.List;

import static net.dv8tion.jda.internal.utils.Helpers.listOf;

@Getter
public enum ReactionType {
    MEDIA(listOf("1148698889780723763", "1148698887796838491", "997592276505198632")),
    MEME(listOf("1148698889780723763", "1148698887796838491", "1044963352507580516"));

    private final List<String> reactions;
    ReactionType(List<String> reactions) {
        this.reactions = reactions;
    }
}