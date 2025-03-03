/*
---------------------------------------------------------------------------------
File Name : PageBuilder

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 07/06/2024
Last Modified : 07/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.components.button.paginate;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
@Builder
@SuppressWarnings("All")
public class PageBuilder {
    private MessageEmbed message;
    private boolean isTextured = false;
    private String texture;
}