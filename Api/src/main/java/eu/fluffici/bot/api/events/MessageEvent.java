package eu.fluffici.bot.api.events;

/*
---------------------------------------------------------------------------------
File Name : ButtonEvent.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@AllArgsConstructor
public class MessageEvent {
    private Message message;
}
